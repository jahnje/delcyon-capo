package com.delcyon.capo.webapp.widgets;

import java.io.ByteArrayInputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import com.delcyon.capo.xml.cdom.CDocumentBuilder;

import eu.webtoolkit.jwt.WCompositeWidget;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WGridLayout;

public class WXMLEditor extends WCompositeWidget
{
	private WContainerWidget implementation = new WContainerWidget();
	private Element rootElement = null;
	private Document schemaDocument = null;
	private WCapoXmlTreeView capoXmlTreeView;
	private WGridLayout contentPaneLayout;
	private WXmlElementEditor xmlElementEditor;
	
	
	public WXMLEditor()
	{
		this(null);
	}
	
	public WXMLEditor(Element rootElement)
	{
		super();
		setImplementation(implementation);		
        setMargin(0);
        implementation.setLayout(getContentPaneLayout());
        getContentPaneLayout().addWidget(getCapoXmlTreeView(), 0, 0,1,0);
        getContentPaneLayout().addWidget(getXmlElementEditor(), 0, 1);
		this.rootElement  = rootElement;
		//build tree of elements
		if(this.rootElement != null)
		{
			getCapoXmlTreeView().setRootElement(this.rootElement);
		}
		//with content pane for selected element
		
		
	}
	
	

	private WXmlElementEditor getXmlElementEditor()
	{
		if(xmlElementEditor == null)
		{
			xmlElementEditor = new WXmlElementEditor();			
		}
		return xmlElementEditor;
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
	
	private WCapoXmlTreeView getCapoXmlTreeView()
	{
		if (capoXmlTreeView == null)
		{
			capoXmlTreeView = new WCapoXmlTreeView();
			capoXmlTreeView.selectionChanged().addListener(this, this::selectionChanged);
		}
		
		return capoXmlTreeView;
	}

	/**
	 * This must be a parseable xml document
	 * @param content
	 * @throws Exception 
	 * @throws SAXException 
	 */
	public void setXml(String content) throws SAXException, Exception
	{
		CDocumentBuilder builder = new CDocumentBuilder();
		setXml(builder.parse(new ByteArrayInputStream(content.getBytes())).getDocumentElement());		
	}

	public void setXml(Element rootElement)
	{
		this.rootElement = rootElement;
		getCapoXmlTreeView().setRootElement(rootElement);
		
	}
	
	private void selectionChanged()
	{
		Node element = (Node) getCapoXmlTreeView().getSelectedIndexes().first().getInternalPointer();
		if(element instanceof Element)
		{
			getXmlElementEditor().setElement((Element)element);
		}
		else if(element instanceof Text)
		{
			getXmlElementEditor().setElement(null);
		}
	}
}
