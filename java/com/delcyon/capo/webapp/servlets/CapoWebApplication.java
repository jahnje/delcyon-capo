
package com.delcyon.capo.webapp.servlets;

import javax.jcr.Session;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.Action;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.State;
import com.delcyon.capo.resourcemanager.types.FileResourceType;
import com.delcyon.capo.server.jackrabbit.CapoJcrServer;
import com.delcyon.capo.webapp.widgets.WCapoResourceExplorer;
import com.delcyon.capo.webapp.widgets.WCapoSearchControl;
import com.delcyon.capo.xml.dom.ResourceDocument;

import eu.webtoolkit.jwt.AlignmentFlag;
import eu.webtoolkit.jwt.PositionScheme;
import eu.webtoolkit.jwt.Signal;
import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WBootstrapTheme;
import eu.webtoolkit.jwt.WBoxLayout;
import eu.webtoolkit.jwt.WBoxLayout.Direction;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WGridLayout;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WLink;
import eu.webtoolkit.jwt.WMenuItem;
import eu.webtoolkit.jwt.WNavigationBar;
import eu.webtoolkit.jwt.WPopupMenu;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WStackedWidget;
import eu.webtoolkit.jwt.WTabWidget;
import eu.webtoolkit.jwt.WWidget;

public class CapoWebApplication extends WApplication {
	
	
	    
    private WContainerWidget rootContainerWidget;
	private WGridLayout rootLayout;
	private WCapoResourceExplorer capoResourceExplorer;
	private WTabWidget subDetailsPane;
	private ResourceDocument document;
    private Session jcrSession;
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
        //WApplication.getInstance().internalPathChanged().addListener(this,this::processInternalPathChange);
    }

	
	
    private void createUI() 
    {
        rootContainerWidget = getRoot();
        //rootContainerWidget.setStyleClass("maindiv");
        rootContainerWidget.setPadding(new WLength(0));
        rootContainerWidget.setLayout(getRootLayout());
       
        
        getRootLayout().addWidget(getNavigationContainer(),0,0);              
        getRootLayout().addWidget(getCapoResourceExplorer(),1,0);
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
            
			
            getCapoResourceExplorer().setRootResourceDescriptor(resourceDescriptor);
			
		} catch (Exception e)
		{			
			e.printStackTrace();
		}
        
        //pathButton.setLink(new WLink(Type.InternalPath, "/legend"));
        
            
    }

    
	
    public Session getJcrSession()
    {
        return this.jcrSession;
    }
    
	

	private WCapoResourceExplorer getCapoResourceExplorer() {
    	if (capoResourceExplorer == null)
    	{
    		capoResourceExplorer = new WCapoResourceExplorer();
    	}
    	return capoResourceExplorer;
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
    
    
    
    
    
    
    
    
    
   
    
    
    
    
    private WWidget getNavigationContainer()
    {
    	//create and make nav container and nav bar widget
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

   
    
}
