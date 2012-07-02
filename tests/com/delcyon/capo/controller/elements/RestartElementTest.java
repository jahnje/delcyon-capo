package com.delcyon.capo.controller.elements;

import java.util.Properties;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tanukisoftware.wrapper.WrapperManager;
import org.tanukisoftware.wrapper.jmx.WrapperManagerTesting;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.Group;
import com.delcyon.capo.controller.client.ServerControllerResponse;
import com.delcyon.capo.server.CapoServer;
import com.delcyon.capo.tests.util.TestServer;
import com.delcyon.capo.tests.util.external.Util;

public class RestartElementTest
{

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {        
        com.delcyon.capo.tests.util.TestServer.start();        
    }

    @Before
    public void setUp() throws Exception
    {
    }

    @Test
    public void testProcessServerSideElement() throws Exception
    {
    	
        RestartElement restartControlElement = new RestartElement();
        
        Document document = CapoApplication.getDocumentBuilder().newDocument();
        Element restartElement = document.createElementNS(CapoApplication.SERVER_NAMESPACE_URI,"server:restart");
        
        Group group = new Group("test", null, null, null);
//TODO figure out how to run restart w/o killing all tests        restartControlElement.init(restartElement, null, group, new ServerControllerResponse());
//        restartControlElement.processServerSideElement();
        
        
       
    }

    
    
    
    @AfterClass
    public static void afterClass() throws Exception
    {        
        TestServer.shutdown();
        Util.deleteTree("capo");        
    }
}
