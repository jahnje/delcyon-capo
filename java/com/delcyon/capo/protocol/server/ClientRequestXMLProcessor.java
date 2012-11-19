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

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.elements.GroupElement.Attributes;
import com.delcyon.capo.server.CapoServer;
import com.delcyon.capo.xml.XMLProcessor;
import com.delcyon.capo.xml.XMLProcessorProvider;
import com.delcyon.capo.xml.XMLStreamProcessor;
import com.delcyon.capo.xml.XPath;

/**
 * @author jeremiah
 *
 */
@SuppressWarnings("unchecked")
@XMLProcessorProvider(documentElementNames={"request"},namespaceURIs={"http://www.delcyon.com/capo-client","http://www.delcyon.com/capo-server"})
public class ClientRequestXMLProcessor implements XMLProcessor
{

	private static HashMap<String, Class<? extends ClientRequestProcessor>> clientRequestProcessorHashMap = new HashMap<String, Class<? extends ClientRequestProcessor>>();

	static
	{

		Set<String> clientRequestProcessorProviderSet =  CapoApplication.getAnnotationMap().get(ClientRequestProcessorProvider.class.getCanonicalName());
		for (String className : clientRequestProcessorProviderSet)
		{
			try
			{
				Class<? extends ClientRequestProcessor> clientRequestProcessorClass = (Class<? extends ClientRequestProcessor>) Class.forName(className);

				ClientRequestProcessorProvider clientRequestProcessorProvider = clientRequestProcessorClass.getAnnotation(ClientRequestProcessorProvider.class);
				
					clientRequestProcessorHashMap.put(clientRequestProcessorProvider.name(), clientRequestProcessorClass);
					CapoApplication.logger.log(Level.CONFIG, "Loaded ClientRequestProcessor '"+clientRequestProcessorProvider.name()+"' from "+clientRequestProcessorClass.getSimpleName());
				
			} catch (Exception e)
			{
				CapoApplication.logger.log(Level.WARNING, "Couldn't load "+className+" as an ClientRequestProcessor", e);
			}
		}
	}
	
	
	public static ClientRequestProcessor getClientRequestProcessor(String clientRequestProcessorName) throws Exception
	{
		Class<? extends ClientRequestProcessor> clientRequestProcessorClass = clientRequestProcessorHashMap.get(clientRequestProcessorName);
		if (clientRequestProcessorClass != null)
		{
			return clientRequestProcessorClass.newInstance();
		}
		else
		{
			return null;
		}
	}
	
	
	
	
	private Document requestDocument;	
	private XMLStreamProcessor xmlStreamProcessor;
	private OutputStream outputStream;
	private String sessionId;
    private HashMap<String, String> sessionHashMap;

	@Override
	public OutputStream getOutputStream()
	{
		return outputStream;
	}

	@Override
	public Document getDocument()
	{
		return requestDocument;
	}

	@Override
	public XMLStreamProcessor getXmlStreamProcessor()
	{
		return xmlStreamProcessor;
	}

	@Override
	public void init(Document requestDocument, XMLStreamProcessor xmlStreamProcessor, OutputStream outputStream,HashMap<String, String> sessionHashMap) throws Exception
	{
		this.outputStream = outputStream;
		this.xmlStreamProcessor = xmlStreamProcessor;		
		this.requestDocument = requestDocument;
		this.sessionHashMap = sessionHashMap;
		if (requestDocument.getDocumentElement().hasAttribute(ClientRequest.SESSION_ID_ATTRIBUTE_NAME) == true)
		{
			sessionId = requestDocument.getDocumentElement().getAttribute(ClientRequest.SESSION_ID_ATTRIBUTE_NAME);
			if (sessionId.trim().isEmpty())
			{
				sessionId = ClientRequestProcessorSessionManager.generateSessionID();
			}
		}
		else
		{
			sessionId = ClientRequestProcessorSessionManager.generateSessionID();
		}
	}

	@Override
	public void process() throws Exception
	{
		//see if we already have a session, and if we do, and there is a request handler, then pass us off to it.
		//TODO change to ClientRequestProcessor from RequestHandler
		ClientRequest clientRequest = new ClientRequest(this,requestDocument, xmlStreamProcessor, outputStream);
		ClientRequestProcessor clientRequestProcessor = ClientRequestProcessorSessionManager.getClientRequestProcessor(clientRequest.getSessionID());
		if (clientRequestProcessor != null)
		{
			
			clientRequestProcessor.process(clientRequest);
			//send finished indicator
			//TODO clientRequest.finish(); no indicator wanted here. If we want to start combining sockets, then this might be a good place to start
		}
		else
		{
			Element documentElement = XPath.unwrapDocument(requestDocument,false).getDocumentElement();
			String requestName = null;
			if (documentElement.hasAttribute("type"))
			{
				requestName = documentElement.getAttribute("type");
			}
			String documentElementName = documentElement.getLocalName();
			clientRequestProcessor = ClientRequestXMLProcessor.getClientRequestProcessor(documentElementName);
					
			if (clientRequestProcessor != null)
			{	
				String sessionID = ClientRequestProcessorSessionManager.generateSessionID();
				clientRequest.setSessionID(sessionID);
				clientRequestProcessor.init(this,sessionID,sessionHashMap,requestName);
				ClientRequestProcessorSessionManager.registerClientRequestProcessor(clientRequestProcessor);
				clientRequestProcessor.process(clientRequest);
				//because once a control main request processor runs, we no longer need to have a link reference to it, remove it. 
				ClientRequestProcessorSessionManager.removeClientRequestProcessor(clientRequestProcessor);
				
				//send finished indicator
				clientRequest.finish();
			}
			else
			{
				CapoApplication.logger.log(Level.SEVERE, "Couldn't find @ClientRequestProcessorProvider for: "+documentElementName);
			}
		}
	}


	public XMLResponse createResponse() throws Exception
	{
		XMLResponse xmlResponse = new XMLResponse();
		xmlResponse.setSessionID(getSessionId());
		return xmlResponse;
	}

	public void writeResponse(Response response) throws Exception
	{
		XMLResponse clientRequestResponse = createResponse();
		clientRequestResponse.setSessionID(response.getSessionID());		
		clientRequestResponse.setResponseElement(response.getResponseDocument().getDocumentElement());
		xmlStreamProcessor.writeDocument(clientRequestResponse.getResponseDocument());
		
	}
	


	@Override
	public Document readNextDocument() throws Exception
	{
		//unwrap document
		Document requestDocument = xmlStreamProcessor.readNextDocument();		
		return requestDocument;
	}
	
	@Override
	public void writeDocument(Document document) throws Exception
	{
		Document responseDocument = XPath.wrapDocument(CapoServer.getDefaultDocument("default_response.xml"),document);
		if (responseDocument.getDocumentElement().hasAttribute(ClientRequest.SESSION_ID_ATTRIBUTE_NAME) == false)
		{
			responseDocument.getDocumentElement().setAttribute(Attributes.sessionId.toString(), getSessionId());
		}
		xmlStreamProcessor.writeDocument(responseDocument);
	}

	

	public String getSessionId()
	{
		return sessionId;
	}

	
}
