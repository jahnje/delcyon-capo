package com.delcyon.capo.xml.dom;


import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;

import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.types.FileResourceType;
import com.delcyon.capo.xml.XPath;

public class ResourceDocumentTest
{

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
    }

    @Before
    public void setUp() throws Exception
    {
    }

    @Test
    public void simpleTest() throws Exception
    {
        FileResourceType fileResourceType = new FileResourceType();
        ResourceDescriptor resourceDescriptor = fileResourceType.getResourceDescriptor("LICENSE.txt");
        
        ResourceDocumentBuilder documentBuilder = new ResourceDocumentBuilder();
        Document document = documentBuilder.buildDocument(resourceDescriptor);
        XPath.dumpNode(document, System.out);
    }
    
}
