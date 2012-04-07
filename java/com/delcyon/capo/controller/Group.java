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
package com.delcyon.capo.controller;

import java.util.HashMap;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.elements.GroupElement;
import com.delcyon.capo.controller.elements.ResourceElement;
import com.delcyon.capo.controller.server.ControllerClientRequestProcessor;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceManager;
import com.delcyon.capo.resourcemanager.ResourceParameterBuilder;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.LifeCycle;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.State;
import com.delcyon.capo.server.CapoServer;
import com.delcyon.capo.xml.XPath;

public class Group implements VariableContainer
{
	
	private HashMap<String, String> entryHashMap = new HashMap<String, String>();
	private HashMap<String, String> variableHashMap = new HashMap<String, String>();
	private transient HashMap<String,ResourceElement> resourceElementHashMap = new HashMap<String, ResourceElement>();
	private transient Vector<ResourceDescriptor> openResourceDescriptorVector = new Vector<ResourceDescriptor>();
	private String groupName;
	private transient Group parentGroup;
	private GroupElement groupElement;
	private ControllerClientRequestProcessor controllerClientRequestProcessor;
	private VariableContainer variableContainer;
	
	@SuppressWarnings("unused")
	private Group() //needed for serialization
	{
		
	}
	
	public Group(String groupName, Group parentGroup, GroupElement groupElement, ControllerClientRequestProcessor controllerClientRequestProcessor)
	{
		this.groupName = groupName;
		this.parentGroup = parentGroup;
		this.groupElement = groupElement;
		this.controllerClientRequestProcessor = controllerClientRequestProcessor;
	}
	
	
	
	/**
	 * Get the name of this group
	 * @return
	 */
	public String getGroupName()
	{
		return groupName;
	}
	
	/**
	 * recurse up and get a unique path to this group
	 * This is really only used in logging in case of emergency
	 * @return
	 */
	public String getGroupPath()
	{
		//think about using saxon path function - this is here as a replacement to using path function()
		//also path function only available in SaxonPE+
		//thought about it, by using the group object, we get a tree through the call elements
		//as opposed to the pure xpath which pays no attention to them.
		if (parentGroup != null)
		{
			return parentGroup.getGroupPath() + ":" +getGroupName();
		}
		else
		{
			return getGroupName();
		}
	}

	/**
	 * iterate through all of the attributes, and replace ant var declarations with their value
	 * @param node
	 * @return
	 */
	public Node replaceVarsInAttributeValues(Node node)
	{
		NamedNodeMap namedNodeMap = node.getAttributes();
		if (namedNodeMap == null)
		{
			return node;
		}
		for(int currentNode = 0; currentNode < namedNodeMap.getLength(); currentNode++)
		{
			Node attribute = namedNodeMap.item(currentNode); 
			attribute.setNodeValue(processVars(attribute.getNodeValue()));	
		}
		return node;
	}
	
	
	/**
	 * Convience method that returns a processed  varString
	 * @param varString
	 * @return
	 */
	public String processVars(String varString)
	{
		StringBuffer stringBuffer = new StringBuffer(varString);
		processVars(stringBuffer);
		return stringBuffer.toString();
	}
	
	/**
	 * Check String for variables and replace them with the value of the var
	 * @param varStringBuffer
	 */
	public void processVars(StringBuffer varStringBuffer)
	{
	    
		while (varStringBuffer != null && varStringBuffer.toString().matches(".*\\$\\{.+}.*"))
		{
		    
			CapoServer.logger.log(Level.FINE,"found var in '"+varStringBuffer+"'");
			Stack<StringBuffer> stack = new Stack<StringBuffer>();
			StringBuffer currentStringBuffer = new StringBuffer();
	        for (int index = 0; index < varStringBuffer.length(); index++) 
	        {
	             
	            if (varStringBuffer.charAt(index) == '$' && varStringBuffer.charAt(index+1) == '{')
	            {
	                stack.push(currentStringBuffer);
	                currentStringBuffer = new StringBuffer();
	                currentStringBuffer.append(varStringBuffer.charAt(index));
	            }
	            else if (varStringBuffer.charAt(index) == '}' && varStringBuffer.charAt(index-1) != '\\' && stack.empty() == false)
	            {
	                //pop, and evaluate
	                currentStringBuffer.append(varStringBuffer.charAt(index));
	                String varName = currentStringBuffer.toString().replaceFirst(".*\\$\\{(.+)}.*", "$1");
	                String value = getVarValue(varName);
	                if (value == null)
	                {
	                    value = "";
	                    CapoServer.logger.log(Level.WARNING,"var '"+varName+"' not found replaced with empty string");
	                }
	                currentStringBuffer = stack.pop();
	                currentStringBuffer.append(value);
	            }
	            else
	            {
	                currentStringBuffer.append(varStringBuffer.charAt(index));    
	            }

	            
	        }
			
			varStringBuffer.replace(0, varStringBuffer.length(), currentStringBuffer.toString());
			
		}
		CapoServer.logger.log(Level.FINE,"final replacement =  '"+varStringBuffer+"'");
		
	}
	
	
	/**
	 * check request
	 * check entries
	 * check variables
	 * @param varName
	 * @return
	 */
	public String getVarValue(String varName)
	{
		if (controllerClientRequestProcessor != null && controllerClientRequestProcessor.getRequestHashMap().containsKey(varName))
		{
			return controllerClientRequestProcessor.getRequestHashMap().get(varName);
		}
		else if (controllerClientRequestProcessor != null && controllerClientRequestProcessor.getSessionHashMap() != null && controllerClientRequestProcessor.getSessionHashMap().containsKey(varName))
		{
		    return controllerClientRequestProcessor.getSessionHashMap().get(varName);
		}
		else if (entryHashMap.containsKey(varName))
		{
			return entryHashMap.get(varName);
		}
		else if (variableHashMap.containsKey(varName))
		{
			return variableHashMap.get(varName);
		}
		else if (variableContainer != null) //this allows us to steer variable look up to some other hierarchy 
		{
			return variableContainer.getVarValue(varName);
		}
		else if (parentGroup != null)
		{
			return parentGroup.getVarValue(varName);
		}
		else 
		{
			return CapoApplication.getVariableValue(varName);
		}
	}



	public void set(String varName, String value)
	{
		variableHashMap.put(varName,value);		
	}
	
	public GroupElement getGroupElement()
	{
		return groupElement;
	}
	

	public boolean containsLocalKey(String varName)
	{
		return (entryHashMap.containsKey(varName) || variableHashMap.containsKey(varName));
	}
	
	
	public String getLocalValue(String varName)
	{
		if (entryHashMap.containsKey(varName))
		{
			return entryHashMap.get(varName);
		}
		else
		{
			return variableHashMap.get(varName);	
		}
		
	}

	public HashMap<String, String> getVariableHashMap()
	{
		return variableHashMap;
	}
	
	public void setVariableHashMap(HashMap<String, String> variableHashMap)
	{
		this.variableHashMap = variableHashMap;
	}
	
	public void putResourceElement(ResourceElement resourceElement)
	{
		resourceElementHashMap.put(resourceElement.getName(), resourceElement);
	}

	public ResourceDescriptor openResourceDescriptor(ControlElement callingControlElement,String resourceURIString) throws Exception
	{		
		ResourceDescriptor resourceDescriptor = getResourceDescriptor(callingControlElement, resourceURIString);
		if (resourceDescriptor != null)
		{
			if (resourceDescriptor.getResourceState() == State.NONE)
			{				
				resourceDescriptor.init(this,null,false,ResourceParameterBuilder.getResourceParameters(callingControlElement.getControlElementDeclaration())); 
			}
			if (resourceDescriptor.getResourceState() != State.OPEN && resourceDescriptor.getResourceState() != State.STEPPING)
			{
				resourceDescriptor.open(this,ResourceParameterBuilder.getResourceParameters(callingControlElement.getControlElementDeclaration()));
			}
		}
		return resourceDescriptor;
	}
	
	public ResourceDescriptor getResourceDescriptor(ControlElement callingControlElement,String resourceURIString) throws Exception
	{
		String scheme = ResourceManager.getScheme(resourceURIString);
		String uriRemainder = ResourceManager.getSchemeSpecificPart(resourceURIString);
		
		
		
		//figure out resource type
		
		
		
		if (scheme != null && scheme.equals("resource"))
		{
			String resourceName = uriRemainder;
			if (resourceName != null)
			{
				if (resourceElementHashMap.containsKey(resourceName))
				{
					return resourceElementHashMap.get(resourceName).getResourceDescriptor();
				}
				else if (parentGroup != null)
				{
					return parentGroup.getResourceDescriptor(callingControlElement, resourceURIString);
				}
				else
				{
					ResourceDescriptor resourceDescriptor =  CapoApplication.getDataManager().getResourceDescriptor(callingControlElement, resourceURIString);
					openResourceDescriptorVector.add(resourceDescriptor);
					return resourceDescriptor;
				}
			}
			else
			{
				throw new Exception("Invalid resource name '"+resourceURIString+"' @"+XPath.getXPath(callingControlElement.getControlElementDeclaration()));
			}
		}
		else
		{
			ResourceDescriptor resourceDescriptor = CapoApplication.getDataManager().getResourceDescriptor(callingControlElement, resourceURIString);
			openResourceDescriptorVector.add(resourceDescriptor);
			return resourceDescriptor;
		}
	
	}

	public void closeResourceDescriptors(LifeCycle lifeCycle) throws Exception
	{
		Set<Entry<String, ResourceElement>> resourceDescriptorEntrySet = resourceElementHashMap.entrySet();
		for (Entry<String, ResourceElement> resourceElementEntry : resourceDescriptorEntrySet)
		{
			if (resourceElementEntry.getValue().getResourceDescriptor().getLifeCycle() == lifeCycle)
			{
				if (resourceElementEntry.getValue().getResourceDescriptor().getResourceState() != State.CLOSED)
				{
					resourceElementEntry.getValue().getResourceDescriptor().close(this);
				}
			}
		}
		for (ResourceDescriptor resourceDescriptor : openResourceDescriptorVector)
		{
			if (resourceDescriptor.getLifeCycle() == lifeCycle)
			{
				if (resourceDescriptor.getResourceState() != State.CLOSED)
				{
					resourceDescriptor.close(this);
				}
			}
		}
	}

	public void destroy() throws Exception
	{
		//make sure everything is closed.. this is over kill
		closeResourceDescriptors(LifeCycle.REF);
		closeResourceDescriptors(LifeCycle.GROUP);
		
		//call release on everything, this will close EVERYTHING including explicit resource descriptors
		Set<Entry<String, ResourceElement>> resourceDescriptorEntrySet = resourceElementHashMap.entrySet();
		for (Entry<String, ResourceElement> resourceElementEntry : resourceDescriptorEntrySet)
		{

			if (resourceElementEntry.getValue().getResourceDescriptor().getResourceState() != State.RELEASED)
			{
				resourceElementEntry.getValue().getResourceDescriptor().release(this);
			}

		}
		for (ResourceDescriptor resourceDescriptor : openResourceDescriptorVector)
		{

			if (resourceDescriptor.getResourceState() != State.RELEASED)
			{
				resourceDescriptor.release(this);
			}

		}
		
		//drop all of our pointers
		resourceElementHashMap.clear();
		openResourceDescriptorVector.clear();
	}

	/**
	 * this allows us to steer variable look up to some other hierarchy 
	 * @param variableContainer
	 */
	public void setVariableContainer(VariableContainer variableContainer)
	{
		this.variableContainer = variableContainer;
		
	}

	public void setVars(Element controlElementDeclaration)
	{
		NamedNodeMap namedNodeMap = controlElementDeclaration.getAttributes();
		if (namedNodeMap == null)
		{
			return;
		}
		for(int currentNode = 0; currentNode < namedNodeMap.getLength(); currentNode++)
		{
			Node attribute = namedNodeMap.item(currentNode);
			set(attribute.getNodeName(), attribute.getNodeValue());				
		}
		
		
	}
	
	
}
