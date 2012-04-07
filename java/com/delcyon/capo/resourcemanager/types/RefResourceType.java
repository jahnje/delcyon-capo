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

import java.util.ArrayList;

import com.delcyon.capo.resourcemanager.ResourceTypeProvider;

/**
 * @author jeremiah
 *
 */
@ResourceTypeProvider(schemes={"ref"},providerClass=RefResourceDescriptor.class)
public class RefResourceType extends AbstractResourceType
{

	
	
	public enum Parameters 
	{
		XMLNS
		
	}

	/* (non-Javadoc)
	 * @see com.delcyon.capo.resourcemanager.ResourceType#getDefaultTokenLists()
	 */
	@Override
	public ArrayList<ArrayList<Integer>> getDefaultTokenLists()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.delcyon.capo.resourcemanager.ResourceType#isIterable()
	 */
	@Override
	public boolean isIterable()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.delcyon.capo.resourcemanager.ResourceType#runtimeDefineableTokenLists()
	 */
	@Override
	public boolean runtimeDefineableTokenLists()
	{
		// TODO Auto-generated method stub
		return false;
	}

}
