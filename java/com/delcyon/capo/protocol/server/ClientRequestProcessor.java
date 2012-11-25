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

import java.util.HashMap;

import org.w3c.dom.Document;

/**
 * @author jeremiah
 * This is used only if a client will open up a second channel to the server. 
 * Generally speaking, a simple write document, readNextDocument is all that needed if there is no additional channels needed.
 *
 */
public interface ClientRequestProcessor
{

	public void init(ClientRequestXMLProcessor clientRequestXMLProcessor,String sessionID,HashMap<String, String> sessionHashMap,String requestName) throws Exception;

	public void process(ClientRequest clientRequest) throws Exception;

	//public void writeResponse(XMLResponse response) throws Exception;

	public Document readNextDocument() throws Exception;

	public String getSessionId();

    public boolean isStreamProcessor();

    public void setNewSession(boolean isNewSession);
    
    public boolean isNewSession();

}
