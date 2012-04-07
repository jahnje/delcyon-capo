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
package com.delcyon.capo.exceptions;

import java.util.Arrays;

import org.w3c.dom.Element;

import com.delcyon.capo.xml.XPath;

/**
 * @author jeremiah
 *
 */
public class MissingAttributeException extends Exception
{
	
	@SuppressWarnings("unchecked")
	public MissingAttributeException(Enum[] missingAttributes, Element controlElementDeclaration) throws Exception
	{
		super("Missing required attribute(s) "+Arrays.asList(missingAttributes)+" on "+XPath.getXPath(controlElementDeclaration));
	}
}
