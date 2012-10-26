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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * @author jeremiah
 *
 */
public class CDocumentBuilderFactory extends DocumentBuilderFactory
{

    /* (non-Javadoc)
     * @see javax.xml.parsers.DocumentBuilderFactory#newDocumentBuilder()
     */
    @Override
    public DocumentBuilder newDocumentBuilder() throws ParserConfigurationException
    {
       return new CDocumentBuilder();
    }

    /* (non-Javadoc)
     * @see javax.xml.parsers.DocumentBuilderFactory#setAttribute(java.lang.String, java.lang.Object)
     */
    @Override
    public void setAttribute(String name, Object value) throws IllegalArgumentException
    {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see javax.xml.parsers.DocumentBuilderFactory#getAttribute(java.lang.String)
     */
    @Override
    public Object getAttribute(String name) throws IllegalArgumentException
    {
        throw new UnsupportedOperationException();        
    }

    /* (non-Javadoc)
     * @see javax.xml.parsers.DocumentBuilderFactory#setFeature(java.lang.String, boolean)
     */
    @Override
    public void setFeature(String name, boolean value) throws ParserConfigurationException
    {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see javax.xml.parsers.DocumentBuilderFactory#getFeature(java.lang.String)
     */
    @Override
    public boolean getFeature(String name) throws ParserConfigurationException
    {
        throw new UnsupportedOperationException();        
    }

}
