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
package com.delcyon.capo.controller.elements;

import java.io.BufferedInputStream;
import java.net.SocketException;
import java.util.HashMap;
import java.util.logging.Level;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.Configuration.PREFERENCE;
import com.delcyon.capo.controller.AbstractControl;
import com.delcyon.capo.controller.ControlElementProvider;
import com.delcyon.capo.controller.LocalRequestProcessor;
import com.delcyon.capo.controller.client.ControllerRequest;
import com.delcyon.capo.controller.server.ControllerClientRequestProcessor;
import com.delcyon.capo.datastream.StreamHandler;
import com.delcyon.capo.datastream.StreamProcessor;
import com.delcyon.capo.protocol.client.CapoConnection;
import com.delcyon.capo.protocol.client.Request;
import com.delcyon.capo.server.CapoServer;

/**
 * @author jeremiah
 * Appends a node to the XML
 */
@ControlElementProvider(name="request")
public class RequestElement extends AbstractControl
{

	
	
	public enum Attributes
	{
		name
	}
	
	
	
	private static final String[] supportedNamespaces = {CapoApplication.SERVER_NAMESPACE_URI};
	
	@Override
	public Attributes[] getAttributes()
	{
		return Attributes.values();
	}
	
	
	@Override
	public Attributes[] getRequiredAttributes()
	{
		return new Attributes[]{Attributes.name};
	}

	
	@Override
	public String[] getSupportedNamespaces()
	{
		return supportedNamespaces;
	}

	
	
	@Override
	public Object processServerSideElement() throws Exception
	{
	    HashMap<String, String> sessionHashMap = new HashMap<String, String>();
	    if (CapoApplication.isServer())
	    {
	        throw new Exception("Server does not support client like request to itself yet. Don't do that!");
	    }
	    else
	    {
	        CapoConnection capoConnection = new CapoConnection();
	        ControllerRequest controllerRequest = new ControllerRequest(capoConnection.getOutputStream(),capoConnection.getInputStream());
	        controllerRequest.setType(getAttributeValue(Attributes.name));
	        controllerRequest.loadSystemVariables();
	        runRequest(capoConnection, controllerRequest,sessionHashMap);
	        capoConnection.close();
	    }
		return null;
	}

	public void runRequest(CapoConnection capoConnection, Request request, HashMap<String, String> sessionHashMap) throws Exception
    {
        String initialRequestType = null;
        if (request instanceof ControllerRequest)
        {
            initialRequestType = ((ControllerRequest) request).getRequestType();
            if (initialRequestType == null)
            {
                initialRequestType = "default";
            }
        }       
        else
        {
            initialRequestType = request.getClass().getSimpleName();
        }
        //send request
        try
        {                   
            request.send();
        }
        catch (SocketException socketException)
        {
            //do nothing, let any errors be processed later, since there might be a message in the buffer
        }
        boolean isFinished = false;
        
        while(isFinished == false)
        {   
            byte[] buffer = getBuffer(capoConnection.getInputStream());
            //System.out.println(new String(buffer));
            //figure out the kind of response
            StreamProcessor streamProcessor = StreamHandler.getStreamProcessor(buffer);
            if (streamProcessor != null)
            {
                streamProcessor.init(sessionHashMap);
                streamProcessor.processStream(capoConnection.getInputStream(), capoConnection.getOutputStream());
            }
            else
            {
                //if we have no data, then we are finished, otherwise wait, then try again?
                if (buffer.length == 0)
                {
                    
                    
                    
                    CapoApplication.logger.log(Level.INFO, "Nothing left to process, finishing up "+initialRequestType+" request.");
                    
                    isFinished = true;
                }
                
            }
        }
    }
	
	private byte[] getBuffer(BufferedInputStream inputStream) throws Exception
    {
        int bufferSize = CapoApplication.getConfiguration().getIntValue(PREFERENCE.BUFFER_SIZE);
        byte[] buffer = new byte[bufferSize];
        inputStream.mark(bufferSize);
        int bytesRead = inputStream.read(buffer);
        inputStream.reset();
        
        //truncate the buffer so we can do accurate length checks on it
        //totally pointless, but seems like a good idea at the time
        if (bytesRead < 0)
        {
            return new byte[0];
        }
        else if (bytesRead < bufferSize)
        {
            byte[] shortenedBuffer = new byte[bytesRead];
            System.arraycopy(buffer, 0, shortenedBuffer, 0, bytesRead);
            return shortenedBuffer;
        }
        else
        {
            return buffer;
        }
    }
	
	
	
}
