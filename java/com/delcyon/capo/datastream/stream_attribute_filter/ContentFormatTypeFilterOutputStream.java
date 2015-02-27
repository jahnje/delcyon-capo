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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.delcyon.capo.datastream.OutputStreamAttributeFilterProvider;
import com.delcyon.capo.resourcemanager.ContentFormatType;

/**
 * @author jeremiah
 *
 */
@OutputStreamAttributeFilterProvider(name=ContentFormatType.ATTRIBUTE_NAME)
public class ContentFormatTypeFilterOutputStream extends FilterOutputStream implements StreamAttributeFilter
{

	private byte[] buffer = new byte[4096];
	private int bufferPosition = 0; 
	private ContentFormatType contentFormatType = null;
	
	
	public ContentFormatTypeFilterOutputStream(OutputStream outputStream)
	{
		super(outputStream);		
	}


	public ContentFormatType getContentFormatType()
	{
		return contentFormatType;
	}


	public void setContentFormatType(ContentFormatType contentFormatType)
	{
		this.contentFormatType = contentFormatType;
	}

	/**
     * override write method  
     */
	 @Override
     public void write(int b) throws IOException
     {
		 if (bufferPosition < buffer.length)
		 {
			 buffer[bufferPosition] = (byte) b;
			 bufferPosition++;
		 }
		 //if we have binary(non-printable) data, just go ahead and set it
		 if (b != 9 && b != 10 && b != 13 && (b >= 0x20 & b <= 0x7e) == false)
		 {
			 //System.out.println(new String(buffer));
			 contentFormatType = ContentFormatType.BINARY;
		 }
		 //don't do any work if we don't have enough data to test not if we've already set something
		 else if (bufferPosition >= ContentFormatType.MINIMUM_BUFFER_LENGTH && contentFormatType == null)
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
         super.write(b);
     }
	
	 /**
     * override write method  
     */
    @Override
    public void write(byte[] data, int offset, int length) throws IOException
    {
        if(contentFormatType == null)
        {
            for (int i = offset; i < offset + length; i++)
            {
                this.write(data[i]);
            }
        }
        else
        {
            out.write(data, offset, length);
        }
    }
    
    /**
     * override write method  
     */
    @Override
    public void write(byte[] b) throws IOException
    {
        write(b, 0, b.length);
    }


	@Override
	public String getName()
	{
		return ContentFormatType.ATTRIBUTE_NAME;
	}


	@Override
	public String getValue()
	{
	    if(contentFormatType == null)
	    {
	        return ContentFormatType.NO_CONTENT.toString();
	    }
	    else
	    {
	        return contentFormatType.toString();
	    }
	}
	
}
