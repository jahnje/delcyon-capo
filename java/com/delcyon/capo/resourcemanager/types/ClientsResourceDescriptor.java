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

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceParameter;
import com.delcyon.capo.server.CapoServer.Preferences;
import com.delcyon.capo.xml.cdom.CElement;
import com.delcyon.capo.xml.cdom.VariableContainer;
import com.delcyon.capo.xml.dom.ResourceDeclarationElement;

/**
 * @author jeremiah
 *
 */
public class ClientsResourceDescriptor extends AbstractResourceDescriptor
{
		
	private ResourceDescriptor clientResourceDescriptor = null;
	
	@Override
	public void init(ResourceDeclarationElement declaringResourceElement, VariableContainer variableContainer, LifeCycle lifeCycle, boolean iterate, ResourceParameter... resourceParameters) throws Exception
	{		
		super.init(declaringResourceElement, variableContainer, lifeCycle, iterate, resourceParameters);
		clientResourceDescriptor = CapoApplication.getDataManager().getResourceDescriptor(null, CapoApplication.getDataManager().getResourceDirectory(Preferences.CLIENTS_DIR.toString()).getResourceURI().getBaseURI()+"/"+getResourceURI().getSchemeSpecificPart());
	}
	
	@Override
	protected Action[] getSupportedActions()
	{
		return new Action[]{Action.CREATE};
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
	public CElement readXML(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{	
	    advanceState(State.STEPPING, variableContainer, resourceParameters);
		return clientResourceDescriptor.readXML(variableContainer, resourceParameters);
	}

    @Override
    public boolean next(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
    {
        advanceState(State.OPEN, variableContainer, resourceParameters);
        return clientResourceDescriptor.next(variableContainer, resourceParameters);
    }

    @Override
    protected void clearContent() throws Exception
    {
        ((AbstractResourceDescriptor)clientResourceDescriptor).clearContent();
        
    }

    @Override
    protected ContentMetaData buildResourceMetaData(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
    {
        return ((AbstractResourceDescriptor)clientResourceDescriptor).buildResourceMetaData(variableContainer, resourceParameters);
    }
	
    @Override
    public ContentMetaData getContentMetaData(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
    {
        return clientResourceDescriptor.getContentMetaData(variableContainer, resourceParameters);
    }
    
    @Override
    public ContentMetaData getOutputMetaData(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
    {
        return clientResourceDescriptor.getOutputMetaData(variableContainer, resourceParameters);
    }
}
