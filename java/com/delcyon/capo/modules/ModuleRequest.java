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
package com.delcyon.capo.modules;

import org.w3c.dom.Element;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.protocol.client.CapoConnection;
import com.delcyon.capo.protocol.client.XMLRequest;

/**
 * @author jeremiah
 *
 */
public class ModuleRequest extends XMLRequest
{

	public enum Attributes
	{
		moduleName
	}
	
	private CapoConnection capoConnection;
	private String moduleName;
	
	
	
	public ModuleRequest(CapoConnection capoConnection,String moduleName) throws Exception
	{
		
		super();
		this.moduleName = moduleName;
		this.capoConnection = capoConnection;
	}

	@Override
	public void init() throws Exception
	{
		setInputStream(capoConnection.getInputStream());
		setOutputStream(capoConnection.getOutputStream());
		super.init();
	}
	
	@Override
	public Element getChildRootElement() throws Exception
	{
		Element updaterRequestElement =  CapoApplication.getDefaultDocument("module_request.xml").getDocumentElement();
		updaterRequestElement.setAttribute(Attributes.moduleName.toString(), moduleName);		
		return updaterRequestElement;
	}
		
}
