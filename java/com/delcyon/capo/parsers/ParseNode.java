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

import com.delcyon.capo.util.EqualityProcessor;
import com.delcyon.capo.xml.cdom.CElement;


/**
 * @author jeremiah
 *
 */
public class ParseNode extends CElement
{

	private ParseNode(){};//clone
	
	public ParseNode(String name)
	{
		super(name);
	}

	@Override
	protected ParseNode clone() 
	{
		
		
		try
		{
			return EqualityProcessor.clone(this);
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

}
