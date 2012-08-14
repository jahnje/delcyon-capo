package com.delcyon.capo.resourcemanager;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public abstract class ResourceDescriptorTest
{
    protected ResourceDescriptor resourceDescriptor = null;
    
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
        this.resourceDescriptor = getResourceDescriptor();
    }

    protected abstract ResourceDescriptor getResourceDescriptor() throws Exception;

    @After
    public void tearDown() throws Exception
    {
    }

    @Test
    public abstract void testGetSupportedStreamTypes() throws Exception;

    @Test
    public abstract void testIsSupportedStreamType() throws Exception;

    @Test
    public abstract void testGetSupportedStreamFormats() throws Exception;

    @Test
    public abstract void testIsSupportedStreamFormat() throws Exception;

    @Test
    public abstract void testGetResourceState() throws Exception;

    @Test
    public abstract void testGetStreamState() throws Exception;

    @Test
    public abstract void testPerformAction() throws Exception;

    @Test
    public abstract void testIsSupportedAction() throws Exception;

    @Test
    public abstract void testIsRemoteResource() throws Exception;

    @Test
    public abstract void testSetup() throws Exception;

    @Test
    public abstract void testInit() throws Exception;

    @Test
    public abstract void testOpen() throws Exception;

    @Test
    public abstract void testReadXML() throws Exception;

    @Test
    public abstract void testWriteXML() throws Exception;

    @Test
    public abstract void testReadBlock() throws Exception;

    @Test
    public abstract void testWriteBlock() throws Exception;

    @Test
    public abstract void testNext() throws Exception;

    @Test
    public abstract void testProcessOutput() throws Exception;

    @Test
    public abstract void testProcessInput() throws Exception;

    @Test
    public abstract void testGetInputStream() throws Exception;
    @Test
    public abstract void testGetOutputStream() throws Exception;

    @Test
    public abstract void testClose() throws Exception;

    @Test
    public abstract void testRelease() throws Exception;

    @Test
    public abstract void testGetContentMetaData() throws Exception;

    @Test
    public abstract void testGetIterationMetaData() throws Exception;

    @Test
    public abstract void testGetLifeCycle() throws Exception;

    @Test
    public abstract void testGetResourceURI() throws Exception;

    @Test
    public abstract void testGetLocalName() throws Exception;

    @Test
    public abstract void testSetResourceURI() throws Exception;

    @Test
    public abstract void testGetResourceType() throws Exception;

    @Test
    public abstract void testAddResourceParameters() throws Exception;

    @Test
    public abstract void testGetChildResourceDescriptor() throws Exception;

}
