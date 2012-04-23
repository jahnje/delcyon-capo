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
package com.delcyon.capo.resourcemanager.types;

import org.w3c.dom.Element;

import com.delcyon.capo.ContextThread;
import com.delcyon.capo.controller.ControlElement;
import com.delcyon.capo.controller.VariableContainer;
import com.delcyon.capo.resourcemanager.ContentFormatType;
import com.delcyon.capo.resourcemanager.ResourceManager;
import com.delcyon.capo.resourcemanager.ResourceParameter;
import com.delcyon.capo.resourcemanager.types.ContentMetaData.Attributes;
import com.delcyon.capo.resourcemanager.types.RefResourceType.Parameters;
import com.delcyon.capo.xml.XPath;

/**
 * @author jeremiah
 *
 */
public class RefResourceDescriptor extends AbstractResourceDescriptor
{

	private SimpleContentMetaData contentMetaData;
	private ControlElement contextControlElement = null;

	private SimpleContentMetaData buildContentMetatData()
	{
		SimpleContentMetaData simpleContentMetaData  = new SimpleContentMetaData(getResourceURI());
		simpleContentMetaData.addSupportedAttribute(Attributes.exists,Attributes.readable,Attributes.writeable,Attributes.container);		
		simpleContentMetaData.setValue(Attributes.exists,true);
		simpleContentMetaData.setValue(Attributes.readable,true);
		simpleContentMetaData.setContentFormatType(ContentFormatType.XML);
		return simpleContentMetaData;
	}
	
	@Override
	public void init(VariableContainer variableContainer, LifeCycle lifeCycle, boolean iterate, ResourceParameter... resourceParameters) throws Exception
	{		
		super.init(variableContainer, lifeCycle, iterate, resourceParameters);
		this.contentMetaData = buildContentMetatData();
		
		if(Thread.currentThread() instanceof ContextThread && ((ContextThread)Thread.currentThread()).getContext() != null && ((ContextThread)Thread.currentThread()).getContext() instanceof ControlElement)
		{
			contextControlElement = (ControlElement) ((ContextThread)Thread.currentThread()).getContext();
			
		}
		
	}
	
	@Override
	protected Action[] getSupportedActions()
	{
		return new Action[]{};
	}

	@Override
	public ContentMetaData getContentMetaData(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		return contentMetaData;
	}

	@Override
	public ContentMetaData getIterationMetaData(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		return contentMetaData;
	}

	@Override
	public StreamFormat[] getSupportedStreamFormats(StreamType streamType) throws Exception
	{
		if (streamType == StreamType.INPUT)
		{
			return new StreamFormat[]{StreamFormat.XML_BLOCK};
		}		
		else
		{
			return null;
		}
	}

	@Override
	public StreamType[] getSupportedStreamTypes() throws Exception
	{
		return new StreamType[]{StreamType.INPUT};
	}

	@Override
	public Element readXML(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{	
		if (getResourceState() != State.OPEN)
		{
			open(variableContainer, resourceParameters);
		}
		addResourceParameters(variableContainer, resourceParameters);
		if(contextControlElement != null)
		{
			if (getVarValue(variableContainer, Parameters.XMLNS) != null)
			{
				return (Element) XPath.selectNSNode(contextControlElement.getControlElementDeclaration(), ResourceManager.getSchemeSpecificPart(getResourceURI()),getVarValue(variableContainer, Parameters.XMLNS).split(","));
			}
			else
			{ 
				return (Element) XPath.selectSingleNode(contextControlElement.getControlElementDeclaration(), ResourceManager.getSchemeSpecificPart(getResourceURI()));
			}
		}
		else
		{
			return null;
		}
	}
	
}