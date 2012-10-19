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

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.AbstractControl;
import com.delcyon.capo.controller.ControlElementProvider;
import com.delcyon.capo.xml.XPath;

/**
 * @author jeremiah
 * Appends a node to the XML
 */
@ControlElementProvider(name="setAttribute")
public class SetAttributeElement extends AbstractControl
{

	
	
	public enum Attributes
	{
		name,srcRef,destRef,value
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
		return new Attributes[]{Attributes.name,Attributes.destRef};
	}

	
	@Override
	public String[] getSupportedNamespaces()
	{
		return supportedNamespaces;
	}

	
	
	@Override
	public Object processServerSideElement() throws Exception
	{
		
		String srcRef = getAttributeValue(Attributes.srcRef);
		String dstRef = getAttributeValue(Attributes.destRef);
		String value = getAttributeValue(Attributes.value);
		String name = getAttributeValue(Attributes.name);
		if(value.isEmpty() == false)
		{
		    value = getParentGroup().processVars(value);
		}
		else if (srcRef.isEmpty() == false)
		{
		    value = XPath.selectSingleNodeValue(getControlElementDeclaration(), srcRef);    
		}
		else
		{
		    throw new Exception("Missing Attribute, must have srcRef or value attribute");
		}
		Element destElement = ((Element)XPath.selectSingleNode(getControlElementDeclaration(), dstRef));
		if (destElement == null)
		{
		    throw new Exception("Couldn't find and element at "+dstRef);
		}
		destElement.setAttribute(name, value);		
		return null;
	}

	
}
