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
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.ContextThread;
import com.delcyon.capo.Configuration.PREFERENCE;
import com.delcyon.capo.datastream.StreamProcessor;
import com.delcyon.capo.datastream.StreamProcessorProvider;
import com.delcyon.capo.datastream.StreamUtil;
import com.delcyon.capo.xml.cdom.CDocument;

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
    //private ThreadGroup subThreadGroup = null;
    private Exception exception = null;
    private Document returnDocument;
    private long processorTID;
    private long initialTID = -1l;
    private Thread processorThread;
    private byte[] ackBuffer = new byte[]{-1};
    private Stack<ContextThread> readerStack = new Stack<ContextThread>();
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
	    //get the thread variables all setup. We don't want to do this in init because we're not in our own thread until we start processing.
	    HashMap<String, ContextThread> threadMap = new HashMap<String, ContextThread>();
	    //subThreadGroup = new ThreadGroup(getClass().getName()+" - "+System.nanoTime());        
        processorThread = Thread.currentThread();
        processorTID = processorThread.getId();
        
        //indicate we're actually in the read loop
        ackBuffer[0] = 0; 
        
        //setup our data streams
		this.inputStream = bufferedInputStream;
		this.outputStream = outputStream;
		
		
		
		while(true)
		{
		    //check to see if a thread has thrown an exception and if so re-throw it, and exit out.
		    if(exception != null)
		    {
		        throw exception;
		    }
		    //read the document stream
		    Document document = getDocument(bufferedInputStream);

		    //check for end of stream
		    if(document == null)
		    {
		        break;
		    }

		    //check to see if we got an empty document, because we're actually dealing with an ACK from the remote end after a document write 
		    if(document.getDocumentElement() == null)
		    {
		        continue;
		    }

		    //load client request
		    String documentElementName = document.getDocumentElement().getLocalName();
		    String tid = document.getDocumentElement().getAttribute("TID");
		    //TODO tid null check

		    
		        XMLProcessor xmlProcessor = getXMLProcessor(documentElementName);		
		        if (xmlProcessor != null)
		        {			
		            xmlProcessor.init(document, this, outputStream,sessionHashMap);
		            ContextThread contextThread = new ContextThread(processorThread.getThreadGroup(), xmlProcessor);
		            threadMap.put(tid, contextThread);
		            if(initialTID < 0l)
		            {
		                initialTID = contextThread.getId();
		            }
		            
		            //don't start until we can make sure that nothing is trying to write
		            synchronized (ackBuffer)
                    {
		                //check to see if this xmlProcessor handles it's own streams.
		                if(xmlProcessor.isStreamProcessor() == true)
		                {
		                    //if so, don't make a separate thread, just run it in this one. 
		                    contextThread.run();
		                    if(exception != null)
		                    {
		                        throw exception;
		                    }
		                    break;
		                }
		                else
		                {
		                    contextThread.start();
		                }
                    }
		            		        
		        }
		        else if(readerStack.size() != 0)
		        {
		            synchronized (readerStack)
		            {
		                synchronized (readerStack.peek())
		                {
		                    this.returnDocument = document;
		                    readerStack.pop().notify();
		                }
		            }
		        }
		        else
		        {
		            //wait for the stack to fill up for a sec, just to double check.
		            int attemptCount = 0;
		            while(readerStack.size() == 0 && attemptCount < 30)
		            {
		                Thread.sleep(500);
		            }
		            if(readerStack.size() != 0)
	                {
		                synchronized (readerStack)
		                {
		                    synchronized (readerStack.peek())
		                    {
		                        this.returnDocument = document;
		                        readerStack.pop().notify();
		                    }
		                }
	                }
	                else
	                {
	                    //well, we tried
	                    CapoApplication.logger.log(Level.SEVERE, "Unknown XML Type: "+documentElementName);
	                    throw new Exception("Unknown XML Type: "+documentElementName);
	                }
		            
		        }
		    }

		
		
	}
	
	/**
	 * Sends a document over the outputStream terminated by a char = '0'
	 * @param document
	 * @throws Exception
	 */
	public void writeDocument(Document document) throws Exception
	{	

	    int writeResponseValue = -1;
        synchronized (ackBuffer)
        {
            long tid = Thread.currentThread().getId();
            //make sure we always use a know TID, 
            //our initial TID will be wrong because the first write doesn't come from the thread, but the initial call to xmlStrema Processor
            if(tid == initialTID) 
            {
                tid = processorTID;
            }
            document.getDocumentElement().setAttribute("TID",tid+"");
            //pause the reading thread, until we get our OK, or Error back from the client, since we're taking over the inputStream for a second

            transformer.transform(new DOMSource(document), new StreamResult(outputStream));
            if (CapoApplication.logger.isLoggable(Level.FINER))
            {
                CapoApplication.logger.log(Level.FINER, "Wrote Document:");
                XPath.dumpNode(document, System.out);
            }
            outputStream.write(0);
            outputStream.flush();
            CapoApplication.logger.log(Level.FINE, "SENT OK bit to Remote After WRITE 0");
		
		
		    boolean notInReadLoop = false;
            //check to see if we have our return value yet
		    if(ackBuffer[0] == 0)
		    {
		        ackBuffer.wait(30000);  
		    }
		    //check to see if we're actually in the read loop here
		    else if(ackBuffer[0] == -1)
		    {
		        notInReadLoop = true;
		        //since we're not in the loop, we're going to have to read the stream ourselves
		        byte[] buffer = new byte[2];
		        StreamUtil.fullyReadIntoBufferUntilPattern(inputStream,buffer, (byte)0);
		        if(buffer[1] == 0)
		        {
		            ackBuffer[0] = buffer[0];
		        }
		        else
		        {		            
		            throw new Exception("Expecting an ACK, not "+buffer);
		        }
		    }
            writeResponseValue = ackBuffer[0];
            //reset the buffer to 0
            if(notInReadLoop == false)
            {
                ackBuffer[0] = 0;
            }
            else
            {
                ackBuffer[0] = -1;
            }
            ackBuffer.notify();
        }
		

		
		CapoApplication.logger.log(Level.FINE, "READ OK bit to Remote After WRITE: "+writeResponseValue);
		if(writeResponseValue != 1)
		{
		    throw new Exception("Remote End Reported an Error");
		}
	}
	
	/**
	 * gets the next document from the inputStream
	 * @return response Document
	 * @throws Exception
	 */
	public Document readNextDocument() throws Exception
	{
	    synchronized (readerStack)
	    {
	        readerStack.push((ContextThread) Thread.currentThread());
	    }
	    synchronized (Thread.currentThread())
	    {
	        Thread.currentThread().wait(30000);    
	    }

	    return returnDocument;		
	}
	
	/**
	 * Reads an input stream until and EOF or '0x0' char is found, and tries to parse the results 
	 * @param inputStream
	 * @return
	 * @throws Exception
	 */
	private Document getDocument(BufferedInputStream inputStream) throws Exception
	{
	    inputStream.mark(CapoApplication.getConfiguration().getIntValue(PREFERENCE.BUFFER_SIZE));
		byte[] buffer = StreamUtil.fullyReadUntilPattern(inputStream,false, (byte)0);
		
		//end of stream reached
		if(buffer == null)
		{
		    return null;
		}
		//check for write ack message
		if(buffer.length == 1)
		{		    
		    synchronized(ackBuffer)
		    {
		        //set the value
		        ackBuffer[0] = buffer[0];
		        //let anyone listening know that we've processed something
		        ackBuffer.notify();
		        //wait until they tell us to keep processing
		        ackBuffer.wait();
		    }
		    //return blank document to indicate this was just an ACK
		    return new CDocument();
		}
		else if(buffer.length >= 10 && new String(buffer,0,9).startsWith("FINISHED:"))
		{
		    inputStream.reset();
		    return null;
		}
		else
		{
		    try
		    {

		        Document readDocument = documentBuilder.parse(new ByteArrayInputStream(buffer));
		        if (CapoApplication.logger.isLoggable(Level.FINER))
		        {
		            CapoApplication.logger.log(Level.FINER, "Read Document:");
		            XPath.dumpNode(readDocument, System.out);
		        }
		        //send ok to sender
		        outputStream.write(1);
		        outputStream.write(0);
		        outputStream.flush();
		        CapoApplication.logger.log(Level.FINE, "SENT OK bit to Remote After READ: 0");            
		        return readDocument;
		    }
		    catch (SAXException saxException)
		    {
		        CapoApplication.logger.log(Level.WARNING,"length = "+buffer.length+" buffer = ["+new String(buffer)+"]\nRAW:["+Arrays.toString(buffer)+"]");		        
		        outputStream.write(2);
		        outputStream.write(0);
		        outputStream.flush();
		        throw saxException;
		    }
		}
	}

	public BufferedInputStream getInputStream()
	{		
		return inputStream;
	}

    public void throwException(Exception exception)
    {
        this.exception = exception;        
    }
	
	
}
