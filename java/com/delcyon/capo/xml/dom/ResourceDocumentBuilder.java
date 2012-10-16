package com.delcyon.capo.xml.dom;

import org.w3c.dom.Document;

import com.delcyon.capo.controller.elements.ResourceControlElement;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceParameterBuilder;
import com.delcyon.capo.xml.XPath;

public class ResourceDocumentBuilder
{
    public ResourceDocumentBuilder()
    {
       
    }
    
    public Document buildDocument(ResourceDescriptor resourceDescriptor) throws Exception
    {
        
        ResourceDocument resourceDocument = new ResourceDocument(resourceDescriptor);
        return resourceDocument;
    }
    
    
    public ResourceDocument buildDocument(ResourceControlElement resourceControlElement) throws Exception
    {
        
        
        
        ResourceDeclarationElement resourceDeclarationElement = new ResourceDeclarationElement(resourceControlElement);
        ResourceElement rootResourceElement = resourceDeclarationElement.readXML(resourceControlElement.getParentGroup(), ResourceParameterBuilder.getResourceParameters(resourceControlElement.getControlElementDeclaration()));
        
        //TODO this doesn't work at all
        ResourceDocument resourceDocument = rootResourceElement.getOwnerResourceDocument();
        resourceDocument.setContentOnly(true);
        XPath.dumpNode(resourceDocument, System.out);
        //resourceDocument.setContentOnly(false);
        //XPath.dumpNode(resourceDocument, System.out);
        return resourceDocument;
    }
    
    public ResourceDocument createDocument()
    {
    	return  new ResourceDocument();
    	
    }
    
}
