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
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.logging.Level;

import com.delcyon.capo.CapoApplication;

/**
 * @author jeremiah
 *
 */
@SuppressWarnings("unchecked")
public class StreamHandler extends Thread 
{
	
	
	private static HashMap<String, Class<? extends StreamProcessor>> streamProcessorHashMap = new HashMap<String, Class<? extends StreamProcessor>>();
	static
	{
		
		Set<String> streamProcessorProviderSet =  CapoApplication.getAnnotationMap().get(StreamProcessorProvider.class.getCanonicalName());
		for (String className : streamProcessorProviderSet)
		{
			try
			{
				Class<? extends StreamProcessor> streamProcessorClass = (Class<? extends StreamProcessor>) Class.forName(className);
				
				StreamProcessorProvider streamProcessorProvider = streamProcessorClass.getAnnotation(StreamProcessorProvider.class);
				String[] streamIdentifierPatterns = streamProcessorProvider.streamIdentifierPatterns();
				for (String streamIdentifierPattern : streamIdentifierPatterns)
				{
					streamProcessorHashMap.put(streamIdentifierPattern, streamProcessorClass);
					CapoApplication.logger.log(Level.CONFIG, "Loaded StreamProcessorProvider '"+streamIdentifierPattern+"' from "+streamProcessorClass.getSimpleName());
				}
			} catch (Exception e)
			{
				CapoApplication.logger.log(Level.WARNING, "Couldn't load "+className+" as an StreamProcessor", e);
			}
		}
	}
	
	
	public static StreamProcessor getStreamProcessor(byte[] buffer) throws Exception
	{
		
			String bufferString = new String(buffer);
			Set<Entry<String, Class<? extends StreamProcessor>>> streamProcessorHashMapEntrySet = streamProcessorHashMap.entrySet();
		
			for (Entry<String, Class<? extends StreamProcessor>> entry : streamProcessorHashMapEntrySet)
			{
				String substring = null;
				if (bufferString.length() > entry.getKey().length())
				{
					substring = bufferString.substring(0, entry.getKey().length());
				}
				else
				{
					substring = bufferString;
				}
				if (substring.matches(entry.getKey()))
				{
					return entry.getValue().newInstance();
				}
			}
			
			return null;
	}
	
	
	private StreamProcessor streamProcessor;
	private BufferedInputStream inputStream;
	private OutputStream outputStream;
	private Vector<StreamFinalizer> streamFinalizerVector = new Vector<StreamFinalizer>();
	@SuppressWarnings("unused")
	private HashMap<String, String> sessionHashMap;
	
	public StreamHandler(StreamProcessor streamProcessor) throws Exception
	{
		super(streamProcessor.getClass().getSimpleName());
		this.streamProcessor = streamProcessor;
	}

	public void init(BufferedInputStream inputStream, OutputStream outputStream,HashMap<String, String> sessionHashMap) throws Exception
	{
	    this.sessionHashMap = sessionHashMap;
		this.inputStream = inputStream;
		this.outputStream = outputStream;
		this.streamProcessor.init(sessionHashMap);
	}
	
	@Override
	public void run()
	{
		try
		{			
			streamProcessor.processStream(inputStream, outputStream);
			outputStream.flush();
			inputStream.close();
			outputStream.close();
			for (StreamFinalizer streamFinalizer : streamFinalizerVector)
			{
				streamFinalizer.shutdown();
			}
		} catch (Exception exception)
		{
			CapoApplication.logger.log(Level.WARNING, "Exception thrown when processing stream with "+getName(),exception);
			//close streams, other wise the client keeps the connection open
			//TODO send an error message to the client here?
			try
			{
				outputStream.write(("EXCEPTION:"+exception.getMessage()).getBytes());
				outputStream.flush();
			} catch (IOException e){}
			try
			{
				inputStream.close();
			} catch (IOException e){}
			try
			{
				outputStream.close();
			} catch (IOException e){}
			
			//loop throw each finalizer and call it's shutdown method ignoring nay errors
			try
			{
				for (StreamFinalizer streamFinalizer : streamFinalizerVector)
				{
					streamFinalizer.shutdown();
				}
			}catch (Exception e){};
			
			
			if (CapoApplication.getApplication().getExceptionList() != null)
			{
				CapoApplication.getApplication().getExceptionList().add(exception);
			}
		}
		
	}

	public void add(StreamFinalizer streamFinalizer)
	{
		streamFinalizerVector.add(streamFinalizer);
		
	}

	
	
}
