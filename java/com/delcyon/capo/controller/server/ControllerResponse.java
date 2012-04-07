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
package com.delcyon.capo.controller.server;

import org.w3c.dom.Element;

import com.delcyon.capo.controller.elements.GroupElement.Attributes;
import com.delcyon.capo.protocol.server.AbstractResponse;
import com.delcyon.capo.server.CapoServer;

/**
 * @author jeremiah
 *
 */
public class ControllerResponse extends AbstractResponse
{

	
	
	public ControllerResponse() throws Exception
	{
		super(CapoServer.getDefaultDocument("controller_response.xml"));
	}
	
	
	/**
	 * 
	 * @param controlElement
	 * @return a copy of the controlElement that is imported into the response document
	 */
	public Element setControlElement(Element controlElement)
	{	
		setType(controlElement.getLocalName());
		return (Element) getResponseDocument().getDocumentElement().appendChild( (Element) getResponseDocument().importNode(controlElement, true));
		
	}
	
	public String getType()
	{
		return getResponseDocument().getDocumentElement().getAttribute(Attributes.type.toString());
	}
	
	public void setType(String type)
	{
		getResponseDocument().getDocumentElement().setAttribute(Attributes.type.toString(), type);
	}

	
	
}
