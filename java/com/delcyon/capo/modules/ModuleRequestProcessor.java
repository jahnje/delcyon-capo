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

import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.server.ControllerProcessingException;
import com.delcyon.capo.protocol.server.AbstractClientRequestProcessor;
import com.delcyon.capo.protocol.server.ClientRequest;
import com.delcyon.capo.protocol.server.ClientRequestProcessorProvider;
import com.delcyon.capo.protocol.server.ClientRequestXMLProcessor;
import com.delcyon.capo.xml.XPath;

/**
 * @author jeremiah
 *
 */
@ClientRequestProcessorProvider(name="ModuleRequest")
public class ModuleRequestProcessor  extends AbstractClientRequestProcessor 
{

	public enum Attributes
	{
	    ERROR
	}
	
	private String sessionID = null;
	
	@Override
	public String getSessionId()
	{
		return sessionID;
	}
	@Override
	public void init(ClientRequestXMLProcessor clientRequestXMLProcessor, String sessionID, HashMap<String, String> sessionHashMap,String requestName) throws Exception
	{ 
		this.sessionID = sessionID;
	}

	@Override
	public void process(ClientRequest clientRequest) throws Exception
	{
		

		String moduleName = XPath.selectSingleNodeValue(clientRequest.getRequestDocument().getDocumentElement(), "//ModuleRequest/@"+ModuleRequest.Attributes.moduleName);
		
		if (moduleName == null || moduleName.trim().isEmpty())
		{
			throw new ControllerProcessingException("ModuleRequest missing moduleName attribute", clientRequest.getRequestDocument());
		}
		else
		{
			Element moduleElement = ModuleProvider.getModuleElement(moduleName);
			if (moduleElement != null)
			{
			    
			    Document moduleDocument = CapoApplication.getDocumentBuilder().newDocument();
			    moduleDocument.appendChild(moduleDocument.importNode(moduleElement, true));			
			    clientRequest.getXmlStreamProcessor().writeDocument(moduleDocument);
			}
			else
			{			   
			    clientRequest.getRequestDocument().getDocumentElement().setAttribute(Attributes.ERROR.toString(), "Module Not Found");
                clientRequest.getXmlStreamProcessor().writeDocument(clientRequest.getRequestDocument());
			}
		}
	}

	@Override
	public Document readNextDocument() throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}

	
	
}
