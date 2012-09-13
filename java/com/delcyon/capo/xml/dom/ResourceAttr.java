package com.delcyon.capo.xml.dom;

import java.lang.reflect.Modifier;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.UserDataHandler;

import com.delcyon.capo.controller.elements.ResourceControlElement;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.util.ReflectionUtility;
import com.delcyon.capo.util.ToStringControl;
import com.delcyon.capo.util.ToStringControl.Control;

@ToStringControl(control=Control.exclude,modifiers=Modifier.FINAL+Modifier.STATIC)
public class ResourceAttr extends ResourceNode implements Attr
{

    @ToStringControl(control=Control.exclude)
    private ResourceElement parentElement = null;
    private String name = null;
    private ResourceText value = null;
    public ResourceAttr(ResourceElement parentElement, String name, String value)
    {
        this.parentElement = parentElement;
        this.name = name;
        this.value = new ResourceText(this,value);
    }

    @Override
    public ResourceDescriptor getResourceDescriptor()
    {
        return this.parentElement.getResourceDescriptor();
    }
    
    @Override
    public ResourceControlElement getResourceControlElement()
    {
        return this.parentElement.getResourceControlElement();
    }
    
    @Override
    public String getNodeName()
    {        
        return name;
    }

    @Override
    public String getNodeValue() throws DOMException
    {
        return value.getNodeValue();
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
        return value;
    }

    @Override
    public Node getLastChild()
    {
        return getFirstChild();   
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
    	throw new UnsupportedOperationException();
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

    /**
     * http://www.w3.org/2003/01/dom2-javadoc/org/w3c/dom/Attr.html#getSpecified__
     * //because our dom is always readonly, this can only be true
     */
    @Override
    public boolean getSpecified()
    {    	
        return true;
       
    }

    @Override
    public String getValue()
    {
        return this.value.getNodeValue();
    }

    @Override
    public void setValue(String value) throws DOMException
    {
       this.value = new ResourceText(this,value);

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

    @Override
    public String toString()
    {
        return ReflectionUtility.processToString(this);
    }
    
}
