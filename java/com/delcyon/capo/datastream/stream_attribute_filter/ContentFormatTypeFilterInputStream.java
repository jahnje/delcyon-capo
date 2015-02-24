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

import com.delcyon.capo.resourcemanager.ContentFormatType;

/**
 * @author jeremiah
 *
 */
@InputStreamAttributeFilterProvider(name=ContentFormatType.ATTRIBUTE_NAME)
public class ContentFormatTypeFilterInputStream extends AbstractFilterInputStream
{

	private byte[] buffer = new byte[ContentFormatType.MINIMUM_BUFFER_LENGTH+1];
	private int bufferPosition = 0; 
	private ContentFormatType contentFormatType = null;
	private boolean hasRead = false;
	
	public ContentFormatTypeFilterInputStream(InputStream inputStream)
	{
		super(inputStream);		
	}

	

	public ContentFormatType getContentFormatType()
	{
		if (contentFormatType == null && hasRead == true)
		{
			return ContentFormatType.NO_CONTENT;
		}
		return contentFormatType;
	}

	@Override
	public String getValue()
	{
		if (getContentFormatType() != null)
		{
		return getContentFormatType().toString();
		}
		else
		{
			return null;
		}
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException
	{
		int bytesRead = super.read(b, off, len);
		hasRead = true;
		if (bytesRead > 0 && contentFormatType != ContentFormatType.BINARY)
		{			
			for (int readByteIndex = off; readByteIndex < off+bytesRead; readByteIndex++)
			{
				
				int readValue = (int)b[readByteIndex];
				if (contentFormatType == ContentFormatType.BINARY || readValue == -1)
				{
					//do nothing
					break;
				}
				//if we have binary(non-printable) data, just go ahead and set it
				else if (readValue != 9 && readValue != 10 && readValue != 13 && (readValue >= 0x20 & readValue <= 0x7e) == false)
				{
					contentFormatType = ContentFormatType.BINARY;
					break;
				}
				else if (bufferPosition < buffer.length)
				{
					buffer[bufferPosition] = (byte) readValue;
					bufferPosition++;
				}

				if (bufferPosition >= ContentFormatType.MINIMUM_BUFFER_LENGTH && contentFormatType == null)
				{
					if (ContentFormatType.XML.isMatch(buffer))
					{
						contentFormatType = ContentFormatType.XML;
					}
					else
					{
						contentFormatType = ContentFormatType.TEXT;
					}
				}
				//when we reach he end of the stream and we don't have enough data, but we have some and we're not BINARY already, default to TEXT
				else if (contentFormatType == null && readValue == -1 && bufferPosition > 0)
				{
					contentFormatType = ContentFormatType.TEXT;
				}
			}
		}
		return bytesRead;
	}
	
	
	
}
