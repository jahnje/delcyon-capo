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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import com.delcyon.capo.controller.VariableContainer;
import com.delcyon.capo.resourcemanager.ResourceParameter;
import com.delcyon.capo.resourcemanager.types.ContentMetaData.Attributes;

/**
 * @author jeremiah
 */
public class HttpResourceDescriptor extends AbstractResourceDescriptor
{
	
	byte[] content = null;
    private SimpleContentMetaData contentMetaData;

	public HttpResourceDescriptor() throws Exception
	{
		
	}
	
	@Override
	protected ContentMetaData buildResourceMetaData(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{
	    SimpleContentMetaData resourceMetaData  = new SimpleContentMetaData(getResourceURI());
	    resourceMetaData.addSupportedAttribute(Attributes.exists,Attributes.readable);
	    resourceMetaData.setValue(Attributes.exists,true);
	    resourceMetaData.setValue(Attributes.readable,true);
        
        return resourceMetaData;
	}
	
	@Override
	protected void clearContent()
	{
	   content = null;	    
	}
	
	@Override
	public boolean next(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
	    advanceState(State.OPEN, variableContainer, resourceParameters);
	    if(getResourceState() == State.OPEN)
	    {
	        
	        contentMetaData = new SimpleContentMetaData(getResourceURI());
	        URL url = new URL(getResourceURI().getBaseURI());   
	        InputStream inputStream = contentMetaData.wrapInputStream(url.openConnection().getInputStream());
	        content = contentMetaData.readInputStream(inputStream);
	        setResourceState(State.STEPPING);
	        return true;
	    }
	    else
	    {
	        setResourceState(State.OPEN);
	        return false;
	    }
	}
	
	@Override
	public ContentMetaData getContentMetaData(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
	    return contentMetaData;
	}
	
	@Override
	public InputStream getInputStream(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{
	    advanceState(State.STEPPING, variableContainer, resourceParameters);
		return new ByteArrayInputStream(content);		
	}

	@Override
	public OutputStream getOutputStream(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{
	    advanceState(State.OPEN, variableContainer, resourceParameters);
		SimpleContentMetaData outputMetaData = new SimpleContentMetaData(getResourceURI());
		URL url = new URL(getResourceURI().getBaseURI());	
		return outputMetaData.wrapOutputStream(url.openConnection().getOutputStream());
	}
	
	

	@Override
	public void close(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{		
		super.close(variableContainer,resourceParameters);
	}

	
	@Override
	public StreamFormat[] getSupportedStreamFormats(StreamType streamType)
	{
		if (streamType == StreamType.INPUT)
		{
			return new StreamFormat[]{StreamFormat.STREAM};
		}
		else
		{
			return null;
		}
	}

	@Override
	public StreamType[] getSupportedStreamTypes()
	{
		return new StreamType[]{StreamType.INPUT};
	}

	@Override
	public Action[] getSupportedActions()
	{		
		return new Action[]{};
	}

	
	
	
}
