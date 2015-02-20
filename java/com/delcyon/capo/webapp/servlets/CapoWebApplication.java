/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package com.delcyon.capo.webapp.servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.SortedSet;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.w3c.dom.Element;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.datastream.StreamUtil;
import com.delcyon.capo.datastream.stream_attribute_filter.MimeTypeFilterInputStream;
import com.delcyon.capo.resourcemanager.ContentFormatType;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.Action;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.State;
import com.delcyon.capo.resourcemanager.ResourceURI;
import com.delcyon.capo.resourcemanager.types.ContentMetaData;
import com.delcyon.capo.resourcemanager.types.JcrResourceDescriptor;
import com.delcyon.capo.server.jackrabbit.CapoJcrServer;
import com.delcyon.capo.util.HexUtil;
import com.delcyon.capo.webapp.models.DomItemModel;
import com.delcyon.capo.webapp.models.DomItemModel.DomUse;
import com.delcyon.capo.webapp.models.ResourceDescriptorItemModel;
import com.delcyon.capo.webapp.servlets.resource.AbstractResourceServlet;
import com.delcyon.capo.webapp.servlets.resource.WResourceDescriptor;
import com.delcyon.capo.webapp.widgets.CapoWTreeView;
import com.delcyon.capo.webapp.widgets.WAceEditor;
import com.delcyon.capo.webapp.widgets.WAceEditor.Theme;
import com.delcyon.capo.webapp.widgets.WCSSItemDelegate;
import com.delcyon.capo.webapp.widgets.CapoWDetailPane;
import com.delcyon.capo.xml.dom.ResourceDocument;
import com.delcyon.capo.xml.dom.ResourceDocumentBuilder;

import eu.webtoolkit.jwt.AlignmentFlag;
import eu.webtoolkit.jwt.AnchorTarget;
import eu.webtoolkit.jwt.MatchOptions;
import eu.webtoolkit.jwt.PositionScheme;
import eu.webtoolkit.jwt.SelectionBehavior;
import eu.webtoolkit.jwt.SelectionMode;
import eu.webtoolkit.jwt.Signal;
import eu.webtoolkit.jwt.Signal.Listener;
import eu.webtoolkit.jwt.Signal1;
import eu.webtoolkit.jwt.Signal2;
import eu.webtoolkit.jwt.TextFormat;
import eu.webtoolkit.jwt.Utils;
import eu.webtoolkit.jwt.WAnchor;
import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WBootstrapTheme;
import eu.webtoolkit.jwt.WBoxLayout;
import eu.webtoolkit.jwt.WBoxLayout.Direction;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WDialog;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WFileUpload;
import eu.webtoolkit.jwt.WGridLayout;
import eu.webtoolkit.jwt.WImage;
import eu.webtoolkit.jwt.WLabel;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WLength.Unit;
import eu.webtoolkit.jwt.WLineEdit;
import eu.webtoolkit.jwt.WLink;
import eu.webtoolkit.jwt.WLink.Type;
import eu.webtoolkit.jwt.WMenuItem;
import eu.webtoolkit.jwt.WModelIndex;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WNavigationBar;
import eu.webtoolkit.jwt.WPopupMenu;
import eu.webtoolkit.jwt.WProgressBar;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WRegExpValidator;
import eu.webtoolkit.jwt.WStackedWidget;
import eu.webtoolkit.jwt.WTabWidget;
import eu.webtoolkit.jwt.WTable;
import eu.webtoolkit.jwt.WTableView;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.WTextArea;
import eu.webtoolkit.jwt.WTreeView;
import eu.webtoolkit.jwt.WVBoxLayout;
import eu.webtoolkit.jwt.WValidator;
import eu.webtoolkit.jwt.WWidget;
import eu.webtoolkit.jwt.servlet.UploadedFile;

public class CapoWebApplication extends WApplication {
	
	
	    
    private WContainerWidget rootContainerWidget;
	private WGridLayout rootLayout;
	private WContainerWidget contentPane;
	private WGridLayout contentPaneLayout;
	private WVBoxLayout detailsPaneLayout;
	private CapoWDetailPane detailsPane;
	private WTabWidget subDetailsPane;
	private CapoWTreeView treeView;
    private ResourceDocument document;
    private Session jcrSession;
    private WPushButton resetButton;
    private WPushButton saveButton;
    private WDialog searchResultsDialog;
    
	public CapoWebApplication(WEnvironment env, boolean embedded) {
        super(env);
        WBootstrapTheme bootstrapTheme = new WBootstrapTheme();
        setTheme(bootstrapTheme);
        useStyleSheet(new WLink("/wr/css/local.css"));
        //setCssTheme("polished");
        setTitle("Capo");
        require("/wr/ace/ace.js");
        try
        {
            jcrSession = CapoJcrServer.createSession();
        }
        catch (Exception e)
        {
        
            e.printStackTrace();
        }
        
        createUI();
        WApplication.getInstance().internalPathChanged().addListener(this, new Signal.Listener()
                {

                    @Override
                    public void trigger()
                    {
                        System.out.println(WApplication.getInstance().getInternalPath());
                        try
                        {
                            if(searchResultsDialog != null && searchResultsDialog.isVisible())
                            {
                                searchResultsDialog.hide();
                            }
                            ResourceDescriptor resourceDescriptor = CapoApplication.getDataManager().getResourceDescriptor(null, "repo:"+WApplication.getInstance().getInternalPath());
                            ResourceURI originalURI = resourceDescriptor.getResourceURI();
                            if(resourceDescriptor.getResourceMetaData(null).isContainer() == false)
                            {
                                if(resourceDescriptor.getParentResourceDescriptor() != null)
                                {
                                    resourceDescriptor = resourceDescriptor.getParentResourceDescriptor();
                                }
                                else
                                {
                                    String parentURI = resourceDescriptor.getResourceURI().getResourceURIString().replaceAll("/"+resourceDescriptor.getLocalName(), "");
                                    if(parentURI.equals("repo:")) //make sure we have a root for the repo
                                    {
                                        parentURI = "repo:/";
                                    }
                                    
                                    resourceDescriptor = CapoApplication.getDataManager().getResourceDescriptor(null,parentURI);
                                    setInternalPath(resourceDescriptor.getResourceURI().getPath());
                                }
                            }
                            treeView.setModel(new ResourceDescriptorItemModel(resourceDescriptor,DomUse.NAVIGATION));
                            if(originalURI != null)
                            {
                                List<WModelIndex> indexes = treeView.getModel().match(treeView.getModel().getIndex(0, 0), ResourceDescriptorItemModel.ResourceURI_ROLE, originalURI.toString(), 1, MatchOptions.defaultMatchOptions);
                                if(indexes.size() > 0)
                                {
                                    treeView.select(indexes.get(0));
                                }
                            }
                            treeView.selectionChanged();
                        }
                        catch (Exception e)
                        {                         
                            e.printStackTrace();
                        }
                    }
            
                });
    }

    private void createUI() 
    {
        rootContainerWidget = getRoot();
        //rootContainerWidget.setStyleClass("maindiv");
        rootContainerWidget.setPadding(new WLength(0));
        rootContainerWidget.setLayout(getRootLayout());
       
        
        getRootLayout().addWidget(getNavigationContainer(),0,0);              
        getRootLayout().addWidget(getContentPane(),1,0);
        try
		{
            //FileResourceType fileResourceType = new FileResourceType();
            //ResourceDescriptor resourceDescriptor = fileResourceType.getResourceDescriptor("file:/");
            ResourceDescriptor resourceDescriptor = CapoApplication.getDataManager().getResourceDescriptor(null, "repo:"+WApplication.getInstance().getInternalPath());
            if(resourceDescriptor.getResourceMetaData(null).exists() == false)
            {
                resourceDescriptor.performAction(null, Action.CREATE);
                resourceDescriptor.reset(State.OPEN);
            }
            ResourceDescriptor resourceDescriptor2 = CapoApplication.getDataManager().getResourceDirectory("CLIENTS_DIR");
            ResourceDocumentBuilder documentBuilder = new ResourceDocumentBuilder();
            document = (ResourceDocument) documentBuilder.buildDocument(resourceDescriptor2);



			getContentPaneLayout().addWidget(getTreeView(resourceDescriptor), 0, 0,1,0);	
			
			
		} catch (Exception e)
		{			
			e.printStackTrace();
		}
        
        getContentPaneLayout().addLayout(getDetailsPaneLayout(), 0, 1);
        
        //pathButton.setLink(new WLink(Type.InternalPath, "/legend"));
        getContentPaneLayout().addWidget(getSaveButton(), 2, 0, 1, 1, AlignmentFlag.AlignTop); 
        
        
        getContentPaneLayout().addWidget(getResetButton(), 3, 0, 1, 1, AlignmentFlag.AlignTop);
        
//        getDetailsPane().addTab(createTitle("Title"), "results");
//        getDetailsPane().addTab(getMenu(), "details");
//        getDetailsPane().getWidget(1).setAttributeValue("style", "background-color: lightgrey;");
//        
        
        getDetailsPaneLayout().addWidget(getDetailsPane(), 0);
        //getDetailsPaneLayout().addWidget(getSubDetailsPane(), 0);
        getDetailsPaneLayout().setResizable(0);
        
//        getSubDetailsPane().addTab(createTitle("Title"), "properties");
//        getSubDetailsPane().addTab(createTitle("Title"), "details");
//        getSubDetailsPane().getWidget(1).setAttributeValue("style", "background-color: lightgrey;");
        
        
       
            
    }

    private WPushButton getSaveButton()
    {
        if(saveButton == null)
        {
            saveButton = new WPushButton("Save");
            saveButton.clicked().addListener(this, new Signal.Listener()
            {

                public void trigger()
                {
                    try
                    {
                        jcrSession.save();
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
        }
        return saveButton;
    }
    
    private WPushButton getResetButton()
    {
        
        if(resetButton == null)
        {
            resetButton = new WPushButton("Reset");
            resetButton.setLink(new WLink(Type.InternalPath, "/"));
            resetButton.clicked().addListener(this, new Signal.Listener()
            {            
                public void trigger()
                {
                    try
                    {
                        jcrSession.refresh(false);
                        ((ResourceDescriptorItemModel) treeView.getModel()).reload();
                        treeView.setModel(new ResourceDescriptorItemModel(CapoApplication.getDataManager().getResourceDescriptor(null, "repo:/"),DomUse.NAVIGATION));
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
        }
        return resetButton;
    }
    
    
    private WTabWidget getSubDetailsPane() {
    	if (subDetailsPane == null)
    	{
    		subDetailsPane = new WTabWidget();
    	}
		return subDetailsPane;
	}

	private CapoWDetailPane getDetailsPane() 
    {
    	if (detailsPane == null)
    	{
    		detailsPane = new CapoWDetailPane();
    	
    	}
    	return detailsPane;
	}

	private WBoxLayout getDetailsPaneLayout() {
		if (detailsPaneLayout == null)
		{
			detailsPaneLayout = new WVBoxLayout();
		}
    	return detailsPaneLayout;
	}

	private WWidget getContentPane() {
    	if (contentPane == null)
    	{
    		contentPane = new WContainerWidget();
    		contentPane.setMargin(0);
    		contentPane.setLayout(getContentPaneLayout());
    		//contentPane.setAttributeValue("style", "background-image: url('/wr/images/background.png'); background-repeat: no-repeat; background-position: bottom right; background-size: contain;");
    	
    	
    	}
    	return contentPane;
	}

	private WGridLayout getContentPaneLayout() {
		if (contentPaneLayout == null)
		{
			contentPaneLayout = new WGridLayout();			
			contentPaneLayout.setColumnStretch(1, 1);
			contentPaneLayout.setContentsMargins(0, 0, 0, 0);
			contentPaneLayout.setColumnResizable(0);
			contentPaneLayout.setRowStretch(1, 1);
			contentPaneLayout.setRowStretch(0, 1);
		}
		 return contentPaneLayout;
	}

	private WGridLayout getRootLayout() {
    	if (rootLayout == null)
    	{
    		rootLayout = new WGridLayout();
    		rootLayout.setContentsMargins(0,0,0,0);
    		getRootLayout().setRowStretch(1, 1);
    	}
    	return rootLayout;
	}

	private WText createTitle(String title) {
        WText result = new WText(title);
        result.setInline(false);
        result.setStyleClass("title");

        return result;
    }
    
    private  WTreeView getTreeView(Object data) {
        if(treeView == null)
        {
            treeView = new CapoWTreeView();
            treeView.setLayoutSizeAware(true);
            treeView.setColumnResizeEnabled(false);
            //treeView.
            /*
             * To support right-click, we need to disable the built-in browser
             * context menu.
             * 
             * Note that disabling the context menu and catching the right-click
             * does not work reliably on all browsers.
             */
            treeView.setAttributeValue("oncontextmenu","event.cancelBubble = true; event.returnValue = false; return false;");
            if (data instanceof Element)
            {
                treeView.setModel(new DomItemModel((Element) data,DomUse.NAVIGATION));            
            }
            else if (data instanceof ResourceDescriptor)
            {
                treeView.setModel(new ResourceDescriptorItemModel((ResourceDescriptor)data,DomUse.NAVIGATION));
            }
            //tree
            treeView.setWidth(new WLength(250));//, WLength.Auto);


            // treeView.setWidth(new WLength("100%"));
            treeView.setSelectionMode(SelectionMode.SingleSelection);
            treeView.setSelectionBehavior(SelectionBehavior.SelectItems);
            treeView.setSelectable(true);
            //treeView.expandToDepth();
            treeView.setAlternatingRowColors(true);
            treeView.selectionChanged().addListener(this, new Signal.Listener() {
                public void trigger() {
                    System.out.println("selectionChanged");
                    selectedItemChanged();
                    treeView.refresh();
                }
            });
            
            treeView.doubleClicked().addListener(this, new Signal2.Listener<WModelIndex, WMouseEvent>() {

                @Override
                public void trigger(WModelIndex arg1, WMouseEvent arg2) {
                    System.out.println("DoubleClicked:");
                    
                    setInternalPath(((ResourceDescriptor)arg1.getInternalPointer()).getResourceURI().getPath(), false);
                    treeView.setModel(new ResourceDescriptorItemModel((ResourceDescriptor)arg1.getInternalPointer(),DomUse.NAVIGATION));
                }
            });
            
            treeView.clicked().addListener(this, new Signal2.Listener<WModelIndex, WMouseEvent>() {

                @Override
                public void trigger(WModelIndex arg1, WMouseEvent arg2) {
                    System.out.println("Clicked:"+arg2.getModifiers());

                }
            });
            
            //Right click Menu
            treeView.mouseWentUp().addListener(this, new Signal2.Listener<WModelIndex, WMouseEvent>() {

                @Override
                public void trigger(WModelIndex arg1, WMouseEvent arg2) {
                    System.out.println("mouseUp");
                    if (arg2.getButton().getValue() == 4)
                    {
                        WPopupMenu adminSubMenu = new WPopupMenu();


                        adminSubMenu.addItem("Create Node...").clicked().addListener(CapoWebApplication.this, new Signal.Listener()
                        {
                            public void trigger() {

                                final WModelIndex index;
                                final ResourceDescriptor selectedResourceDescriptor;
                                if(treeView.getSelectedIndexes().isEmpty())
                                {
                                    index = null;
                                    selectedResourceDescriptor = ((ResourceDescriptorItemModel)treeView.getModel()).getTopLevelResourceDescriptor();
                                }
                                else
                                {
                                    index = treeView.getSelectedIndexes().first();
                                    selectedResourceDescriptor = (ResourceDescriptor) index.getInternalPointer();
                                }
                                if(selectedResourceDescriptor != null)
                                {
                                    System.out.println(selectedResourceDescriptor.getLocalName());
                                    final WDialog dialog = new WDialog("Create Node");
                                    dialog.setClosable(true);
                                    dialog.rejectWhenEscapePressed(true);
                                    WLabel label = new WLabel("Enter a node name", dialog.getContents());
                                    final WLineEdit edit = new WLineEdit(dialog.getContents());
                                    label.setBuddy(edit);
                                    WRegExpValidator validator = new WRegExpValidator("[A-Za-z1-9 \\.]+");
                                    validator.setMandatory(true);
                                    final WPushButton ok = new WPushButton("OK", dialog.getFooter());
                                    ok.setDefault(true);
                                    ok.disable();
                                    WPushButton cancel = new WPushButton("Cancel", dialog.getFooter());

                                    dialog.rejectWhenEscapePressed();
                                    edit.keyWentUp().addListener(CapoWebApplication.this, new Signal.Listener() {
                                        public void trigger() {
                                            ok.setDisabled(edit.validate() != WValidator.State.Valid);
                                        }
                                    });
                                    ok.clicked().addListener(CapoWebApplication.this, new Signal.Listener() {
                                        public void trigger() {
                                            if (edit.validate() != null) {
                                                dialog.accept();
                                            }
                                        }
                                    });
                                    cancel.clicked().addListener(dialog,
                                            new Signal1.Listener<WMouseEvent>() {
                                        public void trigger(WMouseEvent e1) {
                                            dialog.reject();
                                        }
                                    });
                                    dialog.finished().addListener(CapoWebApplication.this, new Signal.Listener() {
                                        public void trigger() {
                                            System.out.println(edit.getText());


                                            try
                                            {
                                                if(selectedResourceDescriptor instanceof JcrResourceDescriptor && selectedResourceDescriptor.getResourceMetaData(null).isContainer() == false)
                                                {
                                                    selectedResourceDescriptor.getResourceMetaData(null).setValue(ContentMetaData.Attributes.container.toString(), true+"");
                                                }
                                                ResourceDescriptor childResourceDescriptor = selectedResourceDescriptor.getChildResourceDescriptor(null, edit.getText());
                                                System.out.println(childResourceDescriptor.getResourceURI());
                                                childResourceDescriptor.init(null, null, null, false);
                                                childResourceDescriptor.performAction(null, Action.CREATE);                                        
                                                //jcrSession.save();
                                                selectedResourceDescriptor.reset(State.OPEN);
                                                if(index != null)
                                                {
                                                    ((ResourceDescriptorItemModel) treeView.getModel()).fireDataChanged(index,true);
                                                    //                                            ((FileResourceDescriptorItemModel) treeView.getModel()).reload();                                           
                                                    //                                            treeView.setExpanded(index, true);
                                                    //                                            treeView.select(index);
                                                }
                                                else
                                                {
                                                    ((ResourceDescriptorItemModel) treeView.getModel()).reload();//TODO this is really heavy weight, when in reality, model needs to fire data changed event
                                                }
                                            }
                                            catch (Exception e)
                                            {
                                                // TODO Auto-generated catch block
                                                e.printStackTrace();
                                            }
                                            if (dialog != null)
                                                dialog.remove();
                                        }
                                    });
                                    dialog.show();
                                }
                            };
                        }
                                );
                        adminSubMenu.addItem("Delete").clicked().addListener(CapoWebApplication.this, new Signal.Listener()
                        {

                            @Override
                            public void trigger()
                            {
                                final WModelIndex index = treeView.getSelectedIndexes().first();
                                if(index != null)
                                {
                                    ResourceDescriptor selectedResourceDescriptor = (ResourceDescriptor) index.getInternalPointer();
                                    try
                                    {
                                        ResourceDescriptor parentResourceDescriptor = selectedResourceDescriptor.getParentResourceDescriptor();
                                        WModelIndex parentIndex = ((ResourceDescriptorItemModel) treeView.getModel()).getParent(index);
                                        ((ResourceDescriptorItemModel) treeView.getModel()).beginRemoveRows(parentIndex, index.getRow(), index.getRow());
                                        selectedResourceDescriptor.performAction(null, Action.DELETE);
                                        if (parentResourceDescriptor != null)
                                        {
                                            parentResourceDescriptor.reset(State.INITIALIZED);
                                        }
                                        if (parentIndex != null)
                                        {
                                            ((ResourceDescriptorItemModel) treeView.getModel()).fireDataChanged(parentIndex,false);
                                        }
                                        else
                                        {
                                            ((ResourceDescriptorItemModel) treeView.getModel()).reload();
                                        }
                                    }
                                    catch (Exception e)
                                    {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }

                                }

                            }
                        });
                        adminSubMenu.addItem("Configuration");
                        adminSubMenu.popup(arg2);
                    }
                }
            });
        }
       // folderView_ = treeView;

        return treeView;
    }
    
    private void selectedItemChanged()
    {
    	SortedSet<WModelIndex> selectedIndexes = treeView.getSelectedIndexes();
    	if(selectedIndexes.size() == 0)
    	{
    		return;
    	}
    	else if (selectedIndexes.size() == 1)
    	{
    		WModelIndex modelIndex = selectedIndexes.first();
    		final Object selectedItem =  modelIndex.getInternalPointer();    		
    		getDetailsPane().setModel(selectedItem);    		
    	}
    }
    
    private WWidget getNavigationContainer()
    {
    	//create and make nav container and nav bar widget
        //WContainerWidget navContainer = new WContainerWidget();
       
       
        WNavigationBar navigation = new WNavigationBar();
       
        
        

       // WContainerWidget navMenuContainer = new WContainerWidget();        
//        WPopupMenu popup = new WPopupMenu();
//        
//        popup.addItem("Connect").triggered().addListener(this,
//                new Signal.Listener() {
//                    public void trigger() {
//                        
//                    }
//                });

//        popup.addSeparator();
//        popup.addItem("icons/house.png", "I'm home").triggered().addListener(
//                this, new Signal.Listener() {
//                    public void trigger() {
//                        
//                    }
//                });
//        final WMenuItem item = popup.addItem("Don't disturb");
//        item.setCheckable(true);
//        item.triggered().addListener(this, new Signal.Listener() {
//            public void trigger() {
//                
//            }
//        });
//        popup.addSeparator();
//        WPopupMenu subMenu = new WPopupMenu();
//        subMenu.addItem("Contents").triggered().addListener(this,
//                new Signal.Listener() {
//                    public void trigger() {
//                       
//                    }
//                });
//        subMenu.addItem("Index").triggered().addListener(this,
//                new Signal.Listener() {
//                    public void trigger() {
//                        
//                    }
//                });
//        subMenu.addSeparator();
//        subMenu.addItem("About").triggered().addListener(this,
//                new Signal.Listener() {
//                    public void trigger() {
//                        final WMessageBox messageBox = new WMessageBox(
//                                "About",
//                                "<p>This is a program to make connections.</p>",
//                                Icon.Information, EnumSet.of(StandardButton.Ok));
//                        messageBox.show();
////                        messageBox.buttonClicked().addListener(this,
////                                new Signal.Listener() {
////                                    public void trigger() {
////                                        if (messageBox != null)
////                                            messageBox.remove();
////                                    }
////                                });
//                    }
//                });
//        popup.addMenu("Help", subMenu);
//        WPushButton button = new WPushButton(navMenuContainer);
//        button.setMenu(popup);
//        button.setText("Text");
//        popup.itemSelected().addListener(this,
//                new Signal1.Listener<WMenuItem>() {
//                    public void trigger(WMenuItem item) {
//                       
//                    }
//                });
        
       
        
        
        
        
        ///build menu system
        
        
        WPushButton systemsMenuButton = new WPushButton("Systems");
        systemsMenuButton.setStyleClass("btn btn-mini");
        WPopupMenu systemsSubMenu = new WPopupMenu();
        systemsSubMenu.setInternalPathEnabled("/Systems");
        systemsMenuButton.setMenu(systemsSubMenu);
        systemsSubMenu.addItem("Tasks").triggered().addListener(this,
                new Signal.Listener() {
                    public void trigger() {
                        WApplication.getInstance().setInternalPath("/Tasks", true);
                    }
                });
        WMenuItem choresMenuItem = systemsSubMenu.addItem("Chores");
        choresMenuItem.setInternalPathEnabled(true);
        choresMenuItem.setPathComponent("taskitos");
        systemsSubMenu.addItem("Groups");
        systemsSubMenu.addItem("Scripts");
        navigation.addWidget(systemsMenuButton);
        
        WPushButton documentsMenuButton = new WPushButton("Documentation");
        documentsMenuButton.setStyleClass("btn btn-mini");
        WPopupMenu documentationSubMenu = new WPopupMenu();
        documentsMenuButton.setMenu(documentationSubMenu);
        navigation.addWidget(documentsMenuButton);
        
       
        
        WPushButton choresMenuButton = new WPushButton("Chores");
        choresMenuButton.setStyleClass("btn btn-mini");
        WPopupMenu choresSubMenu = new WPopupMenu();
        choresMenuButton.setMenu(choresSubMenu);
        navigation.addWidget(choresMenuButton);
        
        WPushButton reportsMenuButton = new WPushButton("Reports");
        reportsMenuButton.setStyleClass("btn btn-mini");
        WPopupMenu reportsSubMenu = new WPopupMenu();
        reportsMenuButton.setMenu(reportsSubMenu);
        navigation.addWidget(reportsMenuButton);
        
        WPushButton adminMenuButton = new WPushButton("Admin");
        adminMenuButton.setStyleClass("btn btn-mini");
        WPopupMenu adminSubMenu = new WPopupMenu();
        adminMenuButton.setMenu(adminSubMenu);

        adminSubMenu.addItem("Users");
        adminSubMenu.addItem("Roles");
        adminSubMenu.addItem("Configuration");
        
        navigation.addWidget(adminMenuButton);
        
        final WLineEdit searchFieldTextEdit = new WLineEdit();
       
        //searchFieldTextEdit.
        
        WPushButton searchButton = new WPushButton("Search");
        Listener searchListener =  new Signal.Listener()
        {
            
           

            public void trigger()
            {
                try
                {
                  //element(*, nt:unstructured)[jcr:contains(., 'foo')]    
                    
                  //Query query = jcrSession.getWorkspace().getQueryManager().createQuery("SELECT * FROM [nt:unstructured] where NAME([nt:unstructured]) = 'server:log' order by message", "JCR-SQL2");
                    String[] langs = jcrSession.getWorkspace().getQueryManager().getSupportedQueryLanguages();
                    for (String lang : langs)
                    {
                        System.out.println(lang+"--"+searchFieldTextEdit.getText());
                    }
                  //Query query = jcrSession.getWorkspace().getQueryManager().createQuery("//element(*, nt:unstructured)[jcr:contains(@content, '"+searchFieldTextEdit.getText()+"')/(@content)]", Query.XPATH);
                  Query query = jcrSession.getWorkspace().getQueryManager().createQuery("SELECT * FROM [nt:unstructured] as n WHERE CONTAINS(n.*, '"+searchFieldTextEdit.getText()+"')", Query.JCR_SQL2);
                  QueryResult result = query.execute();
                  for (String lang : result.getColumnNames())
                  {
                      System.out.println(lang);
                  }
            
                  // Iterate over the nodes in the results ...
                  int excerptWidth = 50;
                  RowIterator rows = result.getRows();
                  System.out.println("=============================");

                  searchResultsDialog = new WDialog("Search Results");
                  searchResultsDialog.setWidth(new WLength(80d, Unit.Percentage));
                  searchResultsDialog.setClosable(true);
                  searchResultsDialog.rejectWhenEscapePressed(true);
                  WTable table = new WTable(searchResultsDialog.getContents());
                  //table.toggleStyleClass("table-hover", true);
                  table.toggleStyleClass("table-condensed", true);
                  table.toggleStyleClass("table-striped", true);
                  table.toggleStyleClass("table-full", true);
                  table.setHeaderCount(1);
                  table.getElementAt(0, 0).addWidget(new WText("Path"));                  
                  table.getElementAt(0, 1).addWidget(new WText("Excerpt"));
                  table.getElementAt(0, 2).addWidget(new WText("Score"));
                  table.getElementAt(0, 2).setContentAlignment(AlignmentFlag.AlignRight);

                  table.doubleClicked().addListener(CapoWebApplication.this, new Signal1.Listener<WMouseEvent>()
                  {
                      @Override
                      public void trigger(WMouseEvent arg1)
                      {
                          System.out.println(arg1);

                      }

                  });

                  int rowNumber = 0;
                  while ( rows.hasNext() ) {
                      
                      rowNumber++;
                      
                      Row row = rows.nextRow();
                      Node _node = row.getNode();
                      String excerpt = _node.getProperty("content").getString().toLowerCase();
                      String searchField = searchFieldTextEdit.getText();
                      int excerptLocation = excerpt.indexOf(searchField.toLowerCase());
                      int startExcerptLocation = excerptLocation -excerptWidth;
                      if(startExcerptLocation < 0 )
                      {
                          startExcerptLocation = 0;
                      }
                      if(excerpt.substring(startExcerptLocation, excerptLocation).indexOf('\n') >= 0)
                      {
                          startExcerptLocation += excerpt.substring(startExcerptLocation, excerptLocation).indexOf('\n')+1;
                      }
                      int endExcerptLocation = excerptLocation+excerptWidth+searchField.length();
                      if(endExcerptLocation >= excerpt.length())
                      {
                          endExcerptLocation = excerpt.length()-1;
                      }
                      if(excerpt.substring(excerptLocation+searchField.length(), endExcerptLocation).indexOf('\n') >= 0)
                      {
                          int crDistance = excerpt.substring(excerptLocation+searchField.length(), endExcerptLocation).indexOf('\n');
                          endExcerptLocation -= (excerptWidth - crDistance);
                      }
                      excerpt = _node.getProperty("content").getString().substring(startExcerptLocation, endExcerptLocation);
                      
                      
                      System.out.println("===>"+_node.getPath()+" type:"+_node.getPrimaryNodeType().getName()+" score="+row.getScore()+" exrp = '"+excerpt+"'");
                      //dump(_node);new WLink(Type.InternalPath, "/legend")
                      table.getElementAt(rowNumber, 0).addWidget(new WAnchor(new WLink(Type.InternalPath, _node.getPath()),_node.getPath(),CapoWebApplication.getInstance().getRoot()));
                      table.getElementAt(rowNumber,1).addWidget(new WText(Utils.htmlEncode(excerpt)));
                      table.getElementAt(rowNumber,1).setAttributeValue("width", "80%");
                      table.getElementAt(rowNumber,1).setContentAlignment(AlignmentFlag.AlignCenter);
                      table.getElementAt(rowNumber, 2).addWidget(new WText(row.getScore()+""));                      
                      table.getElementAt(rowNumber,2).setContentAlignment(AlignmentFlag.AlignRight);                      
                  }
                  
                  searchResultsDialog.show();
                  System.out.println("=============================");
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };
        
        searchButton.clicked().addListener(this,searchListener);
        searchFieldTextEdit.enterPressed().addListener(this, searchListener);
        
        //searchButton.setStyleClass("btn btn-mini");
        navigation.addWidget(searchButton,AlignmentFlag.AlignRight);
        navigation.addWidget(searchFieldTextEdit,AlignmentFlag.AlignRight);
        
        navigation.setResponsive(false);
        navigation.setTitle("Capo");
        navigation.setPopup(true);
        navigation.setHeight(new WLength(5));
        navigation.setMargin(0);
        navigation.setOffsets(0);
        navigation.setPositionScheme(PositionScheme.Relative);
        navigation.setAttributeValue("style", "background-color: black;");
        return navigation;
//        return adminMenuButton;
    }
    
    private WWidget getMenu() {
        WContainerWidget container = new WContainerWidget();
        WStackedWidget contents = new WStackedWidget();
        //WMenu menu = new WMenu(contents,container);
        WBoxLayout boxLayout = new WBoxLayout(Direction.LeftToRight);
        
        //container.setLayout(boxLayout);
        //menu.set
        //menu.setStyleClass("nav nav-pills");
        //menu.setWidth(new WLength(150));
        WPushButton adminMenuButton = new WPushButton("Admin");
        WPopupMenu adminSubMenu = new WPopupMenu();
        adminMenuButton.setMenu(adminSubMenu);
        adminMenuButton.setStyleClass("btn btn-mini");
        adminSubMenu.addItem("Users");
        adminSubMenu.addItem("Roles");
        adminSubMenu.addItem("Configuration");
        container.addWidget(adminMenuButton);
//        menu.addItem("Anchor", new WTextArea("Anchor contents"));
//        menu.addItem("Stacked widget", new WTextArea("Stacked widget contents"));
//        menu.addItem("Tab widget", new WTextArea("Tab widget contents"));
//        menu.addItem("Menu", new WTextArea("Menu contents"));
        container.setInline(true);
        //menu.setHeight(new WLength(25));
        //.addWidget(menu);
        return container;
    }

    public Session getJcrSession()
    {
        return this.jcrSession;
    }
}
