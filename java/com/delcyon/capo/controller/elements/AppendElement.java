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

import org.w3c.dom.Node;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.AbstractControl;
import com.delcyon.capo.controller.ControlElementProvider;
import com.delcyon.capo.xml.XPath;

/**
 * @author jeremiah
 * Appends a node to the XML
 */
@ControlElementProvider(name="append")
public class AppendElement extends AbstractControl
{

	
	
	public enum Attributes
	{
		name,srcRef,destRef
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
		return new Attributes[]{Attributes.srcRef,Attributes.destRef};
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
		Node srcNode = XPath.selectSingleNode(getControlElementDeclaration(), srcRef);
		if(srcNode == null)
		{
		    throw new Exception("no node found at: "+srcRef);
		}
		
		Node destNode = XPath.selectSingleNode(getControlElementDeclaration(), dstRef);
		if(destNode == null)
        {
            throw new Exception("no node found at: "+dstRef);
        }
		
		destNode.appendChild(srcNode);		
		return null;
	}

	
}
