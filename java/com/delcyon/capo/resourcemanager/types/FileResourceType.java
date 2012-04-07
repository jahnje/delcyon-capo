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

import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceTypeProvider;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.LifeCycle;
import com.delcyon.capo.util.diff.InputStreamTokenizer.TokenList;

/**
 * @author jeremiah
 *
 */
@ResourceTypeProvider(schemes={"file"},providerClass=FileResourceDescriptor.class)
public class FileResourceType extends AbstractResourceType
{
	public enum Parameters
	{
		PARENT_PROVIDED_DIRECTORY
	}
	@Override
	public ResourceDescriptor getResourceDescriptor(String resourceURI) throws Exception
	{	
		FileResourceDescriptor fileResourceDescriptor = new FileResourceDescriptor();
		fileResourceDescriptor.setup(this, resourceURI);
		
			
		return fileResourceDescriptor;
	}

	@Override
	public LifeCycle getDefaultLifeCycle()
	{
		return LifeCycle.GROUP;
	}
	
	@Override
	public ArrayList<ArrayList<Integer>> getDefaultTokenLists()
	{
		return TokenList.NEW_LINE.getTokenLists();
	}
	
	@Override
	public boolean isIterable()
	{
		return true;
	}
	
	@Override
	public boolean runtimeDefineableTokenLists()
	{
		return true;
	}
	
}
