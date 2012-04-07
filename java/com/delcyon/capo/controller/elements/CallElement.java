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

import org.w3c.dom.Element;

import com.delcyon.capo.controller.AbstractControl;
import com.delcyon.capo.controller.ControlElementProvider;
import com.delcyon.capo.controller.server.ServerSideControl;
import com.delcyon.capo.server.CapoServer;
import com.delcyon.capo.xml.XPath;

@ControlElementProvider(name="call")
public class CallElement extends AbstractControl
{

	public enum Attributes
	{
		ref
	}
	
	
	private static final String[] supportedNamespaces = {GroupElement.SERVER_NAMESPACE_URI};
	
	
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

	@Override
	public Object processServerSideElement() throws Exception
	{

		Element referencedElement = (Element) XPath.selectSingleNode(getControlElementDeclaration().getOwnerDocument().getDocumentElement(), getAttributeValue(Attributes.ref),getControlElementDeclaration().getPrefix());
		if (referencedElement != null)
		{
			ServerSideControl controlElement = (ServerSideControl) getControlElementInstanceForLocalName(referencedElement.getLocalName());
			controlElement.init(referencedElement, this, getParentGroup(), getControllerClientRequestProcessor());
			controlElement.processServerSideElement();		
		}
		else
		{
			CapoServer.logger.log(Level.SEVERE," no element found matching: "+getAttributeValue(Attributes.ref));
		}
	
		return null;
	}

	
}
