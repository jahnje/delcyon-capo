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
package com.delcyon.capo.controller;

import org.w3c.dom.Element;

import com.delcyon.capo.controller.server.ControllerClientRequestProcessor;


/**
 * @author jeremiah
 *
 */
public interface ControlElement
{
	
	
	public static final String CLIENT_NAMESPACE_URI = "http://www.delcyon.com/capo-client";
	public static final String SERVER_NAMESPACE_URI = "http://www.delcyon.com/capo-server";
	
	public String[] getSupportedNamespaces();
	
	public String getElementName();
	@SuppressWarnings("unchecked")
	public Enum[] getAttributes();
	@SuppressWarnings("unchecked")
	public Enum[] getRequiredAttributes();
	@SuppressWarnings("unchecked")
	public Enum[] getMissingAttributes();
	public void destroy() throws Exception;
	public Group getParentGroup();
	public Element getControlElementDeclaration();
	public Element getOriginalControlElementDeclaration(); //TODO remove, unused
	public ControlElement getParentControlElement();
	public ControllerClientRequestProcessor getControllerClientRequestProcessor();
}
