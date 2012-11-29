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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author jeremiah
 *
 */
public class CDocumentBuilder extends DocumentBuilder  
{
   
    private EntityResolver entityResolver;
    private ErrorHandler errorHandler;
    private static final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
    
    
    
    
    @Override
    public Document parse(String uri) throws SAXException, IOException
    {
        try
        {
            return parse(new InputSource(new URI(uri).toURL().openStream()));
        }
        catch (URISyntaxException e)
        {
            throw new IOException(e);
        }
    }
    
    
    @Override
    public Document parse(InputStream inputStream) throws SAXException, IOException
    {
        return parse(new InputSource(inputStream));
    }
    
    
    @Override
    public Document parse(File file) throws SAXException, IOException
    {
        return parse(new InputSource(new FileInputStream(file)));
    }
    
    @Override
    public Document parse(InputSource is) throws SAXException, IOException
    {
        //System.setProperty("javax.xml.parsers.SAXParserFactory", "com.delcyon.capo.xml.cdom.CSAXParserFactory");
//        String systemSAXParserFactoryName = System.getProperty("javax.xml.parsers.SAXParserFactory");
//        System.clearProperty("javax.xml.parsers.SAXParserFactory");
        //SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        saxParserFactory.setNamespaceAware(true);
        
        CDOMHandler cdomHandler = new CDOMHandler(entityResolver,errorHandler);
        try
        {
            SAXParser saxParser = saxParserFactory.newSAXParser(); 
            saxParser.getXMLReader().setProperty("http://xml.org/sax/properties/lexical-handler", cdomHandler);
            saxParser.parse(is, cdomHandler);
            return cdomHandler.getDocument();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new SAXException(e);
        }        
    }

    /* (non-Javadoc)
     * @see javax.xml.parsers.DocumentBuilder#isNamespaceAware()
     */
    @Override
    public boolean isNamespaceAware()
    {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see javax.xml.parsers.DocumentBuilder#isValidating()
     */
    @Override
    public boolean isValidating()
    {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see javax.xml.parsers.DocumentBuilder#setEntityResolver(org.xml.sax.EntityResolver)
     */
    @Override
    public void setEntityResolver(EntityResolver entityResolver)
    {
        this.entityResolver = entityResolver;        
    }

    /* (non-Javadoc)
     * @see javax.xml.parsers.DocumentBuilder#setErrorHandler(org.xml.sax.ErrorHandler)
     */
    @Override
    public void setErrorHandler(ErrorHandler errorHandler)
    {
        this.errorHandler = errorHandler;        
    }

    /* (non-Javadoc)
     * @see javax.xml.parsers.DocumentBuilder#newDocument()
     */
    @Override
    public Document newDocument()
    {
        return new CDocument();         
    }

    /* (non-Javadoc)
     * @see javax.xml.parsers.DocumentBuilder#getDOMImplementation()
     */
    @Override
    public DOMImplementation getDOMImplementation()
    {
        return new CDOMImplementation();
    }

}
