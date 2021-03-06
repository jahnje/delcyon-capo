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
package com.delcyon.capo.resourcemanager;

import java.util.ArrayList;

import com.delcyon.capo.resourcemanager.ResourceDescriptor.LifeCycle;

/**
 * @author jeremiah
 *
 */
public interface ResourceType
{
	/**
	 * 
	 * @param resourceElement the declaring resource element
	 * @param resourceURI uri value from the resource element, this may have been changed by variable replacement
	 * @param resourceParameters any resource parameters
	 * @return
	 * @throws Exception
	 */
	public ResourceDescriptor getResourceDescriptor(String resourceURI) throws Exception;

	public LifeCycle getDefaultLifeCycle();
	
	
	/**
	 * Returns a boolean indicating if stream tokens can be defined at runtime in the XML.  
	 * @return
	 */
	public boolean runtimeDefineableTokenLists();
	
	/**
	 * Returns an array list of integers/chars that define token boundaries. 
	 * @return
	 */
	public ArrayList<ArrayList<Integer>> getDefaultTokenLists();

	/**
	 * This returns the first scheme that corresponds to this resource type.
	 * @return
	 */
	public String getName();
	
}
