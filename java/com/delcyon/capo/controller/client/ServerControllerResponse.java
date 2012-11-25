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
package com.delcyon.capo.controller.client;

import java.util.HashMap;
import java.util.logging.Level;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.AbstractControl;
import com.delcyon.capo.controller.Group;
import com.delcyon.capo.controller.server.ControllerResponse;
import com.delcyon.capo.protocol.client.XMLServerResponse;
import com.delcyon.capo.protocol.client.XMLServerResponseProcessor;
import com.delcyon.capo.protocol.client.XMLServerResponseProcessorProvider;

/**
 * @author jeremiah
 *
 */
@XMLServerResponseProcessorProvider(documentElementNames={"ControllerResponse"},namespaceURIs={})
public class ServerControllerResponse implements XMLServerResponseProcessor
{

	private Document responseDocument;	
	private XMLServerResponse xmlServerResponse;
    private HashMap<String, String> sessionHashMap = null;

	

	@Override
	public void init(Document responseDocument,XMLServerResponse xmlServerResponse,HashMap<String, String> sessionHashMap) throws Exception
	{
	    this.sessionHashMap = sessionHashMap;
		this.responseDocument = responseDocument;
		this.xmlServerResponse = xmlServerResponse;		
	}

	@Override
	public boolean isStreamProcessor()
	{	 
	    return false;
	}
	
	@Override
	public void process() throws Exception
	{
		Element controlElement = getControlElementDeclaration();
		if (controlElement != null)
		{
			ClientSideControl clientSideControl = (ClientSideControl) AbstractControl.getControlElementInstanceForLocalName(controlElement.getLocalName());
			Element resultElement = null;
			if (clientSideControl != null)
			{
				Group group = new Group(null, null, null, null);
				group.setVariableHashMap(sessionHashMap);
				clientSideControl.init(getControlElementDeclaration(), null, group, this);
				resultElement = clientSideControl.processClientSideElement();
				if (resultElement != null)
				{
				    
				    ControllerResponse controllerResponse = new ControllerResponse();
				    controllerResponse.setSessionID(xmlServerResponse.getSessionID());
				    controllerResponse.setType(xmlServerResponse.getResponseType());
				    controllerResponse.appendElement(resultElement);					
					xmlServerResponse.writeDocument(controllerResponse.getResponseDocument());
				}
			}
			else
			{
				CapoApplication.logger.log(Level.SEVERE, "Couldn't get a ControlElement instance for "+controlElement.getLocalName());
				throw new Exception("Couldn't get a ControlElement instance for "+controlElement.getLocalName());
			}
		}
		//TODO process result Element
		
	}
	
	@Override
	public Document getResponseDocument()
	{
		return responseDocument;
	}

	
	public Element getControlElementDeclaration() throws Exception
	{
		NodeList nodeList = responseDocument.getDocumentElement().getElementsByTagName("*");
		if (nodeList.getLength() > 0)
		{
			return (Element) nodeList.item(0);
		}
		else
		{
			return null;
		}
	}

	public String getSessionID()
	{
		return xmlServerResponse.getSessionID();
	}

	
}
