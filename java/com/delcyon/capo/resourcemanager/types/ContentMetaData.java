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

import java.util.List;

import com.delcyon.capo.resourcemanager.ContentFormatType;
import com.delcyon.capo.resourcemanager.ResourceURI;

/**
 * @author jeremiah
 *
 */
public interface ContentMetaData
{

	public enum Attributes
	{
		exists,
		executable,
		readable,
		writeable,
		container,
		lastModified,
		path, 
		uri,
		MD5
	}

	public enum Parameters {
		DEPTH, 
		USE_RELATIVE_PATHS, 
		ROOT_PATH,
		MONITOR
		
	}
	
    //BEGIN RESOURCE METADATA
	/**
	 * Controls whether this metadata should be refreshed each time each time it's asked for. 
	 * @return
	 */
	public boolean isDynamic();
	
	/**
	 * only relevant when isDynamic is true. If areDynamicAttributeLoaded returns true, then then controlling object may make a decision as to whether or not to refresh the data, since we have all we're going to get at this point.
	 * @return
	 */
	public boolean areDynamicAttributeLoaded();
	
	public void refresh() throws Exception;
	
	public boolean isInitialized();
	
	public void setInitialized(boolean isInitialized);
	
	public  ContentFormatType getContentFormatType();
	
	public  void setContentFormatType(ContentFormatType contentFormatType);
	
	public  Boolean exists();

	public  Boolean isReadable();

	public  Long getLength();

	public  Boolean isWriteable();

	public  String getMD5();

	public  Boolean isContainer();
	
	public void clearAttributes();

	public Long getLastModified();
	
	/**
	 * Set an attributes values
	 * @param name
	 * @param value
	 */
	public void setValue(String name, String value);
	
	/**
	 * returns an attributes values, null if it doesn't exist.
	 * @param name
	 * @return
	 */
	public String getValue(String name);
	
	/**
     * returns an attributes values, null if it doesn't exist.
     * @param name
     * @return
     */
	public String getValue(Enum name);
	
	public boolean isSupported(String attributeName);
	
	public boolean hasAttribute(String attributeName);
	
	public List<String> getSupportedAttributes();

	public List<ContentMetaData> getContainedResources();

	public void addContainedResource(ContentMetaData contentMetaData);

	public ResourceURI getResourceURI();
	
}
