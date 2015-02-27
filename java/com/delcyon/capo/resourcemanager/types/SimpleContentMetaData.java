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

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import com.delcyon.capo.resourcemanager.ResourceParameter;
import com.delcyon.capo.resourcemanager.ResourceURI;

/**
 * @author jeremiah
 *
 */
public class SimpleContentMetaData extends AbstractContentMetaData
{
	
	@SuppressWarnings("unchecked")
	private transient List<Enum> supportedAttributeList = new Vector<Enum>();
	private ResourceParameter[] resourceParameters = new ResourceParameter[0]; 
	
	@SuppressWarnings("unused")
	private SimpleContentMetaData()
	{
		
	}
	
	public SimpleContentMetaData(ResourceURI resourceURI, ResourceParameter... resourceParameters)
	{
	    this.resourceParameters = resourceParameters;
		setResourceURI(resourceURI);
	}
	
	public void refresh(ResourceParameter... resourceParameters)
	{
		//init();
	}
	
	public void init()
	{
		setValue(Attributes.exists, false);

		setValue(Attributes.executable, false);

		setValue(Attributes.exists, false);

		setValue(Attributes.readable, false);

		setValue(Attributes.writeable, false);

		setValue(Attributes.container, false);

		setValue(Attributes.lastModified,null);
	}
	
	/* (non-Javadoc)
	 * @see com.delcyon.capo.resourcemanager.types.ContentMetaData#exists()
	 */
	@Override
	public Boolean exists()
	{
		return Boolean.parseBoolean(getValue(Attributes.exists));
	}

	/* (non-Javadoc)
	 * @see com.delcyon.capo.resourcemanager.types.ContentMetaData#getLastModified()
	 */
	@Override
	public Long getLastModified()
	{		
		return Long.parseLong(getValue(Attributes.lastModified));
	}

	/* (non-Javadoc)
	 * @see com.delcyon.capo.resourcemanager.types.ContentMetaData#isContainer()
	 */
	@Override
	public Boolean isContainer()
	{
		return Boolean.parseBoolean(getValue(Attributes.container));
	}

	/* (non-Javadoc)
	 * @see com.delcyon.capo.resourcemanager.types.ContentMetaData#isReadable()
	 */
	@Override
	public Boolean isReadable()
	{
		return Boolean.parseBoolean(getValue(Attributes.readable));
	}

	/* (non-Javadoc)
	 * @see com.delcyon.capo.resourcemanager.types.ContentMetaData#isWriteable()
	 */
	@Override
	public Boolean isWriteable()
	{
		return Boolean.parseBoolean(getValue(Attributes.writeable));
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public Enum[] getAdditionalSupportedAttributes()
	{
		return supportedAttributeList.toArray(new Enum[]{});
	}
	
	@SuppressWarnings("unchecked")
	public void addSupportedAttribute(Enum... name)
	{
		supportedAttributeList.addAll(Arrays.asList(name));
	}

	@Override
	public ResourceParameter[] getResourceParameters()
	{
	    return this.resourceParameters;
	}

}
