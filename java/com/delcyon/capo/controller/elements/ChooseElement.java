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
package com.delcyon.capo.controller.elements;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.AbstractControl;
import com.delcyon.capo.controller.ControlElementProvider;
import com.delcyon.capo.controller.server.ServerSideControl;

/**
 * @author jeremiah
 *
 */
@ControlElementProvider(name="choose")
public class ChooseElement extends AbstractControl
{
	
	
	private static final String[] supportedNamespaces = {CapoApplication.SERVER_NAMESPACE_URI};
	
	@SuppressWarnings("unchecked")
	@Override
	public Enum[] getAttributes()
	{
		return new Enum[]{};
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public Enum[] getRequiredAttributes()
	{
		return new Enum[]{};
	}

	
	@Override
	public String[] getSupportedNamespaces()
	{
		return supportedNamespaces;
	}
	

	/* (non-Javadoc)
	 * @see com.delcyon.capo.controller.elements.ControlElement#processServerSideElement(org.w3c.dom.Element, com.delcyon.capo.controller.elements.GroupElement, com.delcyon.capo.client.ClientRequest)
	 */
	@Override
	public Object processServerSideElement() throws Exception
	{
		//process all children or until done = true;
		NodeList chooseChildren = getControlElementDeclaration().getChildNodes();
		Boolean done = false;
		for (int currentNode = 0; currentNode < chooseChildren.getLength(); currentNode++)
		{
			//see if we need to break out of the for looop
			if (done == true)
			{
				break;
			}
			
			//make sure we have an element
			Node node = chooseChildren.item(currentNode);
			if (node.getNodeType() != Node.ELEMENT_NODE)
			{
				continue;
			}

			Element chooseChildElement = (Element) node;
			ServerSideControl controlElement = (ServerSideControl) getControlElementInstanceForLocalName(chooseChildElement.getLocalName());
			if (controlElement != null)
			{
				controlElement.init(chooseChildElement, this, getParentGroup(),getControllerClientRequestProcessor());
				
				if (controlElement instanceof WhenElement)
				{					
					done = (Boolean) controlElement.processServerSideElement();
				}
				else if (controlElement instanceof OtherwiseElement)
				{
					controlElement.processServerSideElement();
					done = true;
				}
			}
			
		}
		return null;
	}

}
