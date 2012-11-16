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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import com.delcyon.capo.datastream.StreamEventListener.StreamEvent;

/**
 * @author jeremiah
 *
 */
public class StreamEventFilterInputStream extends FilterInputStream
{
	private Vector<StreamEventListener> eventListenerVector = new Vector<StreamEventListener>();
	
	public StreamEventFilterInputStream(InputStream inputStream)
	{
		super(inputStream);
	}
	
	public StreamEventFilterInputStream(InputStream inputStream,StreamEventListener streamEventListener)
	{
		super(inputStream);
		eventListenerVector.add(streamEventListener);
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
	
	
}
