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
package com.delcyon.capo.xml.dom;

import java.io.InputStream;
import java.io.OutputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.ControlElement;
import com.delcyon.capo.controller.VariableContainer;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceParameter;
import com.delcyon.capo.resourcemanager.ResourceType;
import com.delcyon.capo.resourcemanager.ResourceURI;
import com.delcyon.capo.resourcemanager.types.ContentMetaData;
import com.delcyon.capo.util.EqualityProcessor;
import com.delcyon.capo.xml.XPath;

/**
 * @author jeremiah
 *
 */
public class ResourceElementResourceDescriptor implements ResourceDescriptor
{

	
	private ResourceElement declaringResourceElemnt;
	private ResourceDescriptor proxyedResourceDescriptor;
	
	public ResourceElementResourceDescriptor(ResourceElement declaringResourceElemnt)
	{
		this.declaringResourceElemnt = declaringResourceElemnt;
		this.proxyedResourceDescriptor = declaringResourceElemnt.getProxyedResourceDescriptor();
	}
	
	@Override
	public void setup(ResourceType resourceType, String resourceURI) throws Exception
	{
		proxyedResourceDescriptor.setup(resourceType, resourceURI);
	}
	
	@Override
	public void init(ResourceElement declaringResourceElement, VariableContainer variableContainer, LifeCycle lifeCycle, boolean iterate, ResourceParameter... resourceParameters) throws Exception
	{
		proxyedResourceDescriptor.init(declaringResourceElement, variableContainer, lifeCycle, iterate, resourceParameters);		
	}
	
	@Override
	public State getResourceState() throws Exception
	{
		return proxyedResourceDescriptor.getResourceState();
	}
	
	@Override
	public void open(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		proxyedResourceDescriptor.open(variableContainer, resourceParameters);		
	}
	
	@Override
	public ContentMetaData getContentMetaData(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		return proxyedResourceDescriptor.getContentMetaData(variableContainer, resourceParameters);	
	}
	
	@Override
	public ContentMetaData getIterationMetaData(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		return proxyedResourceDescriptor.getIterationMetaData(variableContainer, resourceParameters);
	}
	
	@Override
	public ResourceDescriptor getChildResourceDescriptor(ControlElement callingControlElement, String relativeURI) throws Exception
	{
	    return proxyedResourceDescriptor.getChildResourceDescriptor(callingControlElement, relativeURI);
	}
	
	@Override
	public InputStream getInputStream(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		return proxyedResourceDescriptor.getInputStream(variableContainer, resourceParameters);
	}
	@Override
	public OutputStream getOutputStream(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		return proxyedResourceDescriptor.getOutputStream(variableContainer, resourceParameters);
	}

	@Override
	public void addResourceParameters(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		proxyedResourceDescriptor.addResourceParameters(variableContainer, resourceParameters);
		
	}
	
	@Override
	public boolean isSupportedStreamFormat(StreamType streamType, StreamFormat streamFormat) throws Exception
	{		
		return proxyedResourceDescriptor.isSupportedStreamFormat(streamType, streamFormat);
	}
	
	@Override
	public void close(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		
		proxyedResourceDescriptor.close(variableContainer, resourceParameters);
	}
	
	@Override
	public void release(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		
		proxyedResourceDescriptor.release(variableContainer, resourceParameters);
	}
	
	@Override
	public void reset(State previousState) throws Exception
	{		
		proxyedResourceDescriptor.reset(previousState);
	}
	
	@Override
	public boolean next(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		return proxyedResourceDescriptor.next(variableContainer, resourceParameters);
	}
	
	private Element readXML(Element parentTestElement, VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		
		
		Element readResultElement = parentTestElement.getOwnerDocument().createElement(getLocalName());
		
		//get our join types
		boolean outerJoinType = this.declaringResourceElemnt.getResourceControlElement().getControlElementDeclaration().getAttribute("joinType").equalsIgnoreCase("outer");
		
		//iterate through all of the iterable children
		while(proxyedResourceDescriptor.next(variableContainer, resourceParameters))
		{
			//read our element element
			Element importedElement = (Element) parentTestElement.getOwnerDocument().importNode(proxyedResourceDescriptor.readXML(variableContainer, resourceParameters),true);
			//append it to the test limb
			parentTestElement.appendChild(importedElement);
			XPath.dumpNode(parentTestElement.getOwnerDocument(), System.out);
			//see if we have any declared resource element children
			NodeList childNodeList = declaringResourceElemnt.getChildNodes();
			
			boolean hasChildrenAdded = false;
			//for each declared child resource element
			for(int index = 0; index < childNodeList.getLength(); index++)
			{
				//check to make sure we're only dealing with resource elements
				if (childNodeList.item(index) instanceof ResourceElement)
				{
					
					ResourceElement childResourceElement = (ResourceElement) childNodeList.item(index);
					NodeList joinNodeList = XPath.selectNodes(childResourceElement.getResourceControlElement().getControlElementDeclaration(), "resource:join");
					
					
					
					//run the query against the child resource element
					Element childElement = ((ResourceElementResourceDescriptor)childResourceElement.getResourceDescriptor()).readXML(importedElement,variableContainer, resourceParameters);

					NodeList subChildNodeList = childElement.getChildNodes();
					for(int subChildIndex = 0; subChildIndex < subChildNodeList.getLength(); subChildIndex++)
					{
						Element subChildElement = (Element) subChildNodeList.item(subChildIndex);



						System.out.println("got successful join");

						boolean joinRulesPassed = true;						
						subChildElement = (Element) parentTestElement.getOwnerDocument().importNode(subChildElement, true);

						for(int joinIndex = 0; joinIndex < joinNodeList.getLength();joinIndex++)
						{
							Element joinElement = (Element) joinNodeList.item(joinIndex);
							String parentPath = joinElement.getAttribute("parent");
							String thisPath = joinElement.getAttribute("this");
							
							System.out.println("===============PARENT IN JOIN=======================");
							XPath.dumpNode(importedElement, System.out);
							String parentValue = XPath.selectSingleNodeValue(importedElement, parentPath);

							System.out.println("===============child IN JOIN=======================");
							XPath.dumpNode(subChildElement, System.out);
							String thisValue = XPath.selectSingleNodeValue(subChildElement, thisPath);

							//if these two values aren't the same, then we're done testing
							if (EqualityProcessor.areSame(parentValue, thisValue) == false)
							{
								joinRulesPassed = false;
								break;
							}
						}

						if(joinRulesPassed)
						{
							importedElement.appendChild(subChildElement);
							hasChildrenAdded = true;
						}

					}
					
				}
			}
			
			//we're done with he children, so we don't need to be in the tree any more.
			parentTestElement.removeChild(importedElement);
			
			boolean joinRulesPassed = false;
			
			//if we don't have any children, then we need to just need to check our own join rules
			if(childNodeList.getLength() == 0)
			{
				NodeList joinNodeList = XPath.selectNodes(declaringResourceElemnt.getResourceControlElement().getControlElementDeclaration(), "resource:join");
				
				
				for(int index = 0; index < joinNodeList.getLength();index++)
				{
					Element joinElement = (Element) joinNodeList.item(index);
					String parentPath = joinElement.getAttribute("parent");
					String thisPath = joinElement.getAttribute("this");
					
					
					System.out.println("===============PARENT IN JOIN=======================");
					XPath.dumpNode(parentTestElement, System.out);
					String parentValue = XPath.selectSingleNodeValue(parentTestElement, parentPath);
					String thisValue = XPath.selectSingleNodeValue(importedElement, thisPath);
					
					//if these two values aren't the same, then we're done testing
					if (EqualityProcessor.areSame(parentValue, thisValue) == false)
					{
						joinRulesPassed = false;
						break;
					}
					else
					{
						joinRulesPassed = true;
					}
				}
			}

			//if we passed any join rules, then append our results.
			if (joinRulesPassed == true || (outerJoinType == true && hasChildrenAdded == false) || hasChildrenAdded == true)
			{
				readResultElement.appendChild(importedElement);
			}
		}
		return readResultElement;
	}
	
	
	@Override
	public Element readXML(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		Document document = CapoApplication.getDocumentBuilder().newDocument();
		Element rootElement = document.createElement(getLocalName());
		document.appendChild(rootElement);
		return readXML(rootElement, variableContainer, resourceParameters);
	}
	
	@Override
	public void writeXML(VariableContainer variableContainer, Element element, ResourceParameter... resourceParameters) throws Exception
	{
		proxyedResourceDescriptor.writeXML(variableContainer, element, resourceParameters);
	}
	
	@Override
	public byte[] readBlock(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		return proxyedResourceDescriptor.readBlock(variableContainer, resourceParameters);
	}

	@Override
	public void writeBlock(VariableContainer variableContainer, byte[] block, ResourceParameter... resourceParameters) throws Exception
	{
		proxyedResourceDescriptor.writeBlock(variableContainer, block, resourceParameters);
		
	}
	
	@Override
	public void processInput(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		proxyedResourceDescriptor.processInput(variableContainer, resourceParameters);		
	}

	@Override
	public void processOutput(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		proxyedResourceDescriptor.processOutput(variableContainer, resourceParameters);	
	}
	
	@Override
	public boolean performAction(VariableContainer variableContainer, Action action, ResourceParameter... resourceParameters) throws Exception
	{
		return proxyedResourceDescriptor.performAction(variableContainer, action, resourceParameters);
	}

	
	@Override
	public LifeCycle getLifeCycle() throws Exception
	{
		return proxyedResourceDescriptor.getLifeCycle();
	}
	
	@Override
	public State getStreamState(StreamType streamType) throws Exception
	{		
		return proxyedResourceDescriptor.getStreamState(streamType);
	}

	@Override
	public StreamFormat[] getSupportedStreamFormats(StreamType streamType) throws Exception
	{		
		return proxyedResourceDescriptor.getSupportedStreamFormats(streamType);
	}

	@Override
	public StreamType[] getSupportedStreamTypes() throws Exception
	{
		return proxyedResourceDescriptor.getSupportedStreamTypes();
	}

	@Override
	public boolean isSupportedAction(Action action) throws Exception
	{		
		return proxyedResourceDescriptor.isSupportedAction(action);
	}

	@Override
	public boolean isSupportedStreamType(StreamType streamType) throws Exception
	{		
		return proxyedResourceDescriptor.isSupportedStreamType(streamType);
	}
	
	
	@Override
	public ResourceType getResourceType()
	{
		return proxyedResourceDescriptor.getResourceType();
	}

	@Override
	public ResourceURI getResourceURI()
	{
		return proxyedResourceDescriptor.getResourceURI();
	}

	
	

	/** returns last piece of URI **/
    @Override
    public String getLocalName()
    {
    	return proxyedResourceDescriptor.getLocalName();
    }
	
	@Override
	public boolean isRemoteResource()
	{	
		return proxyedResourceDescriptor.isRemoteResource();
	}
	
	

	
}
