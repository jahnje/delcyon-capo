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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.delcyon.capo.protocol.server.AbstractResponse;
import com.delcyon.capo.resourcemanager.ResourceParameter;
import com.delcyon.capo.resourcemanager.ResourceType;
import com.delcyon.capo.resourcemanager.ResourceURI;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.Action;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.LifeCycle;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.State;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.StreamFormat;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.StreamType;
import com.delcyon.capo.resourcemanager.types.ContentMetaData;
import com.delcyon.capo.server.CapoServer;
import com.delcyon.capo.util.XMLSerializer;
import com.delcyon.capo.xml.XPath;
import com.delcyon.capo.xml.cdom.CElement;

/**
 * @author jeremiah
 *
 */
public class RemoteResourceDescriptorMessage extends AbstractResponse
{
	public RemoteResourceDescriptorMessage() throws Exception
	{
		super(CapoServer.getDefaultDocument("remoteResourceResponse.xml"));		
	}

	public RemoteResourceDescriptorMessage(Document replyDocument) throws Exception
	{
		super(replyDocument);
		while(replyDocument.getDocumentElement().getLocalName().equals("RemoteResourceDescriptorMessage") == false)
		{
		    replyDocument = XPath.unwrapDocument(replyDocument, true);
		}
		XMLSerializer xmlSerializer = new XMLSerializer();
		xmlSerializer.marshall((Element) replyDocument.getDocumentElement(), this);
	}
	
	public enum MessageType
	{
		SUCCESS,
		FAILURE,
		SETUP,
		INIT, 
		GET_RESOURCE_STATE, 
		OPEN, 
		GET_CONTENT_METADATA, 
		GET_INPUTSTREAM, 
		ADD_RESOURCE_PARAMETERS, 
		IS_STREAM_SUPPORETED_FORMAT, 
		CLOSE, GET_OUTPUTSTREAM, 
		GET_VAR_VALUE, 		 
		STEP, 
		READ_XML, 
		GET_LIFCYCLE, 
		WRITE_XML, 
		READ_BLOCK, 
		WRITE_BLOCK, 
		PROCESS_OUTPUT, 
		PROCESS_INPUT, 
		PERFORM_ACTION, 
		RELEASE, 
		GET_STREAM_STATE, 
		GET_SUPPORTED_STREAM_FORMATS, 
		GET_SUPPORTED_STREAM_TYPES, 
		IS_SUPPORTED_ACTION, 
		IS_SUPPORTED_STREAM_TYPE,
		RESET,
		ADVANCE_STATE,
		GET_OUTPUT_METADATA,
		GET_RESOURCE_METADATA
	}

	private MessageType messageType;
	private ResourceURI resourceURI;
	private ResourceType resourceType;
	private Boolean iterate;
	private LifeCycle lifeCycle;
	private ResourceParameter[] resourceParameters;
	private State resourceState;
	private ContentMetaData contentMetaData;
	private StreamFormat streamFormat;
	private StreamType streamType;
	private Boolean isStreamSupportedFormat;	
	private Boolean stepSuccess;
	private CElement xmlElement;
	private byte[] block;
	private Action action;
	private Boolean actionResult;
	private State streamState;
	private StreamFormat[] supportedStreamFormats;
	private StreamType[] supportedStreamTypes;
	private Boolean isSupportedStreamType;
	private Exception exception;
	private State previousState;
    private State desiredState;
    private ContentMetaData outputMetaData;
    private ContentMetaData resourceMetaData;
    private String value;
    private String varName;	
	

	public void setMessageType(MessageType messageType)
	{
		this.messageType = messageType;		
	}
	
	public MessageType getMessageType()
	{
		return messageType;
	}

	public void prepareResponse() throws Exception
	{
		appendElement(XMLSerializer.export(getResponseDocument(), this));
		
	}

	public void setResourceURI(ResourceURI resourceURI)
	{
		this.resourceURI = resourceURI;		
	}
	
	public ResourceURI getResourceURI()
	{
		return resourceURI;
	}

	public ResourceType getResourceType()
	{
		return this.resourceType;
	}

	public void setResourceType(ResourceType resourceType)
	{
		this.resourceType = resourceType;
		
	}

	public void setIterate(boolean iterate)
	{
		this.iterate = iterate;		
	}
	
	public boolean isIterate()
	{
		return iterate;
	}

	public void setLifeCycle(LifeCycle lifeCycle)
	{
		this.lifeCycle = lifeCycle;
		
	}
	
	public LifeCycle getLifeCycle()
	{
		return lifeCycle;
	}

	public void setResourceParameters(ResourceParameter... resourceParameters)
	{
		this.resourceParameters = resourceParameters;		
	}
	
	public ResourceParameter[] getResourceParameters()
	{
		if (resourceParameters == null)
		{
			return new ResourceParameter[0];
		}
		else
		{
			return resourceParameters;
		}
	}

	public State getResourceState()
	{
		
		return this.resourceState;
	}
	
	public void setResourceState(State resourceState)
	{
		this.resourceState = resourceState;
	}

	

	public void setStreamFormat(StreamFormat streamFormat)
	{
		this.streamFormat = streamFormat;		
	}
	
	public StreamFormat getStreamFormat()
	{
		return streamFormat;
	}

	public void setStreamType(StreamType streamType)
	{
		this.streamType = streamType;		
	}
	
	public StreamType getStreamType()
	{
		return streamType;
	}

	public boolean isSupportedStreamFormat()
	{		
		return this.isStreamSupportedFormat;
	}
	
	public void setStreamSupportedFormat(boolean isStreamSupportedFormat)
	{
		this.isStreamSupportedFormat = isStreamSupportedFormat;
	}

	public void setResourceMetaData(ContentMetaData resourceMetaData)
	{
		this.resourceMetaData = resourceMetaData;		
	}
	
	public ContentMetaData getResourceMetaData()
	{
		return resourceMetaData;
	}
	
	public ContentMetaData getContentMetaData()
    {
        return this.contentMetaData;
    }
    
    public void setContentMetaData(ContentMetaData contentMetaData)
    {
        this.contentMetaData = contentMetaData;
    }
	
	public void setOutputMetaData(ContentMetaData outputMetaData)
    {
        this.outputMetaData = outputMetaData;
    }
	
	public ContentMetaData getOutputMetaData()
	{
	    return this.outputMetaData;
	}
	
	public void setStepSuccess(boolean stepSuccess)
	{
		this.stepSuccess = stepSuccess;
		
	}
	
	public boolean isStepSuccess()
	{
		return stepSuccess;
	}

	public CElement getXMLElement()
	{
		return this.xmlElement;
	}
	
	public void setXMLElement(CElement xmlElement)
	{
		this.xmlElement = xmlElement;
	}

	public void setBlock(byte[] block)
	{
		this.block = block;		
	}
	
	public byte[] getBlock()
	{
		return block;
	}

	public void setAction(Action action)
	{
		this.action = action;		
	}
	
	public Action getAction()
	{
		return action;
	}
	
	public void setActionResult(boolean actionResult)
	{
		this.actionResult = actionResult;
	}
	
	public boolean getActionResult()
	{
		return actionResult;
	}

	public State getStreamState()
	{
		return this.streamState;
	}
	
	public void setStreamState(State streamState)
	{
		this.streamState = streamState;
	}

	public StreamFormat[] getSupportedStreamFormats()
	{
		return this.supportedStreamFormats;
	}
	
	public void setSupportedStreamFormats(StreamFormat[] supportedStreamFormats)
	{
		this.supportedStreamFormats = supportedStreamFormats;
	}

	public StreamType[] getSupportedStreamTypes()
	{
		return this.supportedStreamTypes;
	}
	
	public void setSupportedStreamTypes(StreamType[] supportedStreamTypes)
	{
		this.supportedStreamTypes = supportedStreamTypes;
	}

	public boolean isSupportedStreamType()
	{
		return this.isSupportedStreamType;
	}
	
	public void setSupportedStreamType(boolean isSupportedStreamType)
	{
		this.isSupportedStreamType = isSupportedStreamType;
	}

	public void setException(Exception exception)
	{
		this.exception = exception;		
	}
	public Exception getException()
	{
		return exception;
	}

	public void setPreviousState(State previousState)
	{
		this.previousState = previousState;		
	}
	
	public State getPreviousState()
	{
		return previousState;
	}

    public void setDesiredState(State desiredState)
    {
        this.desiredState = desiredState;        
    }
	
    public State getDesiredState()
    {
        return desiredState;
    }

    public void setValue(String value)
    {
       this.value = value;        
    }

    public String getValue()
    {
        return value;
    }

    public String getVarName()
    {
        return this.varName;
    }

    public void setVarName(String varName)
    {
        this.varName = varName;
    }
}
