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

import org.tanukisoftware.wrapper.WrapperManager;
import org.w3c.dom.Element;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.client.CapoClient.Preferences;
import com.delcyon.capo.controller.AbstractClientSideControl;
import com.delcyon.capo.controller.ControlElementProvider;
import com.delcyon.capo.controller.client.ClientSideControl;
import com.delcyon.capo.xml.XPath;

/**
 * @author jeremiah
 *
 */
@ControlElementProvider(name="restart")
public class RestartElement extends AbstractClientSideControl implements ClientSideControl
{

	
	
	private enum Attributes
	{
		name
	}
	
	
	private static final String[] supportedNamespaces = {CapoApplication.SERVER_NAMESPACE_URI,CapoApplication.CLIENT_NAMESPACE_URI};
	
	
	
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
	public Element processClientSideElement() throws Exception
	{
	    
	    if(CapoApplication.getConfiguration().getBooleanValue(Preferences.IGNORE_RESTART_REQUEST) == false)
	    {
	        CapoApplication.logger.log(Level.WARNING, "Attempting to restart due to server request.");
	        WrapperManager.restartAndReturn();
	    }
	    else
	    {
	        CapoApplication.logger.log(Level.WARNING, "Ignoring restart request because of IGNORE_RESTART_REQUEST preference. See config file.");
	    }
		//Return something here so that the server stops waiting for a document to read, and will shut down the connection. 
		return (Element)(getControlElementDeclaration().cloneNode(true));
	}
	
	
	@Override
	public Object processServerSideElement() throws Exception
	{
		if (getControlElementDeclaration().getNamespaceURI().equals(CapoApplication.CLIENT_NAMESPACE_URI))
		{
		    CapoApplication.logger.log(Level.INFO, "Asking client to restart.");
			getControllerClientRequestProcessor().sendServerSideClientElement((Element) getParentGroup().replaceVarsInAttributeValues(getControlElementDeclaration().cloneNode(true)));
		}
		else
		{
		    CapoApplication.logger.log(Level.WARNING, "Attempting to restart server due to request at: "+XPath.getXPath(getControlElementDeclaration()));
			WrapperManager.restart();
		}
		
		return null;

	}

	
}
