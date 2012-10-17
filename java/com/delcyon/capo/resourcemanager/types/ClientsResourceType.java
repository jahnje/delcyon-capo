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
import java.util.Arrays;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.CapoApplication.Location;
import com.delcyon.capo.annotations.DirectoyProvider;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceType;
import com.delcyon.capo.resourcemanager.ResourceTypeProvider;
import com.delcyon.capo.resourcemanager.ResourceURI;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.LifeCycle;
import com.delcyon.capo.server.CapoServer;
import com.delcyon.capo.server.CapoServer.Preferences;

/**
 * @author jeremiah
 *
 */
@DirectoyProvider(preferenceName="CLIENTS_DIR",preferences=CapoServer.Preferences.class,location=Location.SERVER)
@ResourceTypeProvider(schemes={"clients"},providerClass=ClientsResourceDescriptor.class)
public class ClientsResourceType implements ResourceType
{

	
    /* (non-Javadoc)
     * @see com.delcyon.capo.resourcemanager.ResourceType#getResourceDescriptor(com.delcyon.capo.controller.elements.ResourceElement, java.net.URI, com.delcyon.capo.resourcemanager.ResourceParameter[])
     */
    @Override
    public ResourceDescriptor getResourceDescriptor(String resourceURI) throws Exception
    {
        ResourceDescriptor clientResourceDescriptor = CapoApplication.getDataManager().getResourceDescriptor(null, CapoApplication.getDataManager().getResourceDirectory(Preferences.CLIENTS_DIR.toString()).getResourceURI().getResourceURIString()+"/"+ResourceURI.getSchemeSpecificPart(resourceURI));        
        return clientResourceDescriptor;
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
	 * @see com.delcyon.capo.resourcemanager.ResourceType#runtimeDefineableTokenLists()
	 */
	@Override
	public boolean runtimeDefineableTokenLists()
	{
		// TODO Auto-generated method stub
		return false;
	}

}
