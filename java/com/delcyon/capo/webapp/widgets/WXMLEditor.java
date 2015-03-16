package com.delcyon.capo.webapp.widgets;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import eu.webtoolkit.jwt.WCompositeWidget;
import eu.webtoolkit.jwt.WContainerWidget;

public class WXMLEditor extends WCompositeWidget
{
	private WContainerWidget implementation = new WContainerWidget();
	private Element rootElement = null;
	private Document schemaDocument = null;
	
	
	public WXMLEditor(Element rootElement)
	{
		super();
		setImplementation(implementation);
		this.rootElement  = rootElement;
	}
}
