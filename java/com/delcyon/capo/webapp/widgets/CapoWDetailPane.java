package com.delcyon.capo.webapp.widgets;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.List;

import org.w3c.dom.Element;

import com.delcyon.capo.datastream.StreamUtil;
import com.delcyon.capo.datastream.stream_attribute_filter.MimeTypeFilterInputStream;
import com.delcyon.capo.resourcemanager.ContentFormatType;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.State;
import com.delcyon.capo.util.HexUtil;
import com.delcyon.capo.webapp.models.DomItemModel;
import com.delcyon.capo.webapp.models.DomItemModel.DomUse;
import com.delcyon.capo.webapp.models.ResourceDescriptorItemModel;
import com.delcyon.capo.webapp.servlets.resource.WResourceDescriptor;
import com.delcyon.capo.webapp.widgets.WAceEditor.Theme;

import eu.webtoolkit.jwt.AlignmentFlag;
import eu.webtoolkit.jwt.AnchorTarget;
import eu.webtoolkit.jwt.SelectionMode;
import eu.webtoolkit.jwt.Signal;
import eu.webtoolkit.jwt.Signal1;
import eu.webtoolkit.jwt.WAnchor;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WFileUpload;
import eu.webtoolkit.jwt.WImage;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WLink;
import eu.webtoolkit.jwt.WProgressBar;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WTabWidget;
import eu.webtoolkit.jwt.WTableView;
import eu.webtoolkit.jwt.servlet.UploadedFile;

public class CapoWDetailPane extends WTabWidget
{
    private Object model = null;

    public void setModel(Object model)
    {
        this.model = model;
        while(this.getCount() > 0)
        {               
            this.removeTab(this.getWidget(0));              
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
        ContentFormatType contentFormatType = null;
        String mimeType = null;
        String fileURI = null;
        if (this.model instanceof Element)
        {
            tableView.setModel(new DomItemModel((Element) this.model, DomUse.ATTRIBUTES));
            content = ((Element) this.model).getTextContent();
        }
        else if (this.model instanceof ResourceDescriptor)
        {
            tableView.setModel(new ResourceDescriptorItemModel((ResourceDescriptor) this.model, DomUse.ATTRIBUTES));
            try
            {
                //if(((ResourceDescriptor) this.model).getResourceMetaData(null).isContainer() == false)
                {
                    WAnchor anchor = new WAnchor(new WLink(new WResourceDescriptor((ResourceDescriptor) this.model)),"Download");
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
                                OutputStream outputStream = ((ResourceDescriptor) CapoWDetailPane.this.model).getOutputStream(null);
                                StreamUtil.readInputStreamIntoOutputStream(new FileInputStream(tempFile), outputStream );
                                outputStream.close();
                                ((ResourceDescriptor) CapoWDetailPane.this.model).getResourceMetaData(null).refresh();
                                ((ResourceDescriptor) CapoWDetailPane.this.model).advanceState(State.CLOSED,null);
                                ((ResourceDescriptor) CapoWDetailPane.this.model).reset(State.OPEN);
                                ((ResourceDescriptorItemModel) tableView.getModel()).reload();
                                CapoWDetailPane.this.setModel(CapoWDetailPane.this.model);
                                //selectedItemChanged();
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
                    
                    WPushButton clearContentPushButton = new WPushButton("Clear Content");
                    clearContentPushButton.clicked().addListener(this, new Signal.Listener() {
                        public void trigger() {
                            try
                            {
                                ((ResourceDescriptor) CapoWDetailPane.this.model).writeBlock(null, "".getBytes());                                    
                                ((ResourceDescriptor) CapoWDetailPane.this.model).getResourceMetaData(null).refresh();
                                ((ResourceDescriptorItemModel) tableView.getModel()).reload();
                                CapoWDetailPane.this.setModel(CapoWDetailPane.this.model);
                                //selectedItemChanged();
                            }
                            catch (Exception e)
                            {                                    
                                e.printStackTrace();
                            }
                        }
                    });
                    
                    detailsContainerWidget.addWidget(upload);
                    detailsContainerWidget.addWidget(anchor);
                    detailsContainerWidget.addWidget(clearContentPushButton);
                    contentFormatType = ((ResourceDescriptor) this.model).getResourceMetaData(null).getContentFormatType();
                    mimeType = ((ResourceDescriptor) this.model).getResourceMetaData(null).getValue(MimeTypeFilterInputStream.MIME_TYPE_ATTRIBUTE);
                    fileURI = ((ResourceDescriptor) this.model).getResourceMetaData(null).getResourceURI().getBaseURI();
                    if(mimeType == null)
                    {
                        mimeType = "";
                    }
                    long length = ((ResourceDescriptor) this.model).getResourceMetaData(null).getLength();
                    if(contentFormatType == ContentFormatType.TEXT || contentFormatType == ContentFormatType.NO_CONTENT)
                    {
                        if(contentFormatType == ContentFormatType.NO_CONTENT)
                        {
                            content = "";
                        }
                        else
                        {
                            ((ResourceDescriptor) this.model).getResourceState();
                            content = new String(((ResourceDescriptor) this.model).readBlock(null));
                            ((ResourceDescriptor) this.model).reset(State.OPEN);
                        }
                        String localName = ((ResourceDescriptor) this.model).getLocalName();
                        if(localName.indexOf(".") <= 0) //check for extension
                        {
                           if(mimeType.equalsIgnoreCase("application/x-shellscript"))
                           {
                               contentType = "sh";
                           }
                        }
                        else //use extenstion
                        {
                            contentType = localName.substring(localName.indexOf(".")+1);
                        }
                    }
                    else if (contentFormatType == ContentFormatType.XML)
                    {
                        ((ResourceDescriptor) this.model).getResourceState();
                        content = new String(((ResourceDescriptor) this.model).readBlock(null));
                        ((ResourceDescriptor) this.model).reset(State.OPEN);
                        contentType = "xml";
                    }
                    else if(contentFormatType == ContentFormatType.BINARY && length < 70000l && mimeType.startsWith("image/") == false)
                    {
                        byte[] bytes = ((ResourceDescriptor) this.model).readBlock(null);
                        ((ResourceDescriptor) this.model).reset(State.OPEN);
                        content = HexUtil.dump(bytes); 
                        contentType = "hex";

                    }                       
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        
        if (content != null)
        {               
           
           // WTextArea textEdit = new WTextArea(Utils.htmlEncode(content));
            WAceEditor wText = new WAceEditor(content,contentType);
            wText.setReadOnly(true);
            wText.setTheme(Theme.eclipse);
            this.addTab(wText, "Content");

            //don't allow binary content to be edited
            if (contentFormatType != ContentFormatType.BINARY)
            {
                WAceEditor aceEditor = new WAceEditor(content,contentType);                
                aceEditor.save().addListener(this, new Signal1.Listener<String>()
                {
                    public void trigger(String arg1) 
                    {
                        try
                        {

                            ((ResourceDescriptor) CapoWDetailPane.this.model).writeBlock(null, arg1.getBytes());
                            ((ResourceDescriptor) CapoWDetailPane.this.model).getResourceMetaData(null).refresh();
                            ((ResourceDescriptor) CapoWDetailPane.this.model).advanceState(State.CLOSED,null);
                            ((ResourceDescriptor) CapoWDetailPane.this.model).reset(State.OPEN);
                            ((ResourceDescriptorItemModel) tableView.getModel()).reload();                                                                                                           
                            CapoWDetailPane.this.setModel(CapoWDetailPane.this.model);
                            //selectedItemChanged();
                        } catch (Exception exception)
                        {
                            exception.printStackTrace();
                        }
                    }; 
                });
                this.addTab(aceEditor, "Edit");
            }
        }
        else if (mimeType != null && mimeType.startsWith("image/"))
        {
            WResourceDescriptor wResourceDescriptor = new WResourceDescriptor((ResourceDescriptor) this.model);
            this.addTab(new WImage(wResourceDescriptor, "Content"), "Content");
        }
        
        detailsContainerWidget.addWidget(tableView);
        this.addTab(detailsContainerWidget, "Details");
        this.getWidget(0).setAttributeValue("style", "background-color: rgba(255, 255, 255, 0.55);");
    }

}
