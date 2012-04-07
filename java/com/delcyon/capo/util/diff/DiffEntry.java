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
 * This is in charge out parsing, and writing lines from/for the diff class. 
 * Everything has been put here, so that it is in one place. This is the only place that format should be changed otherwise things won't match up.
 * In general, it's a bad idea to mess around with this class unless the default diff stream format is causing you problems.
 * I think this format is esoteric enough to not run into common collisions out there.
 * <br/>format example: +(12,14)[12]hello world\n<br/>
 * Where the first char indicates the side ala std diff +/-/=
 * The two numbers in the parentheses are the tokenized stream positions for the base, and the modified file respectively.
 * The number in square brackets indicates the length of the data.
 * and everything afterwards is the data, including that newline char.      
 * @author jeremiah
 *
 */
public class DiffEntry 
{
	public static final String INPUT_REGEX_FORMAT = "(["+Side.MOD.getDirectionChar()+Side.BASE.getDirectionChar()+Side.BOTH.getDirectionChar()+"])\\((\\d+),(\\d+)\\)\\[(\\d+)\\]";
	public static final String INPUT_REGEX_REPLACEMENT = "$1,$2,$3,$4";
	public static final String INPUT_REPLACEMENT_SPLIT = ",";
	public static final String OUTPUT_FORMAT = "%c(%d,%d)[%d]";
	public static final char LINE_DESCRIPTOR_TERMINATOR_CHAR = OUTPUT_FORMAT.charAt(OUTPUT_FORMAT.length()-1);
	
	private char directionChar;
	private int expectedTextLength = 0;
	private long baseStreamPosition;
	private long otherStreamPosition;	
	private byte[] data = null;
	
	/**
	 * Used by the diff class to format an outgoing difference
	 * @param side
	 * @param expectedTextLength
	 * @param baseStreamPosition
	 * @param otherStreamPosition
	 * @param data
	 */
	public DiffEntry(Side side, int expectedTextLength, long baseStreamPosition, long otherStreamPosition, byte[] data)
	{
		if (side == null)
		{
			this.directionChar = Side.BOTH.getDirectionChar();
		}
		else
		{
			this.directionChar = side.getDirectionChar();
		}
		
		
		this.expectedTextLength = expectedTextLength;
		this.baseStreamPosition = baseStreamPosition;
		this.otherStreamPosition = otherStreamPosition;
		this.data = data;
	}
	
	/**
	 * Creates a DiffEntry from a line of a diff stream.
	 * @param lineData
	 * @return
	 */
	public static DiffEntry  parseLineData(byte[] lineData)
	{
		return new DiffEntry(lineData);
	}
	
	/**
	 * The parsing is done in the constructor. Probably a bad idea.
	 * @param lineData
	 */
	private DiffEntry(byte[] lineData)
	{
		String text = new String(lineData); 
		int lineDescriptorEndPosition = text.indexOf(LINE_DESCRIPTOR_TERMINATOR_CHAR);
		String[] descriptors = text.substring(0, lineDescriptorEndPosition+1).replaceAll(INPUT_REGEX_FORMAT, INPUT_REGEX_REPLACEMENT).split(INPUT_REPLACEMENT_SPLIT);
		this.data = text.substring(lineDescriptorEndPosition+1).getBytes();
		
		directionChar = descriptors[0].charAt(0);		
		baseStreamPosition = Long.parseLong(descriptors[1]);			
		otherStreamPosition = Long.parseLong(descriptors[2]);
		expectedTextLength = Integer.parseInt(descriptors[3]);
	}

	/**
	 * return the char for this line +/-/= 
	 */
	public char getDirectionChar()
	{
		return directionChar;
	}

	/**	 
	 * @return the length of the data for this entry. minus any diff information, and minus any trailing data. this uses the info stored in the diff entry itself.  
	 */
	public int getExpectedTextLength()
	{
		return expectedTextLength;
	}

	/**
	 * 
	 * @return where this entry is relative to the original stream
	 */
	public long getBaseStreamPosition()
	{
		return baseStreamPosition;
	}

	/**
	 * @return where this entry is relative to the modified stream
	 */
	public long getOtherStreamPosition()
	{
		return otherStreamPosition;
	}

	/**
	 * the data stored in the diff entry, and not the whole entry itself
	 * @return
	 */
	public byte[] getData()
	{
		return data;
	}
	
	/**
	 * @return this diff entry as a byte array, with diff meta data included 
	 */
	public byte[] toByteArray()
	{
		String position = String.format(OUTPUT_FORMAT, getDirectionChar(),getBaseStreamPosition()+1l,getOtherStreamPosition()+1l,data.length);		
		byte[] bytes = new byte[position.length()+data.length];
		System.arraycopy(position.getBytes(), 0, bytes, 0, position.length()); //store the position information
		System.arraycopy(data, 0, bytes, position.length(), data.length); //store the data		
		return bytes;
	}
	
	/**
	 * returns the results of toByteArray as a String for convenience and debugging.
	 */
	@Override
	public String toString()
	{
		return new String(toByteArray());
	}
	
}