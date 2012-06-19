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

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.AbstractControl;
import com.delcyon.capo.controller.ControlElementProvider;
import com.delcyon.capo.xml.XPath;

/**
 * @author jeremiah
 * Creates an Element in the XML
 */
@ControlElementProvider(name="create")
public class CreateElement extends AbstractControl
{

	
	
	private enum Attributes
	{
		elementName,elementDstRef,elementNamespaceURI
	}
	
	
	
	private static final String[] supportedNamespaces = {CapoApplication.SERVER_NAMESPACE_URI};
	
	
	@Override
	public Attributes[] getAttributes()
	{
		return Attributes.values();
	}

		
	@Override
	public Attributes[] getRequiredAttributes()
	{
		return new Attributes[]{Attributes.elementName};
	}

	
	@Override
	public String[] getSupportedNamespaces()
	{
		return supportedNamespaces;
	}

	
	
	@Override
	public Object processServerSideElement() throws Exception
	{
		
		
		String name = getAttributeValue(Attributes.elementName);
		
		String dstRef = getAttributeValue(Attributes.elementDstRef);
		if (dstRef == null || dstRef.trim().isEmpty())
		{
			dstRef = ".";
		}
		
		Element newElement = null;
		
		String elementNamespaceURI = getAttributeValue(Attributes.elementNamespaceURI);
		if (elementNamespaceURI == null || elementNamespaceURI.trim().isEmpty())
		{
			newElement = getControlElementDeclaration().getOwnerDocument().createElement(name);
		}
		else
		{
			newElement = getControlElementDeclaration().getOwnerDocument().createElementNS(elementNamespaceURI, name);
		}
		
		NamedNodeMap namedNodeMap = getControlElementDeclaration().getAttributes();
		
		for(int index = 0; index < namedNodeMap.getLength(); index++)
		{			
			Attr attribute = (Attr) namedNodeMap.item(index);
			if (attribute.getLocalName().equals("elementName") || attribute.getLocalName().equals("dstRef"))
			{
				//do nothing
			}
			else
			{
				newElement.setAttributeNode((Attr) attribute.cloneNode(true));
			}
		}

		XPath.selectSingleNode(getControlElementDeclaration(), dstRef).appendChild(newElement);		

		return null;
	}

	
}
