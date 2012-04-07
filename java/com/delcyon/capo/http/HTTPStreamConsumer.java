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
import java.io.OutputStream;
import java.util.HashMap;
import java.util.logging.Level;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.Configuration.PREFERENCE;
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
public class HTTPStreamConsumer implements StreamProcessor
{
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
        }
		
        
		SimpleHttpRequest request = new SimpleHttpRequest(new String(buffer));

		SimpleHttpResponse response = new SimpleHttpResponse();
		
		String relativePath = CapoApplication.getConfiguration().getValue(PREFERENCE.CAPO_DIR)+File.separator+CapoApplication.getConfiguration().getValue(PREFERENCE.UPDATES_DIR)+File.separator+request.getPath();
		ResourceDescriptor fileResourceDescriptor = CapoApplication.getDataManager().getResourceDescriptor(null, relativePath);
		fileResourceDescriptor.addResourceParameters(null, new ResourceParameter(FileResourceType.Parameters.PARENT_PROVIDED_DIRECTORY,PREFERENCE.UPDATES_DIR,Source.CALL));
		
		
		if (fileResourceDescriptor.getContentMetaData(null).exists() == true && fileResourceDescriptor.getContentMetaData(null).isReadable() == true)
		{
			response.setHeader("Content-Length", fileResourceDescriptor.getContentMetaData(null).getLength()+"");
			response.setHeader("Content-Type", "application/octet-stream");
			outputStream.write(response.getBytes());			
			StreamUtil.readInputStreamIntoOutputStream(fileResourceDescriptor.getInputStream(null), outputStream);
		}
		else
		{
			response.setResponseCode(404,"NOT FOUND");
			CapoServer.logger.log(Level.FINE, "Sent response:\n"+new String(response.getBytes()));
			CapoServer.logger.log(Level.WARNING, "File Not Found: '"+relativePath+"'");
			outputStream.write(response.getBytes());
		}
		outputStream.close();
		
	}
}
