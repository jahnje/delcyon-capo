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

import com.delcyon.capo.resourcemanager.ResourceDescriptor;

public class ResourceElement extends ResourceNode implements Element
{

    private ResourceDescriptor resourceDescriptor;

    public ResourceElement(ResourceDescriptor resourceDescriptor)
    {
        this.resourceDescriptor = resourceDescriptor;
    }

    @Override
    public String getNodeName()
    {
        return resourceDescriptor.getResourceURI();
    }

    @Override
    public String getNodeValue() throws DOMException
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public void setNodeValue(String nodeValue) throws DOMException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public short getNodeType()
    {
        return ELEMENT_NODE;
    }

    @Override
    public Node getParentNode()
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public NodeList getChildNodes()
    {
        // TODO Auto-generated method stub
        
        throw new UnsupportedOperationException();
    }

    @Override
    public Node getFirstChild()
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public Node getLastChild()
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public Node getPreviousSibling()
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public Node getNextSibling()
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public NamedNodeMap getAttributes()
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public Document getOwnerDocument()
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public Node insertBefore(Node newChild, Node refChild) throws DOMException
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public Node replaceChild(Node newChild, Node oldChild) throws DOMException
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public Node removeChild(Node oldChild) throws DOMException
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public Node appendChild(Node newChild) throws DOMException
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasChildNodes()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Node cloneNode(boolean deep)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public void normalize()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isSupported(String feature, String version)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getNamespaceURI()
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPrefix()
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPrefix(String prefix) throws DOMException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public String getLocalName()
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasAttributes()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getBaseURI()
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public short compareDocumentPosition(Node other) throws DOMException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getTextContent() throws DOMException
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTextContent(String textContent) throws DOMException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isSameNode(Node other)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String lookupPrefix(String namespaceURI)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDefaultNamespace(String namespaceURI)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String lookupNamespaceURI(String prefix)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEqualNode(Node arg)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Object getFeature(String feature, String version)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public Object setUserData(String key, Object data, UserDataHandler handler)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getUserData(String key)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public String getTagName()
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public String getAttribute(String name)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAttribute(String name, String value) throws DOMException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeAttribute(String name) throws DOMException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public Attr getAttributeNode(String name)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public Attr setAttributeNode(Attr newAttr) throws DOMException
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public Attr removeAttributeNode(Attr oldAttr) throws DOMException
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public NodeList getElementsByTagName(String name)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public String getAttributeNS(String namespaceURI, String localName) throws DOMException
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAttributeNS(String namespaceURI, String qualifiedName, String value) throws DOMException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeAttributeNS(String namespaceURI, String localName) throws DOMException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public Attr getAttributeNodeNS(String namespaceURI, String localName) throws DOMException
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public Attr setAttributeNodeNS(Attr newAttr) throws DOMException
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public NodeList getElementsByTagNameNS(String namespaceURI, String localName) throws DOMException
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasAttribute(String name)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean hasAttributeNS(String namespaceURI, String localName) throws DOMException
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public TypeInfo getSchemaTypeInfo()
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public void setIdAttribute(String name, boolean isId) throws DOMException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setIdAttributeNS(String namespaceURI, String localName, boolean isId) throws DOMException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setIdAttributeNode(Attr idAttr, boolean isId) throws DOMException
    {
        // TODO Auto-generated method stub

    }

}
