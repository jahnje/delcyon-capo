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
package com.delcyon.capo.xml.cdom;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.DefaultHandler2;

import com.delcyon.capo.xml.XPath;

/**
 * @author jeremiah
 *
 */
public class CDOMHandler extends DefaultHandler2
{
    public static final String NAMESPACE_NAMESPACE_URI = "http://www.w3.org/2000/xmlns/";
    
    private CDocument document = null;
    private Stack<CNode> nodeStack = new Stack<CNode>();
    private EntityResolver entityResolver = null;
    private ErrorHandler errorHandler = null;
    private HashMap<String, String> prefixHashMap = new HashMap<String, String>();
    
    public CDOMHandler(EntityResolver entityResolver, ErrorHandler errorHandler)
    {
        this.entityResolver = entityResolver;
        this.errorHandler = errorHandler;
    }

    public Document getDocument()
    {
        this.document.normalizeDocument();
        this.document.setSilenceEvents(false);
        return this.document;
    }
    
    @Override
    public void startDocument() throws SAXException
    {
        document = new CDocument();
        document.setSilenceEvents(true);
        nodeStack.push(document);
    }
    
    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException
    {
        
       //System.out.println(prefix+"==>"+uri);
       prefixHashMap.put(prefix, uri); 
    }
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        //System.out.println(uri+"-->"+localName+"-->"+qName+"-->"+ReflectionUtility.processToString(attributes));        
        CElement element = new CElement(qName);
        element.setOwnerDocument(document);
        if(uri.isEmpty() == false)
        {
            element.setNamespaceURI(uri);
        }
        nodeStack.peek().appendChild(element);



        for(int index = 0; index < attributes.getLength(); index++)
        {
           //System.out.println("Attr==>"+attributes.getURI(index)+"==>"+attributes.getQName(index)+"==>"+attributes.getValue(index));
            CAttr attr = new CAttr(element,attributes.getURI(index),attributes.getQName(index),attributes.getValue(index));
            element.setAttributeNode(attr);
        }

        
        Set<Entry<String, String>> prefixSet = prefixHashMap.entrySet();
        for (Entry<String, String> entry : prefixSet)
        {
            if(entry.getKey().isEmpty())// && entry.getValue().equals(uri))
            {                
                element.setAttributeNS(NAMESPACE_NAMESPACE_URI,"xmlns", entry.getValue());
                document.setDefaultNamespace(entry.getValue());
            }
            else
            {
                element.setAttributeNS(NAMESPACE_NAMESPACE_URI,"xmlns:"+entry.getKey(), entry.getValue());
            }
        }
        prefixHashMap.clear();
        
        nodeStack.push(element);
    }
    
    @Override
    public void processingInstruction(String target, String data) throws SAXException
    {
        CProcessingInstruction processingInstruction = new CProcessingInstruction(target,data);       
        nodeStack.peek().appendChild(processingInstruction);
    }
    
    @Override
    public void comment(char[] ch, int start, int length) throws SAXException
    {
        CComment text = new CComment();
        text.setData(new String(ch,start,length));
        nodeStack.peek().appendChild(text);
    }
    
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {       
       CText text = new CText();
       text.setData(new String(ch,start,length));
       nodeStack.peek().appendChild(text);
    }
    
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        nodeStack.pop();
    }
    
    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException
    {
        if (entityResolver != null)
        {
            return entityResolver.resolveEntity(publicId, systemId);
        }
        else
        {
            return null;
        }
    }
    
    @Override
    public void error(SAXParseException saxParseException) throws SAXException
    {
        errorHandler.error(saxParseException);
    }
    
    @Override
    public void fatalError(SAXParseException saxParseException) throws SAXException
    {
        try
        {
            XPath.dumpNode(document, System.err);
        }
        catch (Exception e)
        {    
            e.printStackTrace();
        }
        errorHandler.fatalError(saxParseException);
    }

    
    
}
