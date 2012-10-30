package com.delcyon.capo.xml.dom;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

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
import com.delcyon.capo.resourcemanager.types.ContentMetaData;
import com.delcyon.capo.xml.cdom.CDocument;

public class ResourceDocument extends CDocument implements ResourceNode
{
    
    private ResourceDescriptor resourceDescriptor;   
    private ResourceControlElement resourceControlElement = null;
    private boolean exportContentOnly = false;
    
    
    public ResourceDocument()
    {
    	
    }
    
    public void setDocumentElement(ResourceElement documentElement)
    {
    	
    	this.resourceDescriptor = documentElement.getProxyedResourceDescriptor();
    	appendChild(documentElement);
    }
    
    public ResourceDocument(ResourceDescriptor resourceDescriptor) throws Exception
    {
       this.resourceDescriptor = resourceDescriptor;
       appendChild( new ResourceElement(this,this,resourceDescriptor));
       
    }

    public ResourceDocument(ResourceElement documentResourceElement) throws Exception
    {       
       appendChild(documentResourceElement);
    }
    
//    public ResourceDocument(ResourceControlElement resourceControlElement) throws Exception
//    {
//    	this.prefix = resourceControlElement.getControlElementDeclaration().getPrefix();
//    	this.namespaceURI = resourceControlElement.getControlElementDeclaration().getNamespaceURI();
//    	this.documentElement = new ResourceElement(this,resourceControlElement);
//    	
//    	
////    	this.resourceDescriptor = CapoApplication.getDataManager().getResourceDescriptor(resourceControlElement, resourceControlElement.getAttributeValue(Attributes.uri));
////    	this.resourceDescriptor.init(resourceControlElement.getParentGroup(),resourceControlElement.getLifeCycle(),resourceControlElement.getAttributeValue(Attributes.step).equalsIgnoreCase("true"),ResourceParameterBuilder.getResourceParameters(resourceControlElement.getControlElementDeclaration()));
////    	this.resourceDescriptor.open(resourceControlElement.getParentGroup());
////    	resourceControlElement.setResourceDescriptor(this.resourceDescriptor);
////    	this.documentElement = new ResourceElement(this,resourceDescriptor);
//    	resourceNodeList.add(documentElement);
//    }
    
    public void setContentOnly(boolean exportContentOnly)
    {
        this.exportContentOnly = exportContentOnly;        
    }
    
    public boolean isContentOnly()
    {
        return exportContentOnly;
    }
    
    @Override
    public ResourceDescriptor getResourceDescriptor()
    {
        return new ResourceElementResourceDescriptor((ResourceElement) this.getDocumentElement());
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
    public ResourceDocument getOwnerResourceDocument()
    {
    	return this;
    }
    
    

   

    

   
    
   

	public ResourceElement createResourceElement(String localName, Element content,ContentMetaData contentMetaData)
	{
		return new ResourceElement(this, localName, content, contentMetaData);
	}

    public static Document export(Node node, boolean contentOnly) throws Exception
    {
    	ResourceNode resourceNode = (ResourceNode) node;
    	resourceNode.getOwnerResourceDocument().setContentOnly(contentOnly);
        TransformerFactory tFactory = TransformerFactory.newInstance();
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();      
        Document indentityTransforDocument = documentBuilder.parse(ClassLoader.getSystemResource("defaults/identity_transform.xsl").openStream());
        Transformer transformer = tFactory.newTransformer(new DOMSource(indentityTransforDocument));
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        //transformer.setOutputProperty(SaxonOutputKeys.INDENT_SPACES,"4");
        DOMResult domResult = new DOMResult();
        transformer.transform(new DOMSource(node), domResult);
        resourceNode.getOwnerResourceDocument().setContentOnly(false);        
        return (Document) domResult.getNode();
    }

   

}
