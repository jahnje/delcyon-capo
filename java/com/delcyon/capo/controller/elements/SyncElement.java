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

import java.io.OutputStream;
import java.util.HashMap;
import java.util.logging.Level;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.AbstractControl;
import com.delcyon.capo.controller.ControlElementProvider;
import com.delcyon.capo.datastream.NullOutputStream;
import com.delcyon.capo.datastream.RegexFilterOutputStream;
import com.delcyon.capo.datastream.StreamUtil;
import com.delcyon.capo.datastream.TriggerFilterOutputStream;
import com.delcyon.capo.datastream.stream_attribute_filter.MD5FilterOutputStream;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceParameterBuilder;
import com.delcyon.capo.server.CapoServer;

@ControlElementProvider(name="sync")
public class SyncElement extends AbstractControl
{
	
	public enum FilterType
	{
		triggerFilter, regexFilter
	}
	
	private static HashMap<String, FilterType> filterTypeHashMap = null;
	static
	{
		filterTypeHashMap = new HashMap<String, FilterType>();
		FilterType[] filterTypes = FilterType.values();
		for (FilterType filterType : filterTypes)
		{
			filterTypeHashMap.put(filterType.toString(), filterType);
		}
	}
	
	private enum Attributes
	{
		name,ref, src,dest, destMD5, srcMD5, varMD5,onCopy
	}
	
	private static final String[] supportedNamespaces = {GroupElement.SERVER_NAMESPACE_URI};
	
	
	
	@Override
	public Attributes[] getAttributes()
	{
		return Attributes.values();
	}

	
	

	
	@Override
	public Attributes[] getRequiredAttributes()
	{
		return new Attributes[]{Attributes.dest};
	}

	
	@Override
	public String[] getSupportedNamespaces()
	{
		return supportedNamespaces;
	}


	
	@Override
	public Object processServerSideElement() throws Exception
	{
		
		//make a copy to send to the client
		Element tempCopyElement = (Element) getControlElementDeclaration().cloneNode(true);
		
		//make sure we replace all of the variables in any child element attributes before sending this back to the client
		NodeList nodeList = tempCopyElement.getChildNodes();
		for(int currentNode = 0;currentNode < nodeList.getLength();currentNode++)
		{
			getParentGroup().replaceVarsInAttributeValues(nodeList.item(currentNode));
		}
		
		tempCopyElement = (Element) getParentGroup().replaceVarsInAttributeValues(tempCopyElement);
		//this was trying to work off of temp copy element, but don't know why
		ResourceDescriptor sourceResourceDescriptor = getParentGroup().getResourceDescriptor(this,getAttributeValue(Attributes.src));
		
		if (sourceResourceDescriptor != null && sourceResourceDescriptor.getContentMetaData(getParentGroup(), ResourceParameterBuilder.getResourceParameters(getControlElementDeclaration())).exists())
		{
			
			String destMD5 = null;			
			String varMD5 = getVarMD5ForCopyElement(tempCopyElement);
			//commented out until we can find a good reason to cache, other than for the sake of caching!
//			if (varMD5 != null)
//			{
//				//use version control to cache this information, we don't want to process this if we don't have to
//				Element cacheElement = TaskManagerThread.getTaskManagerThread().getTaskDataElement("test_monitor",null,varMD5);
//				if (cacheElement == null)
//				{
//					destMD5 = getDestMD5ForCopyElement(sourceResourceDescriptor, tempCopyElement);
//				}
//				else
//				{
//					destMD5 = cacheElement.getAttribute("destMD5");
//				}
//
//				//cacheing
//				if (cacheElement == null)
//				{
//					cacheElement = CapoApplication.getDocumentBuilder().newDocument().createElement("SyncData");
//					cacheElement.setAttribute("destMD5", destMD5);
//					TaskManagerThread.getTaskManagerThread().setTaskDataElement("test_monitor",null,varMD5,cacheElement);
//				}
//			}
//			else //were not doing anything complicated, so just use the src md5 as the expected dest md5;
//			{
				destMD5 = sourceResourceDescriptor.getContentMetaData(getParentGroup(), ResourceParameterBuilder.getResourceParameters(getControlElementDeclaration())).getMD5();
			//}
			
			ResourceDescriptor destinationResourceDescriptor =  getParentGroup().openResourceDescriptor(this, getAttributeValue(Attributes.dest));
			String currentDestinationMD5 = destinationResourceDescriptor.getContentMetaData(getParentGroup(), ResourceParameterBuilder.getResourceParameters(getControlElementDeclaration())).getMD5();
			if (currentDestinationMD5 == null || currentDestinationMD5.equals(destMD5) == false)
			{
				CapoApplication.logger.log(Level.INFO, "Syncing file: "+sourceResourceDescriptor.getResourceURI()+" ==> "+destinationResourceDescriptor.getResourceURI());
				OutputStream filteredOutputStream = wrapOutputStream(destinationResourceDescriptor.getOutputStream(getParentGroup(), ResourceParameterBuilder.getResourceParameters(getControlElementDeclaration())),getControlElementDeclaration());
				StreamUtil.readInputStreamIntoOutputStream(sourceResourceDescriptor.getInputStream(getParentGroup(),ResourceParameterBuilder.getResourceParameters(getControlElementDeclaration())), filteredOutputStream);
				filteredOutputStream.flush();
				filteredOutputStream.close();
				CapoApplication.logger.log(Level.INFO, "Done Syncing");
				if(getAttributeValue(Attributes.onCopy).isEmpty() == false)
				{
					getParentGroup().set(getAttributeValue(Attributes.onCopy), "true");
				}
			}
		}
		else
		{
			CapoServer.logger.log(Level.WARNING, "couldn't process copy element: "+tempCopyElement.getAttribute("id"));
		}
		
		
		return null;
	}


	
	
	

	private OutputStream wrapOutputStream(OutputStream outputStream, Element copyElement)
	{
		
		OutputStream lastOutputStream = outputStream;
			if (copyElement != null)
			{
				
				NodeList nodeList = copyElement.getChildNodes();
				
				for (int currentNode = nodeList.getLength() - 1; currentNode >= 0; currentNode--)
				{
					Node elementNode = nodeList.item(currentNode);
					if (elementNode instanceof Element)
					{
						Element element = (Element) getParentGroup().replaceVarsInAttributeValues(elementNode);
						String name = element.getLocalName();
						
						FilterType filterType = filterTypeHashMap.get(name);
						if (filterType == null)
						{
							CapoServer.logger.log(Level.WARNING, "unknown filter type: " + name);
							continue;
						}

						switch (filterType)
						{
							case triggerFilter:
								lastOutputStream = new TriggerFilterOutputStream(lastOutputStream, element.getAttribute("trigger"), element.getAttribute("replacement"));
								break;
							case regexFilter:
								lastOutputStream = new RegexFilterOutputStream(lastOutputStream, element.getAttribute("regex"), element.getAttribute("replacement"),1);
								break;
							default:
								break;
						}

					}
				}
				
				
			}
			return lastOutputStream;
		
	}

	private String getVarMD5ForCopyElement(Element tempCopyElement) throws Exception
	{
		String md5 = "";
		MD5FilterOutputStream md5FilterOutputStream = new MD5FilterOutputStream(new NullOutputStream());

		md5FilterOutputStream.write(tempCopyElement.getAttribute("src"));
		md5FilterOutputStream.write(tempCopyElement.getAttribute("dest"));

		NodeList nodeList = tempCopyElement.getChildNodes();
		int relevantChildrenCount = 0;
		
		for (int currentNode = 0; currentNode < nodeList.getLength(); currentNode++)
		{
			Node elementNode = nodeList.item(currentNode);
			if (elementNode instanceof Element)
			{
				String name = elementNode.getLocalName();
				
				FilterType filterType = filterTypeHashMap.get(name);
				if (filterType == null)
				{
					//skip anything that won't effect the end result
					continue;
				}
				else
				{
					relevantChildrenCount++;
				}
				NamedNodeMap attributeList = elementNode.getAttributes();
				
				for (int currentAttribute = 0; currentAttribute < attributeList.getLength(); currentAttribute++)
				{
					Node attributeNode = attributeList.item(currentAttribute);
					if (attributeNode instanceof Attr)
					{
						Attr attr = (Attr) attributeNode;
						md5FilterOutputStream.write(attr.getValue());
					}
				}
			}
		}
		
		md5FilterOutputStream.close();
		md5 = md5FilterOutputStream.getMD5();
		if (relevantChildrenCount > 0)
		{		
			return md5;
		}
		else
		{
			return null;
		}
	}
	
	//This runs sync through a quick test to determine what the final md5 will look like on the other side. We do this since local processing is cheaper than just saying we don't know, and running it over the network
	private String getDestMD5ForCopyElement(ResourceDescriptor fileResourceDescriptor, Element copyElement) throws Exception
	{

		NullOutputStream nullOutputStream = new NullOutputStream();
		MD5FilterOutputStream md5FilterOutputStream = new MD5FilterOutputStream(nullOutputStream);
		OutputStream lastOutputStream = wrapOutputStream(md5FilterOutputStream, copyElement);		
		StreamUtil.readInputStreamIntoOutputStream(fileResourceDescriptor.getInputStream(getParentGroup(),ResourceParameterBuilder.getResourceParameters(copyElement)), lastOutputStream);		
		return md5FilterOutputStream.getMD5();
	}
	
	
}
