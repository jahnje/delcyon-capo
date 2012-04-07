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
package com.delcyon.capo.xml;

import java.util.List;

import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;

/**
 * @author jeremiah
 *
 */
public class CapoXPathFunction implements XPathFunction
{

	private XPathFunctionProcessor xPathFunctionProcessor;
	private String name;

	public CapoXPathFunction(String name, XPathFunctionProcessor xPathFunctionProcessor)
	{
		this.name = name;
		this.xPathFunctionProcessor = xPathFunctionProcessor;
	}

	/* (non-Javadoc)
	 * @see javax.xml.xpath.XPathFunction#evaluate(java.util.List)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object evaluate(List args) throws XPathFunctionException
	{
		try
		{
			return xPathFunctionProcessor.processFunction(name, args.toArray());
		}
		catch (Exception exception)
		{			
			throw new XPathFunctionException("Couldn't process XPath function: "+name+" because "+exception.getMessage());
		}
	}

}
