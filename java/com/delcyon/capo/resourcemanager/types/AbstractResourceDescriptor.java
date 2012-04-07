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
package com.delcyon.capo.resourcemanager.types;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.Vector;
import java.util.logging.Level;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.ControlElement;
import com.delcyon.capo.controller.VariableContainer;
import com.delcyon.capo.datastream.NullOutputStream;
import com.delcyon.capo.datastream.StreamEventFilterOutputStream;
import com.delcyon.capo.datastream.StreamEventListener;
import com.delcyon.capo.datastream.StreamUtil;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceParameter;
import com.delcyon.capo.resourcemanager.ResourceParameter.EvaluationContext;
import com.delcyon.capo.resourcemanager.ResourceParameter.Source;
import com.delcyon.capo.resourcemanager.ResourceType;
import com.delcyon.capo.server.CapoServer;
import com.delcyon.capo.util.ReflectionUtility;
import com.delcyon.capo.xml.XPath;

/**
 * @author jeremiah
 * TODO verify required parameters and values
 */
public abstract class AbstractResourceDescriptor implements ResourceDescriptor
{
	private HashMap<String, String> declaredParameterHashMap = new HashMap<String, String>(); //NOW eval context in ResourceElement
	private HashMap<String, String> delayedParameterHashMap = new HashMap<String, String>(); //
	private HashMap<String, String> contextParameterHashMap = new HashMap<String, String>();
	private HashMap<StreamType, State> streamStateHashMap = new HashMap<StreamType, State>();
	private String resourceURI = null; 
	private ResourceParameter[] initialResourceParameters = null;
	
	
	private LifeCycle lifeCycle;
	private State resourceState = State.NONE;
	private boolean isIterating = false;
	private ResourceType resourceType;
	private VariableContainer declaringVariableContainer;
	private OutputStreamTranslater outputStreamTranslater;
	
	
	@Override
	public void setup(ResourceType resourceType, String resourceURI) throws Exception
	{
		this.resourceType = resourceType;
		this.resourceURI = resourceURI;
		this.lifeCycle = resourceType.getDefaultLifeCycle();
		
	}
	
	@Override
	public void init(VariableContainer variableContainer,LifeCycle lifeCycle, boolean iterate, ResourceParameter... resourceParameters) throws Exception
	{		
		
			this.declaringVariableContainer = variableContainer;
			//override life cycle
			if (lifeCycle == null)
			{
				this.lifeCycle = resourceType.getDefaultLifeCycle();
			}

			if (iterate == true)
			{
				if (resourceType.isIterable() == false)
				{
					CapoServer.logger.log(Level.WARNING,"Resource '"+resourceType.getName()+" in not stepable, will default to returning all data");
				}
				else
				{
					isIterating = true;
				}
			}
		

		//process resource parameters
			initialResourceParameters = resourceParameters;

			for (ResourceParameter resourceParameter : initialResourceParameters)
			{
				if (resourceParameter.getEvaluationContext() == EvaluationContext.NOW)
				{
					declaredParameterHashMap.put(resourceParameter.getName(), processVars(variableContainer, resourceParameter.getValue()));
				}
				else if (resourceParameter.getEvaluationContext() == EvaluationContext.DELAYED)
				{					
					delayedParameterHashMap.put(resourceParameter.getName(), resourceParameter.getValue());
				}
			}
		this.resourceState = State.INITIALIZED;
	}
		

	@Override
	public void open(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{
		if (resourceState == State.NONE)
		{
			init(variableContainer,null,false,resourceParameters);
		}
		
		if (resourceState == State.OPEN || resourceState == State.STEPPING)
		{
			return ; 
		}

		addResourceParameters(variableContainer,resourceParameters);


		this.resourceState = State.OPEN;
 	}
	
	
	@Override
	public void close(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{
		Vector<ResourceParameter> tempResourceParameterVector = new Vector<ResourceParameter>();

		for (ResourceParameter resourceParameter : resourceParameters)
		{
			if(resourceParameter.getSource() == Source.DECLARATION)
			{
				tempResourceParameterVector.add(resourceParameter);
			}
		}
		if (outputStreamTranslater != null)
		{
			outputStreamTranslater.close();
			outputStreamTranslater = null;
		}
		if (isIterating() == false)
		{
			resourceState = State.CLOSED;
		}
		
	}
	//TODO
	/*
	 *  cleanup should remove call parameters
	 *  depending on lifeCycle destroy may want to be called 
	 *  destroy should call close depending
	 *  destroy should only be called when a resourceElement falls out of scope.
	 */
	@Override
	public void release(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{
		//do nothing, children need to overrride this is they want something to happen
		
	}
	


	public String getResourceURI()
	{
		return resourceURI;
	}
	public void setResourceURI(String resourceURI)
	{
		this.resourceURI = resourceURI;
	}
	
	@Override
	public ResourceType getResourceType()
	{
		return this.resourceType;
	}
	
	protected void setResourceType(ResourceType resourceType)
	{
		this.resourceType = resourceType;
	}
	
	public VariableContainer getDeclaringVariableContainer()
	{
		return declaringVariableContainer;
		
	}
	
	
	@Override
	public void addResourceParameters(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		for (ResourceParameter resourceParameter : resourceParameters)
		{
			contextParameterHashMap.put(resourceParameter.getName(), processVars(variableContainer, resourceParameter.getValue())); //this is not right
		}
	}
	
	
	
	
	
	
	public String processVars(VariableContainer variableContainer, String varString)
	{
		StringBuffer stringBuffer = new StringBuffer(varString);
		processVars(variableContainer,stringBuffer);
		return stringBuffer.toString();
	}
	
	/**
	 * Check String for variables and replace them with the value of the var
	 * @param varStringBuffer
	 */
	private void processVars(VariableContainer variableContainer, StringBuffer varStringBuffer)
	{
	    while (varStringBuffer != null && varStringBuffer.toString().matches(".*\\$\\{.+}.*"))
        {
           
            CapoServer.logger.log(Level.FINE,"found var in '"+varStringBuffer+"'");
            Stack<StringBuffer> stack = new Stack<StringBuffer>();
            StringBuffer currentStringBuffer = new StringBuffer();
            for (int index = 0; index < varStringBuffer.length(); index++) 
            {
                 
                if (varStringBuffer.charAt(index) == '$' && varStringBuffer.charAt(index+1) == '{')
                {
                    stack.push(currentStringBuffer);
                    currentStringBuffer = new StringBuffer();
                    currentStringBuffer.append(varStringBuffer.charAt(index));
                }
                else if (varStringBuffer.charAt(index) == '}' && varStringBuffer.charAt(index-1) != '\\' && stack.empty() == false)
                {
                    //pop, and evaluate
                    currentStringBuffer.append(varStringBuffer.charAt(index));
                    String varName = currentStringBuffer.toString().replaceFirst(".*\\$\\{(.+)}.*", "$1");
                    String value = getVarValue(variableContainer,varName);
                    if (value == null) 
                    {
                        value = "";//TODO make this configurable to null,exception,or empty
                        CapoServer.logger.log(Level.WARNING,"var '"+varName+"' not found replaced with empty string");
                    }
                    currentStringBuffer = stack.pop();
                    currentStringBuffer.append(value);
                }
                else
                {
                    currentStringBuffer.append(varStringBuffer.charAt(index));    
                }

                
            }
            
            varStringBuffer.replace(0, varStringBuffer.length(), currentStringBuffer.toString());
            
        }
        CapoServer.logger.log(Level.FINE,"final replacement =  '"+varStringBuffer+"'");
		
	}
	
	
	@SuppressWarnings("rawtypes")
    public String getVarValue(VariableContainer variableContainer, Enum varName)
	{
		return getVarValue(variableContainer, varName.toString());
	}
	
	/**
	 * check request
	 * check entries
	 * check variables
	 * @param varName
	 * @return
	 */
	public String getVarValue(VariableContainer variableContainer, String varName)
	{
		if (contextParameterHashMap.containsKey(varName))
		{
			return contextParameterHashMap.get(varName);
		}
		else if (delayedParameterHashMap.containsKey(varName))
		{			
			return processVars(variableContainer, delayedParameterHashMap.get(varName));
		}
		else if (declaredParameterHashMap.containsKey(varName))
		{
			return declaredParameterHashMap.get(varName);
		}
		else if (variableContainer != null && variableContainer.getVarValue(varName) != null)
		{
			return variableContainer.getVarValue(varName);
		}
		else 
		{
			return  CapoApplication.getVariableValue(varName);
		}
	}
	
	//END PARAMETER PROCESSING
	
	
	@Override
	public LifeCycle getLifeCycle()
	{
		return this.lifeCycle;
	}
	
	@Override
	public State getStreamState(StreamType streamType)
	{
		
		if (streamStateHashMap.containsKey(streamType))
		{
			return streamStateHashMap.get(streamType);
		}
		else
		{
			return State.NONE;
		}
	}
	
	@Override
	public State getResourceState() throws Exception
	{
		return resourceState;
	}
	
	protected void setResourceState(State state)
	{
		this.resourceState = state;
	}
	
	public void setStreamState(StreamType streamType, State state)
	{
		streamStateHashMap.put(streamType, state);
	}
	
	public boolean isIterating()
	{
		return isIterating;
	}
	
	@Override
	public String toString()
	{
		return ReflectionUtility.processToString(this);
	}

	@Override
	public boolean isSupportedStreamFormat(StreamType streamType, StreamFormat streamFormat) throws Exception
	{
		StreamType[] supporedtStreamTypes = getSupportedStreamTypes();
		for (StreamType supportedStreamType : supporedtStreamTypes)
		{
			if (supportedStreamType == streamType)
			{
				StreamFormat[] supportedStreamFormats = getSupportedStreamFormats(supportedStreamType);
				for (StreamFormat supportedSrteamFormat : supportedStreamFormats)
				{
					if (supportedSrteamFormat == streamFormat)
					{
						return true;
					}
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean isSupportedStreamType(StreamType streamType) throws Exception
	{
		StreamType[] supporedtStreamTypes = getSupportedStreamTypes();
		for (StreamType supportedStreamType : supporedtStreamTypes)
		{
			if (supportedStreamType == streamType)
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean isSupportedAction(Action action)
	{
		Action[] supportedActions = getSupportedActions();
		return Arrays.asList(supportedActions).contains(action);
		
	}
	
	@Override
	public boolean performAction(VariableContainer variableContainer,Action action,ResourceParameter... resourceParameters) throws Exception
	{
		if (isSupportedAction(action) == false)
		{
			throw new UnsupportedOperationException();
		}
		else
		{
			return this.performAction(variableContainer,action,resourceParameters);
		}
	}
	
	protected abstract Action[] getSupportedActions();

	@Override //TODO
	public InputStream getInputStream(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{		
		StreamFormat streamFormat = getSupportedStreamFormats(StreamType.INPUT)[0];
		ByteArrayInputStream byteArrayInputStream = null;
		if(streamFormat == StreamFormat.XML_BLOCK)
		{			
			Element dataElement = readXML(variableContainer, resourceParameters);
			Document tempDocument = CapoApplication.getDocumentBuilder().newDocument();
			tempDocument.appendChild(tempDocument.importNode(dataElement, true));
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			XPath.dumpNode(tempDocument.getDocumentElement(), byteArrayOutputStream);
			//XPath.dumpNode(tempDocument.getDocumentElement(), System.err);
			byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
		}
		else if(streamFormat == StreamFormat.PROCESS)
		{
			byteArrayInputStream = new ByteArrayInputStream(new byte[0]);			
		}
		else if(streamFormat == StreamFormat.BLOCK)
		{
			byteArrayInputStream = new ByteArrayInputStream(readBlock(variableContainer, resourceParameters));	
		}
		else
		{
			throw new IOException(this.getClass()+"doesn't support getOutputStream()");
		}
		return byteArrayInputStream;
	}
	
	@Override
	public OutputStream getOutputStream(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{
		StreamFormat streamFormat = getSupportedStreamFormats(StreamType.OUTPUT)[0];
		if (outputStreamTranslater != null)
		{
			return outputStreamTranslater.getOutputStream();
		}
		
		if(streamFormat == StreamFormat.XML_BLOCK)
		{			
			outputStreamTranslater = new OutputStreamTranslater(variableContainer,this, StreamFormat.XML_BLOCK, resourceParameters);
			return outputStreamTranslater.getOutputStream();
		}
		else if(streamFormat == StreamFormat.BLOCK)
		{			
			outputStreamTranslater = new OutputStreamTranslater(variableContainer,this, StreamFormat.BLOCK, resourceParameters);
			return outputStreamTranslater.getOutputStream();
		}
		else if(streamFormat == StreamFormat.PROCESS)
		{			
			outputStreamTranslater = new OutputStreamTranslater(variableContainer,this, StreamFormat.PROCESS, resourceParameters);
			return outputStreamTranslater.getOutputStream();
		}
		else
		{
			throw new IOException(this.getClass()+"doesn't support getOutputStream()");
		}
	}

	@Override
	public Element readXML(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		Document document = CapoApplication.getDocumentBuilder().newDocument();
		Element dataElement = document.createElement("Data");
		document.appendChild(dataElement);
		StreamFormat streamFormat = getSupportedStreamFormats(StreamType.INPUT)[0];
		if(streamFormat == StreamFormat.BLOCK)
		{			
			dataElement.setTextContent(new String(readBlock(variableContainer, resourceParameters)));
		}
		else if(streamFormat == StreamFormat.PROCESS)
		{			
			//skip this as we just want to return an empty element I think			
		}
		else if(streamFormat == StreamFormat.STREAM)
		{
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			StreamUtil.readInputStreamIntoOutputStream(getInputStream(variableContainer, resourceParameters), byteArrayOutputStream) ;
			dataElement.setTextContent(new String(byteArrayOutputStream.toByteArray()));			
		}
		else
		{
			throw new IOException(this.getClass()+"doesn't support getOutputStream()");
		}
		return dataElement;
	}
	
	@Override
	public void writeXML(VariableContainer variableContainer, Element element, ResourceParameter... resourceParameters) throws Exception
	{
		StreamFormat streamFormat = getSupportedStreamFormats(StreamType.OUTPUT)[0];
		if(streamFormat == StreamFormat.BLOCK)
		{	
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			XPath.dumpNode(element, byteArrayOutputStream);
			writeBlock(variableContainer, byteArrayOutputStream.toByteArray(), resourceParameters);	
		}
		else if(streamFormat == StreamFormat.PROCESS)
		{			
			processOutput(variableContainer, resourceParameters);			
		}
		else if(streamFormat == StreamFormat.STREAM)
		{			
			XPath.dumpNode(element, getOutputStream(variableContainer, resourceParameters));			
		}
		else
		{
			throw new IOException(this.getClass()+"doesn't support getOutputStream()");
		}
		
	}
	
	@Override
	public byte[] readBlock(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		
		StreamFormat streamFormat = getSupportedStreamFormats(StreamType.INPUT)[0];
		if(streamFormat == StreamFormat.XML_BLOCK)
		{			
			Element dataElement = readXML(variableContainer, resourceParameters);
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			XPath.dumpNode(dataElement, byteArrayOutputStream);
			return byteArrayOutputStream.toByteArray();
		}
		else if(streamFormat == StreamFormat.PROCESS)
		{			
			return new byte[0];		
		}
		else if(streamFormat == StreamFormat.STREAM)
		{
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			StreamUtil.readInputStreamIntoOutputStream(getInputStream(variableContainer, resourceParameters), byteArrayOutputStream) ;
			return byteArrayOutputStream.toByteArray();			
		}
		else
		{
			throw new IOException(this.getClass()+"doesn't support getOutputStream()");
		}
		
	}
	
	@Override
	public void writeBlock(VariableContainer variableContainer, byte[] block, ResourceParameter... resourceParameters) throws Exception
	{
		StreamFormat streamFormat = getSupportedStreamFormats(StreamType.OUTPUT)[0];
		if(streamFormat == StreamFormat.XML_BLOCK)
		{
			Document document = CapoApplication.getDocumentBuilder().newDocument();
			Element dataElement = document.createElement("Data");
			document.appendChild(dataElement);					
			dataElement.setTextContent(new String(block));			
			writeXML(variableContainer, dataElement, resourceParameters);
			
		}		
		else if(streamFormat == StreamFormat.PROCESS)
		{			
			processOutput(variableContainer, resourceParameters);
			
		}
		else if(streamFormat == StreamFormat.STREAM)
		{	
			OutputStream outputStream = getOutputStream(variableContainer, resourceParameters);
			outputStream.write(block);
			outputStream.close();
		}
		else
		{
			throw new IOException(this.getClass()+"doesn't support getOutputStream()");
		}
		
	}
	
	@Override
	public void processInput(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		StreamFormat streamFormat = getSupportedStreamFormats(StreamType.INPUT)[0];
		if(streamFormat == StreamFormat.XML_BLOCK)
		{
			readXML(variableContainer, resourceParameters);
			
		}
		else if (streamFormat == StreamFormat.BLOCK)
		{
			readBlock(variableContainer, resourceParameters);
		}
		else if(streamFormat == StreamFormat.STREAM)
		{	
			InputStream inputStream = getInputStream(variableContainer, resourceParameters);
			StreamUtil.readInputStreamIntoOutputStream(inputStream, new NullOutputStream());
		}
		else
		{
			throw new IOException(this.getClass()+"doesn't support getOutputStream()");
		}
		
	}
	
	@Override
	public void processOutput(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		StreamFormat streamFormat = getSupportedStreamFormats(StreamType.OUTPUT)[0];
		if(streamFormat == StreamFormat.XML_BLOCK)
		{
			Document document = CapoApplication.getDocumentBuilder().newDocument();
			Element dataElement = document.createElement("Data");
			writeXML(variableContainer, dataElement, resourceParameters);
			
		}
		else if (streamFormat == StreamFormat.BLOCK)
		{
			writeBlock(variableContainer, new byte[0], resourceParameters);
		}
		else if(streamFormat == StreamFormat.STREAM)
		{	
			OutputStream outputStream = getOutputStream(variableContainer, resourceParameters);
			outputStream.write(new byte[0]);
			outputStream.close();
		}
		else
		{
			throw new IOException(this.getClass()+"doesn't support getOutputStream()");
		}
		
	}
	
	private class OutputStreamTranslater implements StreamEventListener
	{
		
		private ByteArrayOutputStream byteArrayOutputStream;
		private StreamEventFilterOutputStream streamEventFilterOutputStream;
		private StreamFormat streamFormat;
		private ResourceDescriptor resourceDescriptor;
		private ResourceParameter[] resourceParameters;
		private VariableContainer variableContainer;
		

		public OutputStreamTranslater(VariableContainer variableContainer,ResourceDescriptor resourceDescriptor, StreamFormat streamFormat, ResourceParameter... resourceParameters)
		{
			byteArrayOutputStream = new ByteArrayOutputStream();
			streamEventFilterOutputStream = new StreamEventFilterOutputStream(byteArrayOutputStream);
			streamEventFilterOutputStream.addStreamEventListener(this);
			this.streamFormat = streamFormat;
			this.resourceDescriptor = resourceDescriptor;
			this.resourceParameters = resourceParameters;
			this.variableContainer = variableContainer;
		}
		
		public OutputStream getOutputStream()
		{
			return streamEventFilterOutputStream;
		}

		public void close()
		{
			byteArrayOutputStream.reset();
			streamEventFilterOutputStream.removeStreamEventListener(this);
		}
		
		@Override
		public void processStreamEvent(StreamEvent streamEvent) throws IOException
		{
			if (streamEvent == StreamEvent.CLOSED)
			{ 			
				CapoApplication.logger.fine(new String(byteArrayOutputStream.toByteArray()));
				//don't write empty blocks
				if (byteArrayOutputStream.size() == 0 && streamFormat != StreamFormat.PROCESS)
				{
					return;
				}
				try
				{
					if (streamFormat == StreamFormat.XML_BLOCK)
					{
						Document document = CapoApplication.getDocumentBuilder().parse(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
						resourceDescriptor.writeXML(variableContainer,document.getDocumentElement(),resourceParameters);
					}
					else if (streamFormat == StreamFormat.BLOCK)
					{
						resourceDescriptor.writeBlock(variableContainer,byteArrayOutputStream.toByteArray(),resourceParameters);
					}
					else if (streamFormat == StreamFormat.PROCESS)
					{
						resourceDescriptor.processOutput(variableContainer,resourceParameters);
					}
					else if (streamFormat == StreamFormat.STREAM)
					{
						OutputStream outputStream = resourceDescriptor.getOutputStream(variableContainer,resourceParameters);
						outputStream.write(byteArrayOutputStream.toByteArray());
						outputStream.close();
					}
					byteArrayOutputStream.reset();
				} 
				catch (Exception exception)
				{
					throw new IOException(exception);
				} 			
			}
			
		}
	}
	
	@Override
	public boolean next(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{		
		return false;
	}

	@Override
	public boolean isRemoteResource()
	{	
		return false;
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
	                return CapoApplication.getDataManager().getResourceDescriptor(callingControlElement, childContentMetaData.getResourceURI());
	            }
	        }
	        return CapoApplication.getDataManager().getResourceDescriptor(callingControlElement, getResourceURI()+"/"+relativeURI);
	    }
	    else
	    {
	        return null;
	    }
	}
	
}
