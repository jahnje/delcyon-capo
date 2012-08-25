package com.delcyon.capo.resourcemanager.types;


import java.util.Arrays;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.LifeCycle;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.State;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.StreamFormat;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.StreamType;
import com.delcyon.capo.resourcemanager.ResourceDescriptorTest;
import com.delcyon.capo.tests.util.TestServer;
import com.delcyon.capo.tests.util.Util;

public class FileResourceDescriptorTest extends ResourceDescriptorTest
{

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        Util.copyTree("test-data/capo", "capo", true, true);
        TestServer.start();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception
    {
        TestServer.shutdown();
    }

    @Override
    protected ResourceDescriptor getResourceDescriptor() throws Exception
    {        
        return TestServer.getServerInstance().getApplication().getDataManager().getResourceDescriptor(null, "file:test-data/main.xml");
    }
    
    

    @After
    public void tearDown() throws Exception
    {
    }

    @Override
    public void testGetSupportedStreamTypes() throws Exception
    {
      Assert.assertTrue("Expected Stream types are not the same",Arrays.equals(new ResourceDescriptor.StreamType[]{StreamType.INPUT,StreamType.OUTPUT},this.resourceDescriptor.getSupportedStreamTypes()));
    }

    @Override
    public void testIsSupportedStreamType() throws Exception
    {
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamType(StreamType.ERROR) == false);
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamType(StreamType.INPUT));
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamType(StreamType.OUTPUT));
    }

    @Override
    public void testGetSupportedStreamFormats() throws Exception
    {
        Assert.assertArrayEquals("Expected Stream formats are not correct for "+StreamType.INPUT+" streamType actual:"+Arrays.toString(this.resourceDescriptor.getSupportedStreamFormats(StreamType.INPUT)),new ResourceDescriptor.StreamFormat[]{StreamFormat.STREAM},this.resourceDescriptor.getSupportedStreamFormats(StreamType.INPUT));
        Assert.assertArrayEquals("Expected Stream formats are not correct for "+StreamType.OUTPUT+" streamType actual:"+Arrays.toString(this.resourceDescriptor.getSupportedStreamFormats(StreamType.OUTPUT)),new ResourceDescriptor.StreamFormat[]{StreamFormat.STREAM},this.resourceDescriptor.getSupportedStreamFormats(StreamType.OUTPUT));
        Assert.assertArrayEquals("Expected Stream formats are not correct for "+StreamType.ERROR+" streamType actual:"+Arrays.toString(this.resourceDescriptor.getSupportedStreamFormats(StreamType.ERROR)),null,this.resourceDescriptor.getSupportedStreamFormats(StreamType.ERROR));
    }

    @Override
    public void testIsSupportedStreamFormat() throws Exception
    {
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.INPUT, StreamFormat.BLOCK) == false);
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.INPUT, StreamFormat.PROCESS) == false);
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.INPUT, StreamFormat.STREAM));
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.INPUT, StreamFormat.XML_BLOCK) == false);
        
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.OUTPUT, StreamFormat.BLOCK) == false);
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.OUTPUT, StreamFormat.PROCESS) == false);
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.OUTPUT, StreamFormat.STREAM));
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.OUTPUT, StreamFormat.XML_BLOCK) == false);
        
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.ERROR, StreamFormat.BLOCK) == false);
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.ERROR, StreamFormat.PROCESS) == false);
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.ERROR, StreamFormat.STREAM) == false);
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.ERROR, StreamFormat.XML_BLOCK) == false);
    }

    @Override
    public void testGetResourceState() throws Exception
    {
        resourceDescriptor.reset(State.NONE);
        Assert.assertSame(State.NONE,resourceDescriptor.getResourceState());
        resourceDescriptor.reset(State.NONE);
        resourceDescriptor.init(null, LifeCycle.EXPLICIT, false);
        Assert.assertSame(State.INITIALIZED,resourceDescriptor.getResourceState());
        resourceDescriptor.reset(State.NONE);
        resourceDescriptor.open(null);
        Assert.assertSame(State.OPEN,resourceDescriptor.getResourceState());
        resourceDescriptor.reset(State.NONE);
        resourceDescriptor.close(null);
        Assert.assertSame(State.CLOSED,resourceDescriptor.getResourceState());
        resourceDescriptor.reset(State.NONE);
        resourceDescriptor.release(null);
        Assert.assertSame(State.RELEASED,resourceDescriptor.getResourceState());
    }

    @Override
    public void testGetStreamState() throws Exception
    {        
        Assert.fail("not yet implementd");
    }

    @Override
    public void testPerformAction() throws Exception
    {
        Assert.fail("not yet implementd");
        
    }

    @Override
    public void testIsSupportedAction() throws Exception
    {
        Assert.fail("not yet implementd");
        
    }

    @Override
    public void testIsRemoteResource() throws Exception
    {
        Assert.fail("not yet implementd");
        
    }

    @Override
    public void testSetup() throws Exception
    {
        Assert.fail("not yet implementd");
        
    }

    @Override
    public void testInit() throws Exception
    {
        Assert.fail("not yet implementd");
        
    }

    @Override
    public void testOpen() throws Exception
    {
        Assert.fail("not yet implementd");
        
    }

    @Override
    public void testReadXML() throws Exception
    {
        Assert.fail("not yet implementd");
        
    }

    @Override
    public void testWriteXML() throws Exception
    {
        Assert.fail("not yet implementd");
        
    }

    @Override
    public void testReadBlock() throws Exception
    {
        Assert.fail("not yet implementd");
        
    }

    @Override
    public void testWriteBlock() throws Exception
    {
        Assert.fail("not yet implementd");
        
    }

    @Override
    public void testNext() throws Exception
    {
        Assert.fail("not yet implementd");
        
    }

    @Override
    public void testProcessOutput() throws Exception
    {
        Assert.fail("not yet implementd");
        
    }

    @Override
    public void testProcessInput() throws Exception
    {
        Assert.fail("not yet implementd");
        
    }

    @Override
    public void testGetInputStream() throws Exception
    {
        Assert.fail("not yet implementd");
        
    }

    @Override
    public void testGetOutputStream() throws Exception
    {
        Assert.fail("not yet implementd");
        
    }

    @Override
    public void testClose() throws Exception
    {
        Assert.fail("not yet implementd");
        
    }

    @Override
    public void testRelease() throws Exception
    {
        Assert.fail("not yet implementd");
        
    }

    @Override
    public void testGetContentMetaData() throws Exception
    {
        Assert.fail("not yet implementd");
        
    }

    @Override
    public void testGetIterationMetaData() throws Exception
    {
        Assert.fail("not yet implementd");
        
    }

    @Override
    public void testGetLifeCycle() throws Exception
    {
        Assert.fail("not yet implementd");
        
    }

    @Override
    public void testGetResourceURI() throws Exception
    {
        Assert.fail("not yet implementd");
        
    }

    @Override
    public void testGetLocalName() throws Exception
    {
        Assert.fail("not yet implementd");
        
    }

    @Override
    public void testSetResourceURI() throws Exception
    {
        Assert.fail("not yet implementd");
        
    }

    @Override
    public void testGetResourceType() throws Exception
    {
        Assert.fail("not yet implementd");
    }

    @Override
    public void testAddResourceParameters() throws Exception
    {
        Assert.fail("not yet implementd");
        
    }

    @Override
    public void testGetChildResourceDescriptor() throws Exception
    {
        Assert.fail("not yet implementd");
        
    }

    
}
