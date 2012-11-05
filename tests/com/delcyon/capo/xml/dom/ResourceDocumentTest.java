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
package com.delcyon.capo.xml.dom;


import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.LifeCycle;
import com.delcyon.capo.resourcemanager.types.FileResourceType;
import com.delcyon.capo.tests.util.TestServer;
import com.delcyon.capo.tests.util.Util;
import com.delcyon.capo.xml.XPath;

public class ResourceDocumentTest
{
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {        
        Util.startMinimalCapoApplication();        
    }

    @AfterClass
    public static void shutdownAfterClass() throws Exception
    {        
        TestServer.shutdown();       
    }
    
    @Before
    public void setUp() throws Exception
    {
    }

    @Test
    public void simpleTest() throws Exception
    {
    	Util.copyTree("test-data/capo", "capo", true, true);
        FileResourceType fileResourceType = new FileResourceType();
        ResourceDescriptor resourceDescriptor = fileResourceType.getResourceDescriptor("server");
        
        ResourceDocumentBuilder documentBuilder = new ResourceDocumentBuilder();
        ResourceDocument document = (ResourceDocument) documentBuilder.buildDocument(resourceDescriptor);
        document.close(LifeCycle.EXPLICIT);        
       XPath.dumpNode(document, System.out);
       String xpath ="/file:server/file:clients/*[matches(local-name(),'cli')]/file:identity.xml/server:identity/*"; 
        NodeList nodeList = XPath.selectNSNodes(document, xpath,"file=http://www.delcyon.com/capo/resource/file","server=http://www.delcyon.com/capo-server");///file:capo/file:server/file:clients/*/*/server:id[@name = 'hostname']/@value");
        System.out.println("=================================================");
        for(int index = 0; index < nodeList.getLength(); index++)
        {
            System.out.println(nodeList.item(index));
        }
        System.out.println("=================================================");
        nodeList = XPath.selectNSNodes(document, "/*:server/file:clients/*[matches(local-name(),'cli')]/file:identity.xml/server:identity/*","file=http://www.delcyon.com/capo/resource/file","server=http://www.delcyon.com/capo-server");///file:capo/file:server/file:clients/*/*/server:id[@name = 'hostname']/@value");
        for(int index = 0; index < nodeList.getLength(); index++)
        {
        	Element idElement = (Element) nodeList.item(index); 
            System.out.println(idElement.getAttribute("name")+" = " +idElement.getAttribute("value"));
        }
        System.out.println("=================================================");
    }
    
}
