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
package com.delcyon.capo.protocol.server;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.delcyon.capo.server.CapoServer;

/**
 * @author jeremiah
 *
 */
public class XMLResponse
{

	
	private Document responseDocument;

	public XMLResponse() throws Exception
	{
		this.responseDocument = CapoServer.getDefaultDocument("default_response.xml");
	}
	
	public void setSessionID(String sessionID)
	{
		
		this.responseDocument.getDocumentElement().setAttribute(ClientRequest.SESSION_ID_ATTRIBUTE_NAME, sessionID);
	}

	public String getSessionID()
	{
		String sessionID = this.responseDocument.getDocumentElement().getAttribute(ClientRequest.SESSION_ID_ATTRIBUTE_NAME);
		if (sessionID == null || sessionID.trim().isEmpty())
		{
			return null;
		}
		else
		{
			return sessionID;
		}		
	}
	
	public Element setResponseElement(Element responseNode)
	{		
		return (Element) responseDocument.getDocumentElement().appendChild(responseDocument.importNode(responseNode, true));
	}

	public Document getResponseDocument()
	{
		return responseDocument;
	}

}
