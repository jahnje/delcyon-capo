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
import org.w3c.dom.ProcessingInstruction;

import com.delcyon.capo.util.ToStringControl;
import com.delcyon.capo.util.ToStringControl.Control;

/**
 * @author jeremiah
 *
 */
@ToStringControl(control=Control.exclude,modifiers=Modifier.FINAL+Modifier.STATIC)
public class CProcessingInstruction extends CNode implements ProcessingInstruction
{

    private String data = "";
    public CProcessingInstruction()
    {
       
    }
    
    public CProcessingInstruction(String target, String data)
    {
        setNodeName(target);
        this.data = data;
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
     * @see org.w3c.dom.Node#getNodeType()
     */
    @Override
    public short getNodeType()
    {
        return PROCESSING_INSTRUCTION_NODE;
    }

    
    

    @Override
    public String getTextContent() throws DOMException
    {
        return data;
    }

    @Override
    public String getTarget()
    {
       return nodeName;
    }
    
}
