/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package com.delcyon.capo.webapp.servlets;

import java.util.EnumSet;

import eu.webtoolkit.jwt.Icon;
import eu.webtoolkit.jwt.Signal;
import eu.webtoolkit.jwt.Signal1;
import eu.webtoolkit.jwt.StandardButton;
import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WBootstrapTheme;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WGridLayout;
import eu.webtoolkit.jwt.WMenuItem;
import eu.webtoolkit.jwt.WMessageBox;
import eu.webtoolkit.jwt.WNavigationBar;
import eu.webtoolkit.jwt.WPopupMenu;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WTabWidget;
import eu.webtoolkit.jwt.WTree;
import eu.webtoolkit.jwt.WTreeNode;

public class CapoWebApplication extends WApplication {
	
	    
    public CapoWebApplication(WEnvironment env, boolean embedded) {
        super(env);
        

        setTheme(new WBootstrapTheme());

        setTitle("Delcyon Capo");

       
      
        //create and add main container to root

       WContainerWidget mainContainerWidget = new WContainerWidget(getRoot());
       //mainContainerWidget.setStyleClass("yellow-box");

       //create and make nav container and nav bar widget
        WContainerWidget navContainer = new WContainerWidget(mainContainerWidget);
       
       
        WNavigationBar navigation = new WNavigationBar(navContainer);
        navigation.setResponsive(true);
        navigation.setTitle("Capo");
        //navigation.setPopup(true);
        
        
        
        

        WContainerWidget navMenuContainer = new WContainerWidget();        
        WPopupMenu popup = new WPopupMenu();
        
        popup.addItem("Connect").triggered().addListener(this,
                new Signal.Listener() {
                    public void trigger() {
                        
                    }
                });
        popup.addItem("Disconnect").triggered().addListener(this,
                new Signal.Listener() {
                    public void trigger() {
                       
                    }
                });
        popup.addSeparator();
        popup.addItem("icons/house.png", "I'm home").triggered().addListener(
                this, new Signal.Listener() {
                    public void trigger() {
                        
                    }
                });
        final WMenuItem item = popup.addItem("Don't disturb");
        item.setCheckable(true);
        item.triggered().addListener(this, new Signal.Listener() {
            public void trigger() {
                
            }
        });
        popup.addSeparator();
        WPopupMenu subMenu = new WPopupMenu();
        subMenu.addItem("Contents").triggered().addListener(this,
                new Signal.Listener() {
                    public void trigger() {
                       
                    }
                });
        subMenu.addItem("Index").triggered().addListener(this,
                new Signal.Listener() {
                    public void trigger() {
                        
                    }
                });
        subMenu.addSeparator();
        subMenu.addItem("About").triggered().addListener(this,
                new Signal.Listener() {
                    public void trigger() {
                        final WMessageBox messageBox = new WMessageBox(
                                "About",
                                "<p>This is a program to make connections.</p>",
                                Icon.Information, EnumSet.of(StandardButton.Ok));
                        messageBox.show();
//                        messageBox.buttonClicked().addListener(this,
//                                new Signal.Listener() {
//                                    public void trigger() {
//                                        if (messageBox != null)
//                                            messageBox.remove();
//                                    }
//                                });
                    }
                });
        popup.addMenu("Help", subMenu);
        WPushButton button = new WPushButton(navMenuContainer);
        button.setMenu(popup);
        button.setText("Text");
        popup.itemSelected().addListener(this,
                new Signal1.Listener<WMenuItem>() {
                    public void trigger(WMenuItem item) {
                       
                    }
                });
        
       
        
        
        
        
        ///build menu system
        WPushButton systemsMenuButton = new WPushButton("Systems");
        WPopupMenu systemsSubMenu = new WPopupMenu();
        systemsMenuButton.setMenu(systemsSubMenu);
        systemsSubMenu.addItem("Tasks");
        systemsSubMenu.addItem("Chores");
        systemsSubMenu.addItem("Groups");
        systemsSubMenu.addItem("Scripts");
        navigation.addWidget(systemsMenuButton);
        
        WPushButton documentsMenuButton = new WPushButton("Documentation");
        WPopupMenu documentationSubMenu = new WPopupMenu();
        documentsMenuButton.setMenu(documentationSubMenu);
        navigation.addWidget(documentsMenuButton);
        
       
        
        WPushButton choresMenuButton = new WPushButton("Chores");
        WPopupMenu choresSubMenu = new WPopupMenu();
        choresMenuButton.setMenu(choresSubMenu);
        navigation.addWidget(choresMenuButton);
        
        WPushButton reportsMenuButton = new WPushButton("Reports");
        WPopupMenu reportsSubMenu = new WPopupMenu();
        reportsMenuButton.setMenu(reportsSubMenu);
        navigation.addWidget(reportsMenuButton);
        
        WPushButton adminMenuButton = new WPushButton("Admin");
        WPopupMenu adminSubMenu = new WPopupMenu();
        adminMenuButton.setMenu(adminSubMenu);

        adminSubMenu.addItem("Users");
        adminSubMenu.addItem("Roles");
        adminSubMenu.addItem("Configuration");
        
        navigation.addWidget(adminMenuButton);
        
        
        //Content pane
        WContainerWidget contentContainerWidget = new WContainerWidget(mainContainerWidget);
        WGridLayout contentGridLayout = new WGridLayout(contentContainerWidget);
        WTree tree = new WTree();
        tree.setTreeRoot(new WTreeNode("root node"));
       // WTreeView treeView = new WTreeView();
        //treeView.setModel(model);
        
        contentGridLayout.addWidget(tree, 0, 0,2,1);
        contentGridLayout.addWidget(new WTabWidget(), 0, 1,1,1);
        contentGridLayout.addWidget(new WTabWidget(), 1, 1,1,1);
        
    }

    
}
