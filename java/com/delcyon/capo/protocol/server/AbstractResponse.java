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

/**
 * @author jeremiah
 *
 */
public abstract class AbstractResponse implements Response
{
	
	private String sessionID;
	private transient Document responseDocument;
	
	public AbstractResponse(Document responseDocument)
	{
		this.responseDocument = responseDocument;
	}
	
	public void setSessionID(String sessionID)
	{
		this.sessionID = sessionID;
		
	}

	public String getSessionID()
	{
		return sessionID;
	}

	public Document getResponseDocument()
	{
		return responseDocument;
	}

	public void appendElement(Element element)
	{
		responseDocument.getDocumentElement().appendChild(responseDocument.importNode(element,true));
	}
}
