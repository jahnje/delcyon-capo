package com.delcyon.capo.xml.dom;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.UserDataHandler;

public class ResourceText extends ResourceNode implements Text
{

	private String data;
	private ResourceNode parentNode;

	public ResourceText(ResourceNode parentNode,String data)
	{
		this.data = data;
		this.parentNode = parentNode;
	}
	
    @Override
    public String getData() throws DOMException
    {
        return data;
    }

    @Override
    public void setData(String data) throws DOMException
    {
        this.data = data;

    }

    @Override
    public int getLength()
    {
        return data.length();
    }

    @Override
    public String substringData(int offset, int count) throws DOMException
    {
        
        return data.substring(offset, offset+count);
    }

    @Override
    public void appendData(String arg) throws DOMException
    {
        throw new UnsupportedOperationException();

    }

    @Override
    public void insertData(int offset, String arg) throws DOMException
    {
    	throw new UnsupportedOperationException();

    }

    @Override
    public void deleteData(int offset, int count) throws DOMException
    {
    	throw new UnsupportedOperationException();

    }

    @Override
    public void replaceData(int offset, int count, String arg) throws DOMException
    {
    	throw new UnsupportedOperationException();

    }

    @Override
    public String getNodeName()
    {
        return "#Text";
    }

    @Override
    public String getNodeValue() throws DOMException
    {
        return data;
    }

    @Override
    public void setNodeValue(String nodeValue) throws DOMException
    {
    	throw new UnsupportedOperationException();

    }

    @Override
    public short getNodeType()
    {

        return TEXT_NODE;
    }

    @Override
    public Node getParentNode()
    {
        throw new UnsupportedOperationException();
        
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
        throw new UnsupportedOperationException();
        
    }

    @Override
    public Node getPreviousSibling()
    {
        throw new UnsupportedOperationException();
        
    }

    @Override
    public Node getNextSibling()
    {
    	if (parentNode instanceof ResourceAttr)
    	{
    		return null;
    	}
    	else
    	{
    		throw new UnsupportedOperationException();
    	}
        
    }

    @Override
    public NamedNodeMap getAttributes()
    {
        throw new UnsupportedOperationException();
        
    }

    @Override
    public Document getOwnerDocument()
    {
        throw new UnsupportedOperationException();
        
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
        throw new UnsupportedOperationException();
        
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
        throw new UnsupportedOperationException();
        
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
        throw new UnsupportedOperationException();
        
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
    public Text splitText(int offset) throws DOMException
    {
        throw new UnsupportedOperationException();
        
    }

    @Override
    public boolean isElementContentWhitespace()
    {
        throw new UnsupportedOperationException();
        
    }

    @Override
    public String getWholeText()
    {
        throw new UnsupportedOperationException();
        
    }

    @Override
    public Text replaceWholeText(String content) throws DOMException
    {
        throw new UnsupportedOperationException();
        
    }

}
