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

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.AbstractControl;
import com.delcyon.capo.controller.ControlElementProvider;
import com.delcyon.capo.controller.server.ControllerClientRequestProcessor.Preferences;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.Action;
import com.delcyon.capo.xml.XPath;

/**
 * @author jeremiah
 * Appends a node to the XML
 */
@ControlElementProvider(name="setID")
public class SetIDElement extends AbstractControl
{

	
	
	public enum Attributes
	{
		name,value
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
		return new Attributes[]{Attributes.name,Attributes.value};
	}

	
	@Override
	public String[] getSupportedNamespaces()
	{
		return supportedNamespaces;
	}

	
	
	@Override
	public Object processServerSideElement() throws Exception
	{
		String clientID = getParentGroup().getVarValue("clientID"); 
	    if (clientID == null)
	    {
	        throw new Exception("Cannot set ID on an un authenticated client");
	    }
		String name = getAttributeValue(Attributes.name);
		String value = getAttributeValue(Attributes.value);
		if (name.isEmpty() || value.isEmpty())
		{
		    throw new Exception("Name '"+name+"' and value '"+value+"' must be set");
		}
		
		//check and see if this is an identity push, identity information gets saved for later use
		String identityControlName = CapoApplication.getConfiguration().getValue(Preferences.DEFAULT_IDENTITY_FILE);

		ResourceDescriptor clientResourceDescriptor = CapoApplication.getDataManager().getResourceDescriptor(null, "clients:"+clientID+"/"+identityControlName);
		if (clientResourceDescriptor.getContentMetaData(null).exists() == false)
		{
		    clientResourceDescriptor.performAction(null, Action.CREATE);
		    CapoApplication.logger.log(Level.INFO,"Creating new identity document for "+clientID);
		    XPath.dumpNode(CapoApplication.getDefaultDocument("ids.xml"), clientResourceDescriptor.getOutputStream(null));
		}

		Element identityDocumentElement = CapoApplication.getDocumentBuilder().parse(clientResourceDescriptor.getInputStream(null)).getDocumentElement();
		String oldMD5 = XPath.getElementMD5(identityDocumentElement);
		
		Element idElement = (Element) XPath.selectSingleNode(identityDocumentElement, "//server:id[@name = '"+name+"']");
		if (idElement != null)
		{
		    idElement.setAttribute("value", value);
		}
		else
		{
		    idElement = identityDocumentElement.getOwnerDocument().createElement("server:id");
		    idElement.setAttribute("name", name);
		    idElement.setAttribute("value", value);
		    identityDocumentElement.appendChild(idElement);
		}
		String newMD5 = XPath.getElementMD5(identityDocumentElement);
		if (oldMD5.equals(newMD5) == false)
		{
		    CapoApplication.logger.log(Level.INFO, "Updating identity document for "+clientID);
		    XPath.dumpNode(identityDocumentElement, clientResourceDescriptor.getOutputStream(null));        
		}                   


		
		return null;
	}

	
}
