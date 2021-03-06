/**
Copyright (c) 2012 Delcyon, Inc.
This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.delcyon.capo.resourcemanager.remote;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.delcyon.capo.protocol.server.AbstractResponse;
import com.delcyon.capo.server.CapoServer;
import com.delcyon.capo.util.XMLSerializer;

/**
 * @author jeremiah
 *
 */
public class RemoteResourceResponse extends AbstractResponse
{

    public RemoteResourceResponse() throws Exception
    {
        super(CapoServer.getDefaultDocument("remoteResourceResponse.xml"));         
    }

    public RemoteResourceResponse(Document replyDocument) throws Exception
    {
        super(replyDocument);
        XMLSerializer xmlSerializer = new XMLSerializer();
        xmlSerializer.marshall((Element) replyDocument.getDocumentElement(), this);
    }
    
}
