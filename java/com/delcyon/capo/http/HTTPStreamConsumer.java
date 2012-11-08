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
package com.delcyon.capo.http;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.Configuration;
import com.delcyon.capo.CapoApplication.Location;
import com.delcyon.capo.Configuration.PREFERENCE;
import com.delcyon.capo.annotations.DirectoyProvider;
import com.delcyon.capo.datastream.StreamProcessor;
import com.delcyon.capo.datastream.StreamProcessorProvider;
import com.delcyon.capo.datastream.StreamUtil;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceParameter;
import com.delcyon.capo.resourcemanager.ResourceParameter.Source;
import com.delcyon.capo.resourcemanager.types.FileResourceType;
import com.delcyon.capo.server.CapoServer;

/**
 * @author jeremiah
 */
@StreamProcessorProvider(streamIdentifierPatterns = { "GET .*","POST .*","PUT .*" })
@DirectoyProvider(preferenceName="WEB_DIR",preferences=Configuration.PREFERENCE.class,location=Location.SERVER)
public class HTTPStreamConsumer implements StreamProcessor
{
    private static final byte[] messageBoundryPattern = new String("\r\n\r\n").getBytes();
    
	@Override
	public void init(HashMap<String, String> sessionHashMap) throws Exception
	{
		

	}
	
	@Override
	public void processStream(BufferedInputStream bufferedInputStream, OutputStream outputStream) throws Exception
	{
	    byte[] buffer = new byte[CapoApplication.getConfiguration().getIntValue(PREFERENCE.BUFFER_SIZE)];

	    int bytesRead = 0;
	    while (bytesRead >= 0)
	    {
	        bytesRead = bufferedInputStream.read(buffer);
	        List<Integer> matchList = StreamUtil.searchForBytePattern(messageBoundryPattern, buffer, 0, bytesRead);
	        if(matchList.size() > 0)
	        {   
	            //we only care about headers here, so trim, and jump out
	            bytesRead = matchList.get(0)+messageBoundryPattern.length;
	            break;
	        }
	    }


	    SimpleHttpRequest request = new SimpleHttpRequest(new String(buffer,0,bytesRead));

	    SimpleHttpResponse response = new SimpleHttpResponse();
	    ResourceDescriptor updatesResourceDescriptor = CapoApplication.getDataManager().getResourceDirectory(PREFERENCE.WEB_DIR.toString());	    
	    ResourceDescriptor fileResourceDescriptor = updatesResourceDescriptor.getChildResourceDescriptor(null, request.getPath());
	    //verify we're not trying to jump out of our directory tree here
	    String trimmedURIString = fileResourceDescriptor.getResourceURI().getResourceURIString().substring(updatesResourceDescriptor.getResourceURI().getResourceURIString().length());
	    if (trimmedURIString.equals(request.getPath()) == false)
	    {
	        CapoServer.logger.log(Level.WARNING, "HTTP Security Issue: '"+request.getPath()+"'");
	        outputStream.close();
            bufferedInputStream.close();	        
	    }
	    try
	    {
	        fileResourceDescriptor.addResourceParameters(null, new ResourceParameter(FileResourceType.Parameters.PARENT_PROVIDED_DIRECTORY,PREFERENCE.WEB_DIR,Source.CALL));
	        fileResourceDescriptor.open(null);
	        

	        if (fileResourceDescriptor.getResourceMetaData(null).exists() == true && fileResourceDescriptor.getResourceMetaData(null).isReadable() == true && fileResourceDescriptor.getResourceMetaData(null).isContainer() == false)
	        {
	            response.setHeader("Content-Length", fileResourceDescriptor.getResourceMetaData(null).getLength()+"");
	            response.setHeader("Content-Type", "application/octet-stream");
	            outputStream.write(response.getBytes());			
	            StreamUtil.readInputStreamIntoOutputStream(fileResourceDescriptor.getInputStream(null), outputStream);
	        }
	        else
	        {
	            response.setResponseCode(404,"NOT FOUND");
	            CapoServer.logger.log(Level.FINE, "Sent response:\n"+new String(response.getBytes()));
	            CapoServer.logger.log(Level.WARNING, "File Not Found: '"+request.getPath()+"'");
	            outputStream.write(response.getBytes());
	        }
	        outputStream.close();
	        bufferedInputStream.close();
	    }
	    catch (IOException ioException)
	    {
	        ioException.printStackTrace();
	    }
	    finally
	    {
	        if(fileResourceDescriptor != null)
	        {
	            fileResourceDescriptor.release(null);
	        }
	    }
	}
}
