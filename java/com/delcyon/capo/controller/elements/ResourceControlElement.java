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

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.AbstractControl;
import com.delcyon.capo.controller.ControlElement;
import com.delcyon.capo.controller.ControlElementProvider;
import com.delcyon.capo.controller.Group;
import com.delcyon.capo.controller.server.ControllerClientRequestProcessor;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.LifeCycle;
import com.delcyon.capo.xml.XPath;
import com.delcyon.capo.xml.XPathFunctionProcessor;
import com.delcyon.capo.xml.XPathFunctionProvider;
import com.delcyon.capo.xml.XPathFunctionUtility;
import com.delcyon.capo.xml.dom.ResourceDocument;
import com.delcyon.capo.xml.dom.ResourceDocumentBuilder;

/**
 * @author jeremiah
 *
 */
@ControlElementProvider(name="resource")
@XPathFunctionProvider
public class ResourceControlElement extends AbstractControl implements XPathFunctionProcessor
{
	
	public enum Attributes
	{		
		name,
		uri,
		lifeCycle,
		step
	}
	
	
	private static final String[] functionNames = {"resource"};
	
	private static final String[] supportedNamespaces = {CapoApplication.SERVER_NAMESPACE_URI};
	
	//TODO need to double check the necessity of this
	public void process()
	{
		
	}
		
	
	private LifeCycle lifeCycle;
	private ResourceDescriptor resourceDescriptor;

	private ResourceDocument resourceDocument;
	
	@Override
	public Attributes[] getAttributes()
	{
		return Attributes.values();
	}
	
	@Override
	public Attributes[] getRequiredAttributes()
	{	    		
		return new Attributes[]{Attributes.name,Attributes.uri};
	}

	@Override
	public String[] getSupportedNamespaces()
	{
		return supportedNamespaces;
	}


	@Override
	public void init(Element controlElementDeclaration, ControlElement parentControlElement, Group parentGroup,ControllerClientRequestProcessor controllerClientRequestProcessor) throws Exception
	{
		
		super.init(controlElementDeclaration, parentControlElement, parentGroup, controllerClientRequestProcessor);
		
		if (controlElementDeclaration.hasAttribute(Attributes.lifeCycle.toString()) == true)
		{
			String lifeCycleString = getAttributeValue(Attributes.lifeCycle);
			try
			{
				this.lifeCycle = LifeCycle.valueOf(lifeCycleString);
			} 
			catch (IllegalArgumentException illegalArgumentException) 
			{
				throw new Exception("invalid @lifcycle attribute value '"+lifeCycleString+"' on "+XPath.getXPath(controlElementDeclaration));
			}
		}
		else
		{
			lifeCycle = LifeCycle.GROUP;
		}
		
	}

	public LifeCycle getLifeCycle()
	{		
		return lifeCycle;
	}

	public ResourceDocument getResourceDocument()
	{
		return this.resourceDocument;
	}
	
	@Override
	public Object processServerSideElement() throws Exception
	{
		ResourceDocumentBuilder resourceDocumentBuilder = new ResourceDocumentBuilder();
		resourceDocument = resourceDocumentBuilder.buildDocument(this);        
		getParentGroup().putResourceElement(this);
		getControlElementDeclaration().getParentNode().replaceChild(getControlElementDeclaration().getOwnerDocument().adoptNode(resourceDocument.getDocumentElement()),getControlElementDeclaration());
		resourceDocument.setFullDocument(false);
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
		String prefix = XPathFunctionUtility.getPrefix(contextNode, arguments, 1);
		if (functionName.equals("resource"))
		{
			return XPath.selectSingleNode(contextNode, "//"+prefix+"resource[@name = '"+arguments[0]+"']");
		}
		else
		{
			return null;
		}
	}

	public String getURIValue()
	{
		return getAttributeValue(Attributes.uri);
	}

	public String getName()
	{
		return getAttributeValue(Attributes.name);
	}

	public void setResourceDescriptor(ResourceDescriptor resourceDescriptor)
	{
		this.resourceDescriptor = resourceDescriptor;		
	}

	public ResourceDescriptor getResourceDescriptor()
	{
		return resourceDescriptor;
	}

	public boolean isIterable()
	{
		return getAttributeValue(Attributes.step).equalsIgnoreCase("true");
	}

	
	
}
