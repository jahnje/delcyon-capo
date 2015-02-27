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
package com.delcyon.capo.datastream.stream_attribute_filter;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.delcyon.capo.datastream.OutputStreamAttributeFilterProvider;

/**
 * @author jeremiah
 *
 */
@OutputStreamAttributeFilterProvider(name=MD5FilterInputStream.ATTRIBUTE_NAME)
public class MD5FilterOutputStream extends FilterOutputStream implements StreamAttributeFilter
{
	 private MessageDigest messageDigest;
	 private String md5;
	 
     public MD5FilterOutputStream(OutputStream out) throws NoSuchAlgorithmException
     {
         super(out);
         messageDigest = MessageDigest.getInstance("MD5");
         messageDigest.reset();
     }

     @Override
     public void write(int b) throws IOException
     {
    	 messageDigest.update((byte) b);
    	 super.write(b);    	 
     }

     /**
      * override write method
      */
     @Override
     public void write(byte[] data, int offset, int length) throws IOException
     {
    	 messageDigest.update(data,offset,length);
    	 out.write(data, offset, length);
    	 //super.write(data, offset, length);
 
         
     }

     /**
      * override write method
      */
     @Override
     public void write(byte[] b) throws IOException
     {
    	 messageDigest.update(b);
    	 super.write(b);
     }

     public String getMD5()
     {
         //TODO deal with leading zeros

         //the message digest only lets us read from it once before it resets things, so persist the value incase we read more than once. 
         if(md5 == null)
         {
             md5 = new BigInteger(1, messageDigest.digest()).toString(16); 
         }
         return md5;
     }

	public void write(String value) throws Exception
	{
		if (value != null)
		{
			write(value.getBytes());			
		}
		
	}

	@Override
	public String getName()
	{
		return MD5FilterInputStream.ATTRIBUTE_NAME;
	}

	@Override
	public String getValue()
	{
		return getMD5();
	}
}
