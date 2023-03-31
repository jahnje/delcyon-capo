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

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;

import com.delcyon.capo.util.EqualityProcessor;
import com.delcyon.capo.util.ToStringControl;
import com.delcyon.capo.util.ToStringControl.Control;
import com.delcyon.capo.xml.XPath;
import com.delcyon.capo.xml.cdom.CDOMEvent.EventType;

/**
 * @author jeremiah
 *
 */
@ToStringControl(control=Control.exclude,modifiers=Modifier.STATIC+Modifier.FINAL)
public class CElement extends CNode implements Element
{

    
   
    @SuppressWarnings("unused")
    protected CElement(){}; //reflection
    
    public CElement(String localName)
    {
        setNodeName(localName);
    }

    public CElement(String namespaceURI,String qName)
    {
    	setNamespaceURI(namespaceURI);
        setNodeName(qName);
    }
    
    public CElement(String namespaceURI,String prefix,String localName)
    {
        if(namespaceURI != null)
        {
            setNamespaceURI(namespaceURI);
        }
        if(prefix != null)
        {
            setNodeName(prefix+":"+localName);
        }
        else
        {
            setNodeName(localName);
        }
    }
    
    @Override
    public short getNodeType()
    {
        return Node.ELEMENT_NODE;
    }
    
    /* (non-Javadoc)
     * @see org.w3c.dom.Element#getTagName()
     */
    @Override
    public String getTagName()
    {
        return getNodeName();
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Element#getAttribute(java.lang.String)
     */
    @Override
    public String getAttribute(String name)
    {
        if (getAttributes().getNamedItem(name) == null)
        {
            return "";
        }
        else
        {
            return getAttributes().getNamedItem(name).getNodeValue();
        }
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Element#setAttribute(java.lang.String, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void setAttribute(String name, String value) throws DOMException
    {
        //don't lose user data
        CAttr attribute = (CAttr) getAttributeNode(name);
        if(attribute == null)
        {
            attribute = new CAttr(this, null, name, value);
        }
        else
        {
            attribute.setNodeValue(value);
            if(attribute.getParentNode() == null)
            {
                System.out.println("WTF");
            }
        }
        
        ((CNamedNodeMap) getAttributes()).setNamedItemNS(attribute);
        cascadeDOMEvent(prepareEvent(EventType.UPDATE, this));
    }

    public CElement addAttribute(String name, String value) throws DOMException
    {
    	setAttribute(name, value);
    	return this;
    }
    
    /* (non-Javadoc)
     * @see org.w3c.dom.Element#removeAttribute(java.lang.String)
     */
    @Override
    public void removeAttribute(String name) throws DOMException
    {
        getAttributes().removeNamedItem(name);
        cascadeDOMEvent(prepareEvent(EventType.UPDATE, this));
    }
    
    public CAttr removeAttributeNode(String name) throws DOMException
    {
        CAttr attribute = (CAttr) getAttributes().removeNamedItem(name);
        cascadeDOMEvent(prepareEvent(EventType.UPDATE, this));
        return attribute;
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Element#getAttributeNode(java.lang.String)
     */
    @Override
    public Attr getAttributeNode(String name)
    {
       return (Attr) getAttributes().getNamedItem(name);
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Element#setAttributeNode(org.w3c.dom.Attr)
     */
    @Override
    public Attr setAttributeNode(Attr newAttr) throws DOMException
    {
        getAttributes().setNamedItem(newAttr);
        cascadeDOMEvent(prepareEvent(EventType.UPDATE, this));
        return newAttr;
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Element#removeAttributeNode(org.w3c.dom.Attr)
     */
    @Override
    public Attr removeAttributeNode(Attr oldAttr) throws DOMException
    {
        CNamedNodeMap namedNodeMap = (CNamedNodeMap) getAttributes();
        for (int index = 0; index < namedNodeMap.getLength(); index++)
        {            
            if(namedNodeMap.get(index).equals(oldAttr))
            {
            	cascadeDOMEvent(prepareEvent(EventType.UPDATE, this));
                return (Attr) namedNodeMap.remove(index);
            }
        }
        return null;
    }

    /**
     * Utility method to get parent element, if it's parent is not an CElement it will return null for example if it's a CDocument;
     * @return
     */
    public CElement getParent()
    {
        Node parent = getParentNode();
        if(parent != null && parent instanceof CElement)
        {
            return (CElement) parent;
        }
        else
        {
            return null;
        }
    }
    
    public CElement addChild(String namespaceURI,String localName)
    {
        return addChild(namespaceURI,getPrefix(),localName);
    }
    
    public CElement addChild(String namespaceURI,String prefix,String localName)
    {
        CElement child = new CElement(namespaceURI,prefix,localName);
        return (CElement) appendChild(child);
    }
    
    
    public CElement addChild(String localName)
    {
        return addChild(getNamespaceURI(),getPrefix(),localName);        
    }
    
    /**
     * Utility method to get all element child in walkable list
     * @return
     */
    public List<CElement> getChildren()
    {
        ArrayList<CElement> children = new ArrayList<CElement>();
        List<CNode> nodeList = getChildNodes(Node.ELEMENT_NODE);
        for(int index = 0; index < nodeList.size();index++)
        {
            children.add((CElement) nodeList.get(index));
        }
        return children;
    }
    
    /**
     * Utility Method to get all children with a particular element name in a walkable list.
     * Ignores prefix and works on local-name only, 
     * @param localName
     * @return List<CElement> never null
     */
    public List<CElement> getChildren(String...localNames)
    {
        ArrayList<CElement> children = new ArrayList<CElement>();
        List<CNode> nodeList = getChildNodes(Node.ELEMENT_NODE);
        for(int index = 0; index < nodeList.size();index++)
        {
            CElement child =  (CElement) nodeList.get(index);
            for (String localName : localNames)
            {
                if(child.getLocalName().equals(localName))
                {
                    children.add(child);
                }    
            }
            
        }
        return children;
    }
    
    
    /**
     * Utility Method to get all children with a particular element name in a walkable list
     * @param namespaceURI defaults to NS of parent element if null;
     * @param localName
     * @return
     */
    public List<CElement> getChildrenNS(String namespaceURI,String localName)
    {
        if(namespaceURI == null)
        {
            namespaceURI = getNamespaceURI();
        }
        ArrayList<CElement> children = new ArrayList<CElement>();
        List<CNode> nodeList = getChildNodes(Node.ELEMENT_NODE);
        for(int index = 0; index < nodeList.size();index++)
        {
            CElement child =  (CElement) nodeList.get(index);
            if(child.getLocalName().equals(localName) && EqualityProcessor.areSame(namespaceURI, child.getNamespaceURI()))
            {
                children.add(child);
            }
        }
        return children;
    }
    
    /* (non-Javadoc)
     * @see org.w3c.dom.Element#getElementsByTagNameNS(java.lang.String, java.lang.String)
     */
    @Override
    public NodeList getElementsByTagName(String localName) throws DOMException
    {
        return getElementsByTagNameNS(null, localName);
    }
    
    /* (non-Javadoc)
     * @see org.w3c.dom.Element#getElementsByTagName(java.lang.String)
     */
    @Override
    public NodeList getElementsByTagNameNS(final String namespaceURI,final String name)
    {
        final CNodeList nodeList = new CNodeList();
        
        NodeProcessor nodeProcessor = new NodeProcessor()
        {            
            @Override
            public void process(Node parentNode,Node node) throws Exception
            {
                if(node instanceof Element)
                {
                    if(name.equals("*") && (namespaceURI == null || namespaceURI.equals(node.getNamespaceURI())))
                    {
                        nodeList.add(node);
                    }
                    else
                    {
                        if(((Element) node).getTagName().equals(name) == true && (namespaceURI == null || namespaceURI.equals(node.getNamespaceURI())))
                        {
                        	nodeList.add(node);
                        }
                    }
                }
                
            }
        };
        try
        {
            CNode.walkTree(null,this, nodeProcessor, true);
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return nodeList;
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Element#getAttributeNS(java.lang.String, java.lang.String)
     */
    @Override
    public String getAttributeNS(String namespaceURI, String localName) throws DOMException
    {
       if(hasAttributeNS(namespaceURI, localName))
       {
           return getAttributes().getNamedItemNS(namespaceURI, localName).getNodeValue();
       }
       else
       {
           return "";
       }
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Element#setAttributeNS(java.lang.String, java.lang.String, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void setAttributeNS(String namespaceURI, String qualifiedName, String value) throws DOMException
    {
        ((CNamedNodeMap) getAttributes()).setNamedItemNS(new CAttr(this, namespaceURI, qualifiedName, value));
        cascadeDOMEvent(prepareEvent(EventType.UPDATE, this));
    }
    
    /* (non-Javadoc)
     * @see org.w3c.dom.Element#removeAttributeNS(java.lang.String, java.lang.String)
     */
    @Override
    public void removeAttributeNS(String namespaceURI, String localName) throws DOMException
    {
        CNamedNodeMap namedNodeMap = (CNamedNodeMap) getAttributes();
        for(int index = 0; index < namedNodeMap.getLength(); index++)
        {
            CAttr attr = (CAttr) namedNodeMap.get(index);
            if(EqualityProcessor.areSame(attr.getNamespaceURI(), namespaceURI) && localName.equals(attr.getLocalName()))
            {
                namedNodeMap.remove(index);
                cascadeDOMEvent(prepareEvent(EventType.UPDATE, this));
                return;
            }
        }
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Element#getAttributeNodeNS(java.lang.String, java.lang.String)
     */
    @Override
    public Attr getAttributeNodeNS(String namespaceURI, String localName) throws DOMException
    {
        CNamedNodeMap namedNodeMap = (CNamedNodeMap) getAttributes();
        for(int index = 0; index < namedNodeMap.getLength(); index++)
        {
            CAttr attr = (CAttr) namedNodeMap.get(index);
            if(EqualityProcessor.areSame(attr.getNamespaceURI(), namespaceURI) && localName.equals(attr.getLocalName()))
            {
                return attr;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Element#setAttributeNodeNS(org.w3c.dom.Attr)
     */
    @Override
    public Attr setAttributeNodeNS(Attr newAttr) throws DOMException
    {
    	Attr attr = (Attr) getAttributes().setNamedItemNS(newAttr); 
    	cascadeDOMEvent(prepareEvent(EventType.UPDATE, this));
        return attr;
        
    }

    

    /* (non-Javadoc)
     * @see org.w3c.dom.Element#hasAttribute(java.lang.String)
     */
    @Override
    public boolean hasAttribute(String name)
    {
       return getAttributes().getNamedItem(name) != null;
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Element#hasAttributeNS(java.lang.String, java.lang.String)
     */
    @Override
    public boolean hasAttributeNS(String namespaceURI, String localName) throws DOMException
    {
        return getAttributes().getNamedItemNS(namespaceURI, localName) != null;
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Element#getSchemaTypeInfo()
     */
    @Override
    public TypeInfo getSchemaTypeInfo()
    {
        Thread.dumpStack();
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Element#setIdAttribute(java.lang.String, boolean)
     */
    @Override
    public void setIdAttribute(String name, boolean isId) throws DOMException
    {
        // TODO Auto-generated method stub
        Thread.dumpStack();
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Element#setIdAttributeNS(java.lang.String, java.lang.String, boolean)
     */
    @Override
    public void setIdAttributeNS(String namespaceURI, String localName, boolean isId) throws DOMException
    {
        // TODO Auto-generated method stub
        Thread.dumpStack();
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Element#setIdAttributeNode(org.w3c.dom.Attr, boolean)
     */
    @Override
    public void setIdAttributeNode(Attr idAttr, boolean isId) throws DOMException
    {
        // TODO Auto-generated method stub
        Thread.dumpStack();
        throw new UnsupportedOperationException();
    }

	public void setAttribute(Enum name, Enum value)
	{
		setAttribute(name.toString(), value.toString());		
	}

	/**
	 * This will return the content of this element as a string including xml  as oppsed to getText() while only returns the text content of the element
	 * @return
	 * @throws Exception 
	 */
    public String getContentAsText() throws Exception
    {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        XPath.dumpNodeContent(this, buffer);
        return new String(buffer.toByteArray());        
    }

}
