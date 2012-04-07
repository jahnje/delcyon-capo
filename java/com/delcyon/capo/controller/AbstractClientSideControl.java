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

import com.delcyon.capo.controller.client.ClientSideControl;
import com.delcyon.capo.controller.client.ServerControllerResponse;

/**
 * @author jeremiah
 *
 */
public abstract class AbstractClientSideControl extends AbstractControl implements ClientSideControl
{

	private ServerControllerResponse serverControllerResponse;

	@Override
	public void init(Element controlElementDeclaration, ControlElement parentControlElement, Group parentGroup, ServerControllerResponse serverControllerResponse) throws Exception
	{
		setParentGroup(parentGroup);
		setOriginalControlElementDeclaration(controlElementDeclaration);
		if (parentGroup != null)
		{
			setControlElementDeclaration((Element)parentGroup.replaceVarsInAttributeValues((controlElementDeclaration.cloneNode(true))));
		}
		else
		{
			setControlElementDeclaration((Element) (controlElementDeclaration.cloneNode(true)));
		}
		setParentControlElement(parentControlElement);		
		this.serverControllerResponse = serverControllerResponse;
		
	}

	@Override
	public ServerControllerResponse getServerControllerResponse()
	{
		return this.serverControllerResponse;
	}
	
}
