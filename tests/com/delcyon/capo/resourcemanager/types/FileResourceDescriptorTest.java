package com.delcyon.capo.resourcemanager.types;


import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.datastream.StreamUtil;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceDescriptorTest;
import com.delcyon.capo.resourcemanager.ResourceParameter;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.Action;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.LifeCycle;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.State;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.StreamFormat;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.StreamType;
import com.delcyon.capo.tests.util.TestServer;
import com.delcyon.capo.tests.util.Util;
import com.delcyon.capo.xml.XMLDiff;

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
        return TestServer.getServerInstance().getApplication().getDataManager().getResourceDescriptor(null, "file:config/config.xml");
    }
    
    @Before
    public void setup() throws Exception
    {
    	Util.copyTree("test-data/capo", "capo", true, true);
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

    @Override //TODO remove, since never used
    public void testGetStreamState() throws Exception
    {   
    	return;
    	/*
    	resourceDescriptor.reset(State.NONE);
    	resourceDescriptor.open(null);
    	if (Arrays.asList(resourceDescriptor.getSupportedStreamTypes()).contains(StreamType.INPUT))
    	{
    		resourceDescriptor.getInputStream(null);
    		Assert.assertSame(State.OPEN,resourceDescriptor.getStreamState(StreamType.INPUT));
    	}
    	if (Arrays.asList(resourceDescriptor.getSupportedStreamTypes()).contains(StreamType.ERROR))
    	{
    		//TODO no get ErrorInputStream
    		Assert.assertSame(State.OPEN,resourceDescriptor.getStreamState(StreamType.ERROR));
    	}
    	
    	
    	if (Arrays.asList(resourceDescriptor.getSupportedStreamTypes()).contains(StreamType.OUTPUT))
    	{
    		resourceDescriptor.getOutputStream(null);
        	Assert.assertSame(State.OPEN,resourceDescriptor.getStreamState(StreamType.OUTPUT));
    	}
    	*/
    }

    @Override
    public void testPerformAction() throws Exception
    {
        resourceDescriptor.reset(State.NONE);
        resourceDescriptor.init(null, LifeCycle.EXPLICIT, false);
        Assert.assertTrue(resourceDescriptor.getContentMetaData(null).exists());
        resourceDescriptor.performAction(null, Action.DELETE);        
        Assert.assertTrue(resourceDescriptor.getContentMetaData(null).exists() == false);
        resourceDescriptor.performAction(null, Action.CREATE);
        Assert.assertTrue(resourceDescriptor.getContentMetaData(null).exists());
    }

    @Override
    public void testIsSupportedAction() throws Exception
    {
    	resourceDescriptor.reset(State.NONE);
        resourceDescriptor.init(null, LifeCycle.EXPLICIT, false);
        Assert.assertTrue(resourceDescriptor.isSupportedAction(Action.CREATE));
        Assert.assertTrue(resourceDescriptor.isSupportedAction(Action.DELETE));
    }

    @Override
    public void testIsRemoteResource() throws Exception
    {
    	Assert.assertTrue(resourceDescriptor.isRemoteResource() == false);
        
    }

    @Override
    public void testSetup() throws Exception
    {
        ResourceDescriptor  resourceDescriptor = this.resourceDescriptor.getResourceType().getResourceDescriptor(this.resourceDescriptor.getResourceURI());
        Assert.assertSame(State.NONE,resourceDescriptor.getResourceState());
        Assert.assertSame(this.resourceDescriptor.getResourceType(),resourceDescriptor.getResourceType());
        Assert.assertSame(this.resourceDescriptor.getResourceURI(),resourceDescriptor.getResourceURI());
        Assert.assertSame(this.resourceDescriptor.getResourceType().getDefaultLifeCycle(),resourceDescriptor.getLifeCycle());
        resourceDescriptor.release(null);
    }

    @Override
    public void testInit() throws Exception
    {
    	ResourceDescriptor  resourceDescriptor = this.resourceDescriptor.getResourceType().getResourceDescriptor(this.resourceDescriptor.getResourceURI());
        resourceDescriptor.init(null, LifeCycle.EXPLICIT, false);
        Assert.assertSame(State.INITIALIZED,resourceDescriptor.getResourceState());
        Assert.assertNotNull(resourceDescriptor.getResourceURI());
        Assert.assertNotNull(resourceDescriptor.getLocalName());
        //TODO check for initialization content meta data
        resourceDescriptor.release(null);
    }

    @Override
    public void testOpen() throws Exception
    {
        resourceDescriptor.reset(State.NONE);
        resourceDescriptor.init(null, LifeCycle.EXPLICIT, false);
        resourceDescriptor.open(null);
        Assert.assertSame(State.OPEN,resourceDescriptor.getResourceState());
      //TODO check for open content meta data
    }

    @Override
    public void testReadXML() throws Exception
    {
    	resourceDescriptor.reset(State.NONE);
        resourceDescriptor.init(null, LifeCycle.EXPLICIT, false);
        resourceDescriptor.open(null);
        Assert.assertSame(State.OPEN,resourceDescriptor.getResourceState());
        Element element = resourceDescriptor.readXML(null);
        Assert.assertNotNull(element);
        Assert.assertTrue(element.hasChildNodes());
        int count = 0;
        NodeList nodeList = element.getChildNodes();
        for(int index = 0; index < nodeList.getLength(); index++)
        {
        	if (nodeList.item(index).getNodeType() == Node.ELEMENT_NODE)
        	{
        		count++;
        	}
        }
        System.out.println("readXML found "+count+" child elements");
        Assert.assertTrue(count > 0);
        resourceDescriptor.release(null);
    }

    

    @Override
    public void testReadBlock() throws Exception
    {
    	resourceDescriptor.reset(State.NONE);
        resourceDescriptor.init(null, LifeCycle.EXPLICIT, false);
        resourceDescriptor.open(null);
        Assert.assertSame(State.OPEN,resourceDescriptor.getResourceState());
        byte[] data = resourceDescriptor.readBlock(null);
        Assert.assertTrue(data.length > 10);
        System.out.println("Read the following bloack data: '"+new String(data)+"'");
        
    }

    @Override
    public void testWriteXML() throws Exception
    {
    	resourceDescriptor.reset(State.NONE);
        resourceDescriptor.init(null, LifeCycle.EXPLICIT, false);
        resourceDescriptor.open(null);
        Document document = CapoApplication.getDocumentBuilder().newDocument();
        Element rootElement = document.createElementNS("BSNS","ns:testRootElement");
        rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/","xmlns:ns","BSNS");
        rootElement.setTextContent("This is a test");
        resourceDescriptor.writeXML(null, rootElement);
        Element readElement = resourceDescriptor.readXML(null);
        XMLDiff xmlDiff = new XMLDiff();
        Element diffElement = xmlDiff.getDifferences(rootElement, readElement);
        Assert.assertEquals(XMLDiff.EQUALITY,diffElement.getAttribute("xdiff:element"));
        
    }
    
    
    @Override
    public void testWriteBlock() throws Exception
    {
    	resourceDescriptor.reset(State.NONE);
        resourceDescriptor.init(null, LifeCycle.EXPLICIT, false);
        resourceDescriptor.open(null);
        resourceDescriptor.writeBlock(null,"this is a test".getBytes());
        Assert.assertArrayEquals("this is a test".getBytes(),resourceDescriptor.readBlock(null));
        
    }

    @Override
    public void testNext() throws Exception
    {
    	resourceDescriptor.reset(State.NONE);
        resourceDescriptor.init(null, LifeCycle.EXPLICIT, false);
        resourceDescriptor.open(null);
        if(resourceDescriptor.getResourceType().isIterable())
        {
        	Assert.assertTrue(resourceDescriptor.next(null));
        }
        else
        {
        	Assert.assertTrue(resourceDescriptor.next(null) == false);
        }
        
    }

    @Override
    public void testProcessOutput() throws Exception
    {
    	resourceDescriptor.reset(State.NONE);
        resourceDescriptor.init(null, LifeCycle.EXPLICIT, false);
        resourceDescriptor.open(null);
        if (resourceDescriptor.isSupportedStreamFormat(StreamType.OUTPUT, StreamFormat.PROCESS))
        {
        	resourceDescriptor.processOutput(null);
        	//TODO figure out what exactly we expect to happen here, or if this method is even useful.
        }
        else
        {
        	//skip, do nothing because it isn't supported
        }
        
    }

    @Override
    public void testProcessInput() throws Exception
    {
    	resourceDescriptor.reset(State.NONE);
        resourceDescriptor.init(null, LifeCycle.EXPLICIT, false);
        resourceDescriptor.open(null);
        if (resourceDescriptor.isSupportedStreamFormat(StreamType.INPUT, StreamFormat.PROCESS))
        {
        	resourceDescriptor.processInput(null);
        	//TODO figure out what exactly we expect to happen here, or if this method is even useful.
        }
        else
        {
        	//skip, do nothing because it isn't supported
        }
        
    }

    @Override
    public void testGetInputStream() throws Exception
    {
    	resourceDescriptor.reset(State.NONE);
        resourceDescriptor.init(null, LifeCycle.EXPLICIT, false);
        resourceDescriptor.open(null);
        if (resourceDescriptor.isSupportedStreamFormat(StreamType.INPUT, StreamFormat.STREAM))
        {
        	InputStream inputStream = resourceDescriptor.getInputStream(null);
        	ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        	Assert.assertTrue(StreamUtil.readInputStreamIntoOutputStream(inputStream, byteArrayOutputStream) > 10);
        	Assert.assertTrue(new String(byteArrayOutputStream.toByteArray()).startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        }
        else
        {
        	//do nothing
        }
        
    }

    @Override
    public void testGetOutputStream() throws Exception
    {
    	resourceDescriptor.reset(State.NONE);
        resourceDescriptor.init(null, LifeCycle.EXPLICIT, false);
        resourceDescriptor.open(null);
        if (resourceDescriptor.isSupportedStreamFormat(StreamType.OUTPUT, StreamFormat.STREAM))
        {
        	OutputStream outputStream = resourceDescriptor.getOutputStream(null);
        	outputStream.write("this is a test".getBytes());
        	outputStream.close();
        	InputStream inputStream = resourceDescriptor.getInputStream(null);
        	ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        	Assert.assertTrue(StreamUtil.readInputStreamIntoOutputStream(inputStream, byteArrayOutputStream) > 10);
        	Assert.assertTrue(new String(byteArrayOutputStream.toByteArray()).equals("this is a test"));
        }
        else
        {
        	//do nothing
        }
        
    }

    @Override
    public void testClose() throws Exception
    {
    	resourceDescriptor.reset(State.NONE);
        resourceDescriptor.init(null, LifeCycle.EXPLICIT, false);
        resourceDescriptor.open(null);
        Assert.assertSame(State.OPEN,resourceDescriptor.getResourceState());
        resourceDescriptor.close(null);
        Assert.assertSame(State.CLOSED,resourceDescriptor.getResourceState());        
    }

    @Override
    public void testRelease() throws Exception
    {
    	resourceDescriptor.reset(State.NONE);
        resourceDescriptor.init(null, LifeCycle.EXPLICIT, false);
        resourceDescriptor.open(null);
        Assert.assertSame(State.OPEN,resourceDescriptor.getResourceState());
        resourceDescriptor.close(null);
        Assert.assertSame(State.CLOSED,resourceDescriptor.getResourceState());
        resourceDescriptor.release(null);
        Assert.assertSame(State.RELEASED,resourceDescriptor.getResourceState());
        
    }

    @Override
    public void testGetContentMetaData() throws Exception
    {
    	resourceDescriptor.reset(State.NONE);
        resourceDescriptor.init(null, LifeCycle.EXPLICIT, false);
        resourceDescriptor.open(null);
        Assert.assertSame(State.OPEN,resourceDescriptor.getResourceState());
        ContentMetaData contentMetaData = resourceDescriptor.getContentMetaData(null);
        List<String> attributeList = contentMetaData.getSupportedAttributes();
        for (String attribute : attributeList)
		{
        	System.out.println(attribute+" = "+contentMetaData.getValue(attribute));
			Assert.assertNotNull(contentMetaData.getValue(attribute));
		}
        
        
    }

    @Override
    public void testGetIterationMetaData() throws Exception
    {
    	if (resourceDescriptor.getResourceType().isIterable())
    	{
    		resourceDescriptor.reset(State.NONE);
    		resourceDescriptor.init(null, LifeCycle.EXPLICIT, false);
    		resourceDescriptor.open(null);
    		resourceDescriptor.next(null);
    		ContentMetaData contentMetaData = resourceDescriptor.getIterationMetaData(null);
    		List<String> attributeList = contentMetaData.getSupportedAttributes();
    		for (String attribute : attributeList)
    		{
    			System.out.println(attribute+" = "+contentMetaData.getValue(attribute));
    			Assert.assertNotNull(contentMetaData.getValue(attribute));
    		}
    	}

    }

    @Override
    public void testGetLifeCycle() throws Exception
    {
    	Assert.assertNotNull(resourceDescriptor.getLifeCycle());
        
    }

    @Override
    public void testGetResourceURI() throws Exception
    {
    	Assert.assertNotNull(resourceDescriptor.getResourceURI());
        
    }

    @Override
    public void testGetLocalName() throws Exception
    {
    	Assert.assertNotNull(resourceDescriptor.getLocalName());
        
    }

    @Override
    public void testGetResourceType() throws Exception
    {
    	Assert.assertNotNull(resourceDescriptor.getResourceType());
    }

    @Override
    public void testAddResourceParameters() throws Exception
    {
        resourceDescriptor.addResourceParameters(null, new ResourceParameter("test","test"));        
    }

    @Override
    public void testGetChildResourceDescriptor() throws Exception
    {
    	resourceDescriptor.reset(State.NONE);
    	resourceDescriptor.init(null, LifeCycle.EXPLICIT, false);
    	resourceDescriptor.open(null);
    	if (resourceDescriptor.getContentMetaData(null).isContainer())
    	{
    		List<ContentMetaData> childContentMetaDataList = resourceDescriptor.getContentMetaData(null).getContainedResources();
    		for (ContentMetaData contentMetaData : childContentMetaDataList)
			{
				Assert.assertNotNull(resourceDescriptor.getChildResourceDescriptor(null, contentMetaData.getResourceURI()));
			}
    	}
        
    }

    
}
