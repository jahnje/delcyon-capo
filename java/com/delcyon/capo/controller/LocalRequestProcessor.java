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

import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.delcyon.capo.ContextThread;
import com.delcyon.capo.controller.elements.GroupElement;
import com.delcyon.capo.controller.server.ControllerClientRequestProcessor;

/**
 * @author jeremiah
 *
 */
public class LocalRequestProcessor extends ControllerClientRequestProcessor
{

	public GroupElement process(Document localDocument) throws Exception
	{
		return process(localDocument.getDocumentElement(),null);		
	}
	
	public GroupElement process(Element localElement, HashMap<String, String> variableHashMap) throws Exception
	{
		GroupElement groupElement = new GroupElement();
		
		groupElement.init(localElement, null, null, this);
		if (variableHashMap != null)
		{
			groupElement.getGroup().setVariableHashMap(variableHashMap);
		}
		//process attribute values
		groupElement.getGroup().replaceVarsInAttributeValues(localElement);
		groupElement.getGroup().setVars(localElement);
		
		return process(groupElement);
	}
	
	public GroupElement process(GroupElement groupElement) throws Exception
	{
		ContextThread contextThread = null;
		if (Thread.currentThread() instanceof ContextThread)
		{
			contextThread = (ContextThread) Thread.currentThread();
			contextThread.setContext(groupElement);
		}
		
		
		groupElement.processServerSideElement();
		if (contextThread != null)
		{
			contextThread.setContext(null);
		}
		
		return groupElement;
	}

	
}
