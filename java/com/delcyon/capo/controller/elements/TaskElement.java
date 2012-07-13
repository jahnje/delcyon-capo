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
import com.delcyon.capo.controller.AbstractClientSideControl;
import com.delcyon.capo.controller.ControlElementProvider;
import com.delcyon.capo.controller.server.ControllerResponse;
import com.delcyon.capo.tasks.TaskManagerThread;

/**
 * @author jeremiah
 * Appends a node to the XML
 */
@ControlElementProvider(name="task")
public class TaskElement extends AbstractClientSideControl
{

	
	
	public enum Attributes
	{
		name,lastAccessTime,local,initialGroup,executionInterval,lastExecutionTime,lifeSpan,orpanAction, taskURI, MD5
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
		return new Attributes[]{Attributes.name
		        };
	}

	
	@Override
	public String[] getSupportedNamespaces()
	{
		return supportedNamespaces;
	}

	
	
	@Override
	public Object processServerSideElement() throws Exception
	{
		//process attribute values
		getParentGroup().replaceVarsInAttributeValues(getControlElementDeclaration());
		getParentGroup().setVars(getControlElementDeclaration());
		
		String name = getValue(Attributes.name);		
		boolean local = getBooleanValue(Attributes.local);
		
		
		//generate md5 for element
		String md5 = getControlElementMD5();
		
		if (local == true)
		{
			TaskManagerThread.getTaskManagerThread().setTask(name,md5,this,null);
		}
		else
		{
			ControllerResponse controllerResponse = getControllerClientRequestProcessor().createResponse();
			controllerResponse.appendElement(getControlElementDeclaration());
			getControllerClientRequestProcessor().writeResponse(controllerResponse);
			TaskManagerThread.getTaskManagerThread().setTask(name,md5,this,getParentGroup().getVarValue("clientID"));
		}
		//see if we have a matching monitor on the local or remote
		//if we don't, then send the whole package to the receiver
		
				
		return null;
	}


	@Override
	public Element processClientSideElement() throws Exception
	{
		getParentGroup().replaceVarsInAttributeValues(getControlElementDeclaration());
		getParentGroup().setVars(getControlElementDeclaration());
		
		String name = getValue(Attributes.name);		
		String md5 = getControlElementMD5();
		TaskManagerThread.getTaskManagerThread().setTask(name,md5,this,null);
		return null;
	}


	

	
	
	
}
