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
package com.delcyon.capo.util.diff;

import com.delcyon.capo.util.diff.Diff.Side;

/**
 * @author jeremiah
 * This is a simple class used to link two window items into a pair. 
 * Print them out if necessary. Compare a windowItem to a proper side of a window item contained within this link. Or two WindowItemLinks wi eachother.
 * They are used in a window's chain array. 
 */
public class WindowItemLink
{
	private WindowItem baseWindowItem = null;
	private WindowItem otherWindowItem = null;
	
	/**
	 * Holder for two window items who's data is the same, but who's location in two different input streams are different. 
	 * @param windowItem
	 * @param windowItem2
	 */
	public WindowItemLink(WindowItem windowItem, WindowItem windowItem2)
	{
		if (windowItem.getSide() == Side.BASE)
		{
			baseWindowItem = windowItem;
			otherWindowItem = windowItem2;
		}
		else
		{
			baseWindowItem = windowItem2;
			otherWindowItem = windowItem;
		}
	}
	
	public WindowItem getWindowItemForSide(Side side)
	{
		if (side == Side.BASE)
		{
			return baseWindowItem;
		}
		else
		{
			return otherWindowItem;
		}
	}

	public String toString(Side side)
	{
		if (side == Side.BASE)
		{
			return String.format("%02d", baseWindowItem.getStreamPosition());
		}
		else
		{
			return String.format("%02d", otherWindowItem.getStreamPosition());
		}
	}

	@Override
	public String toString()
	{
		
		return String.format("(%02d,%02d)", baseWindowItem.getStreamPosition(),otherWindowItem.getStreamPosition());
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(obj == null)
		{
			return false;
		}
		else if (obj instanceof WindowItemLink)
		{
			WindowItemLink windowItemLink = (WindowItemLink) obj;
			if (windowItemLink.baseWindowItem.getStreamPosition() == baseWindowItem.getStreamPosition() && windowItemLink.otherWindowItem.getStreamPosition() == otherWindowItem.getStreamPosition())
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else if (obj instanceof WindowItem)
		{
			WindowItem windowItem = (WindowItem) obj;
			if (windowItem.getSide() == Side.BASE)
			{
				if (windowItem.getStreamPosition() == baseWindowItem.getStreamPosition())
				{
					return true;
				}
				else
				{
					return false;
				}
			}
			else if (windowItem.getStreamPosition() == otherWindowItem.getStreamPosition())
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
	}
	
	public WindowItem getBaseWindowItem()
	{
		return baseWindowItem;
	}
	
	public WindowItem getOtherWindowItem()
	{
		return otherWindowItem;
	}
}
