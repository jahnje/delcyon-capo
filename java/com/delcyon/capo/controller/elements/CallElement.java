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

import java.util.Arrays;
import java.util.logging.Level;

import org.w3c.dom.Element;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.AbstractControl;
import com.delcyon.capo.controller.ControlElement;
import com.delcyon.capo.controller.ControlElementProvider;
import com.delcyon.capo.controller.Group;
import com.delcyon.capo.controller.server.ControllerClientRequestProcessor;
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
	
	
	private static final String[] supportedNamespaces = {CapoApplication.SERVER_NAMESPACE_URI};
    private ServerSideControl controlElement;
	
	
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
	public Enum[] getMissingAttributes()
	{
	    Enum[] missingAttributes = super.getMissingAttributes(); 
	    if(missingAttributes.length == 0)
	    {
	        missingAttributes = controlElement.getMissingAttributes();
	        if(missingAttributes.length != 0)
	        {
	            try
	            {
	                CapoApplication.logger.warning("Missing required attribute(s) "+Arrays.asList(missingAttributes)+" on "+XPath.getXPath(controlElement.getControlElementDeclaration()));
	            }
	            catch (Exception e){}
	        }
	    }

	    return missingAttributes;

	}
	
	
	@Override
	public String[] getSupportedNamespaces()
	{
		return supportedNamespaces;
	}

	@Override
	public void init(Element controlElementDeclaration, ControlElement parentControlElement, Group parentGroup, ControllerClientRequestProcessor controllerClientRequestProcessor) throws Exception
	{
	    
	    super.init(controlElementDeclaration, parentControlElement, parentGroup, controllerClientRequestProcessor);
	    Element referencedElement = (Element) XPath.selectSingleNode(getControlElementDeclaration().getOwnerDocument().getDocumentElement(), getAttributeValue(Attributes.ref),getControlElementDeclaration().getPrefix());
	    if (referencedElement != null)
        {
            controlElement = (ServerSideControl) getControlElementInstanceForLocalName(referencedElement.getLocalName());
            controlElement.init(referencedElement, this, getParentGroup(), getControllerClientRequestProcessor());
                  
        }
        else
        {
            CapoServer.logger.log(Level.SEVERE," no element found matching: "+getAttributeValue(Attributes.ref));
        }
	}
	
	
	@Override
	public Object processServerSideElement() throws Exception
	{
	
		return controlElement.processServerSideElement();
	}

	
}
