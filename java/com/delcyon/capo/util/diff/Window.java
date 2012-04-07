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
import java.util.HashMap;

import com.delcyon.capo.util.diff.Diff.Side;

/**
 * @author jeremiah
 * This class is a window onto a tokenized data stream. It contains a hashmap of all of the window items it contains, 
 * and a collection of 'chains' for each match it has with another window. 
 * It also keeps track of it's current position with regards to the number of window items that have been stored in it.
 * This generally corresponds to the token number of a tokenized input stream.    
 */
public class Window
{
	
	
	private long streamPosition = -1l; 
	private ArrayList<WindowItem> windowItems = null;
	private HashMap<Long, ArrayList<WindowItem>> windowItemHashMap = new HashMap<Long, ArrayList<WindowItem>>();
	private ArrayList<ArrayList<WindowItemLink>> chains = new ArrayList<ArrayList<WindowItemLink>>();	
	private Side side;
	
	
	public Window(Side side,int windowSize)
	{
		this.side = side;
		this.windowItems = new ArrayList<WindowItem>(windowSize); 
	}

	/**
	 * @return the current windowItems within this window.
	 */
	public ArrayList<WindowItem> getWindowItems()
	{			
		return windowItems;
	}
	
	/**
	 * This will remove anything in the window associated with a windowItem up to and including the specified stream position.
	 * @param windowStreamPosition
	 */
	public void removeUntil(long windowStreamPosition)
	{
		
		while(windowItems.isEmpty() == false && windowItems.get(0).getStreamPosition() <= windowStreamPosition)
		{
			WindowItem currentWindowItem = windowItems.get(0);
			currentWindowItem.clearMatches();
			windowItems.remove(0);
			windowItemHashMap.get(currentWindowItem.getDataHashCode()).remove(currentWindowItem);
			
		}

		
		
		//now remove any of our own window level chains that are empty
		for (int chainIndex = 0; chainIndex < chains.size(); chainIndex++)
		{
			ArrayList<WindowItemLink> chain = chains.get(chainIndex);
			if(chain.isEmpty() == false && chain.get(0).getWindowItemForSide(side).getStreamPosition() <= windowStreamPosition)
			{
				chain.clear();
			}
			if (chain.isEmpty() == true)
			{
				chains.remove(chainIndex);
				chainIndex--;					
			}
			

		}
		
	}

	/**
	 * 
	 * @param windowItem
	 * @return any window items who's data is the same as the data from the WindowItem being passed in.
	 */
	public ArrayList<WindowItem> getMatches(WindowItem windowItem)
	{			
		return windowItemHashMap.get(windowItem.getDataHashCode());
	}

	/**
	 * 
	 * @param windowItem
	 * @return returns true if we have a windowItem with the same data
	 */
	public boolean hasMatch(WindowItem windowItem)
	{
		if (windowItem == null)
		{			
			return false;
		}
		else
		{
			return windowItemHashMap.containsKey(windowItem.getDataHashCode());
		}
	}

	/**
	 * creates and adds a window items for this byte array. It will also link this window item's predecessor to this new windowItem
	 * @param data
	 */
	public void addWindowItem(byte[] data)
	{
		streamPosition++;
		//make sure that each window item knows who it's neighbors are
		if (data != null)
		{
			WindowItem previousWindowItem = null;
			if (windowItems.size() > 0)
			{
				previousWindowItem = windowItems.get(windowItems.size()-1);
			}
			WindowItem windowItem = new WindowItem(data,previousWindowItem,this,streamPosition);
			windowItems.add(windowItem);			
			addToHashMap(windowItem);
		}

		
	}
	
	/**
	 * Sometimes you might use a window to match up object, and need a pointer to the original
	 * @param data
	 * @param object
	 */
	public void addWindowItem(byte[] data,Object object)
	{
		streamPosition++;
		//make sure that each window item knows who it's neighbors are
		if (data != null)
		{
			WindowItem previousWindowItem = null;
			if (windowItems.size() > 0)
			{
				previousWindowItem = windowItems.get(windowItems.size()-1);
			}
			WindowItem windowItem = new WindowItem(data,object,previousWindowItem,this,streamPosition);
			windowItems.add(windowItem);			
			addToHashMap(windowItem);
		}

		
	}
	
	/**
	 * add a window item to our internal hashmap, and expand the entry list if needed
	 * @param windowItem
	 */
	private void addToHashMap(WindowItem windowItem)
	{
		if(windowItem == null)
		{			
			return;
		}
		else if (windowItemHashMap.containsKey(windowItem.getDataHashCode()))
		{
			windowItemHashMap.get(windowItem.getDataHashCode()).add(windowItem);
		}
		else
		{
			ArrayList<WindowItem> windowItemArrayList = new ArrayList<WindowItem>();
			windowItemArrayList.add(windowItem);
			windowItemHashMap.put(windowItem.getDataHashCode(), windowItemArrayList);				
		}
		
	}

	
	public WindowItemLink getFirstMatch()
	{
		WindowItemLink firstMatch = null;
		for (WindowItem windowItem : windowItems)
		{
			if (windowItem.getMatches().size() > 0)
			{
				firstMatch = new WindowItemLink(windowItem, windowItem.getMatches().get(0));
				break;
			}
		}
		
		return firstMatch;
	}
	
	/**
	 * Some of the magic is here. Given two windows, this will return an array of WindowItemLinks that are equals with each other, 
	 * and closest to the start of each window.   
	 * @param window
	 * @param window2
	 * @return
	 */
	public ArrayList<WindowItemLink> getCheapestChain(Window window, Window window2)
	{
		ArrayList<WindowItemLink> chain = null;
		long bestDistance = Long.MAX_VALUE;
		
		//printChains();
		
		for (ArrayList<WindowItemLink> searchableChain : chains)
		{
			try
			{
				if (searchableChain.size() > 1)
				{
					long distance1 = searchableChain.get(0).getWindowItemForSide(window.side).getStreamPosition() - window.windowItems.get(0).getStreamPosition();
					long distance2 =  searchableChain.get(0).getWindowItemForSide(window2.side).getStreamPosition() - window2.windowItems.get(0).getStreamPosition();
					long totalDistance = distance1 + distance2;
					if (bestDistance > totalDistance)
					{
						bestDistance = totalDistance;
						chain = searchableChain;
					}
				}
			} 
			catch (IndexOutOfBoundsException indexOutOfBoundsException)
			{
				indexOutOfBoundsException.printStackTrace();
			}
		}
		return chain;
	}
	
	
	/**
	 * debugging
	 */
	public void printChains()
	{
		for (ArrayList<WindowItemLink> chain : chains)
		{
			System.out.print(chain.get(0)+"["+chain.size()+"],");
		}
		System.out.println();
	}

	/**
	 * This will return which side the window belongs to.
	 */
	@Override
	public String toString()
	{
		return side.toString();
	}

	/**
	 * Returns an array list of arrayLists of all of the windowItemLinks. 
	 * Basically this will return every matching window item from both streams that is currently visible within both windows.
	 * @return
	 */
	public ArrayList<ArrayList<WindowItemLink>> getChains()
	{
		return chains;
	}

	/**
	 * return which side this window is looking at.
	 * @return
	 */
	public Side getSide()
	{
		return side; 
	}
	
	
	
}
