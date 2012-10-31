/**
Copyright (c) 2012 Delcyon, Inc.
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
package com.delcyon.capo.xml.cdom;

import com.delcyon.capo.xml.XPath;

/**
 * @author jeremiah
 *
 */
public class CDOMEvent
{
	public enum EventType
	{
		INSERT,
		UPDATE,
		DELETE
		
	}
	
	private CNode sourceNode = null;
	private EventType eventType = null;
	private boolean handled = false;
	
	@SuppressWarnings("unused")
	private CDOMEvent(){}//serialization
	
	public CDOMEvent(EventType eventType, CNode sourceNode)
	{
		this.eventType = eventType;
		this.sourceNode = sourceNode;
	}
	
	public EventType getEventType()
	{
		return eventType;
	}
	
	public CNode getSourceNode()
	{
		return sourceNode;
	}
	
	public boolean isHandled()
	{
		return handled;
	}
	
	public void setHandled(boolean handled)
	{
		this.handled = handled;
	}
	
	@Override
	public String toString()
	{
		try
		{
			return "CDOMEvent["+eventType+":"+XPath.getXPath(sourceNode)+"]";
		} catch (Exception e)
		{
			return "CDOMEvent["+eventType+":"+sourceNode+"@PATH EXCEPTION]";
		}
	}
}
