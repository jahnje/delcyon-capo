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

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.TypeInfo;

import com.delcyon.capo.util.ToStringControl;
import com.delcyon.capo.util.ToStringControl.Control;
import com.delcyon.capo.xml.cdom.CDOMEvent.EventType;

/**
 * @author jeremiah
 *
 */
@ToStringControl(control=Control.exclude,modifiers=Modifier.STATIC+Modifier.FINAL)
public class CAttr extends CNode implements Attr
{
    
    private boolean isConstructing = true;

	@SuppressWarnings("unused")
    protected CAttr(){}//reflection
    
    public CAttr(CElement parentNode, String localName, String value)
    {
    	this(parentNode,null,localName,value);
    	
    }
    
    public CAttr(CElement parentNode, String uri, String qName, String value)
    {
        
        setOwnerDocument(parentNode.getOwnerDocument());
        setParent(parentNode);
        setNodeName(qName);
        //only setNamespaces if they are not empty, otherwise, they should be null.
        if(uri != null && uri.isEmpty() == false)
        {
            setNamespaceURI(uri);
        }
       
        setNodeValue(value);
        isConstructing = false;
    }

    @Override
    protected CDOMEvent prepareEvent(EventType eventType, CNode sourceNode)
    {
    	if (isConstructing  == false)
    	{
    	return super.prepareEvent(eventType, sourceNode);
    	}
    	else
    	{
    		return null;
    	}
    }
    
//    @Override
//    public String getNamespaceURI()
//    {
//       if(super.getNamespaceURI() != null)
//       {
//           return super.getNamespaceURI();
//       }
//       else
//       {
//           return getParentNode().getNamespaceURI();
//       }
//    }
    
    /* (non-Javadoc)
     * @see org.w3c.dom.Attr#getName()
     */
    @Override
    public String getName()
    {
        return getNodeName();
    }

    @Override
    public short getNodeType()
    {
        return Node.ATTRIBUTE_NODE;
    }
    
    /* (non-Javadoc)
     * @see org.w3c.dom.Attr#getSpecified()
     */
    @Override
    public boolean getSpecified()
    {
        Thread.dumpStack();
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Attr#getValue()
     */
    @Override
    public String getValue()
    {
        return this.getNodeValue();
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Attr#setValue(java.lang.String)
     */
    @Override
    public void setValue(String value) throws DOMException
    {
        setNodeValue(value);
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Attr#getOwnerElement()
     */
    @Override
    public Element getOwnerElement()
    {
        return (Element) getParentNode();
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Attr#getSchemaTypeInfo()
     */
    @Override
    public TypeInfo getSchemaTypeInfo()
    {
        Thread.dumpStack();
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Attr#isId()
     */
    @Override
    public boolean isId()
    {
        Thread.dumpStack();
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String toString()
    {    	
    	return getNodeName()+"="+getNodeValue();
    }
    
}
