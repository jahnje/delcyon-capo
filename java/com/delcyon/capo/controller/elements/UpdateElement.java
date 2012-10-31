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
import com.delcyon.capo.resourcemanager.ResourceParameterBuilder;
import com.delcyon.capo.xml.XPath;
import com.delcyon.capo.xml.dom.ResourceElement;

/**
 * @author jeremiah
 * Appends a node to the XML
 */
@ControlElementProvider(name="update")
public class UpdateElement extends AbstractControl
{

	
	
	public enum Attributes
	{
		name,resourceRef
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
		return new Attributes[]{Attributes.resourceRef};
	}

	
	@Override
	public String[] getSupportedNamespaces()
	{
		return supportedNamespaces;
	}

	
	
	@Override
	public Object processServerSideElement() throws Exception
	{
		
		String resourceRef = getAttributeValue(Attributes.resourceRef);
		
		Element resourceRefElement = ((Element)XPath.selectSingleNode(getControlElementDeclaration(), resourceRef));
		if(resourceRefElement instanceof ResourceElement)
		{
			((ResourceElement) resourceRefElement).update(getParentGroup(),this,ResourceParameterBuilder.getResourceParameters(getControlElementDeclaration()));
		}
		else if (resourceRefElement == null)
		{
			 throw new Exception("Couldn't find a resource element at "+resourceRef);
		}
		else
		{
			throw new Exception(resourceRef + " isn't a Resource Element");
		}
		return null;
	}

	
}
