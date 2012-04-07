/**
Copyright (c) 2011 Delcyon, Inc.
This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.delcyon.capo.datastream;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

import com.delcyon.capo.datastream.StreamEventListener.StreamEvent;

/**
 * @author jeremiah
 *
 */
public class StreamEventFilterOutputStream extends FilterOutputStream
{
	private Vector<StreamEventListener> eventListenerVector = new Vector<StreamEventListener>();
	private boolean processWrites = false;
	public StreamEventFilterOutputStream(OutputStream outputStream)
	{
		super(outputStream);
	}
	
	public StreamEventFilterOutputStream(OutputStream outputStream,StreamEventListener streamEventListener)
	{
		super(outputStream);
		eventListenerVector.add(streamEventListener);
	}
	
	public void setProcessWrites(boolean processWrites)
	{
		this.processWrites = processWrites;
	}
	
	public boolean  addStreamEventListener(StreamEventListener eventListener)
	{
		return eventListenerVector.add(eventListener);
	}
	
	public boolean removeStreamEventListener(StreamEventListener eventListener)
	{
		return eventListenerVector.remove(eventListener);
	}
	
	private void processEvent(StreamEventListener.StreamEvent streamEvent) throws IOException
	{
		for (StreamEventListener streamEventListener : eventListenerVector)
		{
			streamEventListener.processStreamEvent(streamEvent);
		}
	}
	
	@Override
	public void close() throws IOException
	{		
		super.close();
		processEvent(StreamEvent.CLOSED);
	}
	
	@Override
	public void flush() throws IOException
	{		
		super.flush();
		processEvent(StreamEvent.FLUSHED);
	}
	
	@Override
	public void write(byte[] b) throws IOException
	{		
		super.write(b);
		if (processWrites == true)
		{
			processEvent(StreamEvent.WRITE);
		}
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException
	{		
		super.write(b, off, len);
		if (processWrites == true)
		{
			processEvent(StreamEvent.WRITE);
		}
	}
	
	@Override
	public void write(int b) throws IOException
	{		
		super.write(b);
		if (processWrites == true)
		{
			processEvent(StreamEvent.WRITE);
		}
	}
}
