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
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import eu.medsea.mimeutil.MimeUtil;

/**
 * @author jeremiah
 *
 */
@InputStreamAttributeFilterProvider(name=MimeTypeFilterInputStream.MIME_TYPE_ATTRIBUTE)
public class MimeTypeFilterInputStream extends AbstractFilterInputStream
{

	public static final String MIME_TYPE_ATTRIBUTE = "mimeType"; 
	private static ConcurrentHashMap<String, String> mimeTypeConcurrentHashMap = new ConcurrentHashMap<String, String>();
	private byte[] buffer = new byte[4096];
	private int bufferPosition = 0; 
	private String mimeType = null;
	private boolean hasRead = false;
	
	public MimeTypeFilterInputStream(InputStream inputStream)
	{
		super(inputStream);		
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getValue()
	{
		if (mimeType == null)
		{
		    String key = new String(buffer);
		    mimeType = mimeTypeConcurrentHashMap.get(key);
		    if(mimeType == null)
		    {
		        Collection mimeTypeColection = MimeUtil.getMimeTypes(buffer);         
	            if (mimeTypeColection.isEmpty() == false)
	            {
	                 mimeType = mimeTypeColection.toArray()[0].toString(); 
	                 mimeTypeConcurrentHashMap.put(key, mimeType);
	            }    
		    }
			
		}
		return mimeType;
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException
	{
		int bytesRead = super.read(b, off, len);
		if (bytesRead > 0 )
		{
			if (bufferPosition < buffer.length)
			{
				int numberOfBytesToCopy = bytesRead;
				if (numberOfBytesToCopy > buffer.length)
				{
					numberOfBytesToCopy = buffer.length;
				}
				if (numberOfBytesToCopy > (buffer.length - bufferPosition))
				{
					numberOfBytesToCopy = buffer.length - bufferPosition;
				}
				System.arraycopy(b, off, buffer, bufferPosition, numberOfBytesToCopy);
				bufferPosition += numberOfBytesToCopy;
			}
		}
		return bytesRead;
	}
	
	
	
}
