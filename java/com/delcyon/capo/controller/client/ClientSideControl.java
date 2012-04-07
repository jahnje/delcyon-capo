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
package com.delcyon.capo.controller.client;

import org.w3c.dom.Element;

import com.delcyon.capo.controller.ControlElement;
import com.delcyon.capo.controller.Group;

/**
 * @author jeremiah
 *
 */
public interface ClientSideControl
{
	
	public void init(Element controlElementDeclaration, ControlElement parentControlElement, Group parentGroup,ServerControllerResponse serverControllerResponse) throws Exception;
	/**
	 * 
	 * @return the resultant element of process call. Can be null. And wil be tied to a different document, so it must be imported or cloned, to really be used for anything else
	 * @throws Exception
	 */
	public Element processClientSideElement() throws Exception;
	
	public ServerControllerResponse getServerControllerResponse();
}
