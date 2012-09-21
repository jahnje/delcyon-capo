package com.delcyon.capo.xml.dom;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.w3c.dom.UserDataHandler;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.elements.ResourceControlElement;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;

public class ResourceDocument extends ResourceNode implements Document
{
    
    private ResourceDescriptor resourceDescriptor;
    private Element documentElement;
    private ResourceNodeList resourceNodeList = new ResourceNodeList();
    private ResourceDOMImplemetation resourceDOMImplemetation = new ResourceDOMImplemetation();
    private String prefix = "resource";
    private String namespaceURI = CapoApplication.RESOURCE_NAMESPACE_URI;
    private ResourceControlElement resourceControlElement = null;
    
    public ResourceDocument(ResourceDescriptor resourceDescriptor) throws Exception
    {
       this.resourceDescriptor = resourceDescriptor;
       this.documentElement = new ResourceElement(this,resourceDescriptor);
       resourceNodeList.add(documentElement);
    }

    public ResourceDocument(ResourceControlElement resourceControlElement) throws Exception
    {
    	this.prefix = resourceControlElement.getControlElementDeclaration().getPrefix();
    	this.namespaceURI = resourceControlElement.getControlElementDeclaration().getNamespaceURI();
    	this.documentElement = new ResourceElement(this,resourceControlElement);
    	
    	
//    	this.resourceDescriptor = CapoApplication.getDataManager().getResourceDescriptor(resourceControlElement, resourceControlElement.getAttributeValue(Attributes.uri));
//    	this.resourceDescriptor.init(resourceControlElement.getParentGroup(),resourceControlElement.getLifeCycle(),resourceControlElement.getAttributeValue(Attributes.step).equalsIgnoreCase("true"),ResourceParameterBuilder.getResourceParameters(resourceControlElement.getControlElementDeclaration()));
//    	this.resourceDescriptor.open(resourceControlElement.getParentGroup());
//    	resourceControlElement.setResourceDescriptor(this.resourceDescriptor);
//    	this.documentElement = new ResourceElement(this,resourceDescriptor);
    	resourceNodeList.add(documentElement);
    }
    
    @Override
    public ResourceDescriptor getResourceDescriptor()
    {
        return new ResourceElementResourceDescriptor((ResourceElement) this.documentElement);
    }
    
    @Override
    public ResourceDescriptor getProxyedResourceDescriptor()
    {
    	return this.resourceDescriptor;
    }
    
    @Override
    public ResourceControlElement getResourceControlElement()
    {
        return this.resourceControlElement;
    }
    
    @Override
    public String getNodeName()
    {
        return "#document";
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
        return DOCUMENT_NODE;
    }

    @Override
    public Node getParentNode()
    {
        return null;
    }

    @Override
    public NodeList getChildNodes()
    {
        return resourceNodeList;
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
        return this;
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
        if (getDocumentElement() != null)
        {
            return true;    
        }
        else
        {
            return false;
        }
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
        return this.namespaceURI;
    }

    @Override
    public String getPrefix()
    {
        return this.prefix;
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
    public DocumentType getDoctype()
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public DOMImplementation getImplementation()
    {
    	return resourceDOMImplemetation;
    }

    @Override
    public Element getDocumentElement()
    {        
        return this.documentElement;
    }

    @Override
    public Element createElement(String tagName) throws DOMException
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public DocumentFragment createDocumentFragment()
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public Text createTextNode(String data)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public Comment createComment(String data)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public CDATASection createCDATASection(String data) throws DOMException
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public ProcessingInstruction createProcessingInstruction(String target, String data) throws DOMException
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public Attr createAttribute(String name) throws DOMException
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public EntityReference createEntityReference(String name) throws DOMException
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public NodeList getElementsByTagName(String tagname)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public Node importNode(Node importedNode, boolean deep) throws DOMException
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public Element createElementNS(String namespaceURI, String qualifiedName) throws DOMException
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public Attr createAttributeNS(String namespaceURI, String qualifiedName) throws DOMException
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public NodeList getElementsByTagNameNS(String namespaceURI, String localName)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public Element getElementById(String elementId)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public String getInputEncoding()
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public String getXmlEncoding()
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getXmlStandalone()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setXmlStandalone(boolean xmlStandalone) throws DOMException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public String getXmlVersion()
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public void setXmlVersion(String xmlVersion) throws DOMException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean getStrictErrorChecking()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setStrictErrorChecking(boolean strictErrorChecking)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public String getDocumentURI()
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDocumentURI(String documentURI)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public Node adoptNode(Node source) throws DOMException
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public DOMConfiguration getDomConfig()
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public void normalizeDocument()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public Node renameNode(Node n, String namespaceURI, String qualifiedName) throws DOMException
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

}
