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
package com.delcyon.capo.parsers;

import java.util.Vector;

/**
 * @author jeremiah
 *
 */
public class ParseTape
{

	private int position = -1;
	private Vector<ParseToken> streamVector = new Vector<ParseToken>();
	private Tokenizer tokenizer = null;
	
	public ParseTape(Tokenizer tokenizer) throws Exception
	{
	    this.tokenizer = tokenizer;
	}
	
	public int getPosition()
	{
		return position;
	}
	
	public void setPosition(int position)
	{
		this.position = position;
	}
	
	public ParseToken next() throws Exception
	{
		position++;
		if(position < streamVector.size())
		{
			return streamVector.get(position);
		}
		else if(hasMore())
		{
		    tokenizer.nextToken();
		    streamVector.add(new ParseToken(tokenizer.getValue(),tokenizer.getTokenType()));
		    return streamVector.lastElement();
		}
		else
		{
		    position--;
		    return null;
		}
	}
	
	public void pushBack()
	{
		position--;
		if(position < -1)
		{
			position = -1;
		}
	}
	
	public ParseToken getCurrent()
	{
		if(position < streamVector.size())
		{
			return streamVector.get(position);
		}
		else
		{
			return null;
		}
	}
	
	public boolean hasMore() throws Exception
	{
		return tokenizer.hasMore();
	}
}
