package com.delcyon.capo.controller.elements;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import org.w3c.dom.NodeList;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.CapoApplication.ApplicationState;
import com.delcyon.capo.controller.Group;
import com.delcyon.capo.controller.client.ServerControllerResponse;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.server.CapoServer;
import com.delcyon.capo.tasks.TaskManagerThread;
import com.delcyon.capo.tasks.TaskManagerThread.Preferences;
import com.delcyon.capo.tests.util.ExternalTestClient;
import com.delcyon.capo.tests.util.ExternalTestServer;
import com.delcyon.capo.tests.util.TestClient;
import com.delcyon.capo.tests.util.TestServer;
import com.delcyon.capo.tests.util.external.Util;
import com.delcyon.capo.util.diff.Diff;
import com.delcyon.capo.xml.XPath;

public class TaskElementTest
{

    private ExternalTestClient externalTestClient;
    private ExternalTestServer externalTestServer;
    private CapoServer capoServer = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception
    {
        //TestCapoApplication.cleanup();
    }

   
    @Before
    public void setUp() throws Exception
    {
        Util.deleteTree("capo");
        Util.copyTree("test-data/capo", "capo");
//        Util.copyTree("lib", "capo/server/lib");
//        Util.copyTree("lib", "capo/client/lib");
           
    }

    @After
    public void tearDown() throws Exception
    {
        if (TaskManagerThread.getTaskManagerThread() != null && TaskManagerThread.getTaskManagerThread().getLock().isLocked() && TaskManagerThread.getTaskManagerThread().getLock().isHeldByCurrentThread())
        {
            TaskManagerThread.getTaskManagerThread().getLock().unlock();
        }
        try
        {
            TestServer.getServerInstance().getConfiguration().setValue(TaskManagerThread.Preferences.TASK_INTERVAL, TaskManagerThread.Preferences.TASK_INTERVAL.getDefaultValue());
            TestServer.getServerInstance().getConfiguration().setValue(TaskManagerThread.Preferences.TASK_DEFAULT_LIFESPAN, TaskManagerThread.Preferences.TASK_DEFAULT_LIFESPAN.getDefaultValue());
            TestServer.getServerInstance().getConfiguration().setValue(TaskManagerThread.Preferences.DEFAULT_CLIENT_SYNC_INTERVAL, TaskManagerThread.Preferences.DEFAULT_CLIENT_SYNC_INTERVAL.getDefaultValue());
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
        System.err.println("==================================================================");
        if (externalTestClient != null)
        {
            externalTestClient.shutdown();
            CopyOnWriteArrayList<Exception> exceptionList = externalTestClient.getExceptionList();
            if (exceptionList.isEmpty() == false)
            {
                throw exceptionList.get(0);
            }
        }
        
        if (TestClient.getClientInstance() != null)
        {
            TestClient.shutdown();
            CopyOnWriteArrayList<Exception> exceptionList = TestClient.getExceptionList();
            if (exceptionList.isEmpty() == false)
            {
                throw exceptionList.get(0);
            }            
        }
        
        if (externalTestServer != null)
        {
            externalTestServer.shutdown();
            CopyOnWriteArrayList<Exception> exceptionList = externalTestServer.getExceptionList();
            if (exceptionList.isEmpty() == false)
            {
                throw exceptionList.get(0);
            }
        }

       
        
        if (TestServer.getServerInstance() != null)
        {
            TestServer.shutdown();
            CopyOnWriteArrayList<Exception> exceptionList = TestServer.getExceptionList();
            if (exceptionList.isEmpty() == false)
            {
                throw exceptionList.get(0);
            }
         
        }
        
        
    }

    @Test
    public void testProcessServerSideElement() throws Exception
    {
        TestServer.start();        
        TaskManagerThread.getTaskManagerThread().getLock().lock();
        externalTestClient = new ExternalTestClient();
        externalTestClient.startClient(ApplicationState.RUNNING);    
        
        TaskElement taskElementControl = new TaskElement();
        
        Document document = CapoApplication.getDocumentBuilder().newDocument();
        Element taskElement = document.createElementNS(CapoApplication.SERVER_NAMESPACE_URI,"server:task");
        taskElement.setAttribute(TaskElement.Attributes.name.toString(), "testTask");
        taskElement.setAttribute(TaskElement.Attributes.local.toString(), "true");
        Group group = new Group("test", null, null, null);
        taskElementControl.init(taskElement, null, group, new ServerControllerResponse());
        taskElementControl.processServerSideElement();
        
        waitForTaskManagerToRun(2);
        
        System.out.println();
        List<ResourceDescriptor> taskResourceDescriptorList = CapoApplication.getDataManager().findDocuments(CapoApplication.getDataManager().getResourceDirectory(Preferences.TASK_DIR.toString()));
        Assert.assertEquals(2,taskResourceDescriptorList.size());
        for (ResourceDescriptor resourceDescriptor : taskResourceDescriptorList)
        {
            
            Document testDocument = CapoApplication.getDocumentBuilder().parse(resourceDescriptor.getInputStream(null));
            if (resourceDescriptor.getLocalName().equals("task-status.xml"))
            {
                XPath.dumpNode(testDocument, System.out);
                NodeList nodeList = XPath.selectNodes(testDocument, "//server:task");
                Assert.assertEquals(1,nodeList.getLength());
                Assert.assertEquals("testTask",((Element)nodeList.item(0)).getAttribute("name"));
                Assert.assertEquals("testTask.xml",((Element)nodeList.item(0)).getAttribute("taskURI"));
            }
            else if (resourceDescriptor.getLocalName().equals("testTask.xml"))
            {
                XPath.dumpNode(testDocument, System.out);
                NodeList nodeList = XPath.selectNodes(testDocument, "//server:task");
                Assert.assertEquals(1,nodeList.getLength());
                Assert.assertEquals("testTask",((Element)nodeList.item(0)).getAttribute("name"));
                Assert.assertTrue(((Element)nodeList.item(0)).getAttribute("lastAccessTime").matches("\\d+"));
            }
            else
            {
                fail("Unknown task file:"+resourceDescriptor.getLocalName());
            }
        }
        TestServer.getServerInstance().getConfiguration().setValue(TaskManagerThread.Preferences.TASK_INTERVAL, "2000");
        TestServer.getServerInstance().getConfiguration().setValue(TaskManagerThread.Preferences.TASK_DEFAULT_LIFESPAN, "1000");        
        
        
        waitForTaskManagerToRun(5);
        System.out.println();
        taskResourceDescriptorList = CapoApplication.getDataManager().findDocuments(CapoApplication.getDataManager().getResourceDirectory(Preferences.TASK_DIR.toString()));
        Assert.assertEquals(1,taskResourceDescriptorList.size());
        ResourceDescriptor resourceDescriptor = taskResourceDescriptorList.get(0);
        Document testDocument = CapoApplication.getDocumentBuilder().parse(resourceDescriptor.getInputStream(null));
        if (resourceDescriptor.getLocalName().equals("task-status.xml"))
        {
            XPath.dumpNode(testDocument, System.out);
            NodeList nodeList = XPath.selectNodes(testDocument, "//server:task");
            Assert.assertEquals(0,nodeList.getLength());                
        }            
        else
        {
            fail("Unknown task file:"+resourceDescriptor.getLocalName());
        }
        TaskManagerThread.getTaskManagerThread().getLock().unlock();
    }

    /**
     * Copy a default test script that includes a task to the server
     * Start an external server.
     * Start a Test client
     * Ensure tasks gets created on server
     * Ensure task gets synced to client
     * Ensure Client runs task
     * Ensure client marks task for deletion
     * Ensure task gets deleted from client
     * Ensure task gets deleted from server
     * 
     * @throws Exception
     */
    @Test
    public void testProcessClientSideScriptedTask() throws Exception
    {
	System.out.println("===================================================================");
        externalTestServer = new ExternalTestServer();
        System.out.println("===================================================================");
        externalTestServer.startServer();
        System.out.println("===================================================================");
        TestClient.start(ApplicationState.RUNNING,"-CLIENT_AS_SERVICE","true");
        System.out.println();
    }
    
    /**
     * copy a test task to the servers client task dir.
     * Start External Server
     * Start local Client 
     * Ensure that it synced over.
     * Ensure that it runs
     * Ensure that it gets deleted
     * @throws Exception
     */    
    @Test
    public void testProcessClientSideManualTask() throws Exception
    {
        Util.copyTree("test-data/test-manual-task-with-error.xml", "capo/server/clients/capo.client.1/tasks/test-manual-task.xml");
    	System.out.println("===================================================================");
    	externalTestServer = new ExternalTestServer();
    	System.out.println("===================================================================");
    	externalTestServer.startServer("-CAPO_DIR","capo/server");
    	System.out.println("===================================================================");
    	TestClient.start(ApplicationState.RUNNING,"-CLIENT_AS_SERVICE","true","-CAPO_DIR","capo/client");
    	TestServer.getServerInstance().getConfiguration().setValue(TaskManagerThread.Preferences.TASK_INTERVAL, "2000");
        TestServer.getServerInstance().getConfiguration().setValue(TaskManagerThread.Preferences.TASK_DEFAULT_LIFESPAN, "1000");
        TestServer.getServerInstance().getConfiguration().setValue(TaskManagerThread.Preferences.DEFAULT_CLIENT_SYNC_INTERVAL, "1000");
        
    	TaskManagerThread.getTaskManagerThread().getLock().lock();
    	
    	List<ResourceDescriptor> taskResourceDescriptorList = CapoApplication.getDataManager().findDocuments(CapoApplication.getDataManager().getResourceDirectory(Preferences.TASK_DIR.toString()));
        Assert.assertEquals(2,taskResourceDescriptorList.size());
        waitForTaskManagerToRun(1);
        
        Assert.assertEquals(2,taskResourceDescriptorList.size());
        Assert.assertTrue("Didn't find ignoreable test-manual-task",validateXMLContent(taskResourceDescriptorList, "task-status.xml", "exists(//server:task[@name = 'test-manual-task' and @ACTION = 'IGNORE' and exists(@EXCEPTION)])"));
        
        Util.copyTree("test-data/test-manual-task.xml", "capo/server/clients/capo.client.1/tasks/test-manual-task.xml");
        waitForTaskManagerToRun(1);
        taskResourceDescriptorList = CapoApplication.getDataManager().findDocuments(CapoApplication.getDataManager().getResourceDirectory(Preferences.TASK_DIR.toString()));
        Assert.assertEquals(2,taskResourceDescriptorList.size());
        Assert.assertTrue("Didn't find new synched test-manual-task",validateXMLContent(taskResourceDescriptorList, "test-manual-task.xml", "exists(//server:task/server:export[exists(@dest)])"));
        Diff diff = new Diff(new FileInputStream("test-data/test.txt"), new FileInputStream("capo/client/test.txt"));
        String[] differences = diff.getDifferences().split("\n");
        Assert.assertTrue("There should be data in the export file",differences.length > 2);
        for (String difference : differences)
        {            
            Assert.assertTrue("Line contains a difference:" +difference, difference.startsWith("="));
        }
    	
        Util.deleteTree("capo/server/clients/capo.client.1/tasks/test-manual-task.xml");
        waitForTaskManagerToRun(1);
        
        taskResourceDescriptorList = CapoApplication.getDataManager().findDocuments(CapoApplication.getDataManager().getResourceDirectory(Preferences.TASK_DIR.toString()));
        Assert.assertEquals(1,taskResourceDescriptorList.size());
        
        Util.copyTree("test-data/test-manual-task-single-run.xml", "capo/server/clients/capo.client.1/tasks/test-manual-task.xml");
        waitForTaskManagerToRun(1);
        taskResourceDescriptorList = CapoApplication.getDataManager().findDocuments(CapoApplication.getDataManager().getResourceDirectory(Preferences.TASK_DIR.toString()));
        Assert.assertEquals(2,taskResourceDescriptorList.size());
        waitForTaskManagerToRun(2);
        Assert.assertTrue("Didn't find ignoreable run test-manual-task",validateXMLContent(taskResourceDescriptorList, "task-status.xml", "exists(//server:task[@name = 'test-manual-task' and @ACTION = 'IGNORE' and not(exists(@EXCEPTION)) and exists(@lastExecutionTime)])"));        
        
        File manualTaskFile = new File("capo/server/clients/capo.client.1/tasks/test-manual-task.xml");
        Document manualTaskDocument = CapoApplication.getDocumentBuilder().parse(manualTaskFile);
        ((Element)XPath.selectSingleNode(manualTaskDocument, "//server:task")).setAttribute("orpanAction", "DELETE");
        XPath.dumpNode(manualTaskDocument, new FileOutputStream(manualTaskFile));
        waitForTaskManagerToRun(3);
        taskResourceDescriptorList = CapoApplication.getDataManager().findDocuments(CapoApplication.getDataManager().getResourceDirectory(Preferences.TASK_DIR.toString()));
        Assert.assertEquals(1,taskResourceDescriptorList.size());
        System.out.println();
    }

    private boolean validateXMLContent(List<ResourceDescriptor> resourceDescriptorList,String documentName,String xpath) throws Exception
    {
        
        for (ResourceDescriptor resourceDescriptor : resourceDescriptorList)
        {
            Document testDocument = CapoApplication.getDocumentBuilder().parse(resourceDescriptor.getInputStream(null));
            if (resourceDescriptor.getLocalName().equals(documentName))
            {
                return XPath.evaluate(testDocument, xpath);
            }
        }
        fail("Didn't find document: "+documentName);
        return false;
    }
    
    private void waitForTaskManagerToRun(int loopCount) throws Exception
    {
        long lastRunTime = TaskManagerThread.getTaskManagerThread().getLastRunTime();
        int currentLoop = 0;
        TaskManagerThread.getTaskManagerThread().getLock().unlock();
        System.out.println("waiting for TaskManager to run "+loopCount+" times: ");
        while(currentLoop < loopCount)
        {
            if (lastRunTime != TaskManagerThread.getTaskManagerThread().getLastRunTime())
            {
                currentLoop++;
                lastRunTime = TaskManagerThread.getTaskManagerThread().getLastRunTime();
                System.out.print(currentLoop);
            }
            System.out.print(".");    
            Thread.sleep(CapoApplication.getConfiguration().getLongValue(Preferences.TASK_INTERVAL)/4l);
        }
        TaskManagerThread.getTaskManagerThread().getLock().lock();
        System.out.println();
    }
    
}
