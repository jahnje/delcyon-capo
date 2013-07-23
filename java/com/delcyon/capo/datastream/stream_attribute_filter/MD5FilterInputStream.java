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
import java.math.BigInteger;
import java.security.MessageDigest;


/**
 * @author jeremiah
 *
 */
@InputStreamAttributeFilterProvider(name=MD5FilterInputStream.ATTRIBUTE_NAME)
public class MD5FilterInputStream extends AbstractFilterInputStream
{

	
	public static final String ATTRIBUTE_NAME = "MD5";
	
	private MessageDigest messageDigest;
	private String md5 = null;
	
	public MD5FilterInputStream(InputStream inputStream) throws Exception
	{
		super(inputStream);
		messageDigest = MessageDigest.getInstance("MD5");
        messageDigest.reset();
	}

	@Override
	public String getValue()
	{	
	    //TODO deal with leading zeros??
	    //the message digest only lets us read from it once before it resets things, so persist the value incase we read more than once. 
	    if(md5 == null)
	    {
	        md5 = new BigInteger(1, messageDigest.digest()).toString(16); 
	    }
	    return md5;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException
	{
		int bytesRead = super.read(b, off, len);
		if (bytesRead > 0)
		{
			messageDigest.update(b, off, bytesRead);
		}
		return bytesRead;
	}

	
	
	
	
}
