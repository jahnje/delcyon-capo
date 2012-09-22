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
package com.delcyon.capo.controller.elements;


import java.io.FileInputStream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.LocalRequestProcessor;
import com.delcyon.capo.tests.util.TestServer;
import com.delcyon.capo.tests.util.Util;
import com.delcyon.capo.xml.XMLDiff;
import com.delcyon.capo.xml.XPath;

public class ResourceElementTest
{
   
    @After
    public void teardown() throws Exception
    {        
        TestServer.shutdown();       
    }
    
    

    @Test
    public void simpleTest() throws Exception
    {
    	
    	Util.copyTree("test-data/capo", "capo", true, true);
    	Util.copyTree("test-data/testdb", "testdb", true, true);
        TestServer.start();
        Document document = CapoApplication.getDocumentBuilder().parse(new FileInputStream("test-data/resource_element_tests/resource-element-test.xml"));
        LocalRequestProcessor localRequestProcessor = new LocalRequestProcessor();
        localRequestProcessor.process(document);
        
        Document expectedDocument = CapoApplication.getDocumentBuilder().parse(new FileInputStream("test-data/resource_element_tests/resource_element_test_output.xml"));
        Document resultDocument = CapoApplication.getDocumentBuilder().parse(new FileInputStream("capo/server/jdbcTestOutput.xml"));
        
        XMLDiff xmlDiff = new XMLDiff();
        Document xmlDiffDocument = xmlDiff.getDifferences(expectedDocument, resultDocument);
        if(XMLDiff.EQUALITY.equals(xmlDiffDocument.getDocumentElement().getAttribute(XMLDiff.XDIFF_PREFIX+":"+XMLDiff.XDIFF_ELEMENT_ATTRIBUTE_NAME)) == false)
        {
            XPath.dumpNode(xmlDiffDocument.getDocumentElement(), System.err);
        }
        Assert.assertEquals(XMLDiff.EQUALITY, xmlDiffDocument.getDocumentElement().getAttribute(XMLDiff.XDIFF_PREFIX+":"+XMLDiff.XDIFF_ELEMENT_ATTRIBUTE_NAME));
    }
    
}
