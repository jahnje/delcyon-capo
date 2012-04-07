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
package com.delcyon.capo.resourcemanager.remote;

import java.util.ArrayList;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.server.ControllerClientRequestProcessor;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.LifeCycle;
import com.delcyon.capo.resourcemanager.ResourceManager;
import com.delcyon.capo.resourcemanager.ResourceManager.Preferences;
import com.delcyon.capo.resourcemanager.ResourceType;
import com.delcyon.capo.resourcemanager.ResourceTypeProvider;

/**
 * @author jeremiah
 *
 */
@ResourceTypeProvider(schemes="remote",defaultLifeCycle=LifeCycle.REF,providerClass=RemoteResourceDescriptorProxy.class)
public class RemoteResourceType implements ResourceType
{

	private String proxyedURI;
	private ResourceType proxyedResourceType;

	@Override
	public LifeCycle getDefaultLifeCycle()
	{
		return proxyedResourceType.getDefaultLifeCycle();
	}

	@Override
	public ArrayList<ArrayList<Integer>> getDefaultTokenLists()
	{
		return proxyedResourceType.getDefaultTokenLists();
	}

	@Override
	public String getName()
	{
		return proxyedResourceType.getName();
	}

	@Override
	public ResourceDescriptor getResourceDescriptor(String resourceURI) throws Exception
	{
		throw new UnsupportedOperationException("Remote Resource can't be used without a ControllerClientRequestProcessor. We have to know who to talk to.");
	}

	@Override
	public boolean isIterable()
	{
		return proxyedResourceType.isIterable();
	}

	@Override
	public boolean runtimeDefineableTokenLists()
	{
		return proxyedResourceType.runtimeDefineableTokenLists();
	}

	public ResourceDescriptor getResourceDescriptor(ControllerClientRequestProcessor controllerClientRequestProcessor, String resourceURI) throws Exception
	{
		RemoteResourceDescriptorProxy remoteResourceDescriptorProxy = new RemoteResourceDescriptorProxy(controllerClientRequestProcessor);		
		proxyedURI = ResourceManager.getSchemeSpecificPart(resourceURI);
		if (ResourceManager.getScheme(proxyedURI) == null)
		{
			proxyedURI = CapoApplication.getConfiguration().getValue(Preferences.DEFAULT_RESOURCE_TYPE)+":"+proxyedURI;
		}
		proxyedResourceType = CapoApplication.getDataManager().getResourceType(ResourceManager.getScheme(proxyedURI));
		remoteResourceDescriptorProxy.setup(proxyedResourceType, proxyedURI);
		return remoteResourceDescriptorProxy;
	}

}
