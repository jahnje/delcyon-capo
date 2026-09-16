package com.delcyon.capo.xml.cdom;

import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
        Document systemDocument = systemDocumentBuilderFactory.newDocumentBuilder().parse(new File(file));
        Assert.assertFalse(systemDocument instanceof CDocument);
        
        CDocumentBuilderFactory cDocumentBuilderFactory = new CDocumentBuilderFactory();
        cDocumentBuilderFactory.setNamespaceAware(true);
        
        CDocument cDocument = (CDocument) cDocumentBuilderFactory.newDocumentBuilder().parse(new File(file));
        Assert.assertTrue(cDocument instanceof CDocument);
        XPath.dumpNode(cDocument, System.err);
        
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
        Document systemDocument = systemDocumentBuilderFactory.newDocumentBuilder().parse(new File(file));
        Assert.assertFalse(systemDocument instanceof CDocument);
        
        CDocumentBuilderFactory cDocumentBuilderFactory = new CDocumentBuilderFactory();
        cDocumentBuilderFactory.setNamespaceAware(true);
        
        CDocument cDocument = (CDocument) cDocumentBuilderFactory.newDocumentBuilder().parse(new File(file));
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

    /**
     * FB23628 regression: CDocument.getElementsByTagName(String) used to
     * unconditionally throw UnsupportedOperationException -- only the
     * NS-qualified Document method (and both Element methods) were actually
     * implemented -- which broke any caller that queried a parsed CDocument
     * (e.g. the security-transform output in gal-reflector's case viewer)
     * the same way code already does against a standard DOM Document.
     */
    @Test
    public void getElementsByTagNameOnTheDocumentMatchesTheStandardDOM() throws Exception
    {
        String file = "test-data/cdom_test_data/parse1.xml";

        DocumentBuilderFactory systemDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
        systemDocumentBuilderFactory.setNamespaceAware(true);
        Document systemDocument = systemDocumentBuilderFactory.newDocumentBuilder().parse(new File(file));
        NodeList systemVars = systemDocument.getElementsByTagName("var");
        Assert.assertTrue("fixture must contain at least one <var>", systemVars.getLength() > 0);

        CDocumentBuilderFactory cDocumentBuilderFactory = new CDocumentBuilderFactory();
        cDocumentBuilderFactory.setNamespaceAware(true);
        CDocument cDocument = (CDocument) cDocumentBuilderFactory.newDocumentBuilder().parse(new File(file));

        NodeList cVars = cDocument.getElementsByTagName("var");

        Assert.assertEquals(systemVars.getLength(), cVars.getLength());
    }

    @Test
    public void simpleTest() throws Exception
    {
        CDocumentBuilderFactory cDocumentBuilderFactory = new CDocumentBuilderFactory();
        cDocumentBuilderFactory.setNamespaceAware(true);        
        CDocument cDocument = (CDocument) cDocumentBuilderFactory.newDocumentBuilder().parse(new File("test-data/cdom_test_data/simple.xml"));
        Assert.assertTrue(cDocument instanceof CDocument);
    }
    
    
}
