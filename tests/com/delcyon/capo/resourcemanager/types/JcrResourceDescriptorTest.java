package com.delcyon.capo.resourcemanager.types;


import java.io.File;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.Action;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.LifeCycle;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.State;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.StreamFormat;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.StreamType;
import com.delcyon.capo.resourcemanager.ResourceDescriptorTest;
import com.delcyon.capo.resourcemanager.ResourceParameter;
import com.delcyon.capo.tests.util.TestServer;
import com.delcyon.capo.xml.XMLDiff;
import com.delcyon.capo.xml.XPath;
import com.delcyon.capo.xml.cdom.CElement;

public class JcrResourceDescriptorTest extends ResourceDescriptorTest
{

    @Override
    @Before
    public void setUp() throws Exception
    {
        //Util.copyTree("test-data/capo", "capo", true, true);        
        super.setUp();
    }
    
	@Override
    protected ResourceDescriptor getResourceDescriptor() throws Exception
    {        
        ResourceDescriptor resourceDescriptor =  TestServer.getServerInstance().getApplication().getDataManager().getResourceDescriptor(null, "repo:/clients/identity");
        resourceDescriptor.init(null, null, LifeCycle.EXPLICIT, false);        
        resourceDescriptor.performAction(null, Action.CREATE);
        resourceDescriptor.writeBlock(null, "this is a test".getBytes());
        resourceDescriptor.performAction(null, Action.COMMIT);
        resourceDescriptor.close(null);
        resourceDescriptor.reset(State.INITIALIZED);
        return resourceDescriptor;
        
    }
    
    @Override
    protected String getExpectedResourceContentPrefix()
    {    	
    	return "this is a test";
    }
    
    
    @Override
    public void testGetSupportedStreamTypes() throws Exception
    {
      Assert.assertTrue("Expected Stream types are not the same",Arrays.equals(new ResourceDescriptor.StreamType[]{StreamType.OUTPUT,StreamType.INPUT},this.resourceDescriptor.getSupportedStreamTypes()));
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
        Assert.assertArrayEquals("Expected Stream formats are not correct for "+StreamType.INPUT+" streamType actual:"+Arrays.toString(this.resourceDescriptor.getSupportedStreamFormats(StreamType.INPUT)),new ResourceDescriptor.StreamFormat[]{StreamFormat.STREAM,StreamFormat.XML_BLOCK},this.resourceDescriptor.getSupportedStreamFormats(StreamType.INPUT));
        Assert.assertArrayEquals("Expected Stream formats are not correct for "+StreamType.OUTPUT+" streamType actual:"+Arrays.toString(this.resourceDescriptor.getSupportedStreamFormats(StreamType.OUTPUT)),new ResourceDescriptor.StreamFormat[]{StreamFormat.STREAM,StreamFormat.XML_BLOCK},this.resourceDescriptor.getSupportedStreamFormats(StreamType.OUTPUT));
        Assert.assertArrayEquals("Expected Stream formats are not correct for "+StreamType.ERROR+" streamType actual:"+Arrays.toString(this.resourceDescriptor.getSupportedStreamFormats(StreamType.ERROR)),null,this.resourceDescriptor.getSupportedStreamFormats(StreamType.ERROR));
    }

    @Override
    public void testIsSupportedStreamFormat() throws Exception
    {
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.INPUT, StreamFormat.BLOCK) == false);
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.INPUT, StreamFormat.PROCESS) == false);
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.INPUT, StreamFormat.STREAM) == true);
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.INPUT, StreamFormat.XML_BLOCK) == true);
        
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.OUTPUT, StreamFormat.BLOCK) == false);
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.OUTPUT, StreamFormat.PROCESS) == false);
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.OUTPUT, StreamFormat.STREAM) == true);
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.OUTPUT, StreamFormat.XML_BLOCK) == true);
        
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.ERROR, StreamFormat.BLOCK) == false);
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.ERROR, StreamFormat.PROCESS) == false);
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.ERROR, StreamFormat.STREAM) == false);
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.ERROR, StreamFormat.XML_BLOCK) == false);
    }

//    @Override
//    @Test
//    public void testReadXML() throws Exception
//    {
//        resourceDescriptor.addResourceParameters(null, new ResourceParameter("query","select * from systems"));
//        super.testReadXML();
//    }
//    
//    @Override
//    @Test
//    public void testReadBlock() throws Exception
//    {
//        resourceDescriptor.addResourceParameters(null, new ResourceParameter("query","select * from systems"));
//        super.testReadBlock();
//    }
//    
//    @Override
//    @Test
//    public void testNext() throws Exception
//    {
//        resourceDescriptor.addResourceParameters(null, new ResourceParameter("query","select * from systems"));
//        super.testNext();
//    }
//    
    
    
    @Test 
    @Override
    public void testPerformAction() throws Exception
    {
        resourceDescriptor.reset(State.NONE);
        resourceDescriptor.init(null, null, LifeCycle.EXPLICIT, false);
        
        if (resourceDescriptor.isSupportedAction(Action.DELETE))
        {
            Assert.assertTrue(resourceDescriptor.getResourceMetaData(null).exists());
            Assert.assertTrue(resourceDescriptor.performAction(null, Action.DELETE));     
            Assert.assertTrue(resourceDescriptor.getResourceMetaData(null).exists() == false);
        }
        
    }
    
    @Override
    @Test
    public void testWriteXML() throws Exception
    {
        resourceDescriptor.reset(State.NONE);
        resourceDescriptor.init(null, null, LifeCycle.EXPLICIT, false);
//        resourceDescriptor.open(null);
//        resourceDescriptor.performAction(null, Action.CREATE);
        Document baseDocument = TestServer.getServerInstance().getDocumentBuilder().parse(new File("test-data/main.xml"));
       resourceDescriptor.writeXML(null, (CElement)baseDocument.getDocumentElement());
    }
    
//    @Override
//    @Test
//    public void testGetContentMetaData() throws Exception
//    {
//        resourceDescriptor.addResourceParameters(null, new ResourceParameter("query","select * from systems"));
//        super.testGetContentMetaData();
//    }
}
