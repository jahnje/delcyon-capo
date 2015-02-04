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

import java.util.List;

import org.w3c.dom.Document;

import com.delcyon.capo.controller.ControlElement;
import com.delcyon.capo.preferences.Preference;


/**
 * @author jeremiah
 *
 */
public abstract class CapoDataManager
{

	
	public static CapoDataManager loadDataManager(String className) throws Exception
	{
		return (CapoDataManager) Class.forName(className).newInstance();
	}

	public abstract void init(Boolean... minimal) throws Exception;
	
	public abstract ResourceDescriptor getResourceDescriptor(ControlElement callingControlElement,String uri) throws Exception;

	public abstract ResourceDescriptor getResourceDirectory(String resourceDirectoryName);

	public abstract ResourceType getResourceType(String schemeSpecificPart);

	public abstract long nextValue(String sequenceName) throws Exception;
		
	public abstract Document findDocument(String documentName, String clientID, Preference directoyPreference) throws Exception;

	public abstract List<ResourceDescriptor> findDocuments(ResourceDescriptor parentDirectory) throws Exception;

    public abstract ResourceDescriptor findDocumentResourceDescriptor(String documentName, String clientID, Preference directoryPreference) throws Exception;
    
    public abstract void release() throws Exception;

    public abstract void setDefaultResourceTypeScheme(String scheme);
    
    public abstract String getDefaultResourceTypeScheme();
    
}
