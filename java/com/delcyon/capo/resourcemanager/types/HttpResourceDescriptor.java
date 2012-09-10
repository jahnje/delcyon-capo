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
	
	private SimpleContentMetaData contentMetaData;
	private SimpleContentMetaData iterationContentMetaData;

	public HttpResourceDescriptor() throws Exception
	{
		
	}
	
	private SimpleContentMetaData buildContentMetatData(boolean useInputStream) throws Exception
	{
		SimpleContentMetaData simpleContentMetaData  = new SimpleContentMetaData(getResourceURI().getBaseURI());
		simpleContentMetaData.addSupportedAttribute(Attributes.exists,Attributes.readable);
		simpleContentMetaData.setValue(Attributes.exists,true);
		simpleContentMetaData.setValue(Attributes.readable,true);
		if (useInputStream)
		{
			simpleContentMetaData.readInputStream(getInputStream(null));
		}
		return simpleContentMetaData;
	}
	
	@Override
	public void open(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{
		super.open(variableContainer,resourceParameters);

		if (contentMetaData == null)
		{			
			contentMetaData = buildContentMetatData(true);
					
		}
	}
	
	
	
	@Override
	public InputStream getInputStream(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{		
		iterationContentMetaData = buildContentMetatData(false);
		URL url = new URL(getResourceURI().getBaseURI());	
		return iterationContentMetaData.wrapInputStream(url.openConnection().getInputStream());		
	}

	@Override
	public OutputStream getOutputStream(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{
		iterationContentMetaData = buildContentMetatData(false);
		URL url = new URL(getResourceURI().getBaseURI());	
		return iterationContentMetaData.wrapOutputStream(url.openConnection().getOutputStream());
	}
	
	

	@Override
	public void close(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{		
		super.close(variableContainer,resourceParameters);
	}

	@Override
	public ContentMetaData getContentMetaData(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{
		return contentMetaData;
	}

	@Override
	public ContentMetaData getIterationMetaData(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{
		return iterationContentMetaData;
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
