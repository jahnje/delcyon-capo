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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.Configuration.PREFERENCE;
import com.delcyon.capo.controller.VariableContainer;
import com.delcyon.capo.controller.elements.StepElement;
import com.delcyon.capo.datastream.StreamUtil;
import com.delcyon.capo.resourcemanager.ContentFormatType;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceManager;
import com.delcyon.capo.resourcemanager.ResourceParameter;
import com.delcyon.capo.resourcemanager.ResourceParameterBuilder;
import com.delcyon.capo.resourcemanager.ResourceType;
import com.delcyon.capo.resourcemanager.types.ContentMetaData.Attributes;
import com.delcyon.capo.util.diff.InputStreamTokenizer;
import com.delcyon.capo.util.diff.InputStreamTokenizer.TokenList;

/**
 * @author jeremiah
 */
public class ShellResourceDescriptor extends AbstractResourceDescriptor
{
	
	public enum Parameter
	{
		REMOVE_CR,
		DEBUG,
		PRINT_BUFFER
	}
	
	private SimpleContentMetaData contentMetaData;
	private SimpleContentMetaData iterationContentMetaData;
	private Process process;	
	private OutputStream stdinOutputStream;
	private ThreadedInputStreamReader stdoutThreadedInputStreamReader;	
	private String command = "";
	private long sleepTime = 1000l;
	private int defaultReadTimeout = 30;
	private ReentrantLock lock = null;
	private Condition notification = null;
	private boolean printBuffer = false;
	private boolean debug = false;
	
	@Override
	public void setup(ResourceType resourceType, String resourceURI) throws Exception
	{		
		super.setup(resourceType, ResourceManager.getSchemeSpecificPart(resourceURI));
	}
	
	private SimpleContentMetaData buildContentMetaData()
	{
		SimpleContentMetaData simpleContentMetaData  = new SimpleContentMetaData(getResourceURI());
		simpleContentMetaData.addSupportedAttribute(Attributes.exists,Attributes.readable,Attributes.writeable);
		
		simpleContentMetaData.setValue(Attributes.exists,true);
		simpleContentMetaData.setValue(Attributes.readable,true);
		simpleContentMetaData.setValue(Attributes.writeable,true);
		simpleContentMetaData.setValue("mimeType","text/text");
        simpleContentMetaData.setValue("MD5","");
        simpleContentMetaData.setValue("contentFormatType",ContentFormatType.TEXT);
        simpleContentMetaData.setValue("size","0");
		return simpleContentMetaData;
	}
	
	 
	
	@Override
	public void init(VariableContainer variableContainer,LifeCycle lifeCycle, boolean iterate, ResourceParameter... resourceParameters) throws Exception
	{
	
		super.init(variableContainer,lifeCycle, iterate, resourceParameters);		
		contentMetaData = buildContentMetaData();
		if(getVarValue(variableContainer, Parameter.DEBUG) != null && getVarValue(variableContainer, Parameter.DEBUG).equalsIgnoreCase("true"))
		{
		    debug = true;
		}
		if(getVarValue(variableContainer, Parameter.PRINT_BUFFER) != null && getVarValue(variableContainer, Parameter.PRINT_BUFFER).equalsIgnoreCase("true"))
        {
            printBuffer = true;
        }
		
		if (debug == true)
		{
			printBuffer = true;
		}
	}
	
	@Override
	public void open(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{
		
		super.open(variableContainer,resourceParameters);
		
		ProcessBuilder processBuilder = new ProcessBuilder();
		String[] commands = getResourceURI().split(" ");		
		processBuilder.command(commands);
		processBuilder.redirectErrorStream(true);
		process = processBuilder.start();
		
		stdoutThreadedInputStreamReader = new ThreadedInputStreamReader(process.getInputStream(),this);
		String removeCRVar = getVarValue(variableContainer, Parameter.REMOVE_CR); 
		if (removeCRVar != null && removeCRVar.equalsIgnoreCase("false"))
		{
			stdoutThreadedInputStreamReader.setRemoveCarriageReturns(false);
		}
		
		
		
		lock = new ReentrantLock();
		notification = lock.newCondition();
		lock.lock();
		//start the reader 
		stdoutThreadedInputStreamReader.start();
		stdoutThreadedInputStreamReader.okToRead(defaultReadTimeout);
		
		if(debug){System.out.println("===> waiting for results");}
				
		notification.await(); //this should automatically release the lock... hmmmm 
		
		if(debug){System.out.println("===> done waiting for results");}

		
//		stderrThreadedInputStreamReader = new ThreadedInputStreamReader(process.getErrorStream());		
		stdinOutputStream = process.getOutputStream();
		
		if(isIterating())
		{
			setResourceState(State.STEPPING);
		}
		if(debug){System.out.println("done opening");}
	}
	
	private long getTimeout(VariableContainer variableContainer) throws Exception
	{
	    
        String timeoutString = defaultReadTimeout+"";
        String timeoutVar = getVarValue(variableContainer, StepElement.Parameters.TIMEOUT);
        
        if  (timeoutVar != null && timeoutVar.matches("\\d+"))
        {       
            timeoutString = timeoutVar;
        }
        long timeout = Long.parseLong(timeoutString);
        return timeout;
	}
	
	@Override
	public boolean next(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		if(debug){System.out.println("stepping");}
		
		addResourceParameters(variableContainer, resourceParameters);
		
		long timeout = getTimeout(variableContainer);
		
		long timeoutTime = System.currentTimeMillis() + (timeout * 1000);
		
		if (getVarValue(variableContainer, StepElement.Parameters.UNTIL) != null)
		{
			long position = 0;
			while (System.currentTimeMillis() < timeoutTime) 
			{
				String regex = getVarValue(variableContainer, StepElement.Parameters.UNTIL);
				
				//scan the new data until we find what we're looking for, and skip any previously scanned data							
				byte[] data = stdoutThreadedInputStreamReader.getByteArrayOutputStream().toByteArray(); 
				ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
				BufferedInputStream bufferedInputStream = new BufferedInputStream(byteArrayInputStream, 40960);
				
				bufferedInputStream.skip(position);
				
				//break things up into lines
				InputStreamTokenizer inputStreamTokenizer = new InputStreamTokenizer(bufferedInputStream, TokenList.NEW_LINE);
				
				byte[] buffer = inputStreamTokenizer.readBytes();
				
				position += (long)buffer.length;
				
				while(buffer.length != 0)
				{
					if(printBuffer){System.out.print(new String(buffer));}
					
					if (new String(buffer).matches(regex))
					{
					    buildIterationMetatData(data);
						return true;
					}
					buffer = inputStreamTokenizer.readBytes();
				}
				
				//wait until notified
				lock.lock();
				stdoutThreadedInputStreamReader.okToRead(timeout);
				if(debug){System.out.println("next waiting for results");}
				notification.await();
				if(debug){System.out.println("next done waiting for results");}
				
			}
			if(debug){System.out.println("next timed out !!!");}
		}
		else //no until parameter, so just move the buffer
		{
		    addResourceParameters(variableContainer, resourceParameters);
            lock.lock();
            stdoutThreadedInputStreamReader.okToRead(getTimeout(variableContainer));
            if(debug){System.out.println("waiting for results");}
            notification.await();
            if(debug){System.out.println("done waiting for results");}
            buildIterationMetatData(stdoutThreadedInputStreamReader.getByteArrayOutputStream().toByteArray());
            return true;
		}
		return false;
	}
	
	@Override
	public byte[] readBlock(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		if(debug){System.out.println("reading block");}
		
		//make sure we've been opened
		if (getResourceState() != State.OPEN && getResourceState() != State.STEPPING)
		{
			open(variableContainer, resourceParameters);
		}
		
		if (isIterating() == false)
		{
		    addResourceParameters(variableContainer, resourceParameters);
            lock.lock();
            stdoutThreadedInputStreamReader.okToRead(getTimeout(variableContainer));
            if(debug){System.out.println("waiting for results");}
            notification.await();
            if(debug){System.out.println("done waiting for results");}
		}
		//get the data
		byte[] data = stdoutThreadedInputStreamReader.getByteArrayOutputStream().toByteArray();
		buildIterationMetatData(data);
		//clear the buffer once we've read it.
		stdoutThreadedInputStreamReader.getByteArrayOutputStream().reset();
		return data; 
	}
	
	@Override
	public void writeBlock(VariableContainer variableContainer, byte[] block, ResourceParameter... resourceParameters) throws Exception
	{
		stdoutThreadedInputStreamReader.getByteArrayOutputStream().reset();		
		command = new String(block);
		if(debug){System.out.println("Running command:"+command);}
		stdinOutputStream.write(block);
		stdinOutputStream.flush();
	}
	
	@Override
	public void close(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		if(debug){System.out.println("closing");}

		stdoutThreadedInputStreamReader.setInterrupted(true);		
		process.destroy();
		stdinOutputStream.close();		
		super.close(variableContainer, resourceParameters);
		
	}
	
//	@Override
//	public InputStream getErrorStream(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
//	{
//		return stderrInputStream;
//	}
	
	
 	@Override
	public ContentMetaData getContentMetaData(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{
		return contentMetaData;
	}

 	private void buildIterationMetatData(byte[] data) throws NoSuchAlgorithmException
 	{
 	  iterationContentMetaData = null;
 	  iterationContentMetaData = buildContentMetaData();          
      iterationContentMetaData.setValue("MD5",StreamUtil.getMD5(data));         
      iterationContentMetaData.setValue("size",data.length);
 	}
 	
	@Override
	public ContentMetaData getIterationMetaData(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{	    
		return iterationContentMetaData;
	}


	@Override
	public StreamFormat[] getSupportedStreamFormats(StreamType streamType)
	{
		if (streamType == StreamType.INPUT)
		{
			return new StreamFormat[]{StreamFormat.BLOCK};
		}
		else if(streamType == StreamType.OUTPUT)
		{
			return new StreamFormat[]{StreamFormat.BLOCK};
		}		
		else
		{
			return null;
		}
	}

	@Override
	public StreamType[] getSupportedStreamTypes()
	{
		return new StreamType[]{StreamType.INPUT,StreamType.OUTPUT};
	}

	@Override
	public Action[] getSupportedActions()
	{		
		return new Action[]{};
	}
	
	@Override
	public void release(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		if(debug){System.out.println("releaseing");}
		if (stdoutThreadedInputStreamReader != null)
		{
		    stdoutThreadedInputStreamReader.setInterrupted(true);
		}
		if (process != null)
		{
		    process.destroy();
		}
		setResourceState(State.RELEASED);
	}
	
	private class ThreadedInputStreamReader extends Thread
	{
		private InputStream inputStream;
		private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		private ShellResourceDescriptor parent = null;
		
		private Boolean okToRead = false;
		private boolean interrupted = false;
		private long timeout;
		private boolean removeCarriageReturns = true;
		
		public ThreadedInputStreamReader(InputStream inputStream, ShellResourceDescriptor  parent)
		{
			super("Shell Process Stream Reader");
			this.inputStream = inputStream;
			this.parent =  parent;
		}
		
		public void setRemoveCarriageReturns(boolean removeCarriageReturns)
		{
			this.removeCarriageReturns = removeCarriageReturns;
		}
		
		

		/** only start reading when we say it's ok */
		public void okToRead(long timeout)
		{
			this.timeout = System.currentTimeMillis() + (timeout * 1000);
			synchronized (okToRead)
			{
				okToRead = true;
			}
		}
		

		public void setInterrupted(boolean interrupted)
		{			
			this.interrupted = interrupted;
		}
		
		// this is a little complicated since setting interrupted to true doesn't always work, so we check our owners state as well	
		private boolean isFinished() throws Exception
		{
			if (this.interrupted == true || parent.getResourceState() == ResourceDescriptor.State.RELEASED || parent.getResourceState() == ResourceDescriptor.State.CLOSED)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		
		@Override
		public void run()
		{
			
			try
			{
				lock.lock();
				lock.unlock();
				long totalBytesRead = 0l;
				
				int bytesRead = 0;
				
				start: while (bytesRead >= 0)
				{
					byte[] buffer = new byte[CapoApplication.getConfiguration().getIntValue(PREFERENCE.BUFFER_SIZE)];	
					//always read bytes until closed
					
					//if we don't have anymore bytes to read and we have read some, notify everyone, that we are done
					//otherwise, keep reading
					
					if (inputStream.available() == 0 && byteArrayOutputStream.size() > 0)
					{
						
						
						if(debug){System.out.println("waiting for proper thread state");}
						boolean lockHasWaiters = false;
						while(lockHasWaiters == false)
						{
							//any while loop should have an exit if the parent goes away
							if (isFinished() == true)
							{
								return;
							}
							
							lock.lock();
							lockHasWaiters = lock.hasWaiters(notification);
							lock.unlock();
							if (lockHasWaiters == false)
							{								
								sleep(sleepTime);
							}

						}
						//tell the waiting parent that it's ok to read the input now
						lock.lock();
						okToRead = false; // they told us it was ok to read, since were done, set this to false
						notification.signal();
						lock.unlock();

						
					}
					
					boolean _okToRead = false;
					if(debug){System.out.println("waiting for OK to read");}
					while(_okToRead == false)
					{
						//was worried about sync issues 
						synchronized (okToRead)
						{
							_okToRead = okToRead;
						}
						if (_okToRead == false)
						{							
							sleep(sleepTime);
							//any while loop should have an exit if the parent goes away
							if (isFinished() == true)
							{
								return;
							}
						}
					}
					
					//wait here until we have something to read
					if(debug){System.out.println("waiting for something to read");}
					while (inputStream.available() == 0)
					{
						
						sleep(sleepTime);
						//any while loop should have an exit if the parent goes away
						if (isFinished() == true)
						{
							return;
						}
						
						//if we've timed out restart, but wakeup the parent
						if (System.currentTimeMillis() > timeout)
						{
							if(debug){System.out.println("timed out!!");}
							lock.lock();
							okToRead = false;
							notification.signal();
							lock.unlock();
							continue start;
						}
					}
					
					if(debug){System.out.println("reading buffer");}
					bytesRead = inputStream.read(buffer);
					if (bytesRead > 0)
					{
						//strip command echo from start of buffer
						//check to see if we're processing a new command
						int offset = 0;
						if (byteArrayOutputStream.size() == 0)
						{
							//see if our new data starts with our command, if so adjust the reading parameters to skip it
							String tempBuffer = new String(buffer,0,bytesRead);
							if (removeCarriageReturns == true)
							{
								tempBuffer = tempBuffer.replaceAll("\r", "");
								buffer = tempBuffer.getBytes();
								bytesRead = tempBuffer.length();
							}
							if (command.length() != 0 && tempBuffer.startsWith(command))
							{
								offset = command.length();
								bytesRead = bytesRead - command.length();
								//if we somehow have managed to remove too much, just skip to the next read
								if (bytesRead <= 0)
								{
									bytesRead = 0; //make sure we don't fall out of out while loop by passing a negative.
									continue;
								}
							}
						}
						
						byteArrayOutputStream.write(buffer, offset, bytesRead);				
						totalBytesRead += bytesRead;
					}
				}
				byteArrayOutputStream.flush();
				
			}
			catch (Exception exception)
			{				
				CapoApplication.logger.log(Level.WARNING, "Error processing shell stream",exception);
				lock.lock();
				notification.signal();
				lock.unlock();
			}
		}
		
		public ByteArrayOutputStream getByteArrayOutputStream()
		{
			return byteArrayOutputStream;
		}
	}
	
	
	
}
