/**
Copyright (c) 2012 Delcyon, Inc.
This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.delcyon.capo.xml.xsd;


import java.util.Vector;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Node;

import com.delcyon.capo.tests.util.TestServer;
import com.delcyon.capo.tests.util.Util;
import com.delcyon.capo.xml.XPath;
import com.delcyon.capo.xml.cdom.CDocument;
import com.delcyon.capo.xml.cdom.CDocumentBuilder;
import com.delcyon.capo.xml.cdom.CNode;
import com.delcyon.capo.xml.cdom.CNodeDefinition.NodeDefinitionType;
import com.delcyon.capo.xml.cdom.CNodeValidator;
import com.delcyon.capo.xml.cdom.CValidationException;

public class SchemaDocumentTest
{
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {        
        //Util.startMinimalCapoApplication();        
    }

    @AfterClass
    public static void shutdownAfterClass() throws Exception
    {        
        //TestServer.shutdown();       
    }
    
    @Before
    public void setUp() throws Exception
    {
    }

    @Test
    public void simpleTest() throws Exception
    {
        try
        {
    	CDocumentBuilder documentBuilder = new CDocumentBuilder();
    	System.out.println(getClass().getCanonicalName().replace(".", "/")+".xsd");
    	CDocument testSchemaDocument = (CDocument) documentBuilder.parse(getClass().getClassLoader().getResource(getClass().getCanonicalName().replace(".", "/")+".xsd").openStream());
    	Assert.assertNotNull(testSchemaDocument);
    	
    	CDocument testXMLDocument = (CDocument) documentBuilder.parse(getClass().getClassLoader().getResource(getClass().getCanonicalName().replace(".", "/")+".xml").openStream());
        Assert.assertNotNull(testXMLDocument);
    	
        System.out.println(testSchemaDocument.getDefaultNamespace());
        testXMLDocument.setSchemaForNamespace(testSchemaDocument.getDefaultNamespace(),testSchemaDocument);
        CNode.walkTree(null, testXMLDocument.getDocumentElement(), this::validateNode, true);
        Vector<CValidationException> validationExceptions = new Vector<CValidationException>();
        //testXMLDocument.isNodeValid((CNode) testXMLDocument.getDocumentElement(), true, true, validationExceptions);
        //validationExceptions.forEach((ex)->System.err.println(ex.getMessage()));
        //updateDocument.isNodeValid((CNode) updateDocument.getDocumentElement().getAttributes().item(0), false, true, null);
        } catch (Exception exception)
        {
            exception.printStackTrace();
            throw exception;
        }
    }
    
    private void validateNode(Node parentNode, Node node) throws Exception
    {
        if(node.getNodeType() == Node.ELEMENT_NODE && ((CNode)node).getNodeDefinition().getNodeDefinitionType() == NodeDefinitionType.complexType)
        {
            Vector<CValidationException> validationExceptions = new Vector<CValidationException>();
            System.out.println("\n\n"+XPath.getXPath(node));
            CNodeValidator nodeValidator = new CNodeValidator((CNode) node, ((CNode)node).getNodeDefinition());
            System.out.println(nodeValidator.getNodeValidationResult());
//            if(nodeValidator.isValid() == false)
//            {
//                
//            }
            //nodeValidator.isValid();
            
        }
    }
    
}
