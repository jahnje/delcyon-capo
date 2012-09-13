package com.delcyon.capo.xml.dom;

import org.w3c.dom.Document;

import com.delcyon.capo.controller.elements.ResourceControlElement;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;

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
    
    
    public Document buildDocument(ResourceControlElement resourceControlElement) throws Exception
    {
        
        ResourceDocument resourceDocument = new ResourceDocument(resourceControlElement);
        return resourceDocument;
    }
}
