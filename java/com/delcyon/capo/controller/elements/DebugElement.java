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

import java.util.logging.Level;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.AbstractControl;
import com.delcyon.capo.controller.ControlElementProvider;
import com.delcyon.capo.xml.XPath;

@ControlElementProvider(name="debug")
public class DebugElement extends AbstractControl
{

	public enum Attributes
	{
		dumpRef,
		dumpVars,
		sleep
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
		return new Attributes[]{};
	}

	
	@Override
	public String[] getSupportedNamespaces()
	{
		return supportedNamespaces;
	}

	@Override
	public Object processServerSideElement() throws Exception
	{

		CapoApplication.logger.log(Level.WARNING, "Found debug at "+XPath.getXPath(getControlElementDeclaration()));
		debugElement(this);
		return null;
	}

	public static void debugElement(AbstractControl control) throws Exception
	{

		if (control.getAttributeBooleanValue(Attributes.dumpVars) == true)
		{
			System.err.println("=====================================DEBUG VAR DUMP===========================================================");
			control.getParentGroup().dumpVars(System.err);
			System.err.println("=====================================END DEBUG VAR DUMP===========================================================");
		}
		if (control.getAttributeValue(Attributes.dumpRef).trim().isEmpty() == false)
		{
			System.err.println("=====================================DEBUG REF DUMP===========================================================");
			XPath.dumpNode(XPath.selectSingleNode(control.getControlElementDeclaration(), control.getAttributeValue(Attributes.dumpRef)), System.err);
			System.err.println("=====================================END DEBUG===========================================================");
		}
		if(control.getAttributeValue(Attributes.sleep).trim().matches("\\d+"))
		{
			System.err.println("DEBUG=> sleeping for "+control.getAttributeValue(Attributes.sleep)+"ms");
			Thread.sleep(Long.parseLong(control.getAttributeValue(Attributes.sleep)));
			System.err.println("DEBUG=> done sleeping");
		}
	}
	
}
