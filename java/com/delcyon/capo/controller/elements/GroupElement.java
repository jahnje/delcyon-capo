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

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.delcyon.capo.controller.AbstractControl;
import com.delcyon.capo.controller.ControlElement;
import com.delcyon.capo.controller.ControlElementProvider;
import com.delcyon.capo.controller.Group;
import com.delcyon.capo.controller.server.ControllerClientRequestProcessor;
import com.delcyon.capo.server.CapoServer;
import com.delcyon.capo.xml.XPath;
import com.delcyon.capo.xml.XPathFunctionProcessor;
import com.delcyon.capo.xml.XPathFunctionProvider;
import com.delcyon.capo.xml.XPathFunctionUtility;

/**
 * @author jeremiah
 *
 */
@ControlElementProvider(name="group")
@XPathFunctionProvider
public class GroupElement extends AbstractControl implements XPathFunctionProcessor
{
	
	

	
	
	public enum Attributes
	{
		type,
		name,
		value,
		ref,
		entry, 
		exec, 
		timeout, 
		exitCode, 
		eval, 
		stylesheet, 
		returns, 
		sessionId,
		initialGroup,
		table
	}
	
	private static final String[] functionNames = {"group","table","entry"};

	private String groupName;
	private Group group;




	@SuppressWarnings("unchecked")
	@Override
	public Enum[] getAttributes()
	{		
		return Attributes.values();
	}


	@SuppressWarnings("unchecked")
	@Override
	public Enum[] getRequiredAttributes()
	{
		return new Attributes[]{Attributes.name};
	}

	@Override
	public String[] getSupportedNamespaces()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Group getGroup()
	{
		return group;
	}

	@Override
	public void init(Element controlElementDeclaration, ControlElement parentControlElement, Group parentGroup,ControllerClientRequestProcessor controllerClientRequestProcessor) throws Exception
	{
		
		
		
		super.init(controlElementDeclaration, parentControlElement, parentGroup, controllerClientRequestProcessor);
		this.groupName = controlElementDeclaration.getAttribute(Attributes.name.toString());
		this.group = new Group(groupName,parentGroup,this,controllerClientRequestProcessor);

		//groupElement.getGroup().replaceVarsInAttributeValues(localElement);
        this.group.setVars(controlElementDeclaration);
		
		CapoServer.logger.log(Level.FINE, "init group = "+groupName);
		
		//proccess entry attribute
		if(getAttributeValue(Attributes.entry) != null && getAttributeValue(Attributes.entry).isEmpty() == false)
		{
			CapoServer.logger.log(Level.FINE, "Found Entry for "+getAttributeValue(Attributes.name));
			Element contextElement = null;
			if (getAttributeValue(Attributes.table).isEmpty() == false)
			{
			    String tablePath = group.processVars(getAttributeValue(Attributes.table));
			    contextElement = (Element) XPath.selectSingleNode(controlElementDeclaration, getAttributeValue(Attributes.table));
			    if(contextElement == null)
			    {
			        throw new Exception("No table found at: "+tablePath);
			    }
			}
			else
			{
			    contextElement = controlElementDeclaration;
			}
			Node entryNode = XPath.selectSingleNode(contextElement, group.processVars(getAttributeValue(Attributes.entry)),controlElementDeclaration.getPrefix());
			if (entryNode != null)
			{
			    group.set("entry."+entryNode.getLocalName(), XPath.getXPath(entryNode));
				//add defaults from parent node, but only if the child has a parent that isn't this node.
				if (entryNode.getParentNode().equals(controlElementDeclaration) == false)
				{
					NamedNodeMap defaultAttributeNodeList = entryNode.getParentNode().getAttributes();
					for (int currentNode = 0; currentNode < defaultAttributeNodeList.getLength(); currentNode++)
					{
						Node attribute = defaultAttributeNodeList.item(currentNode);
						group.set(attribute.getNodeName(), group.processVars(attribute.getNodeValue()));	
					}
				}
				//process attributes on actaul entry node
				NamedNodeMap attributeNodeList = entryNode.getAttributes();
				for (int currentNode = 0; currentNode < attributeNodeList.getLength(); currentNode++)
				{
					Node attribute = attributeNodeList.item(currentNode);
					group.set(attribute.getNodeName(), group.processVars(attribute.getNodeValue()));	
				}
				
			}
			else
			{
				CapoServer.logger.log(Level.WARNING, "no entry found for "+group.processVars(controlElementDeclaration.getAttribute(Attributes.entry.toString()))+" in "+XPath.getXPath(contextElement));
			}
		}
		
	}





	@Override
	public Object processServerSideElement() throws Exception
	{
		
		//process the children of this group
		String initialGroupName = getAttributeValue(Attributes.initialGroup);
		if (initialGroupName != null && initialGroupName.trim().isEmpty() == false)
		{
			processChildren(XPath.selectNodes(getControlElementDeclaration(), "./server:group[@"+Attributes.name+" = '"+initialGroupName+"']"), group, this, getControllerClientRequestProcessor());
		}
		else
		{
			processChildren(getControlElementDeclaration().getChildNodes(), group, this, getControllerClientRequestProcessor());
		}
		//then see if we have a returns attribute, and if so pull any matching vars from this group to the parent group
		if (getControlElementDeclaration().hasAttribute(Attributes.returns.toString()) && getParentGroup() != null)
		{
			
			String[] varnnames = getControlElementDeclaration().getAttribute(Attributes.returns.toString()).split(",");
			for (String varName : varnnames)
			{
				if (group.containsLocalKey(varName))
				{
					getParentGroup().set(varName, group.getLocalValue(varName));
				}				
			}
		}
		
		return null;
	}
	

	
	@Override
	public String[] getXPathFunctionNames()
	{
		return functionNames;
	}
	
	@Override
	public Object processFunction(String functionName, Object... arguments) throws Exception
	{
		
		Node contextNode = getContextNode();
		String prefix = XPathFunctionUtility.getPrefix(contextNode,arguments,1);
		if (functionName.equals("group"))
		{
				return XPath.selectSingleNode(contextNode, "//"+prefix+"group[@name = '"+arguments[0]+"']");
		}
		
		else if (functionName.equals("table"))
		{
			return XPath.selectSingleNode(contextNode, "//"+prefix+"table[@name = '"+arguments[0]+"']");
		}
		else if (functionName.equals("entry"))
		{
			return XPath.selectSingleNode(contextNode, "//"+prefix+"table[@name = '"+arguments[0]+"']/*[@"+arguments[1]+" = //var[@name = '"+arguments[2]+"']/@value]"); 
		}
		else
		{
			return null;
		}
	}
	
	@Override
	public void destroy() throws Exception
	{		
		super.destroy();
		if (group != null)
		{
			group.destroy();
		}
	}


	public void setGroup(Group group)
	{
		this.group = group;
		
	}
	
}
