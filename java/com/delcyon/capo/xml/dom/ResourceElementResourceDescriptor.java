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
package com.delcyon.capo.xml.dom;

import java.io.InputStream;
import java.io.OutputStream;

import com.delcyon.capo.controller.ControlElement;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceParameter;
import com.delcyon.capo.resourcemanager.ResourceType;
import com.delcyon.capo.resourcemanager.ResourceURI;
import com.delcyon.capo.resourcemanager.types.ContentMetaData;
import com.delcyon.capo.xml.cdom.CElement;
import com.delcyon.capo.xml.cdom.VariableContainer;

/**
 * @author jeremiah
 *
 */
public class ResourceElementResourceDescriptor implements ResourceDescriptor
{

	
	private ResourceDescriptor proxyedResourceDescriptor;
	private ResourceElement resourceElement;
	
	public ResourceElementResourceDescriptor(ResourceElement declaringResourceElemnt)
	{
		this.resourceElement = declaringResourceElemnt;		        
		this.proxyedResourceDescriptor = declaringResourceElemnt.getProxyedResourceDescriptor();
	}
	
	@Override
	public void setup(ResourceType resourceType, String resourceURI) throws Exception
	{
		proxyedResourceDescriptor.setup(resourceType, resourceURI);
	}
	
	@Override
	public void init(ResourceDeclarationElement declaringResourceElement, VariableContainer variableContainer, LifeCycle lifeCycle, boolean iterate, ResourceParameter... resourceParameters) throws Exception
	{
		proxyedResourceDescriptor.init(declaringResourceElement, variableContainer, lifeCycle, iterate, resourceParameters);		
	}
	
	@Override
	public State getResourceState() throws Exception
	{
		return proxyedResourceDescriptor.getResourceState();
	}
	
	@Override
	public void open(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		proxyedResourceDescriptor.open(variableContainer, resourceParameters);		
	}
	
	@Override
	public ContentMetaData getResourceMetaData(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		return proxyedResourceDescriptor.getResourceMetaData(variableContainer, resourceParameters);	
	}
	
	@Override
	public ContentMetaData getContentMetaData(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		return proxyedResourceDescriptor.getContentMetaData(variableContainer, resourceParameters);
	}
	
	@Override
	public ContentMetaData getOutputMetaData(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
	    return proxyedResourceDescriptor.getOutputMetaData(variableContainer, resourceParameters);
	}
	
	@Override
	public ResourceDescriptor getChildResourceDescriptor(ControlElement callingControlElement, String relativeURI) throws Exception
	{
	    return proxyedResourceDescriptor.getChildResourceDescriptor(callingControlElement, relativeURI);
	}
	
	@Override
	public ResourceDescriptor getParentResourceDescriptor() throws Exception
	{
	    return proxyedResourceDescriptor.getParentResourceDescriptor();
	}
	
	@Override
	public void setParentResourceDescriptor(ResourceDescriptor parentResourceDescriptor) throws Exception
	{
	    proxyedResourceDescriptor.setParentResourceDescriptor(parentResourceDescriptor);
	    
	}
	
	@Override
	public InputStream getInputStream(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		return proxyedResourceDescriptor.getInputStream(variableContainer, resourceParameters);
	}
	@Override
	public OutputStream getOutputStream(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		return proxyedResourceDescriptor.getOutputStream(variableContainer, resourceParameters);
	}

	@Override
	public void addResourceParameters(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		proxyedResourceDescriptor.addResourceParameters(variableContainer, resourceParameters);
		
	}
	
	@Override
	public boolean isSupportedStreamFormat(StreamType streamType, StreamFormat streamFormat) throws Exception
	{		
		return proxyedResourceDescriptor.isSupportedStreamFormat(streamType, streamFormat);
	}
	
	@Override
	public void close(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		
		proxyedResourceDescriptor.close(variableContainer, resourceParameters);
	}
	
	@Override
	public void release(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		
		proxyedResourceDescriptor.release(variableContainer, resourceParameters);
	}
	
	@Override
	public void reset(State previousState) throws Exception
	{		
		proxyedResourceDescriptor.reset(previousState);
	}
	
	@Override
	public void advanceState(State desiredState, VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
	    proxyedResourceDescriptor.advanceState(desiredState, variableContainer, resourceParameters);	    
	}
	
	@Override
	public boolean next(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		return proxyedResourceDescriptor.next(variableContainer, resourceParameters);
	}
	
	
	
	@Override
	public CElement readXML(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		return resourceElement;
	}
	
	@Override
	public void writeXML(VariableContainer variableContainer, CElement element, ResourceParameter... resourceParameters) throws Exception
	{
		proxyedResourceDescriptor.writeXML(variableContainer, element, resourceParameters);
	}
	
	@Override
	public byte[] readBlock(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		return proxyedResourceDescriptor.readBlock(variableContainer, resourceParameters);
	}

	@Override
	public void writeBlock(VariableContainer variableContainer, byte[] block, ResourceParameter... resourceParameters) throws Exception
	{
		proxyedResourceDescriptor.writeBlock(variableContainer, block, resourceParameters);
		
	}
	
	@Override
	public void processInput(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		proxyedResourceDescriptor.processInput(variableContainer, resourceParameters);		
	}

	@Override
	public void processOutput(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		proxyedResourceDescriptor.processOutput(variableContainer, resourceParameters);	
	}
	
	@Override
	public boolean performAction(VariableContainer variableContainer, Action action, ResourceParameter... resourceParameters) throws Exception
	{
		return proxyedResourceDescriptor.performAction(variableContainer, action, resourceParameters);
	}

	
	@Override
	public LifeCycle getLifeCycle() throws Exception
	{
		return proxyedResourceDescriptor.getLifeCycle();
	}
	
	@Override
	public State getStreamState(StreamType streamType) throws Exception
	{		
		return proxyedResourceDescriptor.getStreamState(streamType);
	}

	@Override
	public StreamFormat[] getSupportedStreamFormats(StreamType streamType) throws Exception
	{		
		return proxyedResourceDescriptor.getSupportedStreamFormats(streamType);
	}

	@Override
	public StreamType[] getSupportedStreamTypes() throws Exception
	{
		return proxyedResourceDescriptor.getSupportedStreamTypes();
	}

	@Override
	public boolean isSupportedAction(Action action) throws Exception
	{		
		return proxyedResourceDescriptor.isSupportedAction(action);
	}

	@Override
	public boolean isSupportedStreamType(StreamType streamType) throws Exception
	{		
		return proxyedResourceDescriptor.isSupportedStreamType(streamType);
	}
	
	
	@Override
	public ResourceType getResourceType()
	{
		return proxyedResourceDescriptor.getResourceType();
	}

	@Override
	public ResourceURI getResourceURI()
	{
		return proxyedResourceDescriptor.getResourceURI();
	}

	
	

	/** returns last piece of URI **/
    @Override
    public String getLocalName()
    {
    	return proxyedResourceDescriptor.getLocalName();
    }
	
	@Override
	public boolean isRemoteResource()
	{	
		return proxyedResourceDescriptor.isRemoteResource();
	}
	
	

	
}
