/**
Copyright (C) 2012  Delcyon, Inc.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.delcyon.capo.protocol.client;

import java.io.BufferedInputStream;
import java.io.OutputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.server.ControllerClientRequestProcessor;
import com.delcyon.capo.protocol.server.ClientRequest;
import com.delcyon.capo.xml.XMLStreamProcessor;

/**
 * @author jeremiah
 *
 */
public abstract class XMLRequest extends Request
{
	
	
	private Document requestDocument;	
	private XMLStreamProcessor xmlStreamProcessor;
	private String sessionId = null;
	private String requestType = null;
	private Element importedChildRootElement = null;
	
	public XMLRequest(OutputStream outputStream, BufferedInputStream inputStream) throws Exception
	{
		super(outputStream,inputStream);
		
		requestDocument = CapoApplication.getDefaultDocument("default_request.xml");						
		xmlStreamProcessor = new XMLStreamProcessor(inputStream,outputStream);
		importedChildRootElement = (Element) requestDocument.getDocumentElement().appendChild(requestDocument.importNode(getChildRootElement(), true));
	}
	
	public XMLRequest() throws Exception
	{
		super();
		requestDocument = CapoApplication.getDefaultDocument("default_request.xml");
	}
	
	public XMLRequest(XMLStreamProcessor xmlStreamProcessor) throws Exception
	{
	    this.xmlStreamProcessor = xmlStreamProcessor;
	    requestDocument = CapoApplication.getDefaultDocument("default_request.xml");
	    importedChildRootElement = (Element) requestDocument.getDocumentElement().appendChild(requestDocument.importNode(getChildRootElement(), true));
	}
	
	public void init() throws Exception
	{
	    if(xmlStreamProcessor == null)
	    {
	        xmlStreamProcessor = new XMLStreamProcessor(getInputStream(),getOutputStream());
	    }
		importedChildRootElement = (Element) requestDocument.getDocumentElement().appendChild(requestDocument.importNode(getChildRootElement(), true));
	}
	
	public Document getRequestDocument()
	{
		return requestDocument;
	}
	
	public abstract Element getChildRootElement() throws Exception;
	
	//CS - CSE
	public void setRequestType(String requestType)
	{
		this.requestType = requestType;				
	}

	public String getRequestType()
	{
		return requestType;
	}
	
	/**
	 * This will add an element to the root element of the request document
	 * @param element
	 * @return the element that was imported
	 */
	//CS - CSE
	public Element appendElement(Element element)
	{
		return (Element) requestDocument.getDocumentElement().appendChild(requestDocument.importNode(element, true));		
	}

	public String getSessionId()
	{
		return sessionId;
	}
	
	public void setSessionId(String sessionId)
	{
		this.sessionId = sessionId;		
	}
	
	@Override
	public void send() throws Exception
	{
		if (getSessionId() != null)
		{
			requestDocument.getDocumentElement().setAttribute(ClientRequest.SESSION_ID_ATTRIBUTE_NAME,getSessionId());
		}
		if (getRequestType() != null)
		{
			requestDocument.getDocumentElement().setAttribute(ControllerClientRequestProcessor.REQUEST_TYPE_ATTRIBUTE, getRequestType());
		}
		xmlStreamProcessor.writeDocument(requestDocument);		
	}

	@Override
	public void setInputStream(BufferedInputStream inputStream) throws Exception
	{
		xmlStreamProcessor = new XMLStreamProcessor(inputStream, getOutputStream());
		super.setInputStream(inputStream);		
	}
	
	@Override
	public void setOutputStream(OutputStream outputStream) throws Exception
	{
		xmlStreamProcessor = new XMLStreamProcessor(getInputStream(), outputStream);
		super.setOutputStream(outputStream);	
	}


	public Element getImportedChildRootElement()
	{
		return importedChildRootElement;
	}
	
	public Document readResponse() throws Exception
	{
		return xmlStreamProcessor.readNextDocument();
	}
	
}
