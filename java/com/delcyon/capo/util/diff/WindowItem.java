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

import java.util.ArrayList;

import com.delcyon.capo.util.diff.Diff.Side;

/**
 * @author jeremiah
 * This is a holder for the data that was read in from a stream.
 * It knows it's position in a stream.
 * How long the data is
 * A hashcode for the data.
 * Which window it belongs to.
 * And contains a list of other window items that it is a match with.
 */
public class WindowItem
{
	private Window window = null;
	private WindowItem previousWindowItem = null;	
	private byte[] data;
	private long dataHashCode = 0l;
	private long streamPosition = 0l;
	private long dataLength = 0l;
	private ArrayList<WindowItem> matches = new ArrayList<WindowItem>();
	private Object object = null;
	
	/**
	 * 
	 * @param data
	 * @param previousWindowItem
	 * @param window
	 * @param streamPosition
	 */
	public WindowItem(byte[] data, WindowItem previousWindowItem, Window window, long streamPosition)
	{		
		this.data = data;
		this.dataHashCode = getHashcode(data);
		this.dataLength = data.length;
		this.previousWindowItem = previousWindowItem;
		this.window = window;
		this.streamPosition = streamPosition;
	}

	/**
	 * 
	 * @param data
	 * @param object to store along with data
	 * @param previousWindowItem
	 * @param window
	 * @param streamPosition
	 */
	public WindowItem(byte[] data,Object object, WindowItem previousWindowItem, Window window, long streamPosition)
	{
		this.data = data;
		this.dataHashCode = getHashcode(data);
		this.dataLength = data.length;
		this.previousWindowItem = previousWindowItem;
		this.window = window;
		this.streamPosition = streamPosition;
		this.object  = object;
	}
	/**
	 * This is for debugging 
	 * @param windowItem
	 * @return
	 */
	public String getChainID(WindowItem windowItem)
	{
		
		for (ArrayList<WindowItemLink> chain : window.getChains())
		{			
			if (chain.contains(windowItem))
			{
				return chain.get(0).toString(windowItem.getSide());
			}
		}
		return "--";
	}
	
	
	
	
	

	
	/**
	 * Adds any matches to both sides. This will add any window item not currently in a chain to he appropriate chains
	 * The passed in array list should be from the opposing side of the window that this window item belongs to.
	 * @param matches
	 * @param requirePreviousMatch if this is true, a chain will only be constructed if the previous window items also match
	 */
	public void addMatches(ArrayList<WindowItem> matches)
	{
		for (WindowItem windowItem : matches)
		{
			if (this.matches.contains(windowItem) == false)
			{
				this.matches.add(windowItem);
				windowItem.addMatch(this);
				//check to see if this is part of a chain
				if (this.previousWindowItem != null && windowItem.previousWindowItem != null)
				{
					if (this.previousWindowItem.dataHashCode == windowItem.previousWindowItem.dataHashCode)
					{
						this.addToChain(windowItem);							
					}
				}
				else
				{
					//we can end up here when dealing with chains that start on a zero stream position
					//do nothing
				}
			}
			else
			{
				//skip
			}
		}			
	}

	
	/**
	 * add this window item to any chain contained in the window. The assumption is made, that this window item is a match someplace. If it's not, things won't work 
	 * @param windowItem
	 */
	private void addToChain(WindowItem windowItem)
	{

		WindowItemLink windowItemLink = new WindowItemLink(this, windowItem);
		
		//figure out which chain to add to
		//this can be done, by looking at the head of the chain, and making sure that it's stream position matches the incoming item's stream position - chain length
		//add to chain for this window
		ArrayList<WindowItemLink> chain = null;

		//see if our parent has a chain that we are adding to
		//we will never already be a member of that chain
		for (ArrayList<WindowItemLink> searchableChain : this.window.getChains())
		{
			//get last item in chain
			if (searchableChain.isEmpty() == false)
			{
				WindowItemLink previousLink = searchableChain.get(searchableChain.size() -1);

				if (previousLink.getWindowItemForSide(windowItem.getSide()).streamPosition == windowItem.streamPosition - 1l)
				{
					if(previousLink.getWindowItemForSide(this.getSide()).streamPosition == this.streamPosition - 1l)
					{
						searchableChain.add(windowItemLink); //add link to every matching chain we find?					
						chain = searchableChain; //we are just using this as a flag to indicate we found one
					}
					else
					{						
						//we can end up here when we have chains that start at a zero stream position, because they have no previos window item
					}
				}
			}
		}
		
		//if we didn't find a chain, make a new one, and make sure everyone has it
		if (chain == null)
		{
			chain = new ArrayList<WindowItemLink>();
			windowItemLink.getBaseWindowItem().window.getChains().add(chain);
			windowItemLink.getOtherWindowItem().window.getChains().add(chain);		

			WindowItemLink previousLink = new WindowItemLink(this.previousWindowItem, windowItem.previousWindowItem);
			chain.add(previousLink);

			//add the incoming windowItem to the chain			
			chain.add(windowItemLink);				
		}				
	}

	
	
	
	/**
	 * adds a match to only this side, should be kept private and internal, so we don't hit some kind of recursive loop
	 */
	private void addMatch(WindowItem windowItem)
	{
		if (this.matches.contains(windowItem) == false)
		{
			this.matches.add(windowItem);
		}
		else
		{
			//skip
		}
	}

	
	
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof WindowItem)
		{
			if (streamPosition == ((WindowItem)obj).streamPosition)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else if (obj instanceof WindowItemLink)
		{
			WindowItemLink windowItemLink = (WindowItemLink) obj;
			if (getSide() == Side.BASE && streamPosition == windowItemLink.getBaseWindowItem().streamPosition)
			{
				return true;
			}
			else if (getSide() == Side.MOD && streamPosition == windowItemLink.getOtherWindowItem().streamPosition)
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
	
	public void clearMatches()
	{
		matches.clear();
	}	
	
	
	@Override
	public String toString()
	{			
		return "[window = '"+window.getSide() +"', streamPosition = '"+streamPosition+"', dataHashCode = '"+Long.toHexString(dataHashCode)+"', data ='"+new String(data)+"', matchCount = '"+matches.size()+"', dataLength = '"+dataLength+"']";
		
	}


	public Side getSide()
	{
		return window.getSide();
	}

	public long getStreamPosition()
	{
		return streamPosition;
	}

	/**
	 * returns a hash code based on a byte[]. This is based on sun's hashcode algorithm for strings, but has been changed to work for byte arrays
	 * @param bytes
	 * @return
	 */
	private long getHashcode(byte[] bytes)
	{
		if (bytes.length == 0)
		{
			return 0l;
		}
		else
		{
			long hashCode = 0l;
			for (int index = 0; index < bytes.length ; index++) 
			{
				hashCode = bytes[index] + ((hashCode << 5) - hashCode);
			}
			return hashCode;
		}
	}

	/**
	 * a hash code for the data, based on sun's hashcode algorithm for strings.
	 * @return
	 */
	public long getDataHashCode()
	{
		return dataHashCode;
	}

	/**
	 * the data 
	 * @return
	 */
	public byte[] getData()
	{
		return data;
	}

	public Object getObject()
	{
		return this.object;
	}

	public ArrayList<WindowItem> getMatches()
	{
		return matches;
	}
	
}