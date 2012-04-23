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
package com.delcyon.capo.datastream.stream_attribute_filter;

import java.io.IOException;
import java.io.InputStream;


/**
 * @author jeremiah
 *
 */
@InputStreamAttributeFilterProvider(name=SizeFilterInputStream.ATTRIBUTE_NAME)
public class SizeFilterInputStream extends AbstractFilterInputStream
{

	
	public static final String ATTRIBUTE_NAME = "size";
	private long size = 0l;
	
	public SizeFilterInputStream(InputStream inputStream) throws Exception
	{
		super(inputStream);
		
	}

	@Override
	public String getValue()
	{
		return size+"";
	}
	

	@Override
	public int read(byte[] b, int off, int len) throws IOException
	{
		int bytesRead = super.read(b, off, len);
		if (bytesRead > 0)
		{
			size += (long)bytesRead;
		}
		return bytesRead;
	}
	

	
	
	
	
}