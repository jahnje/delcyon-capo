package com.delcyon.capo.xml.dom;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.UserDataHandler;

public class ResourceAttr extends ResourceNode implements Attr
{

    private ResourceElement parentElement = null;
    private String name = null;
    private String value = null;
    public ResourceAttr(ResourceElement parentElement, String name, String value)
    {
        this.parentElement = parentElement;
        this.name = name;
        this.value = value;
    }

    @Override
    public String getNodeName()
    {        
        return name;
    }

    @Override
    public String getNodeValue() throws DOMException
    {
        return value;
    }

    @Override
    public void setNodeValue(String nodeValue) throws DOMException
    {
        throw new UnsupportedOperationException();

    }

    @Override
    public short getNodeType()
    {
        return ATTRIBUTE_NODE;
    }

    @Override
    public Node getParentNode()
    {
        return parentElement;
    }

    @Override
    public NodeList getChildNodes()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node getFirstChild()
    {        
        return null;
    }

    @Override
    public Node getLastChild()
    {
        return null;   
    }

    @Override
    public Node getPreviousSibling()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node getNextSibling()
    {        
        throw new UnsupportedOperationException();        
    }

    @Override
    public NamedNodeMap getAttributes()
    {        
        return null;
    }

    @Override
    public Document getOwnerDocument()
    {        
        return parentElement.getOwnerDocument();
    }

    @Override
    public Node insertBefore(Node newChild, Node refChild) throws DOMException
    {
        throw new UnsupportedOperationException();
        
    }

    @Override
    public Node replaceChild(Node newChild, Node oldChild) throws DOMException
    {
        throw new UnsupportedOperationException();
        
    }

    @Override
    public Node removeChild(Node oldChild) throws DOMException
    {
        throw new UnsupportedOperationException();
        
    }

    @Override
    public Node appendChild(Node newChild) throws DOMException
    {
        throw new UnsupportedOperationException();
        
    }

    @Override
    public boolean hasChildNodes()
    {        
        return false;
    }

    @Override
    public Node cloneNode(boolean deep)
    {
        throw new UnsupportedOperationException();
        
    }

    @Override
    public void normalize()
    {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean isSupported(String feature, String version)
    {
        throw new UnsupportedOperationException();
        
    }

    @Override
    public String getNamespaceURI()
    {
        return parentElement.getNamespaceURI();
        
    }

    @Override
    public String getPrefix()
    {
        throw new UnsupportedOperationException();
        
    }

    @Override
    public void setPrefix(String prefix) throws DOMException
    {
        throw new UnsupportedOperationException();

    }

    @Override
    public String getLocalName()
    {        
        return name;
    }

    @Override
    public boolean hasAttributes()
    {
        throw new UnsupportedOperationException();
        
    }

    @Override
    public String getBaseURI()
    {
        throw new UnsupportedOperationException();
        
    }

    @Override
    public short compareDocumentPosition(Node other) throws DOMException
    {
        throw new UnsupportedOperationException();
        
    }

    @Override
    public String getTextContent() throws DOMException
    {
        throw new UnsupportedOperationException();
        
    }

    @Override
    public void setTextContent(String textContent) throws DOMException
    {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean isSameNode(Node other)
    {
        throw new UnsupportedOperationException();
        
    }

    @Override
    public String lookupPrefix(String namespaceURI)
    {
        throw new UnsupportedOperationException();
        
    }

    @Override
    public boolean isDefaultNamespace(String namespaceURI)
    {
        throw new UnsupportedOperationException();
        
    }

    @Override
    public String lookupNamespaceURI(String prefix)
    {
        throw new UnsupportedOperationException();
        
    }

    @Override
    public boolean isEqualNode(Node arg)
    {
        throw new UnsupportedOperationException();
        
    }

    @Override
    public Object getFeature(String feature, String version)
    {
        throw new UnsupportedOperationException();
        
    }

    @Override
    public Object setUserData(String key, Object data, UserDataHandler handler)
    {
        throw new UnsupportedOperationException();
        
    }

    @Override
    public Object getUserData(String key)
    {
        throw new UnsupportedOperationException();
        
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public boolean getSpecified()
    {
        throw new UnsupportedOperationException();
       
    }

    @Override
    public String getValue()
    {
        return this.value;
    }

    @Override
    public void setValue(String value) throws DOMException
    {
       this.value = value;

    }

    @Override
    public Element getOwnerElement()
    {
        return parentElement;
    }

    @Override
    public TypeInfo getSchemaTypeInfo()
    {
        throw new UnsupportedOperationException();
        
    }

    @Override
    public boolean isId()
    {
        throw new UnsupportedOperationException();
        
    }

}