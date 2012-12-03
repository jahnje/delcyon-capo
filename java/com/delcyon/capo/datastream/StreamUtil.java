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

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.Configuration.PREFERENCE;


/**
 * @author jeremiah
 *
 */
public class StreamUtil
{

	
	
	public static int fullyReadIntoBufferUntilPattern(BufferedInputStream inputStream, byte[] buffer, byte... pattern) throws Exception
	{
	    
	    int totalRead = 0;
	    byte[] localBuffer = null;
	    
	    //make sure we don't read more than we need to, so choose the smallest buffer size.
	    if(CapoApplication.getConfiguration().getIntValue(PREFERENCE.BUFFER_SIZE) > buffer.length)
	    {
	        localBuffer = new byte[buffer.length];
	    }
	    else
	    {
	        localBuffer = new byte[CapoApplication.getConfiguration().getIntValue(PREFERENCE.BUFFER_SIZE)];    
	    }
	    
	    
	    int destPos = 0;
	    while(true)
	    {
	        int count =  inputStream.read(localBuffer);
	        totalRead += count;
	        if(count < 0)
	        {
	            break;
	        }
	        if(count > 0)
	        {
	            try
	            {
	                System.arraycopy(localBuffer, 0, buffer, destPos, count);
	            } catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException)
	            {	                
	                throw arrayIndexOutOfBoundsException;
	            }
	            destPos += count;

	            if(searchForBytePattern(pattern, buffer, totalRead - pattern.length, pattern.length).size() > 0)
	            {
	                break;
	            }

	        }
	    }
	    return totalRead;
	}
	
	public static byte[] fullyReadUntilPattern(BufferedInputStream inputStream,boolean includePattern, byte... pattern) throws Exception
    {
	    AccessibleByteArrayOutputStream byteArrayOutputStream = new AccessibleByteArrayOutputStream();
        int lastScanPosition = 0;
        byte[] localBuffer = new byte[CapoApplication.getConfiguration().getIntValue(PREFERENCE.BUFFER_SIZE)];      
        
        boolean isEOF = false;
        boolean needMoreData = false;
        int count = 0;
        while(true)
        {
            if(needMoreData == true)
            {
                inputStream.mark(localBuffer.length);
            }
            else
            {
                count = 0;
            }
            
            count += inputStream.read(localBuffer,count,localBuffer.length-count);
           
            if(count < 0)
            {
                isEOF = true;
                break;
            }
            
            
            
            if(count > 0)
            {
                if(byteArrayOutputStream.size() + count < pattern.length)
                {                     
                    needMoreData = true;
                    continue;
                }
                else
                {
                    needMoreData = false;
                }
                
                byteArrayOutputStream.write(localBuffer, 0, count);
                
                List<Integer> posList = searchForBytePattern(pattern, byteArrayOutputStream.getBuffer(), lastScanPosition, count);
                if(posList.size() > 0)
                {
                    if(posList.get(0) < byteArrayOutputStream.size()-1)
                    {                        
                        //There was more on the buffer than we needed
                        //reset to the start of last read
                        inputStream.reset();
                        //skip to our found pos + length of pattern.
                        inputStream.skip(posList.get(0)-lastScanPosition+pattern.length);
                        
                        //now trim our buffer
                        byte[] osBuffer = byteArrayOutputStream.toByteArray(0, posList.get(0)+pattern.length);
                        byteArrayOutputStream.reset();
                        byteArrayOutputStream.write(osBuffer);                        
                    }
                    break;
                }
                else
                {
                    lastScanPosition += count; 
                }

            }
        }
        
        byteArrayOutputStream.close();//pointless, but gets warnings to go away.
        //use this to indicate that we're done with this stream, because it's closed.
        if(isEOF && byteArrayOutputStream.size() == 0)
        {
            return null;
        }
        else if(includePattern == true || isEOF == true)
        {
            return byteArrayOutputStream.toByteArray();
        }
        else
        {
            return byteArrayOutputStream.toByteArray(0,byteArrayOutputStream.size()-pattern.length);
        }
    }
	
	
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
		byte[] buffer = new byte[bufferSize];
		while (bytesRead != -1)
		{
						
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
	
	/**
	 * Quickly search a byte array for a pattern of bytes.
	 * @param pattern to search for
	 * @param bytes byte array to search for pattern
	 * @param start starting position in bytes to start searching array.
	 * @param length how far passed that start to conclude the search
	 * @return List<Integer> of position in bytes array that are the start of said pattern.
	 */
	public static List<Integer> searchForBytePattern(byte[] pattern, byte[] bytes, int start, int length)
	{
	    
	    Vector<Integer> matchPositions = new Vector<Integer>();
	    if(start < 0)
	    {
	        start = 0;
	    }
	    
	    int endIndex = (start + length - 1);
	    if (endIndex > bytes.length -1)
	    {
	        endIndex = bytes.length -1;
	    }
	    
	    for(int index = start; index <= endIndex; index++)
	    {
	        //as we walk along the whole thing look for a match on the first byte of our pattern
	        if (pattern[0] == bytes[index] && endIndex - index + 1>= pattern.length)
            {
                byte[] match = new byte[pattern.length];
                //copy over enough of the array to do an array comparison against our pattern. 
                System.arraycopy(bytes, index, match, 0, pattern.length);
                if (Arrays.equals(match, pattern))
                {
                    //add the index to our list
                    matchPositions.add(index);
                    //then skip ahead
                    index += pattern.length - 1;
                }
            }
	    }
	    return matchPositions;
	}
	
}
