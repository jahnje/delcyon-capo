package com.delcyon.capo.xml.dom;

import java.util.List;
import java.util.logging.Level;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.UserDataHandler;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.resourcemanager.ContentFormatType;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.types.ContentMetaData;

public class ResourceElement extends ResourceNode implements Element
{

    private ResourceDescriptor resourceDescriptor;
    
    private String namespaceURI = null;
    private String localName = null;
    private List<ContentMetaData> childResourceContentMetaData;
    private ContentMetaData contentMetaData;
    private ResourceNodeList nodeList = new ResourceNodeList();

	private String prefix;

	private ResourceDocument ownerDocument;
    
    public ResourceElement(ResourceDocument ownerDocument,ResourceDescriptor resourceDescriptor) throws Exception
    {
        this.resourceDescriptor = resourceDescriptor;
        this.ownerDocument = ownerDocument;
        namespaceURI = CapoApplication.RESOURCE_NAMESPACE_URI;
        prefix = "resource";
        localName = resourceDescriptor.getLocalName();
        contentMetaData = resourceDescriptor.getContentMetaData(null);
        nodeList.add(new ResourceAttr(this,"uri", contentMetaData.getResourceURI()));
        List<String> supportedAttributeList = contentMetaData.getSupportedAttributes();
        for (String attributeName : supportedAttributeList)
        {
            if (contentMetaData.getValue(attributeName) != null)
            {                
                ResourceAttr resourceAttr = new ResourceAttr(this,attributeName, contentMetaData.getValue(attributeName));
                nodeList.add(resourceAttr);
            }
        }
        
                
    }

    @Override
    public String getNodeName()
    {
        return resourceDescriptor.getLocalName();
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
        if (childResourceContentMetaData == null)
        {
            childResourceContentMetaData = contentMetaData.getContainedResources();
            for (ContentMetaData childContentMetaData : childResourceContentMetaData)
            {
                
                try
                {
                    nodeList.add(new ResourceElement(ownerDocument,CapoApplication.getDataManager().getResourceDescriptor(null, childContentMetaData.getResourceURI())));
                }
                catch (Exception e)
                {
                   CapoApplication.logger.log(Level.WARNING,"couldn't load resource: "+childContentMetaData.getResourceURI(),e);
                }
            }
            if (contentMetaData.isContainer() == false)
            {
                System.err.println(contentMetaData.getResourceURI()+" has data!");
                if (contentMetaData.getContentFormatType() == ContentFormatType.XML)
                {
                    try
                    {
                        Element xmlElement = resourceDescriptor.readXML(null);
                        
                        nodeList.add(xmlElement);
                    }
                    catch (Exception e)
                    {
                        CapoApplication.logger.log(Level.WARNING,"couldn't load resource data: "+contentMetaData.getResourceURI(),e);
                    }
                }
                else 
                {
                    
                }
            }
        }
        return nodeList;
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
        return new ResourceNamedNodeMap(nodeList,Node.ATTRIBUTE_NODE);
    }

    @Override
    public Document getOwnerDocument()
    {
        return ownerDocument;
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
        return true;
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
        return namespaceURI;
    }

    @Override
    public String getPrefix()
    {
       return prefix;
    }

    @Override
    public void setPrefix(String prefix) throws DOMException
    {
        this.prefix = prefix;

    }

    @Override
    public String getLocalName()
    {
        return this.localName;
    }

    @Override
    public boolean hasAttributes()
    {
        //all resources have attributes, be cause they have content data, even if it's just to say they don't exist.
        return true;
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
    	throw new UnsupportedOperationException();
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
    	throw new UnsupportedOperationException();
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
    	throw new UnsupportedOperationException();
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
    	throw new UnsupportedOperationException();
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
    	throw new UnsupportedOperationException();
    }

    @Override
    public void removeAttribute(String name) throws DOMException
    {
        // TODO Auto-generated method stub
    	throw new UnsupportedOperationException();
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
    	if (this.namespaceURI.equalsIgnoreCase(namespaceURI))
    	{
    		return contentMetaData.getValue(localName);
    	}
    	else
    	{
    		return null;
    	}
    }

    @Override
    public void setAttributeNS(String namespaceURI, String qualifiedName, String value) throws DOMException
    {
        // TODO Auto-generated method stub
    	throw new UnsupportedOperationException();
    }

    @Override
    public void removeAttributeNS(String namespaceURI, String localName) throws DOMException
    {
        // TODO Auto-generated method stub
    	throw new UnsupportedOperationException();
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
    	throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasAttributeNS(String namespaceURI, String localName) throws DOMException
    {
    	if (this.namespaceURI.equalsIgnoreCase(namespaceURI))
    	{
    		return (contentMetaData.getValue(localName) != null);
    	}
    	else
    	{
    		return false;
    	}
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
    	throw new UnsupportedOperationException();
    }

    @Override
    public void setIdAttributeNS(String namespaceURI, String localName, boolean isId) throws DOMException
    {
        // TODO Auto-generated method stub
    	throw new UnsupportedOperationException();
    }

    @Override
    public void setIdAttributeNode(Attr idAttr, boolean isId) throws DOMException
    {
        // TODO Auto-generated method stub
    	throw new UnsupportedOperationException();
    }

}
