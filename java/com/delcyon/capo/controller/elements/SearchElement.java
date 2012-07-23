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
package com.delcyon.capo.controller.elements;

import org.w3c.dom.Element;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.AbstractControl;
import com.delcyon.capo.controller.ControlElement;
import com.delcyon.capo.controller.ControlElementProvider;
import com.delcyon.capo.controller.Group;
import com.delcyon.capo.controller.server.ControllerClientRequestProcessor;

@ControlElementProvider(name="search")
public class SearchElement extends AbstractControl
{

	public enum Attributes
	{
		name
	}
	
	
	private static final String[] supportedNamespaces = {CapoApplication.SERVER_NAMESPACE_URI};
	
	
	@Override
	public Attributes[] getAttributes()
	{
		return Attributes.values();
	}
	
	
	@Override
	public Attributes[] getRequiredAttributes()
	{
		return Attributes.values();
	}

	
	@Override
	public String[] getSupportedNamespaces()
	{
		return supportedNamespaces;
	}

	private String groupName;
	private Group group;
	
	@Override
	public void init(Element controlElementDeclaration, ControlElement parentControlElement, Group parentGroup, ControllerClientRequestProcessor controllerClientRequestProcessor) throws Exception
	{
		super.init(controlElementDeclaration, parentControlElement, parentGroup, controllerClientRequestProcessor);
		this.groupName = controlElementDeclaration.getAttribute(Attributes.name.toString());
		this.group = new Group(groupName,parentGroup,null,controllerClientRequestProcessor);
	}
	
	@Override
	public Object processServerSideElement() throws Exception
	{		
		processChildren(getControlElementDeclaration().getChildNodes(), group, this, getControllerClientRequestProcessor());
		return null;
	}

	
}
