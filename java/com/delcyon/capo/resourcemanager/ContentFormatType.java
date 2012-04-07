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
package com.delcyon.capo.resourcemanager;

import java.util.regex.Pattern;


/**
 * This is type of the data in the resource. 
 * We need to know this when we are going to compare things. 
 * The goal is to always encapsulate all content in XML.
 * 
 * XML content always starts with "<\\?xml .*"
 * TEXT content never has a char out of the range "[\0x09\0x10\0x13\x20-\x7e]+"
 * BINARY content is everything 
 *   
 * @author jeremiah
 *
 */
public enum ContentFormatType
{
	XML("<\\?xml .*"),
	/**
	 * Warning!!! this is an untested regex!!
	 */
	TEXT("\\p{Print}+"),
	/**
	 * Warning!!! this is an untested regex!!
	 */
	BINARY("^\\p{Print}+"), 
	NO_CONTENT("");
	
	public static int MINIMUM_BUFFER_LENGTH = 8;
	
	private Pattern regex = null;
	
	private ContentFormatType(String regex)
	{
		this.regex = Pattern.compile(regex);
	}
	
	public boolean isMatch(byte[] buffer)
	{
		return regex.matcher(new String(buffer)).matches();
		
	}
	
	public static final String ATTRIBUTE_NAME = "contentFormatType";
}