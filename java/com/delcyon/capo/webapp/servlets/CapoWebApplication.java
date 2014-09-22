/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package com.delcyon.capo.webapp.servlets;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.SortedSet;

import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.w3c.dom.Element;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.datastream.StreamUtil;
import com.delcyon.capo.datastream.stream_attribute_filter.MimeTypeFilterInputStream;
import com.delcyon.capo.resourcemanager.ContentFormatType;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.Action;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.LifeCycle;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.State;
import com.delcyon.capo.server.jackrabbit.CapoJcrServer;
import com.delcyon.capo.util.HexUtil;
import com.delcyon.capo.webapp.models.DomItemModel;
import com.delcyon.capo.webapp.models.DomItemModel.DomUse;
import com.delcyon.capo.webapp.models.FileResourceDescriptorItemModel;
import com.delcyon.capo.webapp.servlets.resource.AbstractResourceServlet;
import com.delcyon.capo.webapp.servlets.resource.WResourceDescriptor;
import com.delcyon.capo.webapp.widgets.CapoWTreeView;
import com.delcyon.capo.webapp.widgets.WCSSItemDelegate;
import com.delcyon.capo.xml.XPath;
import com.delcyon.capo.xml.dom.ResourceDocument;
import com.delcyon.capo.xml.dom.ResourceDocumentBuilder;

import eu.webtoolkit.jwt.AlignmentFlag;
import eu.webtoolkit.jwt.AnchorTarget;
import eu.webtoolkit.jwt.PositionScheme;
import eu.webtoolkit.jwt.SelectionBehavior;
import eu.webtoolkit.jwt.SelectionMode;
import eu.webtoolkit.jwt.Signal;
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
import eu.webtoolkit.jwt.WTableView;
import eu.webtoolkit.jwt.WText;
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
	private WTabWidget detailsPane;
	private WTabWidget subDetailsPane;
	private CapoWTreeView treeView;
    private ResourceDocument document;
    private Session session;
    
	public CapoWebApplication(WEnvironment env, boolean embedded) {
        super(env);
        WBootstrapTheme bootstrapTheme = new WBootstrapTheme();
        setTheme(bootstrapTheme);
        useStyleSheet(new WLink("/wr/css/local.css"));
        //setCssTheme("polished");
        setTitle("Capo");
        useStyleSheet(new WLink("/wr/source/sh_style.css"));
        require("/wr/source/sh_main.js");
        try
        {
            session = CapoJcrServer.getRepository().login(new SimpleCredentials("admin", "admin".toCharArray()));
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
//            FileResourceType fileResourceType = new FileResourceType();
//            ResourceDescriptor resourceDescriptor = fileResourceType.getResourceDescriptor("file:/");
            ResourceDescriptor resourceDescriptor = CapoApplication.getDataManager().getResourceDescriptor(null, "repo:/");
          ResourceDescriptor resourceDescriptor2 = CapoApplication.getDataManager().getResourceDirectory("CLIENTS_DIR");
            ResourceDocumentBuilder documentBuilder = new ResourceDocumentBuilder();
             document = (ResourceDocument) documentBuilder.buildDocument(resourceDescriptor2);
			
			
			
			getContentPaneLayout().addWidget(getTreeView(resourceDescriptor), 0, 0,1,0);	
			
			
		} catch (Exception e)
		{			
			e.printStackTrace();
		}
        
        getContentPaneLayout().addLayout(getDetailsPaneLayout(), 0, 1);
        WPushButton pathButton = new WPushButton("test");
        pathButton.setLink(new WLink(Type.InternalPath, "/legend"));
        getContentPaneLayout().addWidget(pathButton, 2, 0, 1, 1, AlignmentFlag.AlignTop); 
        
        WPushButton pathButton2 = new WPushButton("test2");
        //pathButton2.setLink(new WLink(Type.InternalPath, "/legend2"));
        pathButton2.clicked().addListener(this, new Signal.Listener()
        {
            
            public void trigger()
            {
                try
                {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                    XPath.dumpNode(document.getDocumentElement(), outputStream);
                    XPath.dumpNode(document.getDocumentElement(), System.out);
                    
                    session.importXML("/", new ByteArrayInputStream(outputStream.toByteArray()), 0);
                    //dump(session.getRootNode());
                    session.save();
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
        getContentPaneLayout().addWidget(pathButton2, 3, 0, 1, 1, AlignmentFlag.AlignTop);
        
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
        treeView.setAttributeValue("oncontextmenu","event.cancelBubble = true; event.returnValue = false; return false;");
        if (data instanceof Element)
        {
            treeView.setModel(new DomItemModel((Element) data,DomUse.NAVIGATION));            
        }
        else if (data instanceof ResourceDescriptor)
        {
            treeView.setModel(new FileResourceDescriptorItemModel((ResourceDescriptor)data,DomUse.NAVIGATION));
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
		       

		        adminSubMenu.addItem("Create Dir").clicked().addListener(CapoWebApplication.this, new Signal.Listener()
		        {
		            public void trigger() {
		                final WModelIndex index = treeView.getSelectedIndexes().first();
		                if(index != null)
		                {
		                    System.out.println(((ResourceDescriptor)index.getInternalPointer()).getLocalName());
		                    final WDialog dialog = new WDialog("Create Directory");
		                    dialog.setClosable(true);
		                    dialog.rejectWhenEscapePressed(true);
		                    WLabel label = new WLabel("Enter a directory name", dialog.getContents());
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
		                            ResourceDescriptor selectedResourceDescriptor = (ResourceDescriptor) index.getInternalPointer();
		                           
		                            try
                                    {
		                                ResourceDescriptor childResourceDescriptor = selectedResourceDescriptor.getChildResourceDescriptor(null, edit.getText());
		                                childResourceDescriptor.init(null, null, LifeCycle.EXPLICIT, false);
                                        childResourceDescriptor.performAction(null, Action.CREATE);                                        
                                        session.save();
                                        childResourceDescriptor.reset(State.INITIALIZED);
                                        ((FileResourceDescriptorItemModel) treeView.getModel()).reload();
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
    		final Object selectedItem =  modelIndex.getInternalPointer();
    		while(getDetailsPane().getCount() > 0)
    		{    			
    			getDetailsPane().removeTab(getDetailsPane().getWidget(0));    			
    		}
    		WContainerWidget detailsContainerWidget = new WContainerWidget();
    		final WTableView tableView = new WTableView();
    		tableView.addStyleClass("bg-transparent");
    		tableView.setItemDelegateForColumn(0,new WCSSItemDelegate("font-weight: bold;"));
    		tableView.setSortingEnabled(true);
    		tableView.setSelectable(true);   		
    		tableView.setAlternatingRowColors(true);    		
    		tableView.setColumnResizeEnabled(true);
    		tableView.setColumnAlignment(0, AlignmentFlag.AlignRight);
    		tableView.setColumnWidth(1, new WLength(500));
    		tableView.setSelectionMode(SelectionMode.SingleSelection);
    		
    		String content = null;
    		String contentType = null;
    		String mimeType = null;
    		String fileURI = null;
    		if (selectedItem instanceof Element)
    		{
    		    tableView.setModel(new DomItemModel((Element) selectedItem, DomUse.ATTRIBUTES));
    		    content = ((Element) selectedItem).getTextContent();
    		}
    		else if (selectedItem instanceof ResourceDescriptor)
    		{
    		    tableView.setModel(new FileResourceDescriptorItemModel((ResourceDescriptor) selectedItem, DomUse.ATTRIBUTES));
    		    try
    		    {
    		        if(((ResourceDescriptor) selectedItem).getResourceMetaData(null).isContainer() == false)
    		        {
    		            WAnchor anchor = new WAnchor(new WLink(new WResourceDescriptor((ResourceDescriptor) selectedItem)),"Download");
    		            anchor.setTarget(AnchorTarget.TargetNewWindow);
    		            final WFileUpload upload = new WFileUpload();
    		            upload.setFileTextSize(10000);
    		            upload.setProgressBar(new WProgressBar());
    		            upload.changed().addListener(this, new Signal.Listener() {
    		                public void trigger() {
    		                    upload.upload();    		                   
    		                }
    		            });
    		            upload.uploaded().addListener(this, new Signal.Listener() {
    		                public void trigger() {
    		                    System.out.println("done");
    		                    List<UploadedFile> uploadedFiles = upload.getUploadedFiles();
    		                    String tempFileName = uploadedFiles.get(0).getSpoolFileName();
    		                    File tempFile = new File(tempFileName);
    		                    try
                                {
                                    StreamUtil.readInputStreamIntoOutputStream(new FileInputStream(tempFile),  ((ResourceDescriptor) selectedItem).getOutputStream(null));
                                    ((ResourceDescriptor) selectedItem).getResourceMetaData(null).refresh();
                                    ((ResourceDescriptor) selectedItem).advanceState(State.CLOSED,null);
                                    ((ResourceDescriptor) selectedItem).reset(State.OPEN);
                                    ((FileResourceDescriptorItemModel) tableView.getModel()).reload();
                                    upload.setProgressBar(new WProgressBar());                                    
                                    upload.show();
                                    upload.enableAjax();
                                    
                                }                                
                                catch (Exception e)
                                {                                    
                                    e.printStackTrace();
                                }
    		                    
    		                }
    		            });
    		            upload.fileTooLarge().addListener(this, new Signal.Listener() {
    		                public void trigger() {
    		                    System.out.println("error, too large");
    		                }
    		            });
    		            
    		            detailsContainerWidget.addWidget(upload);
    		            detailsContainerWidget.addWidget(anchor);
    		            ContentFormatType contentFormatType = ((ResourceDescriptor) selectedItem).getResourceMetaData(null).getContentFormatType();
    		            mimeType = ((ResourceDescriptor) selectedItem).getResourceMetaData(null).getValue(MimeTypeFilterInputStream.MIME_TYPE_ATTRIBUTE);
    		            fileURI = ((ResourceDescriptor) selectedItem).getResourceMetaData(null).getResourceURI().getBaseURI();
    		            if(mimeType == null)
    		            {
    		            	mimeType = "";
    		            }
    		            long length = ((ResourceDescriptor) selectedItem).getResourceMetaData(null).getLength();
    		            if(contentFormatType == ContentFormatType.TEXT)
    		            {
    		                ((ResourceDescriptor) selectedItem).getResourceState();
    		                content = new String(((ResourceDescriptor) selectedItem).readBlock(null));
    		                ((ResourceDescriptor) selectedItem).reset(State.OPEN);
    		                String localName = ((ResourceDescriptor) selectedItem).getLocalName();
    		                contentType = localName.substring(localName.indexOf(".")+1);
    		            }
    		            else if (contentFormatType == ContentFormatType.XML)
    		            {
    		                ((ResourceDescriptor) selectedItem).getResourceState();
                            content = new String(((ResourceDescriptor) selectedItem).readBlock(null));
                            ((ResourceDescriptor) selectedItem).reset(State.OPEN);
                            contentType = "xml";
    		            }
    		            else if(contentFormatType == ContentFormatType.BINARY && length < 70000l && mimeType.startsWith("image/") == false)
    		            {
    		                byte[] bytes = ((ResourceDescriptor) selectedItem).readBlock(null);
    		                ((ResourceDescriptor) selectedItem).reset(State.OPEN);
//    		                char[] hexArray = "0123456789ABCDEF".toCharArray();
//
//    		                char[] hexChars = new char[(bytes.length * 3)+(bytes.length/16)];
//    		                for ( int j = 0; j < bytes.length; j++ ) 
//    		                {
//    		                    int v = bytes[j] & 0xFF;
//    		                    hexChars[j * 3] = hexArray[v >>> 4];
//    		                    hexChars[j * 3 + 1] = hexArray[v & 0x0F];
//    		                    hexChars[j * 3 + 2] = ' ';
//    		                    if(j % 16 == 0)
//    		                    {
//    		                        
//    		                    }
//    		                }
    		                
    		                content = HexUtil.dump(bytes); 
    		                contentType = "hex";

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
    		    
                WText wText = new WText("<pre class='sh_"+contentType+" bg-transparent'>"+Utils.htmlEncode(content)+"</pre>", TextFormat.XHTMLUnsafeText);
                
                if(contentType != null && AbstractResourceServlet.getResourceServletInstance().exists("/wr/source/lang/sh_"+contentType+".js"))
                {
                    
                    WApplication.getInstance().require("/wr/source/lang/sh_"+contentType+".js");
                    wText.doJavaScript("sh_highlightDocument();");
                }
                wText.addStyleClass("bg-transparent");
               // System.out.println(textEdit.getText());
                getDetailsPane().addTab(wText, "Content");
                
            }
    		else if (mimeType != null && mimeType.startsWith("image/"))
    		{
    			WResourceDescriptor wResourceDescriptor = new WResourceDescriptor((ResourceDescriptor) selectedItem);
    			getDetailsPane().addTab(new WImage(wResourceDescriptor, "Content"), "Content");
    		}
    		
    		detailsContainerWidget.addWidget(tableView);
    		getDetailsPane().addTab(detailsContainerWidget, "Details");
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
