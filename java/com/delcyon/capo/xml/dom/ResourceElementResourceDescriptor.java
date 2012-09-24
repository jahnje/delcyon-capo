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
import java.util.HashMap;
import java.util.Vector;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.ControlElement;
import com.delcyon.capo.controller.VariableContainer;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceParameter;
import com.delcyon.capo.resourcemanager.ResourceParameterBuilder;
import com.delcyon.capo.resourcemanager.ResourceType;
import com.delcyon.capo.resourcemanager.ResourceURI;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.LifeCycle;
import com.delcyon.capo.resourcemanager.types.ContentMetaData;
import com.delcyon.capo.util.EqualityProcessor;
import com.delcyon.capo.util.VariableContainerWrapper;
import com.delcyon.capo.xml.XPath;

/**
 * @author jeremiah
 *
 */
public class ResourceElementResourceDescriptor implements ResourceDescriptor
{

	private enum KeyType
	{
		parent,
		child
	}
	
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
	
	/**
	 * Scans the resource element document, and runs all resource queries once, while computing any child join values, against the parent path of the join elements.
	 * @param parentElement
	 * @param variableContainer
	 * @param resourceParameters
	 * @throws Exception
	 */
	private void loadCacheVectors(boolean recurse,Element parentElement,VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		//implement some caching, since we'll always run the same query, with the same rules for each resourceDescriptor.
        //this isn't everything that can be, done but will hold us for a while
        Vector<Element> cacheVector = declaringResourceElement.getCacheVector();
        if(proxyedResourceDescriptor.getResourceState().ordinal() < State.OPEN.ordinal())
        {
        	System.out.println("found an unititialized resource");
        	recurse = false;
        }
        else if(proxyedResourceDescriptor.getContentMetaData(null).exists() == false)
        {
        	System.out.println("found an unexisting resource");
        	recurse = false;
        }
        else if (cacheVector == null)
        {
            cacheVector = new Vector<Element>();
            HashMap<String, Vector<Element>> joinHashMap = new HashMap<String, Vector<Element>>();
            //iterate through all of the iterable children
            while(proxyedResourceDescriptor.next(variableContainer, resourceParameters))
            {
                //get the result xml from the actual resource descriptor
                Element readXMLElement = (Element) parentElement.getOwnerDocument().importNode(proxyedResourceDescriptor.readXML(variableContainer, resourceParameters),true);
                cacheVector.add(readXMLElement);
               
                String key = getHashJoinKey(KeyType.child,readXMLElement, declaringResourceElement.getResourceControlElement().getControlElementDeclaration());
                Vector<Element> keyMatchVector = joinHashMap.get(key);
                if(keyMatchVector == null)
                {
                	keyMatchVector = new Vector<Element>();
                	joinHashMap.put(key, keyMatchVector);                	
                }
                keyMatchVector.add(readXMLElement);
               
            }
            //use our declaring resource element as a place to store the cache data.
            declaringResourceElement.setCacheVector(cacheVector);
            declaringResourceElement.setJoinHashMap(joinHashMap);
           
        }

        if (recurse == true)
        {
        	//get the declared resourceElement children. This is used to define our structure of what we are looking for
        	NodeList declaringResourceElementChildrenNodeList = declaringResourceElement.getChildNodes();

        	for(int index = 0; index < declaringResourceElementChildrenNodeList.getLength(); index++)
        	{
        		//check to make sure we're only dealing with resource elements
        		if (declaringResourceElementChildrenNodeList.item(index) instanceof ResourceElement)
        		{
        			ResourceElement childResourceElement = (ResourceElement) declaringResourceElementChildrenNodeList.item(index);
        			((ResourceElementResourceDescriptor)childResourceElement.getResourceDescriptor()).loadCacheVectors(true,parentElement, variableContainer, resourceParameters);
        		}
        	}
        }
	}
	
	/**
	 * This build our xml document by running the results of the loadCacheVectors method, against the resource element document.
	 * @param parentElement - element to append to
	 * @param variableContainer
	 * @param resourceParameters
	 * @return true if children were added to parent element
	 * @throws Exception
	 */
	private boolean readXML(Element parentElement, VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		//System.err.println("======================================START==="+getLocalName()+"===============================");
		//XPath.dumpNode(parentElement.getOwnerDocument(), System.err);
		
		
		//get the declared resourceElement children. This is used to define our structure of what we are looking for
        NodeList declaringResourceElementChildrenNodeList = declaringResourceElement.getChildNodes();
	        
        
        boolean isDynamic = false;
        if(declaringResourceElement.getJoinHashMap() == null && declaringResourceElement.getResourceControlElement().getControlElementDeclaration().getAttribute("dynamic").equalsIgnoreCase("true"))
        {
        	isDynamic = true;
        	VariableContainerWrapper variableContainerWrapper = new VariableContainerWrapper(declaringResourceElement.getResourceControlElement().getParentGroup());
        	NamedNodeMap attributeNamedNodeMap = declaringResourceElement.getResourceControlElement().getControlElementDeclaration().getAttributes();
        	String uri = null;
        	for(int index = 0; index < attributeNamedNodeMap.getLength(); index++)
        	{
        		Attr attribute = (Attr) attributeNamedNodeMap.item(index);
        		if(attribute.getLocalName().matches("(dynamic)|(uri)|(name)|(path)|(joinType)") == false)
        		{
        			variableContainerWrapper.setVar(attribute.getLocalName(), XPath.selectSingleNodeValue(parentElement, attribute.getValue()));
        		}
        		else if(attribute.getLocalName().matches("uri") == true)
        		{
        			uri = attribute.getValue();
        		}
        	}
        	
        	ResourceDescriptor originalResourceDescriptor = proxyedResourceDescriptor;
        	if(uri != null)
            {
        		uri = variableContainerWrapper.processVars(uri);
        		proxyedResourceDescriptor = CapoApplication.getDataManager().getResourceDescriptor(declaringResourceElement.getResourceControlElement(), uri);
                
        		declaringResourceElement.getResourceControlElement().setResourceDescriptor(proxyedResourceDescriptor);
            }
        	
        	proxyedResourceDescriptor.init(declaringResourceElement,variableContainerWrapper, LifeCycle.EXPLICIT,true,ResourceParameterBuilder.getResourceParameters(declaringResourceElement.getResourceControlElement().getControlElementDeclaration()));
        	proxyedResourceDescriptor.open(variableContainerWrapper, ResourceParameterBuilder.getResourceParameters(declaringResourceElement.getResourceControlElement().getControlElementDeclaration()));
        	
        	loadCacheVectors(false,parentElement, variableContainerWrapper, resourceParameters);
        	//done, so reset everything
        	proxyedResourceDescriptor = originalResourceDescriptor;
        	declaringResourceElement.getResourceControlElement().setResourceDescriptor(originalResourceDescriptor);
        }
        
        boolean hasChildrenAdded = false;

        //get the expected key for this element's parents, now that we have them 
        String parentJoinResultClause = getHashJoinKey(KeyType.parent,parentElement, this.declaringResourceElement.getResourceControlElement().getControlElementDeclaration());
        if(declaringResourceElement.getJoinHashMap() != null)
        {
        	Vector<Element> matchingResultVector = declaringResourceElement.getJoinHashMap().get(parentJoinResultClause);



        	//The first element in the tree is always bad, so process against everything
        	if(parentElement.equals(parentElement.getOwnerDocument().getDocumentElement()))
        	{
        		matchingResultVector = declaringResourceElement.getCacheVector();
        	}



        	//if we had a result in our cache, then walk each element in it
        	if(matchingResultVector != null)
        	{
        		for (Element element : matchingResultVector)
        		{
        			//make a copy, so we don't mess with the originals.
        			Element resultElement =  (Element) element.cloneNode(true);

        			//add it to the parent for testing in the children
        			parentElement.appendChild(resultElement);

        			//keep a pointer to see if we were successful within the for loop
        			boolean resultHadChildrenAdded = false;

        			//walk each resource declaration, and let them add any children they need to.
        			for(int index = 0; index < declaringResourceElementChildrenNodeList.getLength(); index++)
        			{
        				//check to make sure we're only dealing with resource elements
        				if (declaringResourceElementChildrenNodeList.item(index) instanceof ResourceElement)
        				{

        					ResourceElement childResourceElement = (ResourceElement) declaringResourceElementChildrenNodeList.item(index);
        					//first process the children, for joins, we work from the bottom up.
        					if(((ResourceElementResourceDescriptor)childResourceElement.getResourceDescriptor()).readXML(resultElement, variableContainer, resourceParameters) == true)
        					{
        						hasChildrenAdded = true;
        						resultHadChildrenAdded = true;
        					}
        					//if we didn't get any children, and we're an inner join, then bail out
        					else if(childResourceElement.getResourceControlElement().getControlElementDeclaration().getAttribute("joinType").equalsIgnoreCase("inner") == true)
        					{
        						resultHadChildrenAdded = false;
        						break;
        					}    				
        				}
        			}
        			//if we don't have any declared children, then we won't have any joins, so we should add ourselves, since we did have a match from the cache.
        			if(declaringResourceElementChildrenNodeList.getLength() == 0)
        			{
        				hasChildrenAdded = true;
        				resultHadChildrenAdded = true;
        			}

        			//clean ourselves up, if we didn't have any children added.
        			if(resultHadChildrenAdded == false)
        			{
        				parentElement.removeChild(resultElement);
        			}
        		}
        	}
        }
        //if we're dynamic, cleanup our mess we made, so the next dynamic guy will reload as well.
        if(isDynamic == true)
        {
        	declaringResourceElement.setCacheVector(null);
        	declaringResourceElement.setJoinHashMap(null);
        }
            

        //System.err.println("======================================END==="+getLocalName()+"===============================");
        //XPath.dumpNode(parentElement.getOwnerDocument(), System.err);
        
		return hasChildrenAdded;
		
	}
	
	/**
	 * 
	 * @param testElement The element that we're going to test against as a context
	 * @param controlElementDelaration - element that has all of the resource:join children in it.
	 * @return keys look like ':' separated paths followed by ':' separated values.
	 * @throws Exception
	 */
	private String getHashJoinKey(KeyType keyType, Element testElement, Element controlElementDelaration) throws Exception
	{
		NodeList childResourceElementJoinNodeList = XPath.selectNodes(controlElementDelaration, "resource:join");
		StringBuilder valueStringBuilder = new StringBuilder();
		StringBuilder parentPathStringBuilder = new StringBuilder();
		
		
		for(int joinIndex = 0; joinIndex < childResourceElementJoinNodeList.getLength(); joinIndex++)
		{
			Element joinElement = (Element) childResourceElementJoinNodeList.item(joinIndex);
			parentPathStringBuilder.append(joinElement.getAttribute("parent")+":");	
			String path = null;
			if (keyType == KeyType.child)
			{
				path = joinElement.getAttribute("this");
			}
			else
			{
				path = joinElement.getAttribute(keyType.toString());
			}
			valueStringBuilder.append(XPath.selectSingleNodeValue(testElement, path)+":");
		}
		parentPathStringBuilder.append(valueStringBuilder.toString());

		
		return parentPathStringBuilder.toString();
	}
	
	
	
	@Override
	public Element readXML(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		Document document = CapoApplication.getDocumentBuilder().newDocument();
		Element rootElement = document.createElement(getLocalName());
		document.appendChild(rootElement);
		loadCacheVectors(true,rootElement, variableContainer, resourceParameters);
		readXML(rootElement, variableContainer, resourceParameters);
		return rootElement;
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
