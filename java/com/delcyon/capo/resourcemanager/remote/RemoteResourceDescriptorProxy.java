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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.ControlElement;
import com.delcyon.capo.controller.VariableContainer;
import com.delcyon.capo.controller.server.ControllerClientRequestProcessor;
import com.delcyon.capo.protocol.server.ClientRequest;
import com.delcyon.capo.protocol.server.ClientRequestProcessor;
import com.delcyon.capo.protocol.server.ClientRequestProcessorSessionManager;
import com.delcyon.capo.protocol.server.ClientRequestXMLProcessor;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceParameter;
import com.delcyon.capo.resourcemanager.ResourceType;
import com.delcyon.capo.resourcemanager.remote.RemoteResourceDescriptorMessage.MessageType;
import com.delcyon.capo.resourcemanager.types.ContentMetaData;

/**
 * @author jeremiah
 *
 */
public class RemoteResourceDescriptorProxy  implements ResourceDescriptor,ClientRequestProcessor
{

	private ControllerClientRequestProcessor controllerClientRequestProcessor;
	private String lock = "lock";
	private String inputStreamLock = "inputStreamLock";
	private String outputStreamLock = "outputStreamLock";
	private String sessionID = ClientRequestProcessorSessionManager.generateSessionID();
	private VariableContainer variableContainer;
	private InputStream inputStream;
	private OutputStream outputStream;
	private String resourceURI = null;
	private ResourceType resourceType;
	private LifeCycle lifeCycle;
	public RemoteResourceDescriptorProxy(ControllerClientRequestProcessor controllerClientRequestProcessor)
	{
		this.controllerClientRequestProcessor = controllerClientRequestProcessor;
		ClientRequestProcessorSessionManager.registerClientRequestProcessor(this,sessionID);
	}
	
	@Override
	public void setup(ResourceType resourceType, String resourceURI) throws Exception
	{
		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();
		this.resourceURI = resourceURI;
		this.resourceType = resourceType;
		message.setMessageType(MessageType.SETUP);
		message.setSessionID(sessionID);
		message.setResourceURI(resourceURI);		
		message = sendResponse(message, MessageType.SETUP,false);
		this.resourceURI = message.getResourceURI();
		this.resourceType = message.getResourceType();
	}
	
	@Override
	public void init(VariableContainer variableContainer, LifeCycle lifeCycle, boolean iterate, ResourceParameter... resourceParameters) throws Exception
	{
		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();
		this.variableContainer = variableContainer;
		message.setIterate(iterate);
		message.setLifeCycle(lifeCycle);
		message.setResourceParameters(resourceParameters);		
		message = sendResponse(message, MessageType.INIT,false);			
	}
	
	@Override
	public State getResourceState() throws Exception
	{
		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();		
		message = sendResponse(message, MessageType.GET_RESOURCE_STATE,false);
		return message.getResourceState();
	}
	
	@Override
	public void open(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		this.variableContainer = variableContainer;
		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();
		message.setResourceParameters(resourceParameters);		
		message = sendResponse(message, MessageType.OPEN,false);		
	}
	
	@Override
	public ContentMetaData getContentMetaData(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		this.variableContainer = variableContainer;
		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();
		message.setResourceParameters(resourceParameters);		
		message = sendResponse(message, MessageType.GET_CONTENT_METADATA,false);
		return message.getContentMetaData();
	}
	
	@Override
	public ContentMetaData getIterationMetaData(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		this.variableContainer = variableContainer;
		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();
		message.setResourceParameters(resourceParameters);		
		message = sendResponse(message, MessageType.GET_ITERATION_METADATA,false);
		return message.getIterationMetaData();
	}
	
	@Override
	public ResourceDescriptor getChildResourceDescriptor(ControlElement callingControlElement, String relativeURI) throws Exception
	{
	    ContentMetaData contentMetaData = getContentMetaData(null);
        if ( contentMetaData.isContainer() == true)
        {
            List<ContentMetaData> childContentMetaDataList = getContentMetaData(null).getContainedResources();
            for (ContentMetaData childContentMetaData : childContentMetaDataList)
            {
                if(childContentMetaData.getResourceURI().endsWith(relativeURI))
                {
                    return CapoApplication.getDataManager().getResourceDescriptor(callingControlElement, "remote:"+childContentMetaData.getResourceURI());
                }
            }
            return CapoApplication.getDataManager().getResourceDescriptor(callingControlElement, "remote:"+getResourceURI()+"/"+relativeURI);
        }
        else
        {
            return null;
        }
	}
	
	@Override
	public InputStream getInputStream(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		//unlock any existing threads
		synchronized (inputStreamLock)
		{
			inputStreamLock.notify();
		}
		this.variableContainer = variableContainer;
		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();
		message.setResourceParameters(resourceParameters);
		
		message = sendResponse(message, MessageType.GET_INPUTSTREAM,true);
		return this.inputStream;
	}
	@Override
	public OutputStream getOutputStream(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		//unlock any existing threads
		synchronized (outputStreamLock)
		{
			outputStreamLock.notify();
		}
		this.variableContainer = variableContainer;
		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();
		message.setResourceParameters(resourceParameters);
		
		message = sendResponse(message, MessageType.GET_OUTPUTSTREAM,true);
		return this.outputStream;
	}

	@Override
	public void addResourceParameters(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		this.variableContainer = variableContainer;
		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();
		message.setResourceParameters(resourceParameters);		
		message = sendResponse(message, MessageType.ADD_RESOURCE_PARAMETERS,false);
		
	}
	
	@Override
	public boolean isSupportedStreamFormat(StreamType streamType, StreamFormat streamFormat) throws Exception
	{		
		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();
		message.setStreamType(streamType);
		message.setStreamFormat(streamFormat);
		message = sendResponse(message, MessageType.IS_STREAM_SUPPORETED_FORMAT,false);
		return message.isSupportedStreamFormat();
	}
	
	@Override
	public void close(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		
		this.variableContainer = variableContainer;
		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();
		message.setResourceParameters(resourceParameters);		
		message = sendResponse(message, MessageType.CLOSE,false);
		
		synchronized (inputStreamLock)
		{
			this.inputStreamLock.notify();	
		}
		synchronized (outputStreamLock)
		{
			this.outputStreamLock.notify();	
		}
	}
	
	@Override
	public void release(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		
		this.variableContainer = variableContainer;
		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();
		message.setResourceParameters(resourceParameters);		
		message = sendResponse(message, MessageType.RELEASE,false);
		synchronized (inputStreamLock)
		{
			this.inputStreamLock.notify();	
		}
		synchronized (outputStreamLock)
		{
			this.outputStreamLock.notify();	
		}
		ClientRequestProcessorSessionManager.removeClientRequestProcessor(getSessionId());
	}
	
	@Override
	public boolean next(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		this.variableContainer = variableContainer;
		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();
		message.setResourceParameters(resourceParameters);		
		message = sendResponse(message, MessageType.STEP,false);
		return message.isStepSuccess();
	}
	
	@Override
	public Element readXML(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		this.variableContainer = variableContainer;
		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();
		message.setResourceParameters(resourceParameters);		
		message = sendResponse(message, MessageType.READ_XML,false);
		return message.getXMLElement();
	}
	
	@Override
	public void writeXML(VariableContainer variableContainer, Element element, ResourceParameter... resourceParameters) throws Exception
	{
		this.variableContainer = variableContainer;
		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();
		message.setResourceParameters(resourceParameters);
		message.setXMLElement(element);
		message = sendResponse(message, MessageType.WRITE_XML,false);
	}
	
	@Override
	public byte[] readBlock(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		this.variableContainer = variableContainer;
		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();
		message.setResourceParameters(resourceParameters);		
		message = sendResponse(message, MessageType.READ_BLOCK,false);
		return message.getBlock();
	}

	@Override
	public void writeBlock(VariableContainer variableContainer, byte[] block, ResourceParameter... resourceParameters) throws Exception
	{
		this.variableContainer = variableContainer;
		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();
		message.setResourceParameters(resourceParameters);
		message.setBlock(block);
		message = sendResponse(message, MessageType.WRITE_BLOCK,false);
		
	}
	
	@Override
	public void processInput(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		this.variableContainer = variableContainer;
		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();
		message.setResourceParameters(resourceParameters);		
		message = sendResponse(message, MessageType.PROCESS_INPUT,false);		
	}

	@Override
	public void processOutput(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		this.variableContainer = variableContainer;
		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();
		message.setResourceParameters(resourceParameters);		
		message = sendResponse(message, MessageType.PROCESS_OUTPUT,false);		
	}
	
	@Override
	public boolean performAction(VariableContainer variableContainer, Action action, ResourceParameter... resourceParameters) throws Exception
	{
		this.variableContainer = variableContainer;
		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();
		message.setResourceParameters(resourceParameters);
		message.setAction(action);
		message = sendResponse(message, MessageType.PERFORM_ACTION,false);
		return message.getActionResult();
	}

	
	@Override
	public LifeCycle getLifeCycle() throws Exception
	{
		if (this.lifeCycle == null)
		{
			RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();		
			message = sendResponse(message, MessageType.GET_LIFCYCLE,false);
			this.lifeCycle = message.getLifeCycle();
		}
		return this.lifeCycle;
	}
	
	@Override
	public State getStreamState(StreamType streamType) throws Exception
	{		
		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();		
		message.setStreamType(streamType);
		message = sendResponse(message, MessageType.GET_STREAM_STATE,false);
		return message.getStreamState();
	}

	@Override
	public StreamFormat[] getSupportedStreamFormats(StreamType streamType) throws Exception
	{		
		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();		
		message.setStreamType(streamType);
		message = sendResponse(message, MessageType.GET_SUPPORTED_STREAM_FORMATS,false);
		return message.getSupportedStreamFormats();
	}

	@Override
	public StreamType[] getSupportedStreamTypes() throws Exception
	{
		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();
		message = sendResponse(message, MessageType.GET_SUPPORTED_STREAM_TYPES,false);
		return message.getSupportedStreamTypes();
	}

	@Override
	public boolean isSupportedAction(Action action) throws Exception
	{		
		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();		
		message.setAction(action);
		message = sendResponse(message, MessageType.IS_SUPPORTED_ACTION,false);
		return message.getActionResult();
	}

	@Override
	public boolean isSupportedStreamType(StreamType streamType) throws Exception
	{		
		RemoteResourceDescriptorMessage message = new RemoteResourceDescriptorMessage();		
		message.setStreamType(streamType);
		message = sendResponse(message, MessageType.IS_SUPPORTED_STREAM_TYPE,false);
		return message.isSupportedStreamType();
	}
	
	//===================CACHED RESULTS======================
	@Override
	public ResourceType getResourceType()
	{
		return this.resourceType;
	}

	@Override
	public String getResourceURI()
	{
		return this.resourceURI;
	}

	@Override
	public void setResourceURI(String resourceURI)
	{
		this.resourceURI = resourceURI;
		
	}

	/** returns last piece of URI **/
    @Override
    public String getLocalName()
    {
        String[] splitURI = this.resourceURI.split("/");
        return splitURI[splitURI.length-1];
    }
	
	@Override
	public boolean isRemoteResource()
	{	
		return true;
	}
	
	//===================MESSAGING===========================
	
	private RemoteResourceDescriptorMessage sendResponse(RemoteResourceDescriptorMessage message, MessageType messageType,boolean needLock) throws Exception
	{
		message.setSessionID(sessionID);
		message.setMessageType(messageType);
		message.prepareResponse();
		
		controllerClientRequestProcessor.getClientRequestXMLProcessor().writeResponse(message);
		if (needLock)
		{
			synchronized (lock)
			{
				lock.wait(30000);	
			}
		}
		//wait for a message from the client
		Document replyDocument = controllerClientRequestProcessor.readNextDocument();
		
		message = new RemoteResourceDescriptorMessage(replyDocument);
		if (message.getMessageType() == MessageType.FAILURE)
		{
			if (message.getException() != null)
			{
				throw message.getException();
			}
		}
		return message;
	}
	
	@Override
	public String getSessionId()
	{
		return this.sessionID;
	}
	
	@Override
	public void process(ClientRequest clientRequest) throws Exception
	{		
		MessageType messageType = RemoteResourceRequest.getType(clientRequest);
		
		//process the request
		boolean useLock = false;
		switch (messageType)
		{
			case GET_INPUTSTREAM:
				this.inputStream = clientRequest.getInputStream();
				useLock = true;
				break;
			case GET_OUTPUTSTREAM:				
				this.outputStream = clientRequest.getOutputStream();
				useLock = true;
				break;
			case GET_VAR_VALUE:
			    processVarRequest(clientRequest);
				break;
			default:
				throw new UnsupportedOperationException(messageType.toString());
		}
		//notify the other thread the the request has been processed
		if (useLock == true)
		{
			synchronized (lock)
			{
				lock.notify();			
			}
		}
		//see if we need to wait until a lock is released, so any streams don't get closed
		switch (messageType)
		{
			case GET_INPUTSTREAM:
				synchronized (inputStreamLock)
				{
					this.inputStreamLock.wait();	
				}				
				break;
			case GET_OUTPUTSTREAM:
				synchronized (outputStreamLock)
				{
					this.outputStreamLock.wait();	
				}				
				break;

		}
			
	}

	private void processVarRequest(ClientRequest clientRequest) throws Exception
    {
	    String varName = RemoteResourceRequest.getVarName(clientRequest);
	    while(true)
        {
	        if (variableContainer != null)
	        {
	            String value = variableContainer.getVarValue(varName);
	            if (value != null)
	            {
	                clientRequest.getOutputStream().write(value.getBytes());
	            }
	            clientRequest.getOutputStream().write(0);
	            clientRequest.getOutputStream().flush();
	        }
	        Document nextDocument = clientRequest.getXmlStreamProcessor().readNextDocument();
	        if (RemoteResourceRequest.getType(nextDocument) != MessageType.GET_VAR_VALUE)
	        {	            
	            break;
	        }
	        else
	        {
	            varName = RemoteResourceRequest.getVarName(nextDocument);
	        }
        }
    }

    @Override
	public void init(ClientRequestXMLProcessor clientRequestXMLProcessor, String sessionID, HashMap<String, String> sessionHashMap,String requestName) throws Exception
	{
		throw new UnsupportedOperationException();		
	}

	@Override
	public Document readNextDocument() throws Exception
	{
		throw new UnsupportedOperationException();
	}
}
