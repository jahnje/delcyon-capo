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

import java.util.ArrayList;
import java.util.Iterator;

import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;

import com.delcyon.capo.util.EqualityProcessor;

/**
 * @author jeremiah
 *
 */
public class CNamedNodeMap extends ArrayList<Node> implements NamedNodeMap
{

    
    public CNamedNodeMap()
    {
     
    }
    
    

    @Override
    public Node getNamedItem(String name)
    {
        for (Node node : this)
        {
            if (node.getNodeName().equals(name))
            {
                return node;
            }
        }        
        return null;
    }

    @Override
    public Node setNamedItem(Node arg) throws DOMException
    {
        for (int index = 0; index < size(); index++)
        {
            if (get(index).getNodeName().equals(arg.getNodeName()))
            {
                return set(index, arg);
            }
        }
        add(arg);
        return null;
    }

    @Override
    public Node removeNamedItem(String name) throws DOMException
    {
        for (int index = 0; index < size(); index++)
        {
            if (get(index).getNodeName().equals(name))
            {
                return remove(index);
            }
        }
        return null;
    }

    @Override
    public Node item(int index)
    {
        return get(index);
    }

    @Override
    public int getLength()
    {
       return size();
    }

    @Override
    public Node getNamedItemNS(String namespaceURI, String localName) throws DOMException
    {
        for (Node node : this)
        {
            if (node.getLocalName().equals(localName) && EqualityProcessor.areSame(node.getNamespaceURI(),namespaceURI))
            {
                return node;
            }
        }        
        return null;
    }

    @Override
    public Node setNamedItemNS(Node arg) throws DOMException
    {
        for (int index = 0; index < size(); index++)
        {
            
            if (get(index).getLocalName().equals(arg.getLocalName()) && EqualityProcessor.areSame(get(index).getNamespaceURI(),arg.getNamespaceURI()))
            {
                return set(index, arg);
            }
        }
        add(arg);
        return null;
    }

    @Override
    public Node removeNamedItemNS(String namespaceURI, String localName) throws DOMException
    {
        for (int index = 0; index < size(); index++)
        {
            
            if (get(index).getLocalName().equals(localName) && get(index).getNamespaceURI().equals(namespaceURI))
            {
                return remove(index);
            }
        }      
        return null;
    }

    /**
     * This should always add nodes in alphabetical order according to localName;
     */
    @Override
    public boolean add(Node e)
    {

        for (int index = 0; index < size();index++)
        {
            int comparison = get(index).getLocalName().compareTo(e.getLocalName()); 
            if( comparison > 0)
            {
                super.add(index, e);
                return true;
            }
            if(comparison == 0)
            {
                if((index + 1) < size() )
                {
                    super.add(index+1, e);
                    return true;
                }
                else
                {
                    super.add(e);
                    return true;
                }
            }
        }
        super.add(e);
        return true;
        
    }
    
}
