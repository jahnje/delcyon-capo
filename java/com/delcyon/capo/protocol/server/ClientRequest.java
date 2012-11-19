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

import java.io.BufferedInputStream;
import java.io.OutputStream;

import org.w3c.dom.Document;

import com.delcyon.capo.xml.XMLStreamProcessor;

/**
 * @author jeremiah
 *
 */
public class ClientRequest
{

	public static final String SESSION_ID_ATTRIBUTE_NAME = "sessionId";
	
	private Document requestDocument;
	private XMLStreamProcessor xmlStreamProcessor;
	private OutputStream outputStream;
	private String sessionID;
    private ClientRequestXMLProcessor clientRequestXMLProcessor;

	public ClientRequest(ClientRequestXMLProcessor clientRequestXMLProcessor, Document requestDocument, XMLStreamProcessor xmlStreamProcessor, OutputStream outputStream)
	{
		this.requestDocument = requestDocument;
		this.sessionID = requestDocument.getDocumentElement().getAttribute(SESSION_ID_ATTRIBUTE_NAME);
		this.xmlStreamProcessor = xmlStreamProcessor;
		this.outputStream = outputStream;
		this.clientRequestXMLProcessor = clientRequestXMLProcessor;
	}

	public Document getRequestDocument()
	{
		return requestDocument;
	}
	
	public OutputStream getOutputStream()
	{
		return outputStream;
	}

	public ClientRequestXMLProcessor getClientRequestXMLProcessor()
    {
        return clientRequestXMLProcessor;
    }
	
	public XMLStreamProcessor getXmlStreamProcessor()
	{
		return xmlStreamProcessor;
	}

	public void setSessionID(String sessionID)
	{
		this.sessionID = sessionID;
		
	}
	
	public String getSessionID()
	{
		return sessionID;
	}

	public BufferedInputStream getInputStream()
	{
		return xmlStreamProcessor.getInputStream();
	}

    public void finish() throws Exception
    {
        getOutputStream().write(("FINISHED:"+sessionID).getBytes());
        getOutputStream().write(0);
        getOutputStream().flush();
    }
	
}
