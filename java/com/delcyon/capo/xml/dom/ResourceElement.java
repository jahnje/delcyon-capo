package com.delcyon.capo.xml.dom;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
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
import com.delcyon.capo.controller.elements.ResourceControlElement;
import com.delcyon.capo.resourcemanager.ContentFormatType;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceParameterBuilder;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.LifeCycle;
import com.delcyon.capo.resourcemanager.types.ContentMetaData;
import com.delcyon.capo.util.ReflectionUtility;
import com.delcyon.capo.util.ToStringControl;
import com.delcyon.capo.util.ToStringControl.Control;
import com.delcyon.capo.xml.XPath;

@ToStringControl(control=Control.exclude,modifiers=Modifier.STATIC+Modifier.FINAL)
public class ResourceElement extends ResourceNode implements Element
{

    private ResourceDescriptor resourceDescriptor;
    
    private String namespaceURI = null;
    private String localName = null;
    private List<ContentMetaData> childResourceContentMetaData;
    private ContentMetaData contentMetaData;
    private ResourceNodeList nodeList = new ResourceNodeList();
    private ResourceNamedNodeMap attributeList = new ResourceNamedNodeMap();
	private String prefix;
	@ToStringControl(control=Control.exclude)
	private ResourceNode parentNode;
	@ToStringControl(control=Control.exclude)
    private ResourceControlElement resourceControlElement;
	private boolean recursive = true;

    private Vector<Element> cacheVector = null;

	private HashMap<String, Vector<Element>> joinHashMap = null;; 
	
    
    
    public ResourceElement(ResourceNode parentNode,ResourceDescriptor resourceDescriptor) throws Exception
    {
        this.resourceDescriptor = resourceDescriptor;
        this.parentNode = parentNode;
        this.recursive = true;
        namespaceURI = parentNode.getNamespaceURI();
        prefix = parentNode.getPrefix();
        localName = resourceDescriptor.getLocalName();
        contentMetaData = resourceDescriptor.getContentMetaData(null);
        attributeList.add(new ResourceAttr(this,"uri", contentMetaData.getResourceURI().getBaseURI()));
        List<String> supportedAttributeList = contentMetaData.getSupportedAttributes();
        for (String attributeName : supportedAttributeList)
        {
            if (contentMetaData.getValue(attributeName) != null)
            {                
                ResourceAttr resourceAttr = new ResourceAttr(this,attributeName, contentMetaData.getValue(attributeName));
                attributeList.add(resourceAttr);
            }
        }
        
                
    }

    public ResourceElement(ResourceNode parentNode, ResourceControlElement resourceControlElement) throws Exception
    {
        this.parentNode = parentNode;
        this.resourceControlElement = resourceControlElement;
        this.recursive = false;
        namespaceURI = parentNode.getNamespaceURI();
        prefix = parentNode.getPrefix();
        NamedNodeMap attributeNamedNodeMap = resourceControlElement.getControlElementDeclaration().getAttributes();
        for(int index = 0; index < attributeNamedNodeMap.getLength(); index++)
        {
            Attr attr = (Attr) attributeNamedNodeMap.item(index);
            ResourceAttr resourceAttr = new ResourceAttr(this,attr.getNodeName(), attr.getNodeValue());            
            attributeList.add(resourceAttr);
        }
        
        if(attributeList.getNamedItem("name") != null)
        {
            localName = attributeList.getNamedItem("name").getNodeValue();
        }
        else
        {
            localName = resourceControlElement.getControlElementDeclaration().getLocalName();
        }
        
        //if we have a URI, then we can go ahead a load a resourceDescriptor for this element 
        if(attributeList.getNamedItem("uri") != null)
        {
            this.resourceDescriptor = CapoApplication.getDataManager().getResourceDescriptor(resourceControlElement, attributeList.getNamedItem("uri").getNodeValue());
            
            this.resourceControlElement.setResourceDescriptor(resourceDescriptor);
        }
        else if(attributeList.getNamedItem("path") != null) //check for a path attribute, and ask our parent to load us
        {
            this.resourceDescriptor = parentNode.getResourceDescriptor().getChildResourceDescriptor(resourceControlElement, attributeList.getNamedItem("path").getNodeValue());
        }
        else
        {
            //we don't have anything, throw an exception for now?
            throw new Exception("Must have a uri or a path attribute");
        }
        
        //sometimes we may have variables that need to be filled out laster, so we can't init or open this yet.  
        if(resourceControlElement.getControlElementDeclaration().getAttribute("dynamic").equalsIgnoreCase("true") == false)
        {
        	resourceDescriptor.init(this,resourceControlElement.getParentGroup(), LifeCycle.EXPLICIT,true,ResourceParameterBuilder.getResourceParameters(resourceControlElement.getControlElementDeclaration()));
        	resourceDescriptor.open(resourceControlElement.getParentGroup(), ResourceParameterBuilder.getResourceParameters(resourceControlElement.getControlElementDeclaration()));
        }
        
        NodeList childResourceElementDeclarationNodeList =  XPath.selectNSNodes(resourceControlElement.getControlElementDeclaration(), prefix+":child", prefix+"="+namespaceURI);
        for(int index = 0; index < childResourceElementDeclarationNodeList.getLength(); index++)
        {
            ResourceControlElement childResourceControlElement = new ResourceControlElement();
            //XXX This is a hack! we are setting the parent group to null, so that it won't process any of the attributes that might have vars.
            childResourceControlElement.init((Element) childResourceElementDeclarationNodeList.item(index), resourceControlElement, null, resourceControlElement.getControllerClientRequestProcessor());
            //XXX then we set it back here, so the we still have the full var stack. This would all be fine until we change the init method in the AbstractControl class. 
            childResourceControlElement.setParentGroup(resourceControlElement.getParentGroup());
            nodeList.add(new ResourceElement(this, childResourceControlElement));
        }
    }
    

   @Override
   public ResourceDescriptor getResourceDescriptor()
   {
       return new ResourceElementResourceDescriptor(this);
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
        return prefix+":"+resourceDescriptor.getLocalName();
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
    	throw new UnsupportedOperationException();
    }

    @Override
    public short getNodeType()
    {
        return ELEMENT_NODE;
    }

    @Override
    public Node getParentNode()
    {
        return this.parentNode;
    }

    @Override
    public NodeList getChildNodes()
    {
        //depending on how we're created, we either know the list, or we want to figure it out.
        if (recursive == false)
        {
            return nodeList;
        }
        
        if (childResourceContentMetaData == null)
        {
            childResourceContentMetaData = contentMetaData.getContainedResources();
            for (ContentMetaData childContentMetaData : childResourceContentMetaData)
            {
                
                try
                {
                    nodeList.add(new ResourceElement(this,CapoApplication.getDataManager().getResourceDescriptor(null, childContentMetaData.getResourceURI().getBaseURI())));
                }
                catch (Exception e)
                {
                   CapoApplication.logger.log(Level.WARNING,"couldn't load resource: "+childContentMetaData.getResourceURI(),e);
                }
            }
            if (contentMetaData.isContainer() == false)
            {
                System.err.println(contentMetaData.getResourceURI().getBaseURI()+" has data!");
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
    	NodeList childNodes = getChildNodes();
    	if (childNodes.getLength() > 0)
    	{
    		return childNodes.item(0);
    	}
    	else
    	{
    		return null;
    	}
        
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
    
       NodeList siblingList = parentNode.getChildNodes();
       int myPosition = 0;
       for(int index = 0; index < siblingList.getLength(); index++)
       {
    	   Node siblingNode = siblingList.item(index);
    	   if (siblingNode.isSameNode(this))
    	   {
    		   myPosition = index;
    		   break;
    	   }
       }
       if (myPosition+1 < siblingList.getLength())
       {
    	   return siblingList.item(myPosition+1);
       }
       else
       {
    	   return null;
       }
    }

    @Override
    public NamedNodeMap getAttributes()
    {
        return attributeList;
    }

    @Override
    public Document getOwnerDocument()
    {
        if (parentNode instanceof Document)
        {
        	return (Document) parentNode;
        }
        else
        {
        	return parentNode.getOwnerDocument();
        }
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
        return getChildNodes().getLength() > 0;
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
    	throw new UnsupportedOperationException();
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
    	throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSameNode(Node other)
    {    	
    	return this.equals(other);
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
    	if (hasAttribute(name))
    	{
    		return getAttributes().getNamedItem(name).getNodeValue();
    	}
    	else
    	{
    		return "";
    	}
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
    		return "";
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
    	return getAttributes().getNamedItem(name) != null;
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

    @Override
    public String toString()
    {
        return ReflectionUtility.processToString(this);
    }

    public Vector<Element> getCacheVector()
    {
        return this.cacheVector ;
    }
    
    public void setCacheVector(Vector<Element> cacheVector)
    {
        this.cacheVector = cacheVector;
    }

	public void setJoinHashMap(HashMap<String, Vector<Element>> joinHashMap)
	{
		this.joinHashMap  = joinHashMap;		
	}
	
	public HashMap<String, Vector<Element>> getJoinHashMap()
	{
		return joinHashMap;
	}
}
