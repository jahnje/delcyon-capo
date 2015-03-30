package com.delcyon.capo.webapp.widgets;

import org.w3c.dom.Element;

import com.delcyon.capo.webapp.models.DomItemModel;
import com.delcyon.capo.webapp.models.DomItemModel.DomUse;

import eu.webtoolkit.jwt.SelectionBehavior;
import eu.webtoolkit.jwt.SelectionMode;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WPopupMenu;

public class WCapoXmlTreeView extends CapoWTreeView
{
	private WPopupMenu treeViewRightClickMenu;
	private Element rootElement;
	
	public WCapoXmlTreeView()
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
        rightClicked().addListener(this, (modelIndex,mouseEvent)->getTreeViewRightClickMenu().popup(mouseEvent));
		
	}
	
	public void setRootElement(Element rootElement)
	{
		 this.rootElement = rootElement;
	        setModel(new DomItemModel(rootElement,DomUse.NAVIGATION));
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
            treeViewRightClickMenu.addItem("Create Node...").clicked().addListener(this, this::doNothing);
            
            //setup delete method
            treeViewRightClickMenu.addItem("Delete").clicked().addListener(this, this::doNothing);
            
            //TODO we still need a rename, and a move method there
        }
        return this.treeViewRightClickMenu;
    }
	
    private void doNothing(){}
    
}
