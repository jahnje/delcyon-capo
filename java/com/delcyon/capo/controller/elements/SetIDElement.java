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
import com.delcyon.capo.resourcemanager.ResourceParameter;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.Action;
import com.delcyon.capo.resourcemanager.types.ContentMetaData;
import com.delcyon.capo.resourcemanager.types.FileResourceContentMetaData.FileAttributes;
import com.delcyon.capo.resourcemanager.types.FileResourceDescriptor;
import com.delcyon.capo.util.CommandExecution;
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
	
	
	
	private static final String[] supportedNamespaces = {CapoApplication.SERVER_NAMESPACE_URI};
	
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

		//load the clients identity.xml file
		ResourceDescriptor clientResourceDescriptor = CapoApplication.getDataManager().getResourceDescriptor(this, "clients:"+clientID+"/"+identityControlName);
		ContentMetaData clientResourceMetaData = clientResourceDescriptor.getResourceMetaData(getParentGroup()); 
		if (clientResourceMetaData.exists() == false)
		{
		    clientResourceDescriptor.performAction(getParentGroup(), Action.CREATE);
		    CapoApplication.logger.log(Level.INFO,"Creating new identity document for "+clientID);
		    XPath.dumpNode(CapoApplication.getDefaultDocument("ids.xml"), clientResourceDescriptor.getOutputStream(getParentGroup()));
		}

		/*START symlink code
		 * java as of version 6 doesn't support symlinks, so we do most of this via the command line
		 */
		
		//get the clients directory, so we can get a simple location for it to use with symlinks
		ResourceDescriptor clientDirResourceDescriptor = CapoApplication.getDataManager().getResourceDescriptor(this, "clients:"+clientID);
				
		ContentMetaData clientDirResourceMetaData = clientDirResourceDescriptor.getResourceMetaData(getParentGroup());
		String baseURI = clientDirResourceDescriptor.getResourceURI().getResourceURIString().replaceFirst("/"+clientID, "").replaceFirst("file:", "");
		clientDirResourceDescriptor.release(getParentGroup());
		
		//see if we have a place to put this kind of symlink, and if not, then create it.		
		ResourceDescriptor idTypeResourceDescriptor = CapoApplication.getDataManager().getResourceDescriptor(this, "clients:by-"+name);
		if(idTypeResourceDescriptor.getResourceMetaData(getParentGroup()).exists() == false)
		{		    
		    idTypeResourceDescriptor.performAction(getParentGroup(), Action.CREATE,new ResourceParameter(ResourceDescriptor.DefaultParameters.CONTAINER, "true"));
		}
		idTypeResourceDescriptor.release(getParentGroup());
		
		//now check and see if we already have a symlink for this name value pair
		ResourceDescriptor idResourceDescriptor = CapoApplication.getDataManager().getResourceDescriptor(this, "clients:by-"+name+"/"+value);		
		boolean createLink = false;
		if(idResourceDescriptor instanceof FileResourceDescriptor)
		{
		    //if we don't then indicate that we need to make a new one.
		    ContentMetaData idResourceMetaData = idResourceDescriptor.getResourceMetaData(getParentGroup()); 
		    if(idResourceMetaData.exists() == false)
		    {
		        createLink = true;
		    }
		    //if we do, verify that it points to this client, and not some other
		    else if(clientDirResourceMetaData.getValue(FileAttributes.canonicalPath).equals(idResourceMetaData.getValue(FileAttributes.canonicalPath)) == false)
		    {
		        //remove the old symlink, don't want to deal with unpredictable java symlink interaction, so use command line
		        CommandExecution commandExecution = new CommandExecution("cd "+baseURI+"; rm -f by-"+name+"/"+value,1000l);
		        commandExecution.executeCommand();
		        if(commandExecution.getExitCode() == 0)
		        {
		            CapoApplication.logger.log(Level.WARNING,"Removed incorrect link for by-"+name+"/"+value+" to "+idResourceMetaData.getValue(FileAttributes.canonicalPath));
		            //indicate we need to make a symlink
		            createLink = true;
		        }

		    }

		    //make a symlink using command line
		    if(createLink == true)
		    {		        
		        CommandExecution commandExecution = new CommandExecution("cd "+baseURI+"; ln -s ../"+clientID+" by-"+name+"/"+value,1000l);
		        commandExecution.executeCommand();
		        if(commandExecution.getExitCode() == 0)
		        {
		            CapoApplication.logger.log(Level.INFO,"Created link for "+clientID+" to "+idResourceDescriptor.getResourceURI().getResourceURIString());
		        }
		    }
		}
		//free up our last resource
		idResourceDescriptor.release(getParentGroup());
		
		Element identityDocumentElement = CapoApplication.getDocumentBuilder().parse(clientResourceDescriptor.getInputStream(getParentGroup())).getDocumentElement();
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
		    clientResourceDescriptor.getOutputStream(null).close();
		}                   


		
		return null;
	}

	
}
