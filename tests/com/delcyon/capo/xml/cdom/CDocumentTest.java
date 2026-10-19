package com.delcyon.capo.xml.cdom;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
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

    /**
     * FB24556: cloning a document that declares a namespace has to work without an --add-opens for java.util
     */
    @Test
    public void testCloneNamespacedDocument() throws Exception
    {
        CDocumentBuilderFactory cDocumentBuilderFactory = new CDocumentBuilderFactory();
        cDocumentBuilderFactory.setNamespaceAware(true);
        CDocument cDocument = (CDocument) cDocumentBuilderFactory.newDocumentBuilder().parse(new File("test-data/cdom_test_data/parse1.xml"));
        Assert.assertEquals("fixture must declare the server namespace", "http://www.delcyon.com/capo-server", cDocument.getNamespaceContextMap().getNamespaceURI("server"));

        CDocument clonedDocument = (CDocument) cDocument.cloneNode(true);
        Assert.assertNotNull("namespaced document didn't clone", clonedDocument);
        Assert.assertNotSame(cDocument.getDocumentElement(), clonedDocument.getDocumentElement());
        Assert.assertEquals(cDocument.getNamespaceContextMap().toString(), clonedDocument.getNamespaceContextMap().toString());
        Assert.assertEquals("server", clonedDocument.getNamespaceContextMap().getPrefix("http://www.delcyon.com/capo-server"));

        Document diffDocument = new XMLDiff().getDifferences(cDocument, clonedDocument);
        Assert.assertEquals(XMLDiff.EQUALITY, diffDocument.getDocumentElement().getAttribute(XMLDiff.XDIFF_PREFIX+":"+XMLDiff.XDIFF_ELEMENT_ATTRIBUTE_NAME));
    }

    /**
     * FB24546: text and attribute nodes, parsed or cloned, never get the child list, attribute list or listener vector they can't use
     */
    @Test
    public void testLeafNodesHaveNoListsAfterParseAndClone() throws Exception
    {
        CDocumentBuilderFactory cDocumentBuilderFactory = new CDocumentBuilderFactory();
        cDocumentBuilderFactory.setNamespaceAware(true);
        CDocument cDocument = (CDocument) cDocumentBuilderFactory.newDocumentBuilder().parse(new File("test-data/cdom_test_data/parse1.xml"));
        assertLeafNodesHaveNoLists(cDocument);

        CDocument clonedDocument = (CDocument) cDocument.cloneNode(true);
        assertLeafNodesHaveNoLists(clonedDocument);
        Assert.assertEquals(cDocument.stream().count(), clonedDocument.stream().count());
        Assert.assertEquals(XPath.selectNodes(cDocument, "//*").getLength(), XPath.selectNodes(clonedDocument, "//*").getLength());
        Assert.assertEquals(XPath.selectNodes(cDocument, "//@*").getLength(), XPath.selectNodes(clonedDocument, "//@*").getLength());
        Assert.assertEquals(cDocument.getDocumentElement().getTextContent(), clonedDocument.getDocumentElement().getTextContent());
        //strings are shared rather than copied
        Assert.assertSame(cDocument.getDocumentElement().getNodeName(), clonedDocument.getDocumentElement().getNodeName());
    }

    private void assertLeafNodesHaveNoLists(CDocument cDocument)
    {
        long leafCount = cDocument.stream().filter(node->node instanceof CText || node instanceof CAttr).peek(node->{
            Assert.assertNull("child list on "+node, node.nodeList);
            Assert.assertNull("attribute list on "+node, node.attributeList);
        }).count();
        Assert.assertTrue("fixture should have text and attribute nodes", leafCount > 0);
        Assert.assertTrue("no node should have a listener vector", cDocument.stream().allMatch(node->node.cdomEventListenerVector == null));
    }

    /**
     * FB24546: child and attribute lists created on demand are still the node's own live lists, and clones don't share them
     */
    @Test
    public void testLazyListsAreLiveAndNotShared() throws Exception
    {
        CDocument cDocument = new CDocument();
        CElement rootElement = (CElement) cDocument.appendChild(new CElement("root"));
        CElement childElement = new CElement("child");

        NodeList childNodes = rootElement.getChildNodes();
        Assert.assertEquals(0, childNodes.getLength());
        Assert.assertFalse(rootElement.hasChildNodes());
        Assert.assertNull(rootElement.getFirstChild());
        rootElement.appendChild(childElement);
        Assert.assertEquals("child list must be live", 1, childNodes.getLength());
        Assert.assertSame(childElement, rootElement.getFirstChild());

        NamedNodeMap attributes = childElement.getAttributes();
        Assert.assertEquals(0, attributes.getLength());
        childElement.setAttribute("name", "value");
        Assert.assertEquals("attribute list must be live", 1, attributes.getLength());

        CElement clonedChild = (CElement) childElement.cloneNode(true);
        clonedChild.setAttribute("other", "value");
        clonedChild.appendChild(new CElement("grandchild"));
        Assert.assertEquals(1, childElement.getAttributes().getLength());
        Assert.assertFalse(childElement.hasChildNodes());
        Assert.assertNotSame(childElement.getChildNodes(), new CElement("empty").getChildNodes());

        rootElement.removeChild(childElement);
        Assert.assertEquals(0, childNodes.getLength());
        new CText().removeChildrenAll();
        new CElement("noAttributes").removeAttributes();
    }

    /**
     * FB24546: listeners still get events once the listener vector is created on demand
     */
    @Test
    public void testEventListeners() throws Exception
    {
        CDocument cDocument = new CDocument();
        CElement rootElement = (CElement) cDocument.appendChild(new CElement("root"));
        Assert.assertFalse(rootElement.hasEventListeners());

        List<CDOMEvent> events = new ArrayList<CDOMEvent>();
        CDOMEventListener listener = events::add;
        rootElement.addCDOMEventListener(listener);
        Assert.assertTrue(rootElement.hasEventListeners());
        rootElement.appendChild(new CElement("child"));
        Assert.assertEquals(1, events.size());
        Assert.assertEquals(CDOMEvent.EventType.INSERT, events.get(0).getEventType());

        rootElement.removeCDOMEventListener(listener);
        Assert.assertFalse(rootElement.hasEventListeners());
        rootElement.appendChild(new CElement("child2"));
        Assert.assertEquals(1, events.size());
        new CElement("noListeners").removeCDOMEventListener(listener);
    }

}
