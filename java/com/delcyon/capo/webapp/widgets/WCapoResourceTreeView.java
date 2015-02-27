package com.delcyon.capo.webapp.widgets;

import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.Action;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.State;
import com.delcyon.capo.resourcemanager.types.ContentMetaData;
import com.delcyon.capo.resourcemanager.types.JcrResourceDescriptor;
import com.delcyon.capo.webapp.models.DomItemModel.DomUse;
import com.delcyon.capo.webapp.models.ResourceDescriptorItemModel;

import eu.webtoolkit.jwt.SelectionBehavior;
import eu.webtoolkit.jwt.SelectionMode;
import eu.webtoolkit.jwt.Signal;
import eu.webtoolkit.jwt.Signal1;
import eu.webtoolkit.jwt.Signal2;
import eu.webtoolkit.jwt.WDialog;
import eu.webtoolkit.jwt.WLabel;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WLineEdit;
import eu.webtoolkit.jwt.WModelIndex;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WPopupMenu;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WRegExpValidator;
import eu.webtoolkit.jwt.WValidator;

/**
 * This class lets a user navigate a tree of resource descriptors, as well as provide a few node editing methods
 * @author jeremiah
 *
 */
public class WCapoResourceTreeView extends CapoWTreeView
{

    private ResourceDescriptor rootResourceDescriptor = null;
    private WPopupMenu treeViewRightClickMenu;
    private WDialog createNodeDialog;
     
    
    public WCapoResourceTreeView()
    {
        //basic setup
        setLayoutSizeAware(true);
        setColumnResizeEnabled(false);        
        setWidth(new WLength(250));
        setSelectionMode(SelectionMode.SingleSelection);
        setSelectionBehavior(SelectionBehavior.SelectItems);
        setSelectable(true);
        setAlternatingRowColors(true);

        //setup Right click Menu
        setRightClickAware(true);
        rightClicked().addListener(this, new Signal2.Listener<WModelIndex, WMouseEvent>() {
            @Override
            public void trigger(WModelIndex arg1, WMouseEvent arg2) {
                    getTreeViewRightClickMenu().popup(arg2);;
                }
        });
    }
    
   
    /**
     * 
     * Convenience method to set the model appropriately 
     */
    public void setRootResourceDescriptor(ResourceDescriptor rootResourceDescriptor)
    {
        this.rootResourceDescriptor = rootResourceDescriptor;
        setModel(new ResourceDescriptorItemModel(rootResourceDescriptor,DomUse.NAVIGATION));
    }
    
    /**
     * 
     * @return the ResourceDescriptor set with setRootResourceDescriptor
     */
    public ResourceDescriptor getRootResourceDescriptor()
    {
        return rootResourceDescriptor;
    }
    
    /**
     * Generate the views right click menu
     * @return
     */
    private WPopupMenu getTreeViewRightClickMenu()
    {
        if(treeViewRightClickMenu == null)
        {
            treeViewRightClickMenu = new WPopupMenu();
            
            //setup create node method
            treeViewRightClickMenu.addItem("Create Node...").clicked().addListener(this, new Signal.Listener()
            {
                public void trigger() {
                        getCreateNodeDialog().show();
                };
            });
            
            //setup delete method
            treeViewRightClickMenu.addItem("Delete").clicked().addListener(this, new Signal.Listener()
            {
                @Override
                public void trigger()
                {
                    deleteNode();
                }
            });
            
            //TODO we still need a rename, and a move method there
        }
        return this.treeViewRightClickMenu;
    }
    
    /**
     * Creates or returns the node creation dialog
     * @return
     */
    private WDialog getCreateNodeDialog()
    {
        if(createNodeDialog == null)
        {
            createNodeDialog = new WDialog("Create Node");
            createNodeDialog.setClosable(true);
            createNodeDialog.rejectWhenEscapePressed(true);
            createNodeDialog.rejectWhenEscapePressed();
            
            WLabel label = new WLabel("Enter a node name", createNodeDialog.getContents());
            final WLineEdit nodeNameLineEdit = new WLineEdit(createNodeDialog.getContents());
            label.setBuddy(nodeNameLineEdit);
            
            WRegExpValidator nodeNameValidator = new WRegExpValidator("[A-Za-z1-9 \\.]+");
            nodeNameValidator.setMandatory(true);
            
            final WPushButton okPushButton = new WPushButton("OK", createNodeDialog.getFooter());
            okPushButton.setDefault(true);
            okPushButton.disable();

            //watch the nodeNameLineEdit and only enable the ok button when the entered text is valid
            nodeNameLineEdit.keyWentUp().addListener(this, new Signal.Listener() {
                public void trigger() {
                    okPushButton.setDisabled(nodeNameLineEdit.validate() != WValidator.State.Valid);
                }
            });
            
            okPushButton.clicked().addListener(this, new Signal.Listener() {
                public void trigger() {
                    //make sure we got validated
                    if (nodeNameLineEdit.validate() != null) {
                        //fire the acceptance signal
                        createNodeDialog.accept();
                        //send off to have the node created
                        createNode(nodeNameLineEdit.getText());
                        //clear out text out for the next use
                        nodeNameLineEdit.setText("");
                    }
                }
            });
            
            WPushButton cancelPushButton = new WPushButton("Cancel", createNodeDialog.getFooter());
            cancelPushButton.clicked().addListener(createNodeDialog,new Signal1.Listener<WMouseEvent>() {
                public void trigger(WMouseEvent e1) {
                    //fire of rejection  signal
                    createNodeDialog.reject();
                }
            });           
        }
        return createNodeDialog;
    }
    
    /**
     * This will delete the currently selected node if possible. 
     */
    private void deleteNode()
    {
        WModelIndex index = getSelectedIndexes().first();
        if(index != null)
        {
            ResourceDescriptor selectedResourceDescriptor = (ResourceDescriptor) index.getInternalPointer();
            try
            {
                ResourceDescriptor parentResourceDescriptor = selectedResourceDescriptor.getParentResourceDescriptor();
                //get our parent index, since it's our parent's children that are going to change
                WModelIndex parentIndex = ((ResourceDescriptorItemModel) getModel()).getParent(index);
                //let the mode know that we're about to do some updating.
                ((ResourceDescriptorItemModel) getModel()).beginRemoveRows(parentIndex, index.getRow(), index.getRow());
                
                //do the deletion
                selectedResourceDescriptor.performAction(null, Action.DELETE);
                //if we have a parent, have it refresh it's data
                if (parentResourceDescriptor != null)
                {
                    parentResourceDescriptor.reset(State.INITIALIZED);
                }
                
                //let the view know that we're done editing so that it refreshes
                if (parentIndex != null)
                {
                    ((ResourceDescriptorItemModel) getModel()).fireDataChanged(parentIndex,false);
                }
                else
                {
                    //if we don't have a parent, then reload the whole model, fairly heavy weight
                    ((ResourceDescriptorItemModel) getModel()).reload();
                }
            }
            catch (Exception e)
            {                
                e.printStackTrace();
            }

        }
    }

    /**
     * Creates a child node of the currently selected node with the given name
     * @param name
     */
    private void createNode(String name)
    {
        
        WModelIndex index = null;
        ResourceDescriptor selectedResourceDescriptor = null;
        
        //figure out where our selected node is, could be the root 
        if(getSelectedIndexes().isEmpty()) 
        {//yep, it's the root, because nothing else is selected
            index = null;
            selectedResourceDescriptor = ((ResourceDescriptorItemModel)getModel()).getTopLevelResourceDescriptor();
        }
        else
        {
            index = getSelectedIndexes().first();
            selectedResourceDescriptor = (ResourceDescriptor) index.getInternalPointer();
        }

        try
        {
            //if the current node is about to become a first time parent, set the container attribute to true
            if(selectedResourceDescriptor instanceof JcrResourceDescriptor && selectedResourceDescriptor.getResourceMetaData(null).isContainer() == false)
            {
                selectedResourceDescriptor.getResourceMetaData(null).setValue(ContentMetaData.Attributes.container.toString(), true+"");
            }
            
            //get a pointer to the child resource
            ResourceDescriptor childResourceDescriptor = selectedResourceDescriptor.getChildResourceDescriptor(null, name);
            //initialize it, this seems like it shouldn't be needed...
            //childResourceDescriptor.init(null, null, null, false); // going to leave this here for now 
            //tell the pointer to actually create itself.
            childResourceDescriptor.performAction(null, Action.CREATE);
            //refresh it
            selectedResourceDescriptor.reset(State.OPEN);
            
            //cause the view to update
            if(index != null)
            {
                ((ResourceDescriptorItemModel)getModel()).fireDataChanged(index,true);                
            }
            else
            {
                ((ResourceDescriptorItemModel)getModel()).reload();//TODO this is really heavy weight, when in reality, model needs to fire data changed event
            }
        }
        catch (Exception e)
        {            
            e.printStackTrace();
        }
    }
    
}
