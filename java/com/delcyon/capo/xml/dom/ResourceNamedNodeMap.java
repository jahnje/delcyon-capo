package com.delcyon.capo.xml.dom;

import java.util.ArrayList;

import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class ResourceNamedNodeMap extends ArrayList<Node> implements NamedNodeMap
{

    public ResourceNamedNodeMap(ResourceNodeList nodeList, short attributeNode)
    {
        for (Node node : nodeList)
        {
            if (node.getNodeType() == Node.ATTRIBUTE_NODE)
            {
                add(node);
            }
        }
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
            if (node.getLocalName().equals(localName) && node.getNamespaceURI().equals(namespaceURI))
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
            
            if (get(index).getLocalName().equals(arg.getLocalName()) && get(index).getNamespaceURI().equals(arg.getNamespaceURI()))
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

}
