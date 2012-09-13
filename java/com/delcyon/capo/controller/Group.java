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

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;
import java.util.logging.Level;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.elements.GroupElement;
import com.delcyon.capo.controller.elements.ResourceControlElement;
import com.delcyon.capo.controller.server.ControllerClientRequestProcessor;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.LifeCycle;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.State;
import com.delcyon.capo.resourcemanager.ResourceParameterBuilder;
import com.delcyon.capo.resourcemanager.ResourceURI;
import com.delcyon.capo.server.CapoServer;
import com.delcyon.capo.xml.XPath;
import com.delcyon.capo.xml.dom.ResourceDocument;
import com.delcyon.capo.xml.dom.ResourceElement;

public class Group implements VariableContainer
{
	
	private HashMap<String, String> entryHashMap = new HashMap<String, String>();
	private HashMap<String, String> variableHashMap = new HashMap<String, String>();
	private transient HashMap<String,ResourceControlElement> resourceElementHashMap = new HashMap<String, ResourceControlElement>();
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

	/**
	 * for debugging purposes, prints all of the variables
	 * @param printStream
	 */
	public void dumpVars(PrintStream printStream)
	{
		if (parentGroup != null)
		{
			parentGroup.dumpVars(printStream);
		}
		else 
		{
			CapoApplication.dumpVars(printStream);
		}
		String spacing = "";
		int tabCount = getGroupPath().split(":").length;
		for(int depth = 0; depth < tabCount; depth++)
		{
			spacing += "\t";
		}
		printStream.println("\n"+spacing+"===================='"+getGroupName().toUpperCase()+"' GROUP VARS  (path = "+getGroupPath()+")=====================");
		
		printStream.println(spacing+"====================CONTAINER VARS=====================");
		if (variableContainer != null) //this allows us to steer variable look up to some other hierarchy 
		{
			 printStream.println(spacing+variableContainer);
		}
		
		printStream.println(spacing+"====================LOCAL VARS=====================");
		printStream.println(spacing+variableHashMap);
		
		printStream.println(spacing+"====================ENTRY VARS=====================");
		printStream.println(spacing+entryHashMap);
		
		printStream.println(spacing+"====================SESSION VARS=====================");
		if (controllerClientRequestProcessor != null && controllerClientRequestProcessor.getSessionHashMap() != null)
		{
			printStream.println(spacing+controllerClientRequestProcessor.getSessionHashMap());
		}
		
		printStream.println(spacing+"====================REQUEST VARS=====================");
		if (controllerClientRequestProcessor != null)
		{
			printStream.println(spacing+controllerClientRequestProcessor.getRequestHashMap());
		}

		printStream.println(spacing+"====================END '"+getGroupName().toUpperCase()+"' GROUP VARS=====================\n");
		
	}

	public void set(String varName, String value)
	{
	    if (varName.matches("\\$.*:.*"))
	    {
	        String[] scopedVariableArray = varName.split(":");
	        if (scopedVariableArray.length != 2)
	        {
	            CapoApplication.logger.log(Level.WARNING, "Improper format of scoped variable name. '"+varName+"' should be '$<group name>:<var name>'. Going to use local group.");	            
	        }
	        else
	        {
	            //strip off $ char
	            scopedVariableArray[0] = scopedVariableArray[0].replaceFirst("\\$", "");	           
	            Group currentGroup = this;
	            while (scopedVariableArray[0].equals(currentGroup.getGroupName()) == false)
	            {
	                currentGroup = currentGroup.parentGroup;
	                if (currentGroup == null)
	                {
	                    break; //hit top of stack, so exit out
	                }
	            }
	            
	            if (currentGroup != null)
	            {
	                currentGroup.variableHashMap.put(scopedVariableArray[1], value);
	            }
	            else
	            {
	                CapoApplication.logger.log(Level.WARNING, "No parent group named "+scopedVariableArray[0]+" was found. Using local group and full var name '"+varName+"'");
	                variableHashMap.put(varName,value);
	            }
	        }
	    }
	    else
	    {
	        variableHashMap.put(varName,value);
	    }
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
	
	public void putResourceElement(ResourceControlElement resourceControlElement)
	{
		resourceElementHashMap.put(resourceControlElement.getName(), resourceControlElement);
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
		String scheme = ResourceURI.getScheme(resourceURIString);
		String uriRemainder = ResourceURI.getSchemeSpecificPart(resourceURIString);
		
		
		
		//figure out resource type
		
		
		
		if (scheme != null && scheme.equals("resource"))
		{
			String resourceName = uriRemainder;
			String[] parts = ResourceURI.getParts(uriRemainder); 
			if (resourceName != null)
			{
				if (resourceElementHashMap.containsKey(resourceName))
				{
					return resourceElementHashMap.get(resourceName).getResourceDescriptor();
				}
				else if (resourceElementHashMap.containsKey(parts[0]))
				{
					ResourceControlElement resourceControlElement = resourceElementHashMap.get(parts[0]);
					ResourceDocument resourceDocument = resourceControlElement.getResourceDocument();
					Node node = XPath.selectSingleNode(resourceDocument, resourceURIString);
					if (node != null && node instanceof com.delcyon.capo.xml.dom.ResourceElement)
					{
						return ((ResourceElement) node).getResourceDescriptor();
					}
					else
					{
						return null;
					}
					
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
		Set<Entry<String, ResourceControlElement>> resourceDescriptorEntrySet = resourceElementHashMap.entrySet();
		for (Entry<String, ResourceControlElement> resourceElementEntry : resourceDescriptorEntrySet)
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
		Set<Entry<String, ResourceControlElement>> resourceDescriptorEntrySet = resourceElementHashMap.entrySet();
		for (Entry<String, ResourceControlElement> resourceElementEntry : resourceDescriptorEntrySet)
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
