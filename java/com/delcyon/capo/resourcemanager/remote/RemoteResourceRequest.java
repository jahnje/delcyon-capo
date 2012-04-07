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
package com.delcyon.capo.resourcemanager.remote;

import java.io.BufferedInputStream;
import java.io.OutputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.server.ControllerClientRequestProcessor;
import com.delcyon.capo.protocol.client.XMLRequest;
import com.delcyon.capo.protocol.server.ClientRequest;
import com.delcyon.capo.resourcemanager.remote.RemoteResourceDescriptorMessage.MessageType;
import com.delcyon.capo.xml.XPath;

/**
 * @author jeremiah
 *
 */
public class RemoteResourceRequest extends XMLRequest
{

private Element controllerRequestElement = null;
	
	public RemoteResourceRequest(OutputStream outputStream, BufferedInputStream inputStream) throws Exception
	{
		super(outputStream,inputStream);		
		this.controllerRequestElement = super.getImportedChildRootElement();		
	}
	
	/* (non-Javadoc)
	 * @see com.delcyon.capo.protocol.client.XMLRequest#getChildRootElement()
	 */
	@Override
	public Element getChildRootElement() throws Exception
	{
		return CapoApplication.getDefaultDocument("remoteResourceRequest.xml").getDocumentElement();
	}

	public void setType(MessageType messageType)
	{
		controllerRequestElement.setAttribute(ControllerClientRequestProcessor.REQUEST_TYPE_ATTRIBUTE, messageType.toString());		
	}
	
	

	public static MessageType getType(ClientRequest clientRequest) throws Exception
	{
		Document requestDocument = XPath.unwrapDocument(clientRequest.getRequestDocument());
		return MessageType.valueOf(requestDocument.getDocumentElement().getAttribute(ControllerClientRequestProcessor.REQUEST_TYPE_ATTRIBUTE));
	}
	
	public static String getVarName(ClientRequest clientRequest) throws Exception
	{
		Document requestDocument = XPath.unwrapDocument(clientRequest.getRequestDocument());
		return requestDocument.getDocumentElement().getAttribute(MessageType.GET_VAR_VALUE.toString());
	}

	public void setVarName(String varName)
	{
		controllerRequestElement.setAttribute(MessageType.GET_VAR_VALUE.toString(), varName);			
	}
	
}
