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

import java.util.Vector;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.AbstractControl;
import com.delcyon.capo.controller.ControlElementProvider;
import com.delcyon.capo.xml.XPath;

/**
 * @author jeremiah
 *
 */
@ControlElementProvider(name="var")
public class VarElement extends AbstractControl
{

	private enum Attributes
	{
		name,value, eval,scope
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
		return new Attributes[]{Attributes.name};
	}

	
	@Override
	public String[] getSupportedNamespaces()
	{
		return supportedNamespaces;
	}


	@Override
	public Object processServerSideElement() throws Exception
	{
			String varName = getAttributeValue(Attributes.name);
						
			
			String value = null;
			
			//check for a value attr, if none get the text content			
			if (getControlElementDeclaration().hasAttribute(Attributes.value.toString()))
			{
				value = getParentGroup().processVars(getAttributeValue(Attributes.value));
			}
			else if (getControlElementDeclaration().getTextContent().isEmpty() == false)
			{
				value = getParentGroup().processVars(getControlElementDeclaration().getTextContent());
			}
			
			if (getControlElementDeclaration().hasAttribute(Attributes.eval.toString()))
			{
				StringBuffer stringBuffer = new StringBuffer(getAttributeValue(Attributes.eval));
				getParentGroup().processVars(stringBuffer);
				
				NamedNodeMap namedNodeMap = getControlElementDeclaration().getAttributes();
				Vector<String> namespaceVector = new Vector<String>();
				for(int currentNodeIndex = 0; currentNodeIndex < namedNodeMap.getLength(); currentNodeIndex++)
				{
					Node currentNode = namedNodeMap.item(currentNodeIndex);
					if (currentNode.getNodeType() == Node.ATTRIBUTE_NODE && "http://www.w3.org/2000/xmlns/".equals(currentNode.getNamespaceURI()))
					{						
						namespaceVector.add(currentNode.getLocalName()+"="+currentNode.getNodeValue());
					}
				}
				stringBuffer = new StringBuffer(XPath.selectSingleNodeValue(getControlElementDeclaration(), stringBuffer.toString(),namespaceVector.toArray(new String[]{})));
				getParentGroup().processVars(stringBuffer);
				value = stringBuffer.toString();
			}
			
			if (getAttributeValue(Attributes.scope).equals("APPLICATION"))
			{
				CapoApplication.setVariable(varName,value);
			}
			else
			{
				getParentGroup().set(varName, value);
			}
			
		
		return null;
	}
	
}
