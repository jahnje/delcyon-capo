package com.delcyon.capo.xml.dom;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.logging.Level;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.elements.ResourceControlElement;
import com.delcyon.capo.resourcemanager.ContentFormatType;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceURI;
import com.delcyon.capo.resourcemanager.types.ContentMetaData;
import com.delcyon.capo.util.CloneControl;
import com.delcyon.capo.util.ControlledClone;
import com.delcyon.capo.util.EqualityProcessor;
import com.delcyon.capo.util.ToStringControl;
import com.delcyon.capo.util.CloneControl.Clone;
import com.delcyon.capo.util.ToStringControl.Control;
import com.delcyon.capo.xml.cdom.CElement;
import com.delcyon.capo.xml.cdom.CNodeList;

@CloneControl(filter=CloneControl.Clone.exclude,modifiers=Modifier.STATIC+Modifier.FINAL)
@ToStringControl(control=Control.exclude,modifiers=Modifier.STATIC+Modifier.FINAL)
public class ResourceElement extends CElement implements ControlledClone,ResourceNode
{

    
    
    @CloneControl(filter=Clone.exclude)
    @ToStringControl(control=Control.exclude)
    private ResourceControlElement resourceControlElement;
    
    @CloneControl(filter=Clone.exclude)
    @ToStringControl(control=Control.exclude)
    private ResourceDocument ownerResourceDocument;
    
    private ResourceDescriptor resourceDescriptor;    
    
    private List<ContentMetaData> childResourceContentMetaData;
    private ContentMetaData contentMetaData;
   
	private boolean dynamic = true;
	private ResourceURI resourceURI;
	private Element content;
	
	
	@SuppressWarnings("unused")
	private ResourceElement(){} //serialization only
	
	public ResourceElement(ResourceDocument ownerResourceDocument, String localName, Element content,ContentMetaData contentMetaData)
	{
		super(CapoApplication.RESOURCE_NAMESPACE_URI,"resource",localName);		
		this.ownerResourceDocument = ownerResourceDocument;		
		this.content = content;		
		this.dynamic = false;
		setContentMetatData(contentMetaData);
		
	}
    
	/**
	 * This will pull the localName from the resourceDescriptor to use as it's localName
	 * @param ownerResourceDocument
	 * @param parentNode
	 * @param resourceDescriptor
	 * @throws Exception
	 */
    public ResourceElement(ResourceDocument ownerResourceDocument,ResourceNode parentNode,ResourceDescriptor resourceDescriptor) throws Exception
    {
    	super(CapoApplication.RESOURCE_NAMESPACE_URI,"resource",resourceDescriptor.getLocalName());	
    	this.ownerResourceDocument = ownerResourceDocument;
        this.resourceDescriptor = resourceDescriptor;        
        this.dynamic = true;
        setContentMetatData(resourceDescriptor.getResourceMetaData(null));
    }

    public void setContentMetatData(ContentMetaData contentMetaData)
    {
    	this.contentMetaData = contentMetaData;
    	this.resourceURI = contentMetaData.getResourceURI();
    	setAttribute("uri", contentMetaData.getResourceURI().getResourceURIString());
    	
        List<String> supportedAttributeList = contentMetaData.getSupportedAttributes();
        for (String attributeName : supportedAttributeList)
        {
            if (contentMetaData.getValue(attributeName) != null)
            {                
                ResourceAttr resourceAttr = new ResourceAttr(this,attributeName, contentMetaData.getValue(attributeName));
                setAttributeNode(resourceAttr);
            }
        }
    }
    
    
//    public ResourceElement(ResourceNode parentNode, ResourceControlElement resourceControlElement) throws Exception
//    {
//        this.parentNode = parentNode;
//        this.resourceControlElement = resourceControlElement;
//        this.recursive = false;
//        namespaceURI = parentNode.getNamespaceURI();
//        prefix = parentNode.getPrefix();
//        NamedNodeMap attributeNamedNodeMap = resourceControlElement.getControlElementDeclaration().getAttributes();
//        for(int index = 0; index < attributeNamedNodeMap.getLength(); index++)
//        {
//            Attr attr = (Attr) attributeNamedNodeMap.item(index);
//            ResourceAttr resourceAttr = new ResourceAttr(this,attr.getNodeName(), attr.getNodeValue());            
//            attributeList.add(resourceAttr);
//        }
//        
//        if(attributeList.getNamedItem("name") != null)
//        {
//            localName = attributeList.getNamedItem("name").getNodeValue();
//        }
//        else
//        {
//            localName = resourceControlElement.getControlElementDeclaration().getLocalName();
//        }
//        
//        //if we have a URI, then we can go ahead a load a resourceDescriptor for this element 
//        if(attributeList.getNamedItem("uri") != null)
//        {
//            this.resourceDescriptor = CapoApplication.getDataManager().getResourceDescriptor(resourceControlElement, attributeList.getNamedItem("uri").getNodeValue());
//            
//            this.resourceControlElement.setResourceDescriptor(resourceDescriptor);
//        }
//        else if(attributeList.getNamedItem("path") != null) //check for a path attribute, and ask our parent to load us
//        {
//            this.resourceDescriptor = parentNode.getResourceDescriptor().getChildResourceDescriptor(resourceControlElement, attributeList.getNamedItem("path").getNodeValue());
//        }
//        else
//        {
//            //we don't have anything, throw an exception for now?
//            throw new Exception("Must have a uri or a path attribute");
//        }
//        
//        //sometimes we may have variables that need to be filled out laster, so we can't init or open this yet.  
//        if(resourceControlElement.getControlElementDeclaration().getAttribute("dynamic").equalsIgnoreCase("true") == false)
//        {
//        	resourceDescriptor.init(this,resourceControlElement.getParentGroup(), LifeCycle.EXPLICIT,true,ResourceParameterBuilder.getResourceParameters(resourceControlElement.getControlElementDeclaration()));
//        	resourceDescriptor.open(resourceControlElement.getParentGroup(), ResourceParameterBuilder.getResourceParameters(resourceControlElement.getControlElementDeclaration()));
//        }
//        
//        NodeList childResourceElementDeclarationNodeList =  XPath.selectNSNodes(resourceControlElement.getControlElementDeclaration(), prefix+":child", prefix+"="+namespaceURI);
//        for(int index = 0; index < childResourceElementDeclarationNodeList.getLength(); index++)
//        {
//            ResourceControlElement childResourceControlElement = new ResourceControlElement();
//            //XXX This is a hack! we are setting the parent group to null, so that it won't process any of the attributes that might have vars.
//            childResourceControlElement.init((Element) childResourceElementDeclarationNodeList.item(index), resourceControlElement, null, resourceControlElement.getControllerClientRequestProcessor());
//            //XXX then we set it back here, so the we still have the full var stack. This would all be fine until we change the init method in the AbstractControl class. 
//            childResourceControlElement.setParentGroup(resourceControlElement.getParentGroup());
//            nodeList.add(new ResourceElement(this, childResourceControlElement));
//        }
//    }
    
    public void setContent(Element content)
	{
		this.content = content;
	}
    
    public Element getContent()
	{
		return content;
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
        if(getOwnerResourceDocument().isContentOnly() && content != null)
        {
            return content.getNodeName();
        }
        else
        {
            return super.getNodeName();
        }
    }

   

   


    @Override
    public NodeList getChildNodes()
    {    	
        //depending on how we're created, we either know the list, or we want to figure it out.
        if (dynamic == false)
        {
        	try
        	{        		
        		if( content != null)
        		{
        		    CNodeList tempNodeList = null;
        		    if(getOwnerResourceDocument().isContentOnly() == false)
        		    {
        		        tempNodeList = new CNodeList();
        		        tempNodeList.add(0,content);
        		        tempNodeList.addAll(super.getChildNodes());
        		    }
        		    else
        		    {
        		        tempNodeList = new CNodeList();
        		        tempNodeList.addAll(content.getChildNodes());        		        
        		        tempNodeList.addAll(EqualityProcessor.clone(super.getChildNodes()));
        		    }
        			
        			return tempNodeList;
        		}
        	} catch (Exception e)
        	{
        		// TODO Auto-generated catch block
        		e.printStackTrace();
        	}

        	
            return super.getChildNodes();
        }
        
        if (childResourceContentMetaData == null)
        {
            childResourceContentMetaData = contentMetaData.getContainedResources();
            for (ContentMetaData childContentMetaData : childResourceContentMetaData)
            {
                
                try
                {
                    appendChild(new ResourceElement(ownerResourceDocument,this,CapoApplication.getDataManager().getResourceDescriptor(null, childContentMetaData.getResourceURI().getBaseURI())));
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
                        
                        appendChild(xmlElement);
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
        return super.getChildNodes();
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
    public Node getNextSibling()
    {
    
       NodeList siblingList = getParentNode().getChildNodes();
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
        if(getOwnerResourceDocument().isContentOnly())
        {
            if(content != null)
            {
                return content.getAttributes();
            }
            else
            {
                return null;
            }
        }
        else
        {
            return super.getAttributes();
        }
    }

    
    public ResourceDocument getOwnerResourceDocument()
	{
		return ownerResourceDocument;
	}
    
    

    @Override
    public boolean hasChildNodes()
    {
        return getChildNodes().getLength() > 0;
    }

    @Override
    public void preClone(Object clonedParentObject, Object clonedObject) throws Exception
    {
//        if(clonedParentObject != null)
//        {
//            ((ResourceElement)clonedObject).parentNode = ((ResourceElement) clonedParentObject);
//        }
//        
    }
    
    
    public void postClone(Object parentObject, Object clonedObject)
    {
        ResourceElement clonedResourceElement = (ResourceElement) clonedObject;
        //we treat these differently, because we don't want them to recurse
        clonedResourceElement.ownerResourceDocument = ownerResourceDocument;
        
        clonedResourceElement.resourceControlElement = resourceControlElement;

    }



    @Override
    public String getNamespaceURI()
    {        
        if(getOwnerResourceDocument().isContentOnly() == true && content != null)
        {
            return content.getNamespaceURI();
        }
        else
        {
            return super.getNamespaceURI();
        }
    }

   

    @Override
    public String getLocalName()
    {
        if(getOwnerResourceDocument().isContentOnly() == true && content != null)
        {
            return content.getLocalName();
        }
        else
        {
            return super.getLocalName();
        }
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


   

    public Element export(boolean contentOnly) throws Exception
    {    	
        return ResourceDocument.export(this,contentOnly).getDocumentElement();
    }

	

    
}
