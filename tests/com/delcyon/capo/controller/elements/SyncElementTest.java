package com.delcyon.capo.controller.elements;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.Group;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.types.ContentMetaData;
import com.delcyon.capo.resourcemanager.types.FileResourceType;
import com.delcyon.capo.tests.util.external.Util;
import com.delcyon.capo.xml.XMLDiff;
import com.delcyon.capo.xml.XPath;
import com.delcyon.capo.xml.dom.ResourceDocument;

public class SyncElementTest
{

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        Util.deleteTree("capo");
        com.delcyon.capo.tests.util.Util.startMinimalCapoApplication();
    }

    @Before
    public void setUp() throws Exception
    {
    }

    @Test
    public void testProcessServerSideElement() throws Exception
    {
        String src = "test-data/capo";
        String dest = "capo";
        SyncElement syncControlElement = new SyncElement();
        Document document = CapoApplication.getDocumentBuilder().newDocument();
        Element syncElement = document.createElement("sync");
        syncElement.setAttribute(SyncElement.Attributes.src.toString(), src);
        syncElement.setAttribute(SyncElement.Attributes.dest.toString(), dest);
        syncElement.setAttribute(SyncElement.Attributes.recursive.toString(), "true");
        syncElement.setAttribute(SyncElement.Attributes.syncAttributes.toString(), "lastModified");
        Group group = new Group("test", null, null, null);
        syncControlElement.init(syncElement, null, group, null);
        syncControlElement.processServerSideElement();
        
        
        ResourceDescriptor sourceResourceDescriptor = new FileResourceType().getResourceDescriptor(src);
        ResourceDescriptor destinationResourceDescriptor = new FileResourceType().getResourceDescriptor(dest);
        
        //use resource document to get results from both sides
        ResourceDocument baseDocument = new ResourceDocument(sourceResourceDescriptor);
       // XPath.dumpNode(baseDocument, System.out);
        ResourceDocument modDocument = new ResourceDocument(destinationResourceDescriptor);
        
        
        //use xml diff to generate diff between both side
        XMLDiff xmlDiff = new XMLDiff();
        xmlDiff.addIgnoreableAttribute(CapoApplication.RESOURCE_NAMESPACE_URI,ContentMetaData.Attributes.path.toString());
        xmlDiff.addIgnoreableAttribute(CapoApplication.RESOURCE_NAMESPACE_URI,ContentMetaData.Attributes.uri.toString());
        Document diffDocument = xmlDiff.getDifferences(baseDocument, modDocument);
        
        //verify that root element of xml diff contains mod = base
        
        if (diffDocument.getDocumentElement().getAttributeNS(XMLDiff.XDIFF_NAMESPACE_URI, XMLDiff.XDIFF_ELEMENT_ATTRIBUTE_NAME).equals(XMLDiff.EQUALITY) == false)
        {
        	XPath.dumpNode(diffDocument, System.out);
        }
        Assert.assertEquals("There is a difference between "+src+" and "+dest,XMLDiff.EQUALITY,diffDocument.getDocumentElement().getAttributeNS(XMLDiff.XDIFF_NAMESPACE_URI, XMLDiff.XDIFF_ELEMENT_ATTRIBUTE_NAME));
        
    }

    @AfterClass
    public static void afterClass() throws Exception
    {
        Util.deleteTree("capo");
       
    }
}
