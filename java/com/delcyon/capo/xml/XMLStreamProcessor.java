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
package com.delcyon.capo.xml;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.SAXParseException;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.Configuration.PREFERENCE;
import com.delcyon.capo.datastream.StreamProcessor;
import com.delcyon.capo.datastream.StreamProcessorProvider;
import com.delcyon.capo.server.CapoServer;

/**
 * @author jeremiah
 */
@SuppressWarnings("unchecked")
@StreamProcessorProvider(streamIdentifierPatterns = { "<\\?xml .*"})
public class XMLStreamProcessor implements StreamProcessor
{
	
	private static HashMap<String, Class<? extends XMLProcessor>> xmlProcessorHashMap = new HashMap<String, Class<? extends XMLProcessor>>();
	
	static
	{
		
		Set<String> xmlProcessorProviderSet =  CapoApplication.getAnnotationMap().get(XMLProcessorProvider.class.getCanonicalName());
		for (String className : xmlProcessorProviderSet)
		{
			try
			{
				Class<? extends XMLProcessor> xmlRequestProcessorClass = (Class<? extends XMLProcessor>) Class.forName(className);
				
				XMLProcessorProvider streamConsumer = xmlRequestProcessorClass.getAnnotation(XMLProcessorProvider.class);
				String[] documentElementNames = streamConsumer.documentElementNames();
				//TODO make this namespace aware
				String[] namespaces = streamConsumer.namespaceURIs();
				for (String documentElementName : documentElementNames)
				{
					xmlProcessorHashMap.put(documentElementName, xmlRequestProcessorClass);
					CapoApplication.logger.log(Level.CONFIG, "Loaded XMLProcessor '"+documentElementName+"' from "+xmlRequestProcessorClass.getSimpleName());
				}
			} catch (Exception e)
			{
				CapoApplication.logger.log(Level.WARNING, "Couldn't load "+className+" as an XMLProcessor", e);
			}
		}
	}
	
	public static  XMLProcessor getXMLProcessor(String xmlProcessorName) throws Exception
	{
		Class<? extends XMLProcessor> xmlProcessorClass = xmlProcessorHashMap.get(xmlProcessorName);
		if (xmlProcessorClass != null)
		{
			return xmlProcessorClass.newInstance();
		}
		else
		{
			return null;
		}
	}
	
	
	private Transformer transformer;
	private DocumentBuilder documentBuilder;
	
	private BufferedInputStream inputStream;
	private OutputStream outputStream;
    private HashMap<String, String> sessionHashMap;

	public XMLStreamProcessor() throws Exception
	{
		_init();
	}
	
	public XMLStreamProcessor(BufferedInputStream bufferedInputStream, OutputStream outputStream) throws Exception
	{
		this.inputStream = bufferedInputStream;
		this.outputStream = outputStream;
		_init();
	}
	
	/**
	 * This is the internal initialization method, the API requires an init method, 
	 * but we don't want to always have to call it as we use this class as a utility class as well
	 * @throws Exception
	 */
	private void _init() throws Exception
	{
		TransformerFactory tFactory = TransformerFactory.newInstance();
		transformer = tFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		documentBuilder = documentBuilderFactory.newDocumentBuilder();
	}
	
	/**
	 * does nothing, just here for API
	 * TODO make sure we actually need this for the API, could just be a hold over from refactoring
	 */
	@Override
	public void init(HashMap<String, String> sessionHashMap) throws Exception
	{
		this.sessionHashMap = sessionHashMap;
	}
	
	@Override
	public void processStream(BufferedInputStream bufferedInputStream, OutputStream outputStream) throws Exception
	{
		this.inputStream = bufferedInputStream;
		this.outputStream = outputStream;
		Document document = getDocument(bufferedInputStream);
		if (CapoServer.LOGGING_LEVEL.intValue() <= Level.FINE.intValue())
        {
			transformer.transform(new DOMSource(document), new StreamResult(System.out));
			System.out.println();
        }
		
		//load client request
		String documentElementName = document.getDocumentElement().getLocalName();
		XMLProcessor xmlProcessor = getXMLProcessor(documentElementName);		
		if (xmlProcessor != null)
		{			
			xmlProcessor.init(document, this, outputStream,sessionHashMap);
			xmlProcessor.process();
		}
		else
		{
			CapoApplication.logger.log(Level.SEVERE, "Unknown XML Type: "+documentElementName);
		}
		
	}
	
	/**
	 * Sends a document over the outputStream terminated by a char = '0'
	 * @param document
	 * @throws Exception
	 */
	public void writeDocument(Document document) throws Exception
	{		
		transformer.transform(new DOMSource(document), new StreamResult(outputStream));		
		outputStream.write(0);
		outputStream.flush();
		CapoApplication.logger.log(Level.FINE, "SENT OK bit to Remote After WRITE 0");
		int writeResponseValue = inputStream.read();
		CapoApplication.logger.log(Level.FINE, "READ OK bit to Remote After WRITE: "+writeResponseValue);
		if(writeResponseValue != 1)
		{
		    throw new Exception("Server Reported an Error");
		}
	}
	
	/**
	 * gets the next document from the inputStream
	 * @return
	 * @throws Exception
	 */
	public Document readNextDocument() throws Exception
	{
		return getDocument(inputStream);
	}
	
	/**
	 * Reads an input stream until and EOF or '0x0' char is found, and tries to parse the results 
	 * @param inputStream
	 * @return
	 * @throws Exception
	 */
	public Document getDocument(BufferedInputStream inputStream) throws Exception
	{
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		
		int bytesRead = 0;
		long totalBytesRead = 0l;
		while(true)
		{
			byte[] buffer = new byte[CapoApplication.getConfiguration().getIntValue(PREFERENCE.BUFFER_SIZE)];
			
			bytesRead = inputStream.read(buffer);			
			totalBytesRead += (long)bytesRead;
			//end of stream
			if (bytesRead == -1)
			{			
				break;
			}
			//end of document per capo multi-document protocol
			else if (buffer[bytesRead - 1] == 0)
			{
				byteArrayOutputStream.write(buffer, 0, bytesRead-1);				
				break;
			}
			else
			{
				//scan buffer for 0; sometimes we stack non XML data right behind XML data, and we need to not process it as an XML file
				int endPosition = indexOf(buffer, (byte) 0, bytesRead);
				
				if (endPosition >= 0)
				{					
					inputStream.reset();
					inputStream.skip(endPosition+1);
					byteArrayOutputStream.write(buffer,0,endPosition);					
					break;
				}
				else
				{
					byteArrayOutputStream.write(buffer,0,bytesRead);
				}
			}
		}
		try
		{
		    
			Document readDocument = documentBuilder.parse(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
			if (CapoApplication.logger.isLoggable(Level.FINER))
			{
			    XPath.dumpNode(readDocument, System.out);
			}
			//send ok to sender			
            outputStream.write(1);
            outputStream.flush();
            CapoApplication.logger.log(Level.FINE, "SENT OK bit to Remote After READ: 1");            
            return readDocument;
		}
		catch (SAXParseException saxParseException)
		{
			CapoApplication.logger.log(Level.WARNING,"length = "+byteArrayOutputStream.size()+" buffer = ["+new String(byteArrayOutputStream.toByteArray())+"]");
			outputStream.write(0);
			outputStream.flush();
			throw saxParseException;
		}
	}

	public BufferedInputStream getInputStream()
	{		
		return inputStream;
	}
	
	/**
	 * 
	 * @param byteArray
	 * @param value
	 * @param endPosition (exclusive)
	 * @return
	 */
	private int indexOf(byte[] byteArray, byte value, int length)
	{
		
		for (int index = 0; index < byteArray.length && index < length; index++)
		{
			if (byteArray[index] == value)
			{
				return index;
			}
		}
		return -1;
	}

   
	
}
