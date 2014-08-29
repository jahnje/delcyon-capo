/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package com.delcyon.capo.webapp.servlets;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.util.XMLSerializer;
import com.delcyon.capo.xml.XPath;
import com.delcyon.capo.xml.cdom.CElement;

import eu.webtoolkit.jwt.AlignmentFlag;
import eu.webtoolkit.jwt.PositionScheme;
import eu.webtoolkit.jwt.SelectionMode;
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
import eu.webtoolkit.jwt.WModelIndex;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WNavigationBar;
import eu.webtoolkit.jwt.WPopupMenu;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WStackedWidget;
import eu.webtoolkit.jwt.WStandardItemModel;
import eu.webtoolkit.jwt.WTabWidget;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.WTreeView;
import eu.webtoolkit.jwt.WVBoxLayout;
import eu.webtoolkit.jwt.WWidget;

public class CapoWebApplication extends WApplication {
	
	
	    
    private WContainerWidget rootContainerWidget;
	private WGridLayout rootLayout;
	private WContainerWidget contentPane;
	private WGridLayout contentPaneLayout;
	private WVBoxLayout detailsPaneLayout;
	private WTabWidget detailsPane;
	private WTabWidget subDetailsPane;

	public CapoWebApplication(WEnvironment env, boolean embedded) {
        super(env);
        WBootstrapTheme bootstrapTheme = new WBootstrapTheme();
        setTheme(bootstrapTheme);
        //setCssTheme("polished");
        setTitle("Delcyon Capo");
        createUI();
    }

    private void createUI() 
    {
        rootContainerWidget = getRoot();
        rootContainerWidget.setStyleClass("maindiv");
        rootContainerWidget.setPadding(new WLength(0));
        rootContainerWidget.setLayout(getRootLayout());
       
        
        getRootLayout().addWidget(getNavigationContainer(),0,0);              
        getRootLayout().addWidget(getContentPane(),1,0);

        getContentPaneLayout().addWidget(getTreeView(), 0, 0,1,0);
        getContentPaneLayout().addLayout(getDetailsPaneLayout(), 0, 1);
        getContentPaneLayout().addWidget(createTitle("Legend"), 2, 0, 1, 1, AlignmentFlag.AlignTop); 
        
        getDetailsPane().addTab(createTitle("Title"), "results");
        getDetailsPane().addTab(getMenu(), "details");
        getDetailsPane().getWidget(1).setAttributeValue("style", "background-color: lightgrey;");
        
        
        getDetailsPaneLayout().addWidget(getDetailsPane(), 0);
        getDetailsPaneLayout().addWidget(getSubDetailsPane(), 0);
        getDetailsPaneLayout().setResizable(0);
        
        getSubDetailsPane().addTab(createTitle("Title"), "properties");
        getSubDetailsPane().addTab(createTitle("Title"), "details");
        getSubDetailsPane().getWidget(1).setAttributeValue("style", "background-color: lightgrey;");
        
        try
		{
			ResourceDescriptor clientsResourceDescriptor = CapoApplication.getDataManager().getResourceDescriptor(null, "clients:capo.client.1");
			CElement e = clientsResourceDescriptor.readXML(null);
			
			
			XPath.dumpNode(e, System.out);
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
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

	private WTabWidget getDetailsPane() 
    {
    	if (detailsPane == null)
    	{
    		detailsPane = new WTabWidget();		
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
    		contentPane.setAttributeValue("style", "background-image: url('/img/Konachan.com - 181934 animal_ears black_hair breasts catgirl cleavage green_eyes kerasu original tail thighhighs.png'); background-repeat: no-repeat; background-position: bottom right;");
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
    
    private  WTreeView getTreeView() {
        WTreeView treeView = new WTreeView();

        /*
         * To support right-click, we need to disable the built-in browser
         * context menu.
         * 
         * Note that disabling the context menu and catching the right-click
         * does not work reliably on all browsers.
         */
        treeView.setAttributeValue("oncontextmenu","event.cancelBubble = true; event.returnValue = false; return false;");
        treeView.setModel(new WStandardItemModel(0, 1, this));
        treeView.resize(new WLength(200), WLength.Auto);
        treeView.setSelectionMode(SelectionMode.SingleSelection);
        treeView.expandToDepth(1);
        treeView.selectionChanged().addListener(this, new Signal.Listener() {
            public void trigger() {
         //       folderChanged();
            }
        });
        treeView.clicked().addListener(this, new Signal2.Listener<WModelIndex, WMouseEvent>() {

			@Override
			public void trigger(WModelIndex arg1, WMouseEvent arg2) {
				//System.out.println(arg2);
				
			}
		});
        treeView.mouseWentUp().addListener(this, new Signal2.Listener<WModelIndex, WMouseEvent>() {

			@Override
			public void trigger(WModelIndex arg1, WMouseEvent arg2) {
				System.out.println(arg2);
				if (arg2.getButton().getValue() == 4)
				{
				WPopupMenu adminSubMenu = new WPopupMenu();
		       

		        adminSubMenu.addItem("Users");
		        adminSubMenu.addItem("Roles");
		        adminSubMenu.addItem("Configuration");
		        adminSubMenu.popup(arg2);
				}
			}
		});

       // folderView_ = treeView;

        return treeView;
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
        systemsMenuButton.setMenu(systemsSubMenu);
        systemsSubMenu.addItem("Tasks").triggered().addListener(this,
                new Signal.Listener() {
                    public void trigger() {
                        
                    }
                });
        systemsSubMenu.addItem("Chores");
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
}
