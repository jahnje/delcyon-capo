package com.delcyon.capo.controller.elements;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.Group;
import com.delcyon.capo.controller.client.ServerControllerResponse;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.types.FileResourceType;
import com.delcyon.capo.tasks.TaskManagerThread;
import com.delcyon.capo.tasks.TaskManagerThread.Preferences;
import com.delcyon.capo.tests.util.ExternalTestClient;
import com.delcyon.capo.tests.util.ExternalTestServer;
import com.delcyon.capo.tests.util.TestCapoApplication;
import com.delcyon.capo.tests.util.TestServer;
import com.delcyon.capo.tests.util.external.Util;
import com.delcyon.capo.xml.XPath;

public class TaskElementTest
{

    private ExternalTestClient externalTestClient;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception
    {
        TestCapoApplication.cleanup();
    }

    

    @Before
    public void setUp() throws Exception
    {
        Util.deleteTree("capo");
        Util.copyTree("test-data/capo", "capo");
        TestServer.start();
        externalTestClient = new ExternalTestClient();
        externalTestClient.startClient();        
    }

    @After
    public void tearDown() throws Exception
    {
        externalTestClient.shutdown();
        TestServer.shutdown();
        CopyOnWriteArrayList<Exception> exceptionList = externalTestClient.getExceptionList();
        if (exceptionList.isEmpty() == false)
        {
            throw exceptionList.get(0);
        }
        
        exceptionList = TestServer.getExceptionList();
        if (exceptionList.isEmpty() == false)
        {
            throw exceptionList.get(0);
        }
        TestServer.cleanup();
    }

    @Test
    public void testProcessServerSideElement() throws Exception
    {
        TaskElement taskElementControl = new TaskElement();
        
        Document document = CapoApplication.getDocumentBuilder().newDocument();
        Element taskElement = document.createElementNS(CapoApplication.SERVER_NAMESPACE_URI,"server:task");
        taskElement.setAttribute(TaskElement.Attributes.name.toString(), "testTask");
        taskElement.setAttribute(TaskElement.Attributes.local.toString(), "true");
        Group group = new Group("test", null, null, null);
        taskElementControl.init(taskElement, null, group, new ServerControllerResponse());
        taskElementControl.processServerSideElement();
        Thread.sleep(2000);
        long lastRunTime = TaskManagerThread.getTaskManagerThread().getLastRunTime();
        int loopCount = 0;
        while(lastRunTime == TaskManagerThread.getTaskManagerThread().getLastRunTime() || loopCount < 1)
        {
            if (lastRunTime != TaskManagerThread.getTaskManagerThread().getLastRunTime())
            {
                loopCount++;
                lastRunTime = TaskManagerThread.getTaskManagerThread().getLastRunTime();
            }
            System.out.println("waiting for TaskManager to run loop# "+loopCount);
            Thread.sleep(1000);
        }
        List<ResourceDescriptor> taskResourceDescriptorList = CapoApplication.getDataManager().findDocuments(CapoApplication.getDataManager().getResourceDirectory(Preferences.TASK_DIR.toString()));
        Assert.assertEquals(2,taskResourceDescriptorList.size());
        for (ResourceDescriptor resourceDescriptor : taskResourceDescriptorList)
        {
            Document testDocument = CapoApplication.getDocumentBuilder().parse(resourceDescriptor.getInputStream(null));
            if (resourceDescriptor.getLocalName().equals("task-status.xml"))
            {
                XPath.dumpNode(testDocument, System.out);
            }
            else if (resourceDescriptor.getLocalName().equals("testTask.xml"))
            {
                XPath.dumpNode(testDocument, System.out);
            }
            else
            {
                fail("Unknown task file:"+resourceDescriptor.getLocalName());
            }
        }
    }

    @Test
    public void testProcessClientSideElement()
    {
        fail("Not yet implemented");
    }

}
