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

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.AbstractControl;
import com.delcyon.capo.controller.ControlElementProvider;
import com.delcyon.capo.xml.XPath;

/**
 * @author jeremiah
 * Deletes a node from the XML
 */
@ControlElementProvider(name="replace")
public class ReplaceElement extends AbstractControl
{

	
	
	private enum Attributes
	{
		name,srcRef,dstRef
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
		return new Attributes[]{Attributes.srcRef,Attributes.dstRef};
	}

	
	@Override
	public String[] getSupportedNamespaces()
	{
		return supportedNamespaces;
	}


	@Override
	public Object processServerSideElement() throws Exception
	{
		
		String srcRef = getAttributeValue(Attributes.srcRef);
		String dstRef = getAttributeValue(Attributes.dstRef);
		getControlElementDeclaration().replaceChild(XPath.selectSingleNode(getControlElementDeclaration(), srcRef),XPath.selectSingleNode(getControlElementDeclaration(), dstRef));		
		return null;
	}

}
