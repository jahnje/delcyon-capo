/**
Copyright (c) 2011 Delcyon, Inc.
This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.delcyon.capo.resourcemanager.types;

import java.util.Arrays;

import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceType;
import com.delcyon.capo.resourcemanager.ResourceTypeProvider;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.LifeCycle;

/**
 * @author jeremiah
 *
 */
public abstract class AbstractResourceType implements ResourceType
{

	/* (non-Javadoc)
	 * @see com.delcyon.capo.resourcemanager.ResourceType#getResourceDescriptor(com.delcyon.capo.controller.elements.ResourceElement, java.net.URI, com.delcyon.capo.resourcemanager.ResourceParameter[])
	 */
	@Override
	public ResourceDescriptor getResourceDescriptor(String resourceURI) throws Exception
	{
		ResourceDescriptor resourceDescriptor = getClass().getAnnotation(ResourceTypeProvider.class).providerClass().newInstance();
		resourceDescriptor.setup(this, resourceURI);
		return resourceDescriptor;
	}

	@Override
	public LifeCycle getDefaultLifeCycle()
	{
		return getClass().getAnnotation(ResourceTypeProvider.class).defaultLifeCycle();
	}
	
	@Override
	public String getName()
	{
		return Arrays.toString(getClass().getAnnotation(ResourceTypeProvider.class).schemes());
	}
	
}
