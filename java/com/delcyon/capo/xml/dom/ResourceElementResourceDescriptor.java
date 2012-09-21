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
import java.util.Vector;

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

	
	private ResourceElement declaringResourceElement;
	private ResourceDescriptor proxyedResourceDescriptor;
	
	public ResourceElementResourceDescriptor(ResourceElement declaringResourceElemnt)
	{
		this.declaringResourceElement = declaringResourceElemnt;
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
		
		//create our return element
		Element readResultElement = parentTestElement.getOwnerDocument().createElement(getLocalName());
		
		//get our join types, and see if this is an outer join type
		boolean outerJoinType = this.declaringResourceElement.getResourceControlElement().getControlElementDeclaration().getAttribute("joinType").equalsIgnoreCase("outer");
		
		//get the declared resourceElement children. This is used to define our structure of what we are looking for
        NodeList declaringResourceElementChildrenNodeList = declaringResourceElement.getChildNodes();
	
        //implement some caching, since we'll always run the same query, with the same rules for each resourceDescriptor.
        //this isn't everything that can be, done but will hold us for a while
        Vector<Element> cacheVector = declaringResourceElement.getCacheVector();         
        if (cacheVector == null)
        {
            cacheVector = new Vector<Element>();
            //iterate through all of the iterable children
            while(proxyedResourceDescriptor.next(variableContainer, resourceParameters))
            {
                //get the readXML from the actual resource descriptor
                Element readXMLElement = (Element) parentTestElement.getOwnerDocument().importNode(proxyedResourceDescriptor.readXML(variableContainer, resourceParameters),true);
                cacheVector.add(readXMLElement);
            }
            //use our declaring resource element as a place to store the cache data.
            declaringResourceElement.setCacheVector(cacheVector);
        }
        
		for (Element readXMLElementOriginal : cacheVector)
        {
		    //since we're going to be modifying things, make a copy of the element
		    Element readXMLElement = (Element) readXMLElementOriginal.cloneNode(true);
		    
			//append it to the test limb, so we can run join tests against the parent xml
			parentTestElement.appendChild(readXMLElement);
			XPath.dumpNode(parentTestElement.getOwnerDocument(), System.out);
			
			//keep a pointer so that we know if we've actually done something inside the for loop
			boolean hasChildrenAdded = false;
			
			//for each declared child resource element
			for(int index = 0; index < declaringResourceElementChildrenNodeList.getLength(); index++)
			{
				//check to make sure we're only dealing with resource elements
				if (declaringResourceElementChildrenNodeList.item(index) instanceof ResourceElement)
				{
					
					ResourceElement childResourceElement = (ResourceElement) declaringResourceElementChildrenNodeList.item(index);
					
					//get the child resource element's list of joins, for testing later 
					NodeList childResourceElementJoinNodeList = XPath.selectNodes(childResourceElement.getResourceControlElement().getControlElementDeclaration(), "resource:join");
					
					//!! RECURSE !! into the childResourceElement's ResourceElementResourceDescriptor
					//we walk all the way to the bottom of the tree before we start filtering out things, with the join rules
					Element childResourceElementReadResultElement = ((ResourceElementResourceDescriptor)childResourceElement.getResourceDescriptor()).readXML(readXMLElement,variableContainer, resourceParameters);

					//the contract says that we always return an element, but it doesn't have to have any children
					//so get the list of actual Child readResultElements
					NodeList childReadResultElementNodeList = childResourceElementReadResultElement.getChildNodes();					
					for(int childReadResultElementIndex = 0; childReadResultElementIndex < childReadResultElementNodeList.getLength(); childReadResultElementIndex++)
					{
					    //get a handle on the childReadResultElement
					    //if we are here, then all of the children we encountered, passed all of the grand children's join rules
					    Element childReadResultElement = (Element) childReadResultElementNodeList.item(childReadResultElementIndex);
					    
					    //import this into our document
					    childReadResultElement = (Element) parentTestElement.getOwnerDocument().importNode(childReadResultElement, true);
					    
					    //keep a pointer so we can tell if the for loop did something
						boolean joinRulesPassed = true;						

						//start checking this childReadResultElement against the childResourceElement's join rules
						for(int joinIndex = 0; joinIndex < childResourceElementJoinNodeList.getLength(); joinIndex++)
						{
						      
							Element joinElement = (Element) childResourceElementJoinNodeList.item(joinIndex);
							String parentPath = joinElement.getAttribute("parent");
							String thisPath = joinElement.getAttribute("this");
							
							System.out.println("===============PARENT IN JOIN=======================");
							XPath.dumpNode(readXMLElement, System.out);
							//we use the readXML element here, because it should be the bottom node on the test limb at this point
							//as opposed to the element that was passed in to the method call.
							String parentValue = XPath.selectSingleNodeValue(readXMLElement, parentPath);

							System.out.println("===============child IN JOIN=======================");
							XPath.dumpNode(childReadResultElement, System.out);
							String thisValue = XPath.selectSingleNodeValue(childReadResultElement, thisPath);

							//if these two values aren't the same, then we're done testing
							if (EqualityProcessor.areSame(parentValue, thisValue) == false)
							{
								joinRulesPassed = false;
								break;
							}
						}

						//if our join rules all passed, then we can go ahead, and add this to our local readXML element
						if(joinRulesPassed)
						{
							readXMLElement.appendChild(childReadResultElement);
							hasChildrenAdded = true;
						}

					} //end childReadResultElement
					
				} //end resource element instanceof check
				
			} //end declaringResourceElementChildrenNodeList
			
			//we're done with he children, so we don't need to be in the tree any more.
            parentTestElement.removeChild(readXMLElement);
			
			
			
			//if we don't have any children, then just add ourself, because we're a leaf.
			if(declaringResourceElementChildrenNodeList.getLength() == 0)
			{
			    readResultElement.appendChild(readXMLElement);
								
			}
			else if ((outerJoinType == true && hasChildrenAdded == false) || hasChildrenAdded == true)
            {
                readResultElement.appendChild(readXMLElement);
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
