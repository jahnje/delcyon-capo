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
package com.delcyon.capo.datastream;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.Configuration.PREFERENCE;


/**
 * @author jeremiah
 *
 */
public class StreamUtil
{

	public static long readInputStreamIntoOutputStream(InputStream inputStream, OutputStream outputStream) throws Exception
	{
		return readInputStreamIntoOutputStream(inputStream, outputStream,CapoApplication.getConfiguration().getIntValue(PREFERENCE.BUFFER_SIZE));
	}
	
	/**
	 * 
	 * @param inputStream
	 * @param outputStream
	 * @return total bytes read
	 * @throws Exception
	 */
	public static long readInputStreamIntoOutputStream(InputStream inputStream, OutputStream outputStream, int  bufferSize) throws Exception
    {
		long totalBytesRead = 0l;
		
		int bytesRead = 0;
		while (bytesRead != -1)
		{
			byte[] buffer = new byte[bufferSize];			
			bytesRead = inputStream.read(buffer);			
			if (bytesRead > 0)
			{				
				outputStream.write(buffer, 0, bytesRead);				
				totalBytesRead += bytesRead;
			}
		}

		outputStream.flush();
		
		return totalBytesRead;
    }

	public static String getMD5(byte[] data) throws NoSuchAlgorithmException
	{
	    MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        messageDigest.reset();
        messageDigest.update(data);
        return new BigInteger(1, messageDigest.digest()).toString(16);
	}
	
}
