package com.delcyon.capo.xml.dom;


import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.types.FileResourceType;
import com.delcyon.capo.server.CapoServer;
import com.delcyon.capo.xml.XPath;

public class ResourceDocumentTest
{

    static CapoServer capoServer = null;
    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        
        Thread thread = new Thread(){
            @Override
            public void run()
            {
                
                try
                {
                    capoServer = new CapoServer();
                    capoServer.main(new String[]{});
                }
                catch (Exception e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
               
            }
        };
        thread.start();
        while(CapoApplication.getApplication() == null || CapoApplication.getApplication().isReady() == false)
        {            
            Thread.sleep(1000);
        }
        
    }

    @Before
    public void setUp() throws Exception
    {
    }

    @Test
    public void simpleTest() throws Exception
    {
        FileResourceType fileResourceType = new FileResourceType();
        ResourceDescriptor resourceDescriptor = fileResourceType.getResourceDescriptor("capo");
        
        ResourceDocumentBuilder documentBuilder = new ResourceDocumentBuilder();
        Document document = documentBuilder.buildDocument(resourceDescriptor);
       //XPath.dumpNode(document, System.out);
        NodeList nodeList = XPath.selectNSNodes(document, "/file:capo/*:server/file:clients/*[matches(local-name(),'cli')]/file:identity.xml/server:identity/*","file=http://capo.delcyon.com/resource","server=http://www.delcyon.com/capo-server");///file:capo/file:server/file:clients/*/*/server:id[@name = 'hostname']/@value");
        for(int index = 0; index < nodeList.getLength(); index++)
        {
            System.out.println(nodeList.item(index));
        }
        nodeList = XPath.selectNSNodes(document, "/file:capo/*:server/file:clients/*[matches(local-name(),'cli')]/file:identity.xml/server:identity/*","file=http://capo.delcyon.com/resource","server=http://www.delcyon.com/capo-server");///file:capo/file:server/file:clients/*/*/server:id[@name = 'hostname']/@value");
        for(int index = 0; index < nodeList.getLength(); index++)
        {
            System.out.println(nodeList.item(index));
        }
    }
    
}
