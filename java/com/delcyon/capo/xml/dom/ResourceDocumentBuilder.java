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
        ResourceElement rootResourceElement = resourceDeclarationElement.buildXML(resourceControlElement.getParentGroup(), ResourceParameterBuilder.getResourceParameters(resourceControlElement.getControlElementDeclaration()));
        return rootResourceElement.getOwnerResourceDocument();

    }
    
    public ResourceDocument createDocument()
    {
    	return  new ResourceDocument();
    	
    }
    
}
