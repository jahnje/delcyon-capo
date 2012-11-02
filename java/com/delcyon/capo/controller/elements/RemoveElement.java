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

import java.util.logging.Level;

import org.w3c.dom.Node;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.AbstractControl;
import com.delcyon.capo.controller.ControlElementProvider;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.Action;
import com.delcyon.capo.xml.XPath;

/**
 * @author jeremiah
 * Deletes a node from the XML
 */
@ControlElementProvider(name="remove")
public class RemoveElement extends AbstractControl
{

	
	
	private enum Attributes
	{
		ref,uri
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
		return new Attributes[]{};
	}
	
	@Override
	public String[] getSupportedNamespaces()
	{
		return supportedNamespaces;
	}
	
	@Override
	public Object processServerSideElement() throws Exception
	{
		
		String ref = getAttributeValue(Attributes.ref);
		String uri = getAttributeValue(Attributes.uri);
		if (ref.isEmpty() == false)
		{
		    Node selectedNode = XPath.selectSingleNode(getControlElementDeclaration(), ref);
		    if (selectedNode != null)
		    {
		        Node parentNode = selectedNode.getParentNode(); 
		        if (parentNode != null)
		        {
		            selectedNode.getParentNode().removeChild(selectedNode);
		        }
		    }
		}
		else if (uri.isEmpty() == false)
		{
		    ResourceDescriptor resourceDescriptor = CapoApplication.getDataManager().getResourceDescriptor(this, uri);
	        if (resourceDescriptor.getResourceMetaData(null).exists() == true)
	        {
	            resourceDescriptor.performAction(null, Action.DELETE);	            
	        }
	        resourceDescriptor.close(getParentGroup());
		}
		return null;
	}
	
}
