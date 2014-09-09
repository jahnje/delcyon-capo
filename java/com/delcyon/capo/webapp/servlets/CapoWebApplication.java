/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package com.delcyon.capo.webapp.servlets;

import java.util.SortedSet;

import org.w3c.dom.Element;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.resourcemanager.ContentFormatType;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.State;
import com.delcyon.capo.resourcemanager.types.FileResourceDescriptor;
import com.delcyon.capo.webapp.models.DomItemModel;
import com.delcyon.capo.webapp.models.DomItemModel.DomUse;
import com.delcyon.capo.webapp.models.FileResourceDescriptorItemModel;
import com.delcyon.capo.webapp.widgets.CapoWTreeView;

import eu.webtoolkit.jwt.AlignmentFlag;
import eu.webtoolkit.jwt.PositionScheme;
import eu.webtoolkit.jwt.SelectionBehavior;
import eu.webtoolkit.jwt.SelectionMode;
import eu.webtoolkit.jwt.Signal;
import eu.webtoolkit.jwt.Signal2;
import eu.webtoolkit.jwt.TextFormat;
import eu.webtoolkit.jwt.Utils;
import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WBootstrapTheme;
import eu.webtoolkit.jwt.WBoxLayout;
import eu.webtoolkit.jwt.WBoxLayout.Direction;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WGridLayout;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WLink;
import eu.webtoolkit.jwt.WModelIndex;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WNavigationBar;
import eu.webtoolkit.jwt.WPopupMenu;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WStackedWidget;
import eu.webtoolkit.jwt.WTabWidget;
import eu.webtoolkit.jwt.WTableView;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.WTextArea;
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
	private CapoWTreeView treeView;

	public CapoWebApplication(WEnvironment env, boolean embedded) {
        super(env);
        WBootstrapTheme bootstrapTheme = new WBootstrapTheme();
        setTheme(bootstrapTheme);
        useStyleSheet(new WLink("/wr/css/local.css"));
        //setCssTheme("polished");
        setTitle("Capo");
        useStyleSheet(new WLink("/wr/source/sh_style.css"));
        require("/wr/source/sh_main.js");
        createUI();
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
			ResourceDescriptor clientsResourceDescriptor = CapoApplication.getDataManager().getResourceDirectory("CAPO_DIR");
			
			
			getContentPaneLayout().addWidget(getTreeView(clientsResourceDescriptor), 0, 0,1,0);	
			
			
		} catch (Exception e)
		{			
			e.printStackTrace();
		}
        
        getContentPaneLayout().addLayout(getDetailsPaneLayout(), 0, 1);
        getContentPaneLayout().addWidget(createTitle("Legend"), 2, 0, 1, 1, AlignmentFlag.AlignTop); 
        
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
    		contentPane.setAttributeValue("style", "background-image: url('/wr/images/background.png'); background-repeat: no-repeat; background-position: bottom right; background-size: contain;");
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
//        treeView.setAttributeValue("oncontextmenu","event.cancelBubble = true; event.returnValue = false; return false;");
        if (data instanceof Element)
        {
            treeView.setModel(new DomItemModel((Element) data,DomUse.NAVIGATION));            
        }
        else if (data instanceof ResourceDescriptor)
        {
            treeView.setModel(new FileResourceDescriptorItemModel((FileResourceDescriptor)data,DomUse.NAVIGATION));
        }
        //tree
        treeView.setWidth(new WLength(250));//, WLength.Auto);
        
        
       // treeView.setWidth(new WLength("100%"));
        treeView.setSelectionMode(SelectionMode.SingleSelection);
        treeView.setSelectionBehavior(SelectionBehavior.SelectItems);
        treeView.setSelectable(true);
        treeView.expandToDepth(1);
        treeView.setAlternatingRowColors(true);
        treeView.selectionChanged().addListener(this, new Signal.Listener() {
            public void trigger() {                
            	selectedItemChanged();
            	treeView.refresh();
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
    		Object selectedItem =  modelIndex.getInternalPointer();
    		while(getDetailsPane().getCount() > 0)
    		{    			
    			getDetailsPane().removeTab(getDetailsPane().getWidget(0));    			
    		}
    		WTableView tableView = new WTableView();
    		tableView.addStyleClass("bg-transparent");
    		tableView.setSortingEnabled(true);
    		tableView.setSelectable(true);   		
    		tableView.setAlternatingRowColors(true);    		
    		tableView.setColumnResizeEnabled(true);
    		tableView.setColumnAlignment(0, AlignmentFlag.AlignRight);
    		tableView.setColumnWidth(1, new WLength(500));
    		tableView.setSelectionMode(SelectionMode.SingleSelection);
    		
    		String content = null;
    		if (selectedItem instanceof Element)
    		{
    		    tableView.setModel(new DomItemModel((Element) selectedItem, DomUse.ATTRIBUTES));
    		    content = ((Element) selectedItem).getTextContent();
    		}
    		else if (selectedItem instanceof ResourceDescriptor)
    		{
    		    tableView.setModel(new FileResourceDescriptorItemModel((FileResourceDescriptor) selectedItem, DomUse.ATTRIBUTES));
    		    try
    		    {
    		        if(((FileResourceDescriptor) selectedItem).getResourceMetaData(null).isContainer() == false)
    		        {
    		            if(((FileResourceDescriptor) selectedItem).getResourceMetaData(null).getContentFormatType() != ContentFormatType.BINARY)
    		            {
    		                ((FileResourceDescriptor) selectedItem).getResourceState();
    		                content = new String(((FileResourceDescriptor) selectedItem).readBlock(null));
    		                ((FileResourceDescriptor) selectedItem).reset(State.OPEN);
    		            }
    		            else
    		            {
    		                ((FileResourceDescriptor) selectedItem).readBlock(null);
    		                ((FileResourceDescriptor) selectedItem).reset(State.OPEN);
    		            }
    		        }
    		    } catch (Exception e)
    		    {
    		        e.printStackTrace();
    		    }
    		}
    		
    		if (content != null && content.trim().isEmpty() == false)
            {               
               
               // WTextArea textEdit = new WTextArea(Utils.htmlEncode(content));
    		    
                WText wText = new WText("<pre class='sh_xml bg-transparent'>"+Utils.htmlEncode(content)+"</pre>", TextFormat.XHTMLUnsafeText);
                WApplication.getInstance().require("/wr/source/lang/sh_xml.js");
                wText.doJavaScript("sh_highlightDocument();");
                wText.addStyleClass("bg-transparent");
               // System.out.println(textEdit.getText());
                getDetailsPane().addTab(wText, "Content");
                
            }
    		
    		getDetailsPane().addTab(tableView, "Details");
    		getDetailsPane().getWidget(0).setAttributeValue("style", "background-color: rgba(255, 255, 255, 0.55);");
    		
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
