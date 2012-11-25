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
package com.delcyon.capo.protocol.client;

import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;

import org.w3c.dom.Document;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.elements.GroupElement.Attributes;
import com.delcyon.capo.xml.XMLProcessor;
import com.delcyon.capo.xml.XMLProcessorProvider;
import com.delcyon.capo.xml.XMLStreamProcessor;
import com.delcyon.capo.xml.XPath;

/**
 * @author jeremiah
 *
 */
@XMLProcessorProvider(documentElementNames={"ServerResponse"},namespaceURIs={})
public class XMLServerResponse implements XMLProcessor
{

private static HashMap<String, Class<? extends XMLServerResponseProcessor>> xmlServerResponseProcessorHashMap = new HashMap<String, Class<? extends XMLServerResponseProcessor>>();
	
	static
	{
		
		Set<String> streamConsumerProviderSet =  CapoApplication.getAnnotationMap().get(XMLServerResponseProcessorProvider.class.getCanonicalName());
		for (String className : streamConsumerProviderSet)
		{
			try
			{
				Class<? extends XMLServerResponseProcessor> xmlRequestProcessorClass = (Class<? extends XMLServerResponseProcessor>) Class.forName(className);
				
				XMLServerResponseProcessorProvider xmlServerResponseProcessorProvider = xmlRequestProcessorClass.getAnnotation(XMLServerResponseProcessorProvider.class);
				String[] documentElementNames = xmlServerResponseProcessorProvider.documentElementNames();
				//TODO make this namespace aware
				String[] namespaces = xmlServerResponseProcessorProvider.namespaceURIs();
				for (String documentElementName : documentElementNames)
				{
					xmlServerResponseProcessorHashMap.put(documentElementName, xmlRequestProcessorClass);
					CapoApplication.logger.log(Level.CONFIG, "Loaded XMLServerResponseProcessor '"+documentElementName+"' from "+xmlRequestProcessorClass.getSimpleName());
				}
			} catch (Exception e)
			{
				CapoApplication.logger.log(Level.WARNING, "Couldn't load "+className+" as an XMLServerResponseProcessor", e);
			}
		}
	}
	
	public static  XMLServerResponseProcessor getXMLServerResponseProcessor(String xmlServerResponseProcessor) throws Exception
	{
		Class<? extends XMLServerResponseProcessor> xmlServerResponseProcessorClass = xmlServerResponseProcessorHashMap.get(xmlServerResponseProcessor);
		if (xmlServerResponseProcessorClass != null)
		{
			return xmlServerResponseProcessorClass.newInstance();
		}
		else
		{
			return null;
		}
	}
	
	
	private Document document;
	private XMLStreamProcessor xmlStreamProcessor;
	private OutputStream outputStream;
	private String responseType;
	private String sessionID;
    private HashMap<String, String> sessionHashMap = null;
    private XMLServerResponseProcessor xmlServerResponseProcessor;

	@Override
	public void init(Document document, XMLStreamProcessor xmlStreamProcessor, OutputStream outputStream,HashMap<String, String> sessionHashMap) throws Exception
	{
	    this.sessionHashMap  = sessionHashMap;
		this.document = document;
		this.xmlStreamProcessor = xmlStreamProcessor;
		this.outputStream = outputStream;
		this.sessionID = document.getDocumentElement().getAttribute(Attributes.sessionId.toString());
		this.responseType = document.getDocumentElement().getAttribute(Attributes.type.toString());
		String localName = XPath.unwrapDocument(document,false).getDocumentElement().getLocalName();
        xmlServerResponseProcessor = getXMLServerResponseProcessor(localName);
        if (xmlServerResponseProcessor != null)
        {
            xmlServerResponseProcessor.init(XPath.unwrapDocument(document,false),this,sessionHashMap);
            
        }
        else
        {
            throw new Exception("Couldn't find @XMLServerResponseProcessorProvider(documentElementNames={\""+localName+"\"}, namespaceURIs={})");
        }
	}

	@Override
	public void run()
	{
	    try
	    {
	        
	        xmlServerResponseProcessor.process();
	    }
	    catch (Exception exception)
	    {

	        CapoApplication.logger.log(Level.SEVERE, "Exception in  session:"+sessionID,exception);
	        xmlStreamProcessor.throwException(exception);
	    }
	}

	
	public String getResponseType()
	{
		return responseType;
	}
	
	public String getSessionID()
	{
		return sessionID;
	}
	
	@Override
	public Document getDocument()
	{
		return document;
	}

	@Override
	public OutputStream getOutputStream()
	{
		return outputStream;
	}

	@Override
	public XMLStreamProcessor getXmlStreamProcessor()
	{
		return xmlStreamProcessor;
	}


	@Override
	public Document readNextDocument() throws Exception
	{
		return xmlStreamProcessor.readNextDocument();
	}

	@Override
	public void writeDocument(Document document) throws Exception
	{
		xmlStreamProcessor.writeDocument(document);
		
	}

	public BufferedInputStream getInputStream()
	{
		return xmlStreamProcessor.getInputStream();
	}
	
	@Override
	public boolean isStreamProcessor()
	{
	    return xmlServerResponseProcessor.isStreamProcessor();
	}
}
