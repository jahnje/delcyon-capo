package com.delcyon.capo.controller.elements;

import java.io.File;

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
import com.delcyon.capo.resourcemanager.ResourceParameter;
import com.delcyon.capo.resourcemanager.types.ContentMetaData;
import com.delcyon.capo.resourcemanager.types.FileResourceType;
import com.delcyon.capo.tests.util.TestCapoApplication;
import com.delcyon.capo.tests.util.external.Util;
import com.delcyon.capo.xml.XMLDiff;
import com.delcyon.capo.xml.XPath;
import com.delcyon.capo.xml.dom.ResourceDocument;

public class SyncElementTest
{

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        
        com.delcyon.capo.tests.util.Util.startMinimalCapoApplication();
    }

    @Before
    public void setUp() throws Exception
    {
    }

    @Test
    public void testProcessServerSideElement() throws Exception
    {
    	Util.deleteTree("capo");
        String src = "test-data/capo";
        String dest = "capo";
        SyncElement syncControlElement = new SyncElement();
        Document document = CapoApplication.getDocumentBuilder().newDocument();
        Element syncElement = document.createElement("sync");
        syncElement.setAttribute(SyncElement.Attributes.src.toString(), src);
        syncElement.setAttribute(SyncElement.Attributes.dest.toString(), dest);
        syncElement.setAttribute(SyncElement.Attributes.recursive.toString(), "true");
        syncElement.setAttribute(SyncElement.Attributes.syncAttributes.toString(), "lastModified");
        Element resourceParameterElement = document.createElementNS(CapoApplication.RESOURCE_NAMESPACE_URI, "resouce:parameter");
        resourceParameterElement.setAttribute("name", FileResourceType.Parameters.ROOT_DIR.toString());
        resourceParameterElement.setAttribute("value", new File(".").getCanonicalPath());
        syncElement.appendChild(resourceParameterElement);
        Group group = new Group("test", null, null, null);
        syncControlElement.init(syncElement, null, group, null);
        syncControlElement.processServerSideElement();
        
        
        ResourceDescriptor sourceResourceDescriptor = new FileResourceType().getResourceDescriptor(src);
        ResourceDescriptor destinationResourceDescriptor = new FileResourceType().getResourceDescriptor(dest);
        
        //use resource document to get results from both sides
        ResourceDocument baseDocument = new ResourceDocument(sourceResourceDescriptor);
        XPath.dumpNode(baseDocument, System.out);
        Assert.assertNotNull(XPath.selectSingleNodeValue(baseDocument.getDocumentElement(), "//resource:keystore"));
        ResourceDocument modDocument = new ResourceDocument(destinationResourceDescriptor);
        Assert.assertNotNull(XPath.selectSingleNodeValue(modDocument.getDocumentElement(), "//resource:keystore"));
        //XPath.dumpNode(modDocument, System.err);
        
        //use xml diff to generate diff between both side
        XMLDiff xmlDiff = new XMLDiff();
        xmlDiff.addIgnoreableAttribute(null,ContentMetaData.Attributes.path.toString());
        xmlDiff.addIgnoreableAttribute(null,ContentMetaData.Attributes.uri.toString());
        Document diffDocument = xmlDiff.getDifferences(baseDocument, modDocument);
        
        //verify that root element of xml diff contains mod = base
        
        if (diffDocument.getDocumentElement().getAttributeNS(XMLDiff.XDIFF_NAMESPACE_URI, XMLDiff.XDIFF_ELEMENT_ATTRIBUTE_NAME).equals(XMLDiff.EQUALITY) == false)
        {
        	XPath.dumpNode(diffDocument, System.out);
        }
        Assert.assertEquals("There is a difference between "+src+" and "+dest,XMLDiff.EQUALITY,diffDocument.getDocumentElement().getAttributeNS(XMLDiff.XDIFF_NAMESPACE_URI, XMLDiff.XDIFF_ELEMENT_ATTRIBUTE_NAME));
        
    }

    @Test
    public void testPrune() throws Exception
    {
    	Util.deleteTree("capo");
    	 String src = "test-data/capo";
         String dest = "capo";
         Util.copyTree(src, dest+"/"+dest);
         //create sync element
         SyncElement syncControlElement = new SyncElement();
         Document document = CapoApplication.getDocumentBuilder().newDocument();
         Element syncElement = document.createElement("sync");
         syncElement.setAttribute(SyncElement.Attributes.src.toString(), src);
         syncElement.setAttribute(SyncElement.Attributes.dest.toString(), dest);
         syncElement.setAttribute(SyncElement.Attributes.recursive.toString(), "true");
         syncElement.setAttribute(SyncElement.Attributes.syncAttributes.toString(), "lastModified");
         Element resourceParameterElement = document.createElementNS(CapoApplication.RESOURCE_NAMESPACE_URI, "resouce:parameter");
         resourceParameterElement.setAttribute("name", FileResourceType.Parameters.ROOT_DIR.toString());
         resourceParameterElement.setAttribute("value", new File(".").getCanonicalPath());
         syncElement.appendChild(resourceParameterElement);
         Group group = new Group("test", null, null, null);
         syncControlElement.init(syncElement, null, group, null);
         syncControlElement.processServerSideElement();
         
         
         ResourceDescriptor sourceResourceDescriptor = new FileResourceType().getResourceDescriptor(src);
         sourceResourceDescriptor.addResourceParameters(null, new ResourceParameter(FileResourceType.Parameters.ROOT_DIR,new File(".").getCanonicalPath()));
         ResourceDescriptor destinationResourceDescriptor = new FileResourceType().getResourceDescriptor(dest);
         //destinationResourceDescriptor.addResourceParameters(null, new ResourceParameter(FileResourceType.Parameters.ROOT_DIR,new File(".").getCanonicalPath()));
         
         //use resource document to get results from both sides
         ResourceDocument baseDocument = new ResourceDocument(sourceResourceDescriptor);
         //XPath.dumpNode(baseDocument, System.out);
         ResourceDocument modDocument = new ResourceDocument(destinationResourceDescriptor);
         //XPath.dumpNode(modDocument, System.out);
         
         //use xml diff to generate diff between both side
         XMLDiff xmlDiff = new XMLDiff();
         xmlDiff.addIgnoreableAttribute(CapoApplication.RESOURCE_NAMESPACE_URI,ContentMetaData.Attributes.path.toString());
         xmlDiff.addIgnoreableAttribute(CapoApplication.RESOURCE_NAMESPACE_URI,ContentMetaData.Attributes.uri.toString());
         Document diffDocument = xmlDiff.getDifferences(baseDocument, modDocument);
         
         //verify that root element of xml diff contains mod != base
         //make sure things are differrent before we prune 
         if (diffDocument.getDocumentElement().getAttributeNS(XMLDiff.XDIFF_NAMESPACE_URI, XMLDiff.XDIFF_ELEMENT_ATTRIBUTE_NAME).equals(XMLDiff.INEQUALITY) == false)
         {
         	XPath.dumpNode(diffDocument, System.out);
         }
         Assert.assertEquals("There is no difference between "+src+" and "+dest,XMLDiff.INEQUALITY,diffDocument.getDocumentElement().getAttributeNS(XMLDiff.XDIFF_NAMESPACE_URI, XMLDiff.XDIFF_ELEMENT_ATTRIBUTE_NAME));
     
         syncElement.setAttribute(SyncElement.Attributes.prune.toString(), "true");
         
         syncControlElement.processServerSideElement();
         
         
          sourceResourceDescriptor = new FileResourceType().getResourceDescriptor(src);
          destinationResourceDescriptor = new FileResourceType().getResourceDescriptor(dest);
         
         //use resource document to get results from both sides
          baseDocument = new ResourceDocument(sourceResourceDescriptor);
          Assert.assertTrue(baseDocument.getDocumentElement().getAttribute("exists").equals("true"));
         //XPath.dumpNode(baseDocument, System.out);
          modDocument = new ResourceDocument(destinationResourceDescriptor);
          Assert.assertTrue(modDocument.getDocumentElement().getAttribute("exists").equals("true"));
         //XPath.dumpNode(modDocument, System.out);
         
         //use xml diff to generate diff between both side
          xmlDiff = new XMLDiff();
         xmlDiff.addIgnoreableAttribute(null,ContentMetaData.Attributes.path.toString());
         xmlDiff.addIgnoreableAttribute(null,ContentMetaData.Attributes.uri.toString());
          diffDocument = xmlDiff.getDifferences(baseDocument, modDocument);
         
         //verify that root element of xml diff contains mod != base
         //make sure things are differrent before we prune 
         if (diffDocument.getDocumentElement().getAttributeNS(XMLDiff.XDIFF_NAMESPACE_URI, XMLDiff.XDIFF_ELEMENT_ATTRIBUTE_NAME).equals(XMLDiff.EQUALITY) == false)
         {
         	XPath.dumpNode(diffDocument, System.out);
         }
         Assert.assertEquals("There is a difference between "+src+" and "+dest,XMLDiff.EQUALITY,diffDocument.getDocumentElement().getAttributeNS(XMLDiff.XDIFF_NAMESPACE_URI, XMLDiff.XDIFF_ELEMENT_ATTRIBUTE_NAME));
     
         
    }
    
    
    @AfterClass
    public static void afterClass() throws Exception
    {
//        Util.deleteTree("capo");        
//        TestCapoApplication.cleanup();
    }
}
