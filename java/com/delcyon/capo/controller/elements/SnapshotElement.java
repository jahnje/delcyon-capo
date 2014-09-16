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

import java.io.File;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import org.w3c.dom.Element;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.AbstractClientSideControl;
import com.delcyon.capo.controller.ControlElementProvider;
import com.delcyon.capo.controller.client.ClientSideControl;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.types.ContentMetaData;
import com.delcyon.capo.xml.XPath;
import com.delcyon.capo.xml.dom.ResourceAttr;
import com.delcyon.capo.xml.dom.ResourceDocument;

/**
 * @author jeremiah
 *
 */
@ControlElementProvider(name="snapshot")
public class SnapshotElement extends AbstractClientSideControl implements ClientSideControl
{

	
	
	private enum Attributes
	{
		name, startPath,endPath
		
	}
	
	
	private static final String[] supportedNamespaces = {CapoApplication.SERVER_NAMESPACE_URI,CapoApplication.CLIENT_NAMESPACE_URI};
	
	
	
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
	public Element processClientSideElement() throws Exception
	{
	    
	    CapoApplication.logger.log(Level.INFO, "Asking client for snapshot");
		//Return something here so that the server stops waiting for a document to read, and will shut down the connection. 
		return (Element)(getControlElementDeclaration().cloneNode(true));
	}
	
	
	@Override
	public Object processServerSideElement() throws Exception
	{
		if (getControlElementDeclaration().getNamespaceURI().equals(CapoApplication.CLIENT_NAMESPACE_URI))
		{
		    CapoApplication.logger.log(Level.INFO, "Asking client to capture snapshot.");
			getControllerClientRequestProcessor().sendServerSideClientElement((Element) getParentGroup().replaceVarsInAttributeValues(getControlElementDeclaration().cloneNode(true)));
		}
		else
		{
		    CapoApplication.logger.log(Level.INFO, "Asking server for snapshot");		    
		    ResourceDescriptor rootResourceDescriptor = getParentGroup().getResourceDescriptor(this,getAttributeValue(Attributes.startPath));
		    walkTree(getControlElementDeclaration(),rootResourceDescriptor);
		    CapoApplication.logger.log(Level.INFO, "Done");
		    //XPath.dumpNode(getControlElementDeclaration(), System.out);
		    
		}
		
		return null;

	}
	private void walkTree(Element rootElement,ResourceDescriptor rootResourceDescriptor) throws Exception
    {
    	ArrayDeque<ResourceDescriptor> stack = new ArrayDeque<ResourceDescriptor>(50);
    	setAttributes(rootElement, rootResourceDescriptor);
    	stack.push(rootResourceDescriptor);
    	ResourceDescriptor currentResourceDescriptor = null;
    	Element currentElement = rootElement;
    	while(stack.isEmpty() == false)
    	{
    		currentResourceDescriptor = getNextChild(stack.peek(),currentResourceDescriptor); 
    		if(currentResourceDescriptor == null) //no more kids
    		{
    			currentResourceDescriptor = stack.pop();
    			currentResourceDescriptor.release(getParentGroup());
    			currentElement = (Element) currentElement.getParentNode();    			
    		}
    		else if(getNextChild(currentResourceDescriptor, null) != null)//has more/next kids 
    		{
    			
    			Element _currentElement = rootElement.getOwnerDocument().createElementNS(CapoApplication.RESOURCE_NAMESPACE_URI,"resource:"+currentResourceDescriptor.getLocalName());
    			setAttributes(_currentElement, currentResourceDescriptor);
    			currentElement.appendChild(_currentElement);
    			currentElement = _currentElement;
    			stack.push(currentResourceDescriptor);
    			currentResourceDescriptor = null;//getNextChild(currentFile, null);
    		}
    		else //no kids
    		{   
    			Element childElement = rootElement.getOwnerDocument().createElementNS(CapoApplication.RESOURCE_NAMESPACE_URI,"resource:"+currentResourceDescriptor.getLocalName());
    			setAttributes(childElement, currentResourceDescriptor);
    			currentElement.appendChild(childElement);
    			currentResourceDescriptor.release(getParentGroup());
    		}
    	}
    }
   
   
    private ResourceDescriptor getNextChild(ResourceDescriptor parent, ResourceDescriptor child) throws Exception
    {
    	List<ContentMetaData> children = parent.getResourceMetaData(getParentGroup()).getContainedResources();
    	if(children == null || children.size() == 0)
    	{
    		return null;
    	}
    	else
    	{
    		//Arrays.sort(children);
    		if(child == null)
    		{
    			return parent.getChildResourceDescriptor(null, children.get(0).getResourceURI().getPath());
    		}
    		for(int index =0; index < children.size();index++)
    		{
    			if(children.get(index).getResourceURI().equals(child.getResourceURI()))
    			{
    				if(children.size() > index+1)
    				{
    					return parent.getChildResourceDescriptor(getContextControlElement(), children.get(index+1).getResourceURI().getPath());    					
    				}
    				else
    				{
    					return null;
    				}
    			}    					
    		}
    		return null;
    	}
    	
    }
	
    private void setAttributes(Element element, ResourceDescriptor resourceDescriptor) throws Exception
    {
    		ContentMetaData contentMetaData = resourceDescriptor.getResourceMetaData(getParentGroup());
    		
    		element.setAttribute("uri", contentMetaData.getResourceURI().getResourceURIString());
    		
    		element.setAttribute("new",false+"");
    		element.setAttribute("modified",false+"");
        	List<String> supportedAttributeList = contentMetaData.getSupportedAttributes();
        	for (String attributeName : supportedAttributeList)
        	{
        		if (contentMetaData.getValue(attributeName) != null)
        		{                
        			element.setAttribute(attributeName,contentMetaData.getValue(attributeName));
        			
        		}
        	}
        	//System.out.println(element.getAttribute("symlink")+" "+element.getAttribute("uri"));
        	
    }
}
