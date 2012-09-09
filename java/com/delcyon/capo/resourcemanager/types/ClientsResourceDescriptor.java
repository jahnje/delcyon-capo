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

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.ControlElement;
import com.delcyon.capo.controller.VariableContainer;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceParameter;
import com.delcyon.capo.resourcemanager.ResourceURI;
import com.delcyon.capo.resourcemanager.types.RefResourceType.Parameters;
import com.delcyon.capo.server.CapoServer.Preferences;
import com.delcyon.capo.xml.XPath;

/**
 * @author jeremiah
 *
 */
public class ClientsResourceDescriptor extends AbstractResourceDescriptor
{

	private ContentMetaData contentMetaData;
	private ControlElement contextControlElement = null;
	private ResourceDescriptor clientResourceDescriptor = null;

	
	
	@Override
	public void init(VariableContainer variableContainer, LifeCycle lifeCycle, boolean iterate, ResourceParameter... resourceParameters) throws Exception
	{		
		super.init(variableContainer, lifeCycle, iterate, resourceParameters);
		
		clientResourceDescriptor = CapoApplication.getDataManager().getResourceDescriptor(null, CapoApplication.getDataManager().getResourceDirectory(Preferences.CLIENTS_DIR.toString()).getResourceURI()+"/"+ResourceURI.getSchemeSpecificPart(getResourceURI()));
		
		this.contentMetaData = clientResourceDescriptor.getContentMetaData(variableContainer, resourceParameters);
		
	}
	
	@Override
	protected Action[] getSupportedActions()
	{
		return new Action[]{Action.CREATE};
	}

	@Override
	public ContentMetaData getContentMetaData(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
	    if (getResourceState() != State.OPEN)
	    {
	        super.open(variableContainer, resourceParameters);
	    }
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
		else if (streamType == StreamType.OUTPUT)
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
		return new StreamType[]{StreamType.INPUT,StreamType.OUTPUT};
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
				return (Element) XPath.selectNSNode(contextControlElement.getControlElementDeclaration(), ResourceURI.getSchemeSpecificPart(getResourceURI()),getVarValue(variableContainer, Parameters.XMLNS).split(","));
			}
			else
			{ 
				return (Element) XPath.selectSingleNode(contextControlElement.getControlElementDeclaration(), ResourceURI.getSchemeSpecificPart(getResourceURI()));
			}
		}
		else
		{
			return null;
		}
	}
	
}
