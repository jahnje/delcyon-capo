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

import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.w3c.dom.Element;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.client.CapoClient;
import com.delcyon.capo.controller.elements.GroupElement.Attributes;
import com.delcyon.capo.controller.server.ControllerClientRequestProcessor;
import com.delcyon.capo.protocol.client.XMLRequest;

/**
 * @author jeremiah
 */
public class ControllerRequest extends XMLRequest
{

	private Element controllerRequestElement = null;
	
	public ControllerRequest(OutputStream outputStream, BufferedInputStream inputStream) throws Exception
	{
		super(outputStream,inputStream);		
		this.controllerRequestElement = super.getImportedChildRootElement();		
	}

	@Override
	public Element getChildRootElement() throws Exception
	{
		return CapoApplication.getDefaultDocument("controller_request.xml").getDocumentElement();
	}
	
	// CS - app
	public void loadSystemVariables()
	{
		Element rootElement = controllerRequestElement;
		// load environment
		Set<Entry<String, String>> envEntrySet = System.getenv().entrySet();
		for (Entry<String, String> entry : envEntrySet)
		{
			Element variableElement = getRequestDocument().createElement("var"); // TODO
			// remove
			// constant

			variableElement.setAttribute(Attributes.name.toString(), entry.getKey());
			variableElement.setAttribute(Attributes.value.toString(), entry.getValue());
			rootElement.appendChild(variableElement);
			CapoClient.logger.log(Level.FINEST, "Storing " + variableElement.getAttribute(Attributes.name.toString()) + " ==> " + variableElement.getAttribute(Attributes.value.toString()));
		}

		// load system properties
		Set<Entry<Object, Object>> propEntrySet = System.getProperties().entrySet();
		for (Entry<Object, Object> entry : propEntrySet)
		{
			Element variableElement = getRequestDocument().createElement("var"); // TODO
			// remove
			// constant
			variableElement.setAttribute(Attributes.name.toString(), entry.getKey().toString());
			variableElement.setAttribute(Attributes.value.toString(), entry.getValue().toString());
			rootElement.appendChild(variableElement);
			CapoClient.logger.log(Level.FINEST, "Storing " + variableElement.getAttribute(Attributes.name.toString()) + " ==> " + variableElement.getAttribute(Attributes.value.toString()));
		}
		
		//load id values
		Set<Entry<String, String>> idEntrySet = ((CapoClient)CapoApplication.getApplication()).getIDMap().entrySet();
		for (Entry<String, String> entry : idEntrySet)
		{
			Element variableElement = getRequestDocument().createElement("var"); // TODO
			// remove
			// constant
			variableElement.setAttribute(Attributes.name.toString(), entry.getKey().toString());
			variableElement.setAttribute(Attributes.value.toString(), entry.getValue().toString());
			rootElement.appendChild(variableElement);
			CapoClient.logger.log(Level.FINEST, "Storing " + variableElement.getAttribute(Attributes.name.toString()) + " ==> " + variableElement.getAttribute(Attributes.value.toString()));
		}
	}

	@Override
	public Element appendElement(Element element)
	{
		return (Element) controllerRequestElement.appendChild(controllerRequestElement.getOwnerDocument().importNode(element, true));
	}
	
	public void setType(String type)
	{
		controllerRequestElement.setAttribute(ControllerClientRequestProcessor.REQUEST_TYPE_ATTRIBUTE, type);
		
	}

	public String getType() {
		return controllerRequestElement.getAttribute(ControllerClientRequestProcessor.REQUEST_TYPE_ATTRIBUTE);
	}
	
}
