/**
Copyright (c) 2012 Delcyon, Inc.
This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.delcyon.capo.xml.dom;

import java.util.HashMap;
import java.util.Vector;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.Group;
import com.delcyon.capo.controller.elements.ResourceControlElement;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceParameter;
import com.delcyon.capo.resourcemanager.ResourceParameterBuilder;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.LifeCycle;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.State;
import com.delcyon.capo.util.VariableContainerWrapper;
import com.delcyon.capo.xml.XPath;
import com.delcyon.capo.xml.cdom.CDocument;
import com.delcyon.capo.xml.cdom.CElement;
import com.delcyon.capo.xml.cdom.VariableContainer;

/**
 * @author jeremiah
 *
 */
public class ResourceDeclarationElement 
{

	private enum KeyType
	{
		parent,
		child
	}
	
	public static final String DECLARATION_PATH_ATTRIBUTE_NAME = "declarationPath";
	private Element declaringElement = null;
	private ResourceDescriptor resourceDescriptor;
	private String localName = null;
	private ResourceControlElement resourceControlElement;
	private Vector<ResourceDeclarationElement> childResourceDeclarationElementVector = new Vector<ResourceDeclarationElement>();
	private Vector<ResourceElement> cacheVector = null;
	private HashMap<String, Vector<ResourceElement>> joinHashMap = null;
	private ResourceDeclarationElement parent;
	private ResourceElement containerResourceElement; 
	private String declarationPath = null;
	private Group parentGroup = null;
	
	public ResourceDeclarationElement(ResourceControlElement resourceControlElement) throws Exception
	{
		this.parentGroup = resourceControlElement.getParentGroup();
		this.resourceControlElement = resourceControlElement;
		this.declaringElement = resourceControlElement.getControlElementDeclaration();
		if(declaringElement.hasAttribute("name"))
		{
			localName = declaringElement.getAttribute("name");
		}
		else
		{
			localName = resourceControlElement.getName();
		}
		loadResourceDescriptor();
		NodeList childNodeList = XPath.selectNodes(resourceControlElement.getControlElementDeclaration(), "resource:child");
		for(int index = 0; index < childNodeList.getLength(); index++)
		{
			childResourceDeclarationElementVector.add(new ResourceDeclarationElement(this,(Element)childNodeList.item(index)));
		}
		this.declarationPath = XPath.getXPath(declaringElement).hashCode()+"";
	}

	
	private ResourceDeclarationElement(ResourceDeclarationElement parent,Element resourceDeclarationElement) throws Exception
	{
		this.parent = parent;
		this.resourceControlElement = parent.resourceControlElement;
		this.declaringElement = resourceDeclarationElement;
		if(declaringElement.hasAttribute("name"))
		{
			localName = declaringElement.getAttribute("name");
		}
		else
		{
			localName = resourceControlElement.getName();
		}
		loadResourceDescriptor();
		NodeList childNodeList = XPath.selectNodes(resourceDeclarationElement, "resource:child");
		for(int index = 0; index < childNodeList.getLength(); index++)
		{
			childResourceDeclarationElementVector.add(new ResourceDeclarationElement(this,(Element)childNodeList.item(index)));
		}
		this.declarationPath = XPath.getXPath(declaringElement).hashCode()+"";
	}

	
	private Group getParentGroup() throws Exception
	{
		if(this.parentGroup == null && parent != null)
		{
			return parent.getParentGroup();
		}
		else if (parentGroup != null)
		{
			return parentGroup;
		}
		else
		{
			throw new Exception("ResourceDeclaration has now parent or parent group");
		}
	}
	
	private void loadResourceDescriptor() throws Exception
	{
		NamedNodeMap attributeList = declaringElement.getAttributes();
		
		//if we have a URI, then we can go ahead a load a resourceDescriptor for this element 
        if(attributeList.getNamedItem("uri") != null)
        {
            this.resourceDescriptor = resourceControlElement.getParentGroup().getResourceDescriptor(resourceControlElement, attributeList.getNamedItem("uri").getNodeValue());
            
            this.resourceControlElement.setResourceDescriptor(resourceDescriptor);
        }
        else if(attributeList.getNamedItem("path") != null) //check for a path attribute, and ask our parent to load us
        {
            this.resourceDescriptor = parent.getResourceDescriptor().getChildResourceDescriptor(resourceControlElement, attributeList.getNamedItem("path").getNodeValue());
        }
        else
        {
            //we don't have anything, throw an exception for now?
            throw new Exception("Must have a uri or a path attribute");
        }
        
      //sometimes we may have variables that need to be filled out later, so we can't init or open this yet.  
        if(declaringElement.getAttribute("dynamic").equalsIgnoreCase("true") == false)
        {
        	resourceDescriptor.init(this,resourceControlElement.getParentGroup(), LifeCycle.GROUP,true,ResourceParameterBuilder.getResourceParameters(resourceControlElement.getControlElementDeclaration()));
        	resourceDescriptor.open(resourceControlElement.getParentGroup(), ResourceParameterBuilder.getResourceParameters(resourceControlElement.getControlElementDeclaration()));

        }        
        else // this is a dynamic request
        {
        	//so just ignore it, as it will get loaded later.
        }
	}
	
	public ResourceDescriptor getResourceDescriptor()
	{
		return resourceDescriptor;
	}
	
	public Element getDeclaration()
	{
		return declaringElement;
	}
	
	private String getLocalName()
	{
		return this.localName ;
	}
	
	public ResourceElement buildXML(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		ResourceDocument resourceDocument = new ResourceDocument(resourceControlElement);
		resourceDocument.setFullDocument(true);
		resourceDocument.setSilenceEvents(true);		
		ResourceElement rootResourceElement = new ResourceElement(resourceDocument,resourceDocument,resourceDescriptor);
		rootResourceElement.setResourceAttribute(DECLARATION_PATH_ATTRIBUTE_NAME, declarationPath);
		resourceDocument.setDocumentElement(rootResourceElement);
		CDocument testDocument = (CDocument) CapoApplication.getDocumentBuilder().newDocument();
		testDocument.setSilenceEvents(true);
		Element testRootElement = testDocument.createElement(getLocalName());
		testDocument.appendChild(testRootElement);
		loadCacheVectors(true,rootResourceElement,testDocument, variableContainer, resourceParameters);
		buildXML(rootResourceElement,testRootElement, variableContainer, resourceParameters);
		
		
		applyResourceContainerGrouping(resourceDocument);
		
		
		resourceDocument.setSilenceEvents(false);
		return rootResourceElement;
	}
	
	private void applyResourceContainerGrouping(ResourceDocument document) throws Exception
	{
		if (containerResourceElement != null)
		{			
			NodeList nodeList = XPath.selectNodes(document, "//*[@"+DECLARATION_PATH_ATTRIBUTE_NAME+"="+declarationPath+" and @container = 'false' and not(../@"+DECLARATION_PATH_ATTRIBUTE_NAME+"="+declarationPath+")]");
			HashMap<Node, Node> parentNodeHashMap = new HashMap<Node, Node>();
			for(int index = 0; index < nodeList.getLength(); index++)
			{
				ResourceElement resourceElement = (ResourceElement) nodeList.item(index);
				Node parentNode = resourceElement.getParentNode();
				ResourceElement localContainerElement = null;
				if(parentNodeHashMap.containsKey(parentNode) == false)
				{
					localContainerElement = (ResourceElement) containerResourceElement.cloneNode(true);
					parentNodeHashMap.put(parentNode, localContainerElement);
					if(parentNode instanceof ResourceDocument)
					{
						((ResourceDocument) parentNode).setDocumentElement(localContainerElement);
					}
					else
					{
						parentNode.appendChild(localContainerElement);
					}
				}
				else
				{
					localContainerElement = (ResourceElement) parentNodeHashMap.get(parentNode);
				}
				localContainerElement.appendChild(resourceElement);
			}
			
		}
		for (ResourceDeclarationElement childResourceDeclarationElement : childResourceDeclarationElementVector)
		{
			childResourceDeclarationElement.applyResourceContainerGrouping(document);
		}
		
		

		
		
	}


	
		
	
	/**
	 * Scans the resource element document, and runs all resource queries once, while computing any child join values, against the parent path of the join elements.
	 * @param parentElement
	 * @param variableContainer
	 * @param resourceParameters
	 * @throws Exception
	 */
	private void loadCacheVectors(boolean recurse,ResourceElement parentResourceElement,CDocument testDocument,VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		//implement some caching, since we'll always run the same query, with the same rules for each resourceDescriptor.
        //this isn't everything that can be, done but will hold us for a while
        Vector<ResourceElement> cacheVector = getCacheVector();
        if(resourceDescriptor.getResourceState().ordinal() < State.OPEN.ordinal())
        {
        	System.out.println("found an unititialized resource");
        	recurse = false;
        }
        else if(resourceDescriptor.getResourceMetaData(null).exists() == false)
        {
            //If it doesn't exist, skip it, this is a join after all
        	//System.out.println("found an unexisting resource: "+resourceDescriptor.getResourceURI());
        	recurse = false;
        }
        else if (cacheVector == null)
        {
        	ResourceDocument resourceDocument = parentResourceElement.getOwnerResourceDocument();
            cacheVector = new Vector<ResourceElement>();
            HashMap<String, Vector<ResourceElement>> joinHashMap = new HashMap<String, Vector<ResourceElement>>();
            if (resourceDescriptor.getResourceMetaData(null).isContainer())
            {
            	//we need to keep this around as a parent, for any content
            	this.containerResourceElement = new ResourceElement(resourceDocument, localName, null, resourceDescriptor.getResourceMetaData(null));
            	this.containerResourceElement.setResourceAttribute(DECLARATION_PATH_ATTRIBUTE_NAME,declarationPath);
            }
            //iterate through all of the iterable children
            while(resourceDescriptor.next(variableContainer, resourceParameters))
            {
                //get the result xml from the actual resource descriptor
            	CElement readXMLElement = (CElement) testDocument.adoptNode(resourceDescriptor.readXML(variableContainer, resourceParameters));                
               
                ResourceElement readResourceElement = resourceDocument.createResourceElement(getLocalName(),readXMLElement,resourceDescriptor.getContentMetaData(variableContainer, resourceParameters));
                //readResourceElement.setResourceDescriptor(resourceDescriptor); //don't do this, will fail on cloning
                readResourceElement.setAttribute("name", getLocalName());
                readResourceElement.setResourceAttribute(DECLARATION_PATH_ATTRIBUTE_NAME,declarationPath);
                cacheVector.add(readResourceElement);
               
                String key = getHashJoinKey(KeyType.child,readXMLElement, declaringElement);
                Vector<ResourceElement> keyMatchVector = joinHashMap.get(key);
                if(keyMatchVector == null)
                {
                	keyMatchVector = new Vector<ResourceElement>();
                	joinHashMap.put(key, keyMatchVector);                	
                }
                keyMatchVector.add(readResourceElement);
               
            }
            //use our declaring resource element as a place to store the cache data.
            setCacheVector(cacheVector);
            setJoinHashMap(joinHashMap);
           
        }

        if (recurse == true)
        {
        	//get the declared resourceElement children. This is used to define our structure of what we are looking for
        	//childResourceDeclarationElementVector
        	for (ResourceDeclarationElement childResourceDeclarationElement : childResourceDeclarationElementVector)
			{
				childResourceDeclarationElement.loadCacheVectors(true,parentResourceElement,testDocument, variableContainer, resourceParameters);
			}        	
        }
	}
	
	/**
	 * This build our xml document by running the results of the loadCacheVectors method, against the resource element document.
	 * @param parentTestElement - element to append to
	 * @param variableContainer
	 * @param resourceParameters
	 * @return true if children were added to parent element
	 * @throws Exception
	 */
	private boolean buildXML(ResourceElement parentResourceElement,Element parentTestElement, VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		//System.err.println("======================================START==="+getLocalName()+"===============================");
		//XPath.dumpNode(parentElement.getOwnerDocument(), System.err);
		
        
        boolean isDynamic = false;
        //=====================START DYNAMIC CACHE VECTOR=======================================================
        if(getJoinHashMap() == null && declaringElement.getAttribute("dynamic").equalsIgnoreCase("true"))
        {
        	isDynamic = true;
        	VariableContainerWrapper variableContainerWrapper = new VariableContainerWrapper(resourceControlElement.getParentGroup());
        	NamedNodeMap attributeNamedNodeMap = declaringElement.getAttributes();
        	String uri = null;
        	for(int index = 0; index < attributeNamedNodeMap.getLength(); index++)
        	{
        		Attr attribute = (Attr) attributeNamedNodeMap.item(index);
        		if(attribute.getLocalName().matches("(dynamic)|(uri)|(name)|(path)|(joinType)") == false)
        		{
        			variableContainerWrapper.setVar(attribute.getLocalName(), XPath.selectSingleNodeValue(parentTestElement, attribute.getValue()));
        		}
        		else if(attribute.getLocalName().matches("uri") == true)
        		{
        			uri = attribute.getValue();
        		}
        	}
        	
        	ResourceDescriptor originalResourceDescriptor = resourceDescriptor;
        	if(uri != null)
            {
        		uri = variableContainerWrapper.processVars(uri);
        		//replace our current resource descriptor with new resource descriptor
        		resourceDescriptor = CapoApplication.getDataManager().getResourceDescriptor(resourceControlElement, uri); //WE may want this to be ResouceDeclarationElement, and not parent
            }
        	
        	resourceDescriptor.init(this,variableContainerWrapper, LifeCycle.EXPLICIT,true,ResourceParameterBuilder.getResourceParameters(declaringElement));
        	resourceDescriptor.open(variableContainerWrapper, ResourceParameterBuilder.getResourceParameters(declaringElement));
        	
        	loadCacheVectors(false,parentResourceElement,(CDocument) parentTestElement.getOwnerDocument(), variableContainerWrapper, resourceParameters);
        	//done, so reset everything
        	resourceDescriptor = originalResourceDescriptor;
        	
        }
        //=====================END DYNAMIC CACHE VECTOR=======================================================
        
        boolean hasChildrenAdded = false;

        //get the expected key for this element's parents, now that we have them 
        String parentJoinResultClause = getHashJoinKey(KeyType.parent,parentTestElement, declaringElement);
        if(getJoinHashMap() != null)
        {
        	Vector<ResourceElement> matchingResultVector = getJoinHashMap().get(parentJoinResultClause);



        	//The first element in the tree is always bad, so process against everything
        	if(parentTestElement.equals(parentTestElement.getOwnerDocument().getDocumentElement()))
        	{
        		matchingResultVector = getCacheVector();
        	}



        	//if we had a result in our cache, then walk each element in it
        	if(matchingResultVector != null)
        	{
        		for (ResourceElement element : matchingResultVector)
        		{
        			//make a copy, so we don't mess with the originals.
        			ResourceElement resultElement =  (ResourceElement) element.cloneNode(true);

        			//add it to the parent for testing in the children
        			parentResourceElement.appendChild(resultElement);

        			//keep a pointer to see if we were successful within the for loop
        			boolean resultHadChildrenAdded = false;

        			//walk each resource declaration, and let them add any children they need to.
        			for(int index = 0; index < childResourceDeclarationElementVector.size(); index++)
        			{
        				//check to make sure we're only dealing with resource declaration elements


        				ResourceDeclarationElement childResourceDeclarationElement = childResourceDeclarationElementVector.get(index);
        				//first process the children, for joins, we work from the bottom up.
        				if(childResourceDeclarationElement.buildXML(resultElement,resultElement.getContent(), variableContainer, resourceParameters) == true)
        				{
        					hasChildrenAdded = true;
        					resultHadChildrenAdded = true;
        				}
        				//if we didn't get any children, and we're an inner join, then bail out
        				else if(childResourceDeclarationElement.getDeclaration().getAttribute("joinType").equalsIgnoreCase("inner") == true)
        				{
        					resultHadChildrenAdded = false;
        					break;
        				}    				
        				
        			}
        			//if we don't have any declared children, then we won't have any joins, so we should add ourselves, since we did have a match from the cache.
        			if(childResourceDeclarationElementVector.size() == 0)
        			{
        				hasChildrenAdded = true;
        				resultHadChildrenAdded = true;
        			}

        			//clean ourselves up, if we didn't have any children added.
        			if(resultHadChildrenAdded == false)
        			{
        				parentResourceElement.removeChild(resultElement);
        			}
        		}
        	}
        }
        //if we're dynamic, cleanup our mess we made, so the next dynamic guy will reload as well.
        if(isDynamic == true)
        {
        	setCacheVector(null);
        	setJoinHashMap(null);
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
	
	
	public Vector<ResourceElement> getCacheVector()
    {
        return this.cacheVector ;
    }
    
    public void setCacheVector(Vector<ResourceElement> cacheVector)
    {
        this.cacheVector = cacheVector;
    }

	public void setJoinHashMap(HashMap<String, Vector<ResourceElement>> joinHashMap)
	{
		this.joinHashMap  = joinHashMap;		
	}
	
	public HashMap<String, Vector<ResourceElement>> getJoinHashMap()
	{
		return joinHashMap;
	}
	
}
