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
package com.delcyon.capo.util.diff;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;

import com.delcyon.capo.util.diff.InputStreamTokenizer.TokenList;

/**
 * @author jeremiah
 * Generates DiffEntrys based on inputStream or String or byte[] arrays.
 * This can be used to process very long streams of data, or just simple text.
 * If you use one of the getDifferences methods, the differences will be processed, and the class is finished.
 * If however you use the getInputStream() method, Diff will spin off it's own thread, an continiously process the two input streams for data
 * and make all of the diff entries available in the input stream for reading. In theory this should should handle a huge amount of streaming data.    
 * 
 * You can use the addCustomTokenList to add tokens on which to break up the input stream into 'lines'.  
 * These lines are what's compared to each other to generate the diff.
 * If you do so, you will need to keep track of them so you can determine how your stream was tokenized.
 * The default tokenList is NEW_LINE. 
 * 
 * The window is a sliding window, so it size will limit how many lines are compared to each other at once. 
 * If it's too small, you might not get 100% accuracy, and if too large will probably slow things down. Generally not a big deal though. And defaults to 256 'lines'  
 */
public class Diff implements Runnable
{

	public enum Side
	{
		BASE('-'),
		MOD('+'),
		BOTH('=');
		
		private char directionChar;

		Side(char directionChar)
		{
			this.directionChar = directionChar;
		}
		
		public char getDirectionChar()
		{
			return directionChar;
		}
		
		/**
		 * Simple convince method to always return the opposite side
		 * @return
		 */
		public Side getOppositeSide()
		{
			switch (this)
			{
				case BASE:
					return MOD;					
				case MOD:
					return BASE;
				default:
					return BOTH;					
			}
		}
	}
	
	public static final int DEFAULT_WINDOW_SIZE = 256;
	

	private int windowSize;
	private InputStream baseInputStream;
	private InputStream otherInputStream;
	private OutputStream outputStream;
	
	private boolean threadStarted = false;
	private ArrayList<ArrayList<Integer>> tokenLists = new ArrayList<ArrayList<Integer>>();
	private TokenList tokenList;
	
	
	
	
	public Diff(InputStream baseInputStream, InputStream otherInputStream,int windowSize,TokenList tokenList) throws IOException
	{
		this.windowSize = windowSize;		
		this.baseInputStream = baseInputStream;
		this.otherInputStream = otherInputStream;
		
		this.tokenLists = tokenList.getTokenLists();
		this.tokenList = tokenList;
		
	}
	
	/**
	 * Uses DEFAULT_WINDOW_SIZE
	 * Uses NEW_LINE token list
	 * @param baseInputStream
	 * @param otherInputStream
	 * @throws IOException
	 */
	public Diff(InputStream baseInputStream, InputStream otherInputStream) throws IOException
	{
		this(baseInputStream, otherInputStream, DEFAULT_WINDOW_SIZE,TokenList.NEW_LINE);
	}
	
	/**
	 *  Uses DEFAULT_WINDOW_SIZE
	 * @param baseInputStream
	 * @param otherInputStream
	 * @param tokenList
	 * @throws IOException
	 */
	public Diff(InputStream baseInputStream, InputStream otherInputStream,TokenList tokenList) throws IOException
	{
		this(baseInputStream, otherInputStream, DEFAULT_WINDOW_SIZE,tokenList);
	}
	
	/**
	 * Uses the addition of the text lengths for the window size
	 * Uses NEW_LINE token list
	 * @param base
	 * @param other
	 * @throws IOException
	 */
	public Diff(String baseText, String otherText) throws IOException
	{
		this(new ByteArrayInputStream(baseText.getBytes()),new ByteArrayInputStream(otherText.getBytes()),baseText.length()+otherText.length(),TokenList.NEW_LINE);
	}
	
	/**
	 * Uses the addition of the text lengths for the window size
	 * Allow the specification of a CUSTOM tokenList 
	 * @param base
	 * @param other
	 * @param tokenLists 
	 * @throws IOException
	 */
	public Diff(String baseText, String otherText, ArrayList<ArrayList<Integer>> tokenLists) throws IOException
	{
		this(new ByteArrayInputStream(baseText.getBytes()),new ByteArrayInputStream(otherText.getBytes()),baseText.length()+otherText.length(),TokenList.CUSTOM);
		this.tokenLists = tokenLists;
	}

	
	/**
	 * Uses the addition of the text lengths for the window size
	 * @param base
	 * @param other
	 * @throws IOException
	 */
	public Diff(String baseText, String otherText,TokenList tokenList) throws IOException
	{
		this(new ByteArrayInputStream(baseText.getBytes()),new ByteArrayInputStream(otherText.getBytes()),baseText.length()+otherText.length(),tokenList);
	}
	
	/**
	 * Uses the addition of the array lengths for the window size
	 * Uses NEW_LINE token list
	 * @param base
	 * @param other
	 * @throws IOException
	 */
	public Diff(byte[] base, byte[] other) throws IOException
	{
		this(new ByteArrayInputStream(base),new ByteArrayInputStream(other),base.length+other.length,TokenList.NEW_LINE);
	}
	
	/**
	 * Uses the addition of the array lengths for the window size
	 * @param base
	 * @param other
	 * @throws IOException
	 */
	public Diff(byte[] base, byte[] other,TokenList tokenList) throws IOException
	{
		this(new ByteArrayInputStream(base),new ByteArrayInputStream(other),base.length+other.length,tokenList);
	}
	
	
	public int getWindowSize()
	{
		return windowSize;
	}
	/**
	 * see class description
	 * @param windowSize
	 */
	public void setWindowSize(int windowSize)
	{
		this.windowSize = windowSize;
	}
	
	/**
	 * 	Will change the tokenList to CUSTOM, and add this list of tokens to the current tokenLists array.
	 *  You will need to clear he token list if you want only your custom tokens used. 
	 *  Clear it BEFORE you add you custom token lists.
	 * @param eolMatch
	 */
	public void addCustomTokenList(char... eolMatch)
	{
		this.tokenList = TokenList.CUSTOM;
		ArrayList<Integer> lineBrake = new ArrayList<Integer>();
		for (char c : eolMatch)
		{
			lineBrake.add((int)c);
		}
		tokenLists.add(lineBrake);
	}
	
	/**
	 * Clears out all of the current token lists, and sets tokenList to CUSTOM
	 */
	public void clearTokenLists()
	{
		tokenLists.clear();
		this.tokenList = TokenList.CUSTOM;
	}
	
	public TokenList getTokenList()
	{
		return tokenList;
	}
	
	public ArrayList<ArrayList<Integer>> getTokenLists()
	{
		return tokenLists;
	}
	
	/**
	 * @return difference entries as a string
	 * @throws Exception
	 */
	public String getDifferences() throws Exception
	{
		outputStream = new ByteArrayOutputStream();
		processDifferences();
		return outputStream.toString();
	}
	
	/**
	 * 
	 * @return difference entries as a byte[]
	 * @throws Exception
	 */
	public byte[] getDifferencesAsBytes() throws Exception
	{
		outputStream = new ByteArrayOutputStream();
		processDifferences();
		return ((ByteArrayOutputStream) outputStream).toByteArray();
	}
	
	/**
	 * 
	 * @return an input stream which can be used for reading DiffEntries from. This will cause a new Thread to be started, and can be used in conjunction with large data streams.
	 * @throws IOException
	 */
	public InputStream getInputStream() throws IOException
	{
		PipedInputStream pipedInputStream = null;
		if (this.threadStarted == false)
		{
			this.outputStream = new PipedOutputStream();
			pipedInputStream = new PipedInputStream((PipedOutputStream) outputStream, windowSize*80);
			new Thread(this).start();			
		}
		return pipedInputStream;
	}
	
	/**
	 * used by getInputStream method 
	 */
	@Override
	public void run()
	{
		try
		{
			this.threadStarted  = true;
			processDifferences();
		}
		catch (Exception e)
		{			
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Actually creates an processes the DiffEntries this is the main method of this class.
	 * @throws Exception
	 */
	private void processDifferences() throws Exception
	{
		
		Window baseWindow = new Window(Side.BASE,windowSize);
		
		Window otherWindow = new Window(Side.MOD,windowSize);
		
		InputStreamTokenizer baseInputStreamBreaker = null;
		InputStreamTokenizer otherInputStreamBreaker = null;
		
		if (tokenList == TokenList.CUSTOM)
		{
			baseInputStreamBreaker = new InputStreamTokenizer(baseInputStream, tokenLists);
			otherInputStreamBreaker = new InputStreamTokenizer(otherInputStream, tokenLists);	
		}
		else
		{
			baseInputStreamBreaker = new InputStreamTokenizer(baseInputStream, tokenList);
			otherInputStreamBreaker = new InputStreamTokenizer(otherInputStream, tokenList);	
		}

		
		
		//fill base window
		readIntoWindow(baseWindow, baseInputStreamBreaker, windowSize);
		
		//fill other window
		readIntoWindow(otherWindow, otherInputStreamBreaker, windowSize);
		
		
		
		
		for (WindowItem otherWindowItem : otherWindow.getWindowItems())
		{			
			if (baseWindow.hasMatch(otherWindowItem) == true)
			{	
				
				otherWindowItem.addMatches(baseWindow.getMatches(otherWindowItem));				
			}
			else
			{
				//skip		
			}
		}

		
		//printMatchTable(baseWindow, otherWindow);
		
		/*
		 * Basic algorithm
		 * starting with base window
		 * get Cheapest chain from current stream position for current window (starting w/ base)
		 * walk to that chains start position
		 * while walking:
		 * 	decapitate/break any chains we intersect for that windowItem's stream position
		 * 	add windowItem to script
		 * 	increment script position
		 * 	remove windowItem from window
		 * when we arrive at position:
		 * walk chain
		 * while walking:
		 * 	add window item to script (equals)
		 * 	increment script position
		 * when done walking
		 * remove window items in chain
		 * remove chain
		 * refresh Window
		 * change direction !! apparently we shouldn't do this, or we do it implicitly. 
		 * The examples online appears to be wacky. We are not looking for the largest common sequence, we are looking for the closest common sequence in the match grid.
		 * Maybe it's just my lack of understanding, but the LCS is not always the best sequence to use, since it might require a large amount changes just to get to it.     
		 * rinse repeat
		 */
		

		long scriptPosition = 0;
		long baseStreamPosition = 0l;
		long otherStreamPosition = 0l;
		
		while(true)
		{	
			
			
			int baseWindowItemsIndex = 0;
			//get list of window items to walk through
			ArrayList<WindowItem> baseWindowItems = baseWindow.getWindowItems();
			ArrayList<WindowItem> otherWindowItems = otherWindow.getWindowItems();
			if(baseWindowItems.isEmpty() && otherWindowItems.isEmpty())
			{
				break;
			}
			
			//get cheapest chain for first window item
			ArrayList<WindowItemLink> currentChain = baseWindow.getCheapestChain(baseWindow,otherWindow);
			
			long baseWindowChainStartPosition = -1;
			long otherWindowChainStartPosition = -1;
			long otherWindowChainEndPosition = -1;
			
			int currentChainSize = 0;
			
			if(currentChain == null)
			{
				//give us a bogus end to read to
				//add one to the ends to make sure we read fully when dealing with the difference below.
				if (otherWindowItems.isEmpty() == false)
				{					
					otherWindowChainStartPosition = otherWindowItems.get(otherWindowItems.size() - 1).getStreamPosition()+1l;					
					otherWindowChainEndPosition = otherWindowItems.get(otherWindowItems.size() - 1).getStreamPosition()+1l;
				}
				if (baseWindowItems.isEmpty() == false)
				{					
					baseWindowChainStartPosition = baseWindowItems.get(baseWindowItems.size()-1).getStreamPosition()+1l;
				}
				
			}
			else 
			{
				 
				 baseWindowChainStartPosition = currentChain.get(0).getBaseWindowItem().getStreamPosition();
				 
				 
				 otherWindowChainStartPosition = currentChain.get(0).getOtherWindowItem().getStreamPosition();				 
				 otherWindowChainEndPosition = currentChain.get(currentChain.size()-1).getOtherWindowItem().getStreamPosition();
			}
			
			
			if (currentChain != null)
			{
				currentChainSize = currentChain.size();
				//System.out.println(currentWindow+" chain = "+currentChain.get(0)+"["+currentChainSize+"]");
			}
			//figure out distance from currentWindowItem.streamPosition to start of chain
			//walk to cheapest chain start position in the base window
			
			if (baseWindowItems.isEmpty() == false)
			{
				int diffrence = (int) (baseWindowChainStartPosition - baseWindowItems.get(0).getStreamPosition());
				for(int currentIndex = 0;currentIndex < diffrence; currentIndex++)
				{					
			
					writeLine(baseWindowItems.get(currentIndex).getSide(), baseStreamPosition, otherStreamPosition, scriptPosition, baseWindowItems.get(currentIndex).getData(), outputStream, true);
					baseWindowItemsIndex++;				
					baseStreamPosition++;
				}
			}
			
			
			//walk to cheapest chain start position in the other window
			if (otherWindowItems.isEmpty() == false)
			{
				int otherWindowItemsIndex = 0;
				while(otherWindowItems.size() > otherWindowItemsIndex && otherWindowItems.get(otherWindowItemsIndex).getStreamPosition() < otherWindowChainStartPosition)
				{
					
					writeLine(otherWindowItems.get(otherWindowItemsIndex).getSide(), baseStreamPosition, otherStreamPosition, scriptPosition, otherWindowItems.get(otherWindowItemsIndex).getData(), outputStream, true);
					otherWindowItemsIndex++;				
					scriptPosition++;
					otherStreamPosition++;
				}
			}
			//walk the chain
			if (currentChain != null)
			{
				for (WindowItemLink windowItemLink : currentChain)
				{
					writeLine(null, baseStreamPosition, otherStreamPosition, scriptPosition, windowItemLink.getWindowItemForSide(Side.BASE).getData(), outputStream, true);
					scriptPosition++;
					baseStreamPosition++;
					otherStreamPosition++;
				}
			}
			//cleanup
			if (baseWindowItems.isEmpty() == false)
			{
				long baseWindowEndStreamPosition = baseWindowItems.get(0).getStreamPosition()+baseWindowItemsIndex+(long)currentChainSize-1l;
				baseWindow.removeUntil(baseWindowEndStreamPosition);
			}
			if (otherWindowItems.isEmpty() == false)
			{
				otherWindow.removeUntil(otherWindowChainEndPosition);
			}
			
			//try and fill the buffers a little more 
			readIntoWindow(baseWindow, baseInputStreamBreaker, windowSize - baseWindowItems.size());
			
			//fill other window
			readIntoWindow(otherWindow, otherInputStreamBreaker, windowSize - otherWindowItems.size());
			
			//process all of the matches
			for (WindowItem baseWindowItem : baseWindow.getWindowItems())
			{
				if (otherWindow.hasMatch(baseWindowItem) == true)
				{			
					baseWindowItem.addMatches(otherWindow.getMatches(baseWindowItem));				
				}
				else
				{
					//skip
				}
			}
			
			//printMatchTable(baseWindow, otherWindow);
		}
		
		
	 outputStream.flush();
	 outputStream.close();
		
	}
	
	/**
	 * Creates a DiffEntry from our current processing data
	 * @param side
	 * @param basePosition
	 * @param otherPosition
	 * @param outputPosition
	 * @param data
	 * @param outputStream
	 * @param addLineDelimiter
	 * @throws Exception
	 */
	private void writeLine(Side side, long basePosition,long otherPosition,long outputPosition, byte[] data, OutputStream outputStream,boolean addLineDelimiter) throws Exception
	{
		//this is probably a little heavy but it insures that the format of the stream is the same for everyone
		DiffEntry diffEntry = new DiffEntry(side, data.length, basePosition, otherPosition, data);
		outputStream.write(diffEntry.toByteArray());
	}
	
	
	
	/**
	 * Reads the data from the TokenizedInputStream into the window
	 * @param window
	 * @param inputStreamTokenizer to read from 
	 * @param numberOfLinesToRead
	 */
	private void readIntoWindow(Window window, InputStreamTokenizer inputStreamTokenizer, int count) throws Exception
	{		
		for(int readCount = 0; readCount < count; readCount++)
		{			
			byte[] readLine = inputStreamTokenizer.readBytes();
			
			if (readLine.length != 0)
			{				
				window.addWindowItem(readLine);
			}
			else
			{
				break;
			}			
		}		
	}

	
	/**
	 * This is used for debugging, and kinda cool, so I left it in.
	 * @param baseWindow
	 * @param otherWindow
	 */	
	public static void printMatchTable(Window baseWindow, Window otherWindow)
	{
		System.out.print("\n\t\t\t  ");
		for (WindowItem otherWindowItem : otherWindow.getWindowItems())
		{
			
			System.out.print(String.format(" %02d", otherWindowItem.getStreamPosition()));
		}
		System.out.println();
		for (WindowItem baseWindowItem : baseWindow.getWindowItems())
		{
			
			if (baseWindowItem == null)
			{
				break;
			}
			
			System.out.print(String.format("%016x\t%02d", baseWindowItem.getDataHashCode(),baseWindowItem.getStreamPosition()));
			for (WindowItem otherWindowItem : otherWindow.getWindowItems())
			{
				if (otherWindowItem == null)
				{
					break;
				}
				if (otherWindowItem.getDataHashCode() == baseWindowItem.getDataHashCode())
				{
					System.out.print("|"+baseWindowItem.getChainID(otherWindowItem));
				}
				else
				{
					System.out.print("|  ");
				}
			}
			System.out.println("|");
			
		}
	}
	

	
	
}
