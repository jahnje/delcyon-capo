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

import java.lang.reflect.Modifier;

import org.w3c.dom.DOMException;
import org.w3c.dom.Text;

import com.delcyon.capo.util.ToStringControl;
import com.delcyon.capo.util.ToStringControl.Control;

/**
 * @author jeremiah
 *
 */
@ToStringControl(control=Control.exclude,modifiers=Modifier.FINAL+Modifier.STATIC)
public class CText extends CNode implements Text
{

    private String data = "";
    public CText()
    {
       
    }
    
    /* (non-Javadoc)
     * @see org.w3c.dom.CharacterData#getData()
     */
    @Override
    public String getData() throws DOMException
    {
        return this.data;
    }

    @Override
    public String getNodeValue() throws DOMException
    {
        return this.data;
    }
    
    
    @Override
    public void setNodeValue(String nodeValue) throws DOMException
    {
        this.data = nodeValue;
    }
    /* (non-Javadoc)
     * @see org.w3c.dom.CharacterData#setData(java.lang.String)
     */
    @Override
    public void setData(String data) throws DOMException
    {
        this.data = data;

    }

    /* (non-Javadoc)
     * @see org.w3c.dom.CharacterData#getLength()
     */
    @Override
    public int getLength()
    {
        return data.length();
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.CharacterData#substringData(int, int)
     */
    @Override
    public String substringData(int offset, int count) throws DOMException
    {
        return data.substring(offset, offset+count);
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.CharacterData#appendData(java.lang.String)
     */
    @Override
    public void appendData(String arg) throws DOMException
    {
        this.data = this.data+arg;

    }

    /* (non-Javadoc)
     * @see org.w3c.dom.CharacterData#insertData(int, java.lang.String)
     */
    @Override
    public void insertData(int offset, String arg) throws DOMException
    {
        StringBuffer stringBuffer = new StringBuffer(data);
        stringBuffer.insert(offset, arg);
        data = stringBuffer.toString();
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.CharacterData#deleteData(int, int)
     */
    @Override
    public void deleteData(int offset, int count) throws DOMException
    {
        StringBuffer stringBuffer = new StringBuffer(data);
        stringBuffer.delete(offset, offset+count);
        data = stringBuffer.toString();
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.CharacterData#replaceData(int, int, java.lang.String)
     */
    @Override
    public void replaceData(int offset, int count, String arg) throws DOMException
    {
        StringBuffer stringBuffer = new StringBuffer(data);
        stringBuffer.replace(offset, offset+count, arg);
        data = stringBuffer.toString();
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Node#getNodeType()
     */
    @Override
    public short getNodeType()
    {
        return TEXT_NODE;
    }

    @Override
    public String getNodeName()
    {
        return "#text";
    }
    
    /* (non-Javadoc)
     * @see org.w3c.dom.Text#splitText(int)
     */
    @Override
    public Text splitText(int offset) throws DOMException
    {
        // TODO Auto-generated method stub
        Thread.dumpStack();
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Text#isElementContentWhitespace()
     */
    @Override
    public boolean isElementContentWhitespace()
    {
        return data.trim().length() != data.length();
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Text#getWholeText()
     */
    @Override
    public String getWholeText()
    {
        Thread.dumpStack();
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Text#replaceWholeText(java.lang.String)
     */
    @Override
    public Text replaceWholeText(String content) throws DOMException
    {
        Thread.dumpStack();
        throw new UnsupportedOperationException();
    }

    @Override
    public String getTextContent() throws DOMException
    {
        return data;
    }
    
}
