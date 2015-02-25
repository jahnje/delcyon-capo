
package com.delcyon.capo.webapp.servlets;

import java.util.List;
import java.util.SortedSet;

import javax.jcr.Session;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.Action;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.State;
import com.delcyon.capo.resourcemanager.ResourceURI;
import com.delcyon.capo.resourcemanager.types.FileResourceType;
import com.delcyon.capo.server.jackrabbit.CapoJcrServer;
import com.delcyon.capo.webapp.models.DomItemModel.DomUse;
import com.delcyon.capo.webapp.models.ResourceDescriptorItemModel;
import com.delcyon.capo.webapp.widgets.WCapoResourceEditor;
import com.delcyon.capo.webapp.widgets.WCapoResourceTreeView;
import com.delcyon.capo.webapp.widgets.WCapoSearchControl;
import com.delcyon.capo.xml.dom.ResourceDocument;

import eu.webtoolkit.jwt.AlignmentFlag;
import eu.webtoolkit.jwt.MatchOptions;
import eu.webtoolkit.jwt.PositionScheme;
import eu.webtoolkit.jwt.Signal;
import eu.webtoolkit.jwt.Signal2;
import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WBootstrapTheme;
import eu.webtoolkit.jwt.WBoxLayout;
import eu.webtoolkit.jwt.WBoxLayout.Direction;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WGridLayout;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WLink;
import eu.webtoolkit.jwt.WLink.Type;
import eu.webtoolkit.jwt.WMenuItem;
import eu.webtoolkit.jwt.WModelIndex;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WNavigationBar;
import eu.webtoolkit.jwt.WPopupMenu;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WStackedWidget;
import eu.webtoolkit.jwt.WTabWidget;
import eu.webtoolkit.jwt.WVBoxLayout;
import eu.webtoolkit.jwt.WWidget;

public class CapoWebApplication extends WApplication {
	
	
	    
    private WContainerWidget rootContainerWidget;
	private WGridLayout rootLayout;
	private WContainerWidget contentPane;
	private WGridLayout contentPaneLayout;
	private WVBoxLayout detailsPaneLayout;
	private WCapoResourceEditor capoResourceEditor;
	private WTabWidget subDetailsPane;
	private WCapoResourceTreeView treeView;
    private ResourceDocument document;
    private Session jcrSession;
    private WPushButton resetButton;
    private WPushButton saveButton;
    private WCapoSearchControl capoSearchControl;
   
    
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
        WApplication.getInstance().internalPathChanged().addListener(this,this::processInternalPathChange);
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
            FileResourceType fileResourceType = new FileResourceType();
            //ResourceDescriptor resourceDescriptor = fileResourceType.getResourceDescriptor("file:/");
            ResourceDescriptor resourceDescriptor = CapoApplication.getDataManager().getResourceDescriptor(null, "repo:"+WApplication.getInstance().getInternalPath());
            if(resourceDescriptor.getResourceMetaData(null).exists() == false)
            {
                resourceDescriptor.performAction(null, Action.CREATE);
                resourceDescriptor.reset(State.OPEN);
            }
//            ResourceDescriptor resourceDescriptor2 = CapoApplication.getDataManager().getResourceDirectory("CLIENTS_DIR");
//            ResourceDocumentBuilder documentBuilder = new ResourceDocumentBuilder();
//            document = (ResourceDocument) documentBuilder.buildDocument(resourceDescriptor2);
            
			
			getTreeView().setRootResourceDescriptor(resourceDescriptor);
			
		} catch (Exception e)
		{			
			e.printStackTrace();
		}
        
        //pathButton.setLink(new WLink(Type.InternalPath, "/legend"));
        getContentPaneLayout().addWidget(getTreeView(), 0, 0,1,0);
        getContentPaneLayout().addLayout(getDetailsPaneLayout(), 0, 1);
        getContentPaneLayout().addWidget(getSaveButton(), 2, 0, 1, 1, AlignmentFlag.AlignTop); 
        getContentPaneLayout().addWidget(getResetButton(), 3, 0, 1, 1, AlignmentFlag.AlignTop);
        getDetailsPaneLayout().addWidget(getDetailsPane(), 0);
        getDetailsPaneLayout().setResizable(0);
            
    }

    private WPushButton getSaveButton()
    {
        if(saveButton == null)
        {
            saveButton = new WPushButton("Save");
            saveButton.clicked().addListener(this, this::saveSession);
        }
        return saveButton;
    }
    
    /**
     * saves current jcrSession, TODO this should really work with the current users workspace or something.
     */
    private void saveSession()
    {
        try
        {
            jcrSession.save();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private WPushButton getResetButton()
    {
        
        if(resetButton == null)
        {
            resetButton = new WPushButton("Reset");
            resetButton.setLink(new WLink(Type.InternalPath, "/"));
            resetButton.clicked().addListener(this, this::reset);           
        }
        return resetButton;
    }
    
    /**
     * Resets session and tree view to root
     */
    private void reset()
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
    
    private WTabWidget getSubDetailsPane() {
    	if (subDetailsPane == null)
    	{
    		subDetailsPane = new WTabWidget();
    	}
		return subDetailsPane;
	}

	private WCapoResourceEditor getDetailsPane() 
    {
    	if (capoResourceEditor == null)
    	{
    		capoResourceEditor = new WCapoResourceEditor();
    	
    	}
    	return capoResourceEditor;
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
    
    private  WCapoResourceTreeView getTreeView() {
        if(treeView == null)
        {
            treeView = new WCapoResourceTreeView();
            //watch for selection change events
            treeView.selectionChanged().addListener(this, this::selectedItemChanged);
            //watch for internal patch change requests from tree
            treeView.doubleClicked().addListener(this, this::processTreeDoubleClick);
        }       

        return treeView;
    }
    
    /**
     * Process request for tree root/internal path changes from tree
     * @param arg1
     * @param arg2
     */
    private void processTreeDoubleClick(WModelIndex arg1, WMouseEvent arg2)
    {
        setInternalPath(((ResourceDescriptor)arg1.getInternalPointer()).getResourceURI().getPath(), false);
        getTreeView().setModel(new ResourceDescriptorItemModel((ResourceDescriptor)arg1.getInternalPointer(),DomUse.NAVIGATION));
    }
    
    /**
     * sends selection changes in the tree to the resource editor
     */
    private void selectedItemChanged()
    {
        SortedSet<WModelIndex> selectedIndexes = getTreeView().getSelectedIndexes();
        if(selectedIndexes.size() == 0)
        {
            return;
        }
        else if (selectedIndexes.size() == 1)
        {
            WModelIndex modelIndex = selectedIndexes.first();
            final Object selectedItem =  modelIndex.getInternalPointer();           
            getDetailsPane().setModel(selectedItem); //TODO add somesort of event/Signal listener to process this            
        }
        refresh();
    }
    
    
    
    /**
     * Deal with any internal path changes in the system. Make sure that the proper components and resources are loaded and views are show etc
     */
    private void processInternalPathChange()
    {
        System.out.println(WApplication.getInstance().getInternalPath());
        try
        {
            
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
            getTreeView().setRootResourceDescriptor(resourceDescriptor);
            if(originalURI != null)
            {
                List<WModelIndex> indexes = getTreeView().getModel().match(getTreeView().getModel().getIndex(0, 0), ResourceDescriptorItemModel.ResourceURI_ROLE, originalURI.toString(), 1, MatchOptions.defaultMatchOptions);
                if(indexes.size() > 0)
                {
                    getTreeView().select(indexes.get(0));
                }
            }
            getTreeView().selectionChanged();
        }
        catch (Exception e)
        {                         
            e.printStackTrace();
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
        
        
        
        //searchButton.setStyleClass("btn btn-mini");
        navigation.addWidget(getSearchControl(),AlignmentFlag.AlignRight);
        
        
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
    
    private WCapoSearchControl getSearchControl()
    {
        if(capoSearchControl == null)
        {
            capoSearchControl = new WCapoSearchControl();
        }
        return capoSearchControl;
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
