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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * This will break up an InputStream according to a matching set of patterns. This should be usable for binary streams, but was originally written to handle text based streams.
 * The general contract is that you should create a StreamTokenizer with a custom ArrayList<ArrayList<Integer>> of match chars, or use one of the predefined tokenLists.
 * The maximum length of each list of integers is '8'. We use bit shifting for quick comparisons, and it is based on longs, which will start sliding off the end if we bit shift our bytes by more than 64 places, 8 for each byte   
 * You cannot construct this class with the CUSTOM TokenList. It is set automatically if you create w/ you own lists.
 * Once you have a instance, call readBytes() to get the next matching set of bytes from your stream that ends in a particular set of tokens (these will be included in the data you receive).
 * End of Streams are a little tricky, and will be available as the final getBytes(). 
 * This class does NOT close the InputStream.  
 * @author jeremiah
 *
 */
public class InputStreamTokenizer
{
	public enum TokenList
	{
		NEW_LINE(new char[][]{{'\n'},{'\r'},{'\r','\n'}}),
		WORD_BOUNDRY(new char[][]{{'\n'},{'\r'},{'\r','\n'},{'\t'},{' '}}),
		CUSTOM(new int[0][0]);
		
		private ArrayList<ArrayList<Integer>> tokenLists = null;
		
		private TokenList(char[][] tokensArray)
		{
			tokenLists = convertArrayIntoTokenLists(tokensArray);			
		}
		
		
		private TokenList(int[][] tokensArray)
		{
			tokenLists = convertArrayIntoTokenLists(tokensArray);
		}
		
		public ArrayList<ArrayList<Integer>> getTokenLists()
		{
			return tokenLists;
		}
		
	}
	
	/**
	 * Utility to convert a 2d array of ints into a 2d ArrayList of Integers 
	 * @param tokenListsArray
	 * @return
	 */
	public static ArrayList<ArrayList<Integer>> convertArrayIntoTokenLists(int[][] tokenListsArray)
	{
		ArrayList<ArrayList<Integer>> tokenLists = null;
		if (tokenListsArray.length != 0)
		{
			tokenLists = new ArrayList<ArrayList<Integer>>();
			for (int[] intTokenArray : tokenListsArray)
			{
				ArrayList<Integer> tokenArray = new ArrayList<Integer>();
				tokenLists.add(tokenArray);
				for (int i : intTokenArray)
				{
					tokenArray.add(i);
				}
			}
		}
		
		return tokenLists;
	}
	
	/**
	 * Utility to convert a 2d array of chars into a 2d ArrayList of Integers 
	 * @param tokenListsArray
	 * @return
	 */
	public static ArrayList<ArrayList<Integer>> convertArrayIntoTokenLists(char[][] tokenListsArray)
	{
		ArrayList<ArrayList<Integer>> tokenLists = null;
		if (tokenListsArray.length != 0)
		{
			tokenLists = new ArrayList<ArrayList<Integer>>();
			for (char[] charTokenArray : tokenListsArray)
			{
				ArrayList<Integer> tokenArray = new ArrayList<Integer>();
				tokenLists.add(tokenArray);
				for (int c : charTokenArray)
				{
					tokenArray.add((int)c);
				}
			}
		}
		
		return tokenLists;
	}
	
	
	private InputStream inputStream;	
	private int longestArraySize = 0;
	private int value = -1;
	private ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	private long[][] matches = null;
	private TokenList tokenList;
	private ArrayList<ArrayList<Integer>> tokenLists;
	
	/** 
	 * @param inputStream
	 * @param tokenList (You cannot use CUSTOM here!) If you want a custom list, use the other constructor, and CUSTOM will be set for you.
	 * @throws Exception
	 */
	public InputStreamTokenizer(InputStream inputStream, TokenList tokenList) throws Exception
	{
		if (tokenList == TokenList.CUSTOM)
		{
			throw new Exception("InputStreamTokenizer cannot be created with CUSTOM tokenList. ");
		}
		else
		{
			this.tokenList = tokenList;
			init(inputStream,tokenList.getTokenLists());
		}
	}
	
	/**
	 * Effective wraps a byte array in a stream using ByteArrayInputStream.
	 * Convenience method
	 * @param data
	 * @param tokenLists
	 */
	public InputStreamTokenizer(byte[] data, TokenList tokenList) throws Exception
	{
		if (tokenList == TokenList.CUSTOM)
		{
			throw new Exception("InputStreamTokenizer cannot be created with CUSTOM tokenList. ");
		}
		else
		{
			this.tokenList = tokenList;
			init(new ByteArrayInputStream(data),tokenList.getTokenLists());
		}
	}
	
	/**
	 * 
	 * @param inputStream stream to break into 'lines'
	 * @param tokenLists char arrays represented by integers that signify a line break. The classic would be int[] lineBreak = new int[]{(int)'\n',(int)'\r'}; as well as individual '\r' and '\n'  
	 * Be cause our matching algorithm uses bit shifting of integers, the maximum length of a line break is 4 chars. 
	 */
	public InputStreamTokenizer(InputStream inputStream, ArrayList<ArrayList<Integer>> tokenLists)
	{
		this.tokenList = TokenList.CUSTOM;
		init(inputStream,tokenLists);
	}

	/**
	 * Effective wraps a byte array in a stream using ByteArrayInputStream.
	 * Convenience method
	 * @param data
	 * @param tokenLists
	 */
	public InputStreamTokenizer(byte[] data, ArrayList<ArrayList<Integer>> tokenLists)
	{
		this.tokenList = TokenList.CUSTOM;
		init(new ByteArrayInputStream(data),tokenLists);
	}
	
	public TokenList getTokenList()
	{
		return tokenList;
	}
	
	public ArrayList<ArrayList<Integer>> getTokenLists()
	{
		return tokenLists;
	}
	
	private void init(InputStream inputStream, ArrayList<ArrayList<Integer>> tokenLists)
	{
		this.inputStream = inputStream;
		this.tokenLists = tokenLists;
		//look through all of out line breaks and fine the maximum size for the read ahead limit 
		for (ArrayList<Integer> arrayList : tokenLists)
		{
			if (arrayList.size() > longestArraySize)
			{
				longestArraySize = arrayList.size();
			}
		}
		matches = new long[tokenLists.size()][longestArraySize];

		//populate our 2d array
		for (int lineBreakIndex = 0; lineBreakIndex < tokenLists.size(); lineBreakIndex++)
		{
			ArrayList<Integer> lineBreakArray = tokenLists.get(lineBreakIndex);
			for (int index = 0; index < lineBreakArray.size(); index++)
			{
				if (index == 0)
				{
					matches[lineBreakIndex][index] = lineBreakArray.get(index);
				}
				else
				{
					//for each char beyond the first char bit shift the previous value to the left 8 places and then and our current value
					//this lest us test for a full match against all of the values at once, by bit shifting all of our previously read values during a match hit.
					//this is a little funky, but it seems to work, and requires less cpu and memory
					matches[lineBreakIndex][index] = (matches[lineBreakIndex][index] << 8) & lineBreakArray.get(index).longValue();
				}
			}
		}
	}
	
	
	public byte[] readBytes() throws IOException
	{
		buffer.reset();
		int columnIndex = 0;
		boolean firstLoop = true;
		long compareValue = 0l;
		boolean foundMatch = false;
		while(true)
		{
			if (firstLoop == true && value != -1)
			{
				//do nothing, because we already have a value from the last readline call, and it's not the EOF
				//this also lets us use a value of -1 to for reading even if we have a previous value, when that value is a match value
				firstLoop = false;
			}
			else
			{
				value = inputStream.read();
				firstLoop = false;
			}
			
			if (value == -1)
			{
				break;
			}
			else
			{
				
				foundMatch = false;
				
				if (columnIndex == 0) //if we aren't in a match, then no strange bit math is needed
				{
					compareValue = (long)value;
				}
				else //otherwise we need to and our current value with the previously left shifted value that's still stored in the compare value. 
				{
					compareValue = compareValue & (long)value; //we can do a straight addition here, because we have already shifted things to the left by this point
				}
				
				for (long[] row : matches)
				{
					long matchValue = row[columnIndex];

					if (matchValue == 0l) //skip any uninitialized array entry
					{
						continue;
					}
					
					if (compareValue == matchValue) //found a match, so write it out, and increment things
					{
						compareValue = compareValue << 8; //slide compare value over for next iteration
						columnIndex++;
						buffer.write(value);
						foundMatch = true;
						break;
					}
				}
				
				if (foundMatch == false)
				{
					if (columnIndex > 0) //return the buffer if we don't have a match but we did have one and had incremented the column index
					{
						return buffer.toByteArray();
					}
					else //this isn't a match, and we haven't yet found one so just add it to the buffer
					{
						buffer.write(value);
					}
				}						
				else if (columnIndex == longestArraySize) //we can't find any more things to match on since we've reached the max length of searches 
				{
					value = -1; //since we'll only get here if we've just found a match, and we don't want to add it in next time we read a line
					return buffer.toByteArray();
				}
			}
		}
		
		/*		
		 * This is a bad idea, as we would have to indicate that we've modified the data, and as far as i can tell, it's just not worth it. 
		 * If we don't indicate modification, then the original document will not match that produced by patching from the diff due to an additional new line char at the end.
		 * So might as well take the original GNU route, and have people just add their own new lines if they care about it that much.
		 * //we've reached the end of the file w/o a match 
		if (value == -1 && buffer.size() != 0  && tokenList != TokenList.CUSTOM && columnIndex == 0)
		{			
			buffer.write((int)'\n');			
		}
		*/
		return buffer.toByteArray();
	}
}