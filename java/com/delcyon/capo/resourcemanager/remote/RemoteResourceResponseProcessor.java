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
package com.delcyon.capo.resourcemanager.remote;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.logging.Level;

import org.w3c.dom.Document;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.CapoApplication.Location;
import com.delcyon.capo.controller.VariableContainer;
import com.delcyon.capo.datastream.StreamUtil;
import com.delcyon.capo.preferences.Preference;
import com.delcyon.capo.preferences.PreferenceInfo;
import com.delcyon.capo.preferences.PreferenceInfoHelper;
import com.delcyon.capo.preferences.PreferenceProvider;
import com.delcyon.capo.protocol.client.CapoConnection;
import com.delcyon.capo.protocol.client.XMLServerResponse;
import com.delcyon.capo.protocol.client.XMLServerResponseProcessor;
import com.delcyon.capo.protocol.client.XMLServerResponseProcessorProvider;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.remote.RemoteResourceDescriptorMessage.MessageType;
import com.delcyon.capo.xml.XPath;

/**
 * @author jeremiah
 *
 */
@PreferenceProvider(preferences=RemoteResourceResponseProcessor.Preferences.class)
@XMLServerResponseProcessorProvider(documentElementNames={"RemoteResourceResponse"}, namespaceURIs={})
public class RemoteResourceResponseProcessor implements XMLServerResponseProcessor, VariableContainer
{
    
    public enum Preferences implements Preference
	{
		
		@PreferenceInfo(arguments={}, defaultValue="30", description="The number of seconds to  wait before timing out a remote output stream", longOption="OUTPUT_STREAM_TIMEOUT", option="OUTPUT_STREAM_TIMEOUT")
		OUTPUT_STREAM_TIMEOUT;
		
		@Override
		public String[] getArguments()
		{
			return PreferenceInfoHelper.getInfo(this).arguments();
		}

		@Override
		public String getDefaultValue()
		{
			return PreferenceInfoHelper.getInfo(this).defaultValue();
		}

		@Override
		public String getDescription()
		{
			return PreferenceInfoHelper.getInfo(this).description();
		}

		@Override
		public String getLongOption()
		{
			return PreferenceInfoHelper.getInfo(this).longOption();
		}

		@Override
		public String getOption()
		{		
			return PreferenceInfoHelper.getInfo(this).option();
		}
		
		@Override
		public Location getLocation() 
		{
			return PreferenceInfoHelper.getInfo(this).location();
		}
	}
    
	private static Hashtable<String, ResourceDescriptor> resourceDescriptorHashtable = new Hashtable<String, ResourceDescriptor>();
	private static Hashtable<String, ThreadedInputStreamReader> threadedInputStreamReaderHashtable = new Hashtable<String, ThreadedInputStreamReader>();
	private Document responseDocument = null;
	private XMLServerResponse xmlServerResponse;
	private String sessionID;
	@SuppressWarnings("unused")
	private HashMap<String, String> sessionHashMap = null;

	@Override
	public Document getResponseDocument()
	{
		return responseDocument;
	}

	@Override
	public void init(Document responseDocument, XMLServerResponse xmlServerResponse,HashMap<String, String> sessionHashMap) throws Exception
	{
		this.responseDocument = responseDocument;
		this.xmlServerResponse = xmlServerResponse;
		this.sessionHashMap  = sessionHashMap;
	}

	@Override
	public void process() throws Exception
	{
		
		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage(XPath.unwrapDocument(responseDocument));

		RemoteResourceDescriptorMessage reply = new RemoteResourceDescriptorMessage();
		reply.setMessageType(MessageType.SUCCESS);

		sessionID = message.getSessionID();
		ResourceDescriptor resourceDescriptor = resourceDescriptorHashtable.get(sessionID);
		try
		{
			CapoConnection capoConnection = null;
			RemoteResourceRequest request = null;
			
			ThreadedInputStreamReader threadedInputSreamReader = null;
			CapoApplication.logger.log(Level.FINE, sessionID+":"+message.getMessageType());
			switch (message.getMessageType())
			{
				case SETUP:
					resourceDescriptor = CapoApplication.getDataManager().getResourceDescriptor(null, message.getResourceURI().toString());
					reply.setResourceURI(resourceDescriptor.getResourceURI());
					reply.setResourceType(resourceDescriptor.getResourceType());
					resourceDescriptorHashtable.put(sessionID, resourceDescriptor);
					break;
				case INIT:				
					resourceDescriptor.init(this, message.getLifeCycle(), message.isIterate(), message.getResourceParameters());
					break;
				case GET_RESOURCE_STATE:
					reply.setResourceState(resourceDescriptor.getResourceState());
					break;
				case OPEN:
					resourceDescriptor.open(this, message.getResourceParameters());
					break;
				case CLOSE:
				    waitforOutputStreamToFinish(sessionID);
					resourceDescriptor.close(this, message.getResourceParameters());
					break;
				case RELEASE:
				    waitforOutputStreamToFinish(sessionID);
					resourceDescriptor.release(this, message.getResourceParameters());
					resourceDescriptorHashtable.remove(sessionID);
					break;
				case GET_CONTENT_METADATA:
					reply.setContentMetaData(resourceDescriptor.getContentMetaData(this, message.getResourceParameters()));
					break;
				case GET_ITERATION_METADATA:
					reply.setIterationMetaData(resourceDescriptor.getIterationMetaData(this, message.getResourceParameters()));
					break;
				case GET_INPUTSTREAM:
					capoConnection = new CapoConnection();
					request = new RemoteResourceRequest(capoConnection.getOutputStream(),capoConnection.getInputStream());
					request.setType(MessageType.GET_INPUTSTREAM);
					request.setSessionId(sessionID);
					request.send();
					threadedInputSreamReader = new ThreadedInputStreamReader(sessionID,resourceDescriptor.getInputStream(this, message.getResourceParameters()), capoConnection.getOutputStream());
					threadedInputSreamReader.start();				
					break;
				case GET_OUTPUTSTREAM:
					capoConnection = new CapoConnection();
					request = new RemoteResourceRequest(capoConnection.getOutputStream(),capoConnection.getInputStream());
					request.setType(MessageType.GET_OUTPUTSTREAM);
					request.setSessionId(sessionID);
					request.send();					
					threadedInputSreamReader = new ThreadedInputStreamReader(sessionID,capoConnection.getInputStream(),resourceDescriptor.getOutputStream(this, message.getResourceParameters()));
					threadedInputSreamReader.setCapoConnection(capoConnection);
					if (threadedInputStreamReaderHashtable.contains(sessionID))
					{
					    throw new Exception("Stream Read already exists! Somebody didn't close something!");
					}
					threadedInputStreamReaderHashtable.put(sessionID, threadedInputSreamReader);
					threadedInputSreamReader.start();				
					break;
				case ADD_RESOURCE_PARAMETERS:
					resourceDescriptor.addResourceParameters(this, message.getResourceParameters());
					break;
				case IS_STREAM_SUPPORETED_FORMAT:
					reply.setStreamSupportedFormat(resourceDescriptor.isSupportedStreamFormat(message.getStreamType(), message.getStreamFormat()));
					break;
				case STEP:
					reply.setStepSuccess(resourceDescriptor.next(this, message.getResourceParameters()));				
					break;
				case READ_XML:
					reply.setXMLElement(resourceDescriptor.readXML(this, message.getResourceParameters()));				
					break;
				case WRITE_XML:
					resourceDescriptor.writeXML(this, message.getXMLElement(), message.getResourceParameters());				
					break;
				case READ_BLOCK:
					reply.setBlock(resourceDescriptor.readBlock(this, message.getResourceParameters()));				
					break;
				case WRITE_BLOCK:
					resourceDescriptor.writeBlock(this, message.getBlock(), message.getResourceParameters());				
					break;
				case PROCESS_INPUT:
					resourceDescriptor.processInput(this, message.getResourceParameters());				
					break;
				case PROCESS_OUTPUT:
					resourceDescriptor.processOutput(this,  message.getResourceParameters());				
					break;
				case PERFORM_ACTION:
					reply.setActionResult(resourceDescriptor.performAction(this,  message.getAction(), message.getResourceParameters()));				
					break;
				case IS_SUPPORTED_ACTION:
					reply.setActionResult(resourceDescriptor.isSupportedAction(message.getAction()));				
					break;
				case GET_LIFCYCLE:
					reply.setLifeCycle(resourceDescriptor.getLifeCycle());					
					break;
				case GET_STREAM_STATE:
					reply.setStreamState(resourceDescriptor.getStreamState(message.getStreamType()));					
					break;
				case GET_SUPPORTED_STREAM_FORMATS:
					reply.setSupportedStreamFormats(resourceDescriptor.getSupportedStreamFormats(message.getStreamType()));				
					break;
				case GET_SUPPORTED_STREAM_TYPES:
					reply.setSupportedStreamTypes(resourceDescriptor.getSupportedStreamTypes());				
					break;
				case IS_SUPPORTED_STREAM_TYPE:
					reply.setSupportedStreamType(resourceDescriptor.isSupportedStreamType(message.getStreamType()));			
					break;
				default:
					throw new UnsupportedOperationException(message.getMessageType().toString());
			}
		} 
		catch (Exception exception)
		{
		    exception.printStackTrace();
			reply.setMessageType(MessageType.FAILURE);
			reply.setException(exception);
		}
		reply.prepareResponse();
		//XPath.dumpNode(reply.getResponseDocument(), System.out);
		xmlServerResponse.writeDocument(reply.getResponseDocument());

	}
	
	private void waitforOutputStreamToFinish(String sessionID) throws Exception
	{
	    ThreadedInputStreamReader threadedInputStreamReader = threadedInputStreamReaderHashtable.get(sessionID); 
	    if (threadedInputStreamReader != null)
	    {
		int loopCount = 0;
		int waitTime = 100;
		int timeoutSeconds = CapoApplication.getConfiguration().getIntValue(Preferences.OUTPUT_STREAM_TIMEOUT);
		while(threadedInputStreamReader.isFinished() == false)
		{
		    loopCount++;
		    Thread.sleep(waitTime);
		    if (loopCount * waitTime > timeoutSeconds * 1000)
		    {
			CapoApplication.logger.log(Level.WARNING, "OutputStream timeout");
			break;
		    }
		}
		threadedInputStreamReaderHashtable.remove(sessionID);
	    }
	}

	private class ThreadedInputStreamReader extends Thread
	{

		private InputStream inputStream;
		private OutputStream outputStream;
		private String sessionID;
		private boolean finished = false;
		@SuppressWarnings("unused")
		private CapoConnection capoConnection;
		public ThreadedInputStreamReader(String sessionID,InputStream inputStream, OutputStream outputStream)
		{
			super("ThreadedInputStreamReader SID:"+sessionID);
			this.inputStream = inputStream;
			this.outputStream = outputStream;
			this.sessionID = sessionID;
		}

		/**
		 * This is here to keep connection from being garbage collected while still active.
		 * @param capoConnection
		 */
		public void setCapoConnection(CapoConnection capoConnection)
		{
		    this.capoConnection = capoConnection;
		    
		}

		public boolean isFinished()
		{
		    return finished;
		}

		@Override
		public void run()
		{
			try
			{
				CapoApplication.logger.log(Level.FINE, "Preparing bytes to remote resource descriptor");
				long read = StreamUtil.readInputStreamIntoOutputStream(inputStream, outputStream);
				outputStream.flush();
				outputStream.close();
				finished = true;
				this.capoConnection = null;
				CapoApplication.logger.log(Level.FINE, "Sent "+read+" bytes to remote resource descriptor");
			} 
			catch (Exception exception)
			{	
			    	ResourceDescriptor currentResourceDescriptor = resourceDescriptorHashtable.get(sessionID);
			    	String uri = null;
			    	if (currentResourceDescriptor != null)
			    	{
			    	    uri = currentResourceDescriptor.getResourceURI();
			    	}
				CapoApplication.logger.log(Level.SEVERE, "Error sending bytes to remote resource descriptor: "+uri+" SID:"+sessionID,exception);
			}
		}

	}

	@Override
	public String getVarValue(String varName)
	{
		try
		{
			CapoConnection capoConnection = new CapoConnection();
			RemoteResourceRequest request = new RemoteResourceRequest(capoConnection.getOutputStream(),capoConnection.getInputStream());
			request.setType(MessageType.GET_VAR_VALUE);
			request.setVarName(varName);
			request.setSessionId(sessionID);
			request.send();
			capoConnection.getInputStream();
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			StreamUtil.readInputStreamIntoOutputStream(capoConnection.getInputStream(), byteArrayOutputStream);
			return new String(byteArrayOutputStream.toByteArray()); 
		} catch (Exception exception)
		{
			exception.printStackTrace();
			return null;
		}
	}


}
