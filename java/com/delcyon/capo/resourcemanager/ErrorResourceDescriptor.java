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
package com.delcyon.capo.resourcemanager;

import java.io.InputStream;

import org.w3c.dom.Node;

import com.delcyon.capo.controller.ControlElement;

/**
 * @author jeremiah
 *
 */
public interface ErrorResourceDescriptor extends ResourceDescriptor
{
	/** Requires that the resource support the ERROR stream type, and the STREAM format on the Error Stream */
	public InputStream getErrorStream(ControlElement callingControlElement) throws Exception;
	
	/** Requires that the resource support the ERROR stream type, and the XML_BLOCK format on the Error Stream */
	public Node readXMLError(ControlElement callingControlElement) throws Exception;
	
	/** Requires that the resource support the ERROR stream type, and the BLOCK format on the Error Stream */
	public byte[] read(ControlElement callingControlElement) throws Exception;
}
