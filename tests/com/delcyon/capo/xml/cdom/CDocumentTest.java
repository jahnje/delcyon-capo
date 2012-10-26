package com.delcyon.capo.xml.cdom;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.delcyon.capo.xml.XMLDiff;
import com.delcyon.capo.xml.XPath;

public class CDocumentTest
{

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception
    {
    }

    @Before
    public void setUp() throws Exception
    {
        System.clearProperty("javax.xml.parsers.DocumentBuilderFactory");
    }

    @After
    public void tearDown() throws Exception
    {
        System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "com.delcyon.capo.xml.cdom.CDocumentBuilderFactory");
    }

    @Test
    public void testBuilderDifferences() throws Exception
    {
       
        String file = "test-data/cdom_test_data/parse1.xml";
        DocumentBuilderFactory systemDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
        systemDocumentBuilderFactory.setNamespaceAware(true);        
        Document systemDocument = systemDocumentBuilderFactory.newDocumentBuilder().parse(file);
        Assert.assertFalse(systemDocument instanceof CDocument);
        
        CDocumentBuilderFactory cDocumentBuilderFactory = new CDocumentBuilderFactory();
        cDocumentBuilderFactory.setNamespaceAware(true);
        
        CDocument cDocument = (CDocument) cDocumentBuilderFactory.newDocumentBuilder().parse(file);
        Assert.assertTrue(cDocument instanceof CDocument);
        
        XMLDiff xmlDiff = new XMLDiff();
        //xmlDiff.setAllowNamespaceMismatches(true);
        Document diffDocument = xmlDiff.getDifferences(systemDocument, cDocument);
        
        if(XMLDiff.EQUALITY.equals(diffDocument.getDocumentElement().getAttribute(XMLDiff.XDIFF_PREFIX+":"+XMLDiff.XDIFF_ELEMENT_ATTRIBUTE_NAME)) == false)
        {
            XPath.dumpNode(diffDocument.getDocumentElement(), System.err);
        }
        Assert.assertEquals(XMLDiff.EQUALITY, diffDocument.getDocumentElement().getAttribute(XMLDiff.XDIFF_PREFIX+":"+XMLDiff.XDIFF_ELEMENT_ATTRIBUTE_NAME));
        
    }
    
    @Test
    public void testBuilderDifferences2() throws Exception
    {
       
        String file = "test-data/cdom_test_data/parse2.xml";
        DocumentBuilderFactory systemDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
        systemDocumentBuilderFactory.setNamespaceAware(true);        
        Document systemDocument = systemDocumentBuilderFactory.newDocumentBuilder().parse(file);
        Assert.assertFalse(systemDocument instanceof CDocument);
        
        CDocumentBuilderFactory cDocumentBuilderFactory = new CDocumentBuilderFactory();
        cDocumentBuilderFactory.setNamespaceAware(true);
        
        CDocument cDocument = (CDocument) cDocumentBuilderFactory.newDocumentBuilder().parse(file);
        Assert.assertTrue(cDocument instanceof CDocument);
        
        Node sNode = XPath.selectSingleNode(systemDocument, "//stdout");
        String spath = XPath.getXPath(sNode);
        sNode = XPath.selectSingleNode(systemDocument, spath);
        sNode.getNodeValue();
        
        Node cNode = XPath.selectSingleNode(cDocument, "//stdout");
        String cpath = XPath.getXPath(cNode);
        cNode = XPath.selectSingleNode(cDocument, cpath);
        cNode.getNodeValue();
        
        XMLDiff xmlDiff = new XMLDiff();
        //xmlDiff.setAllowNamespaceMismatches(true);
        Document diffDocument = xmlDiff.getDifferences(systemDocument, cDocument);
        
        if(XMLDiff.EQUALITY.equals(diffDocument.getDocumentElement().getAttribute(XMLDiff.XDIFF_PREFIX+":"+XMLDiff.XDIFF_ELEMENT_ATTRIBUTE_NAME)) == false)
        {
            XPath.dumpNode(diffDocument.getDocumentElement(), System.err);
        }
        Assert.assertEquals(XMLDiff.EQUALITY, diffDocument.getDocumentElement().getAttribute(XMLDiff.XDIFF_PREFIX+":"+XMLDiff.XDIFF_ELEMENT_ATTRIBUTE_NAME));
        
    }

    
    
    
}
