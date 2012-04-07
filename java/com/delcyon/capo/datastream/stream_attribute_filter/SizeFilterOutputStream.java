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

/**
 * @author jeremiah
 *
 */
@OutputStreamAttributeFilterProvider(name=SizeFilterInputStream.ATTRIBUTE_NAME)
public class SizeFilterOutputStream extends FilterOutputStream implements StreamAttributeFilter
{

	
	
	private long size = 0l;
	
	public SizeFilterOutputStream(OutputStream outputStream) throws Exception
	{
		super(outputStream);
		
	}

	@Override
	public String getValue()
	{
		return size+"";
	}
	
	@Override
	public String getName()
	{
		return SizeFilterInputStream.ATTRIBUTE_NAME;
	}
	
	 @Override
     public void write(int b) throws IOException
     {
    	size++;
    	 super.write(b);    	 
     }

     /**
      * override write method
      */
     @Override
     public void write(byte[] data, int offset, int length) throws IOException
     {
    	 size = size + length;
    	 super.write(data, offset, length);
 
         
     }

     /**
      * override write method
      */
     @Override
     public void write(byte[] b) throws IOException
     {
    	 size = size + b.length;
        super.write(b);
     }
	
	

	
	
	
	
}
