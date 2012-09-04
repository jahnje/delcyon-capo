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
import com.delcyon.capo.resourcemanager.ResourceDescriptor.LifeCycle;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.State;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.StreamFormat;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.StreamType;
import com.delcyon.capo.resourcemanager.ResourceDescriptorTest;
import com.delcyon.capo.resourcemanager.ResourceParameter;
import com.delcyon.capo.tests.util.TestServer;
import com.delcyon.capo.tests.util.Util;
import com.delcyon.capo.xml.XMLDiff;
import com.delcyon.capo.xml.XPath;

public class JdbcResourceDescriptorTest extends ResourceDescriptorTest
{

    @Override
    @Before
    public void setUp() throws Exception
    {
        Util.copyTree("test-data/testdb", "testdb", true, true);
        super.setUp();
    }
    
	@Override
    protected ResourceDescriptor getResourceDescriptor() throws Exception
    {        
        ResourceDescriptor resourceDescriptor =  TestServer.getServerInstance().getApplication().getDataManager().getResourceDescriptor(null, "jdbc:hsqldb:file:testdb/testdb");
        resourceDescriptor.addResourceParameters(null, new ResourceParameter("user","user"));
        resourceDescriptor.addResourceParameters(null, new ResourceParameter("password",""));        
        return resourceDescriptor;
        
    }
    
    @Override
    protected String getExpectedResourceContentPrefix()
    {    	
    	return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
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
        Assert.assertArrayEquals("Expected Stream formats are not correct for "+StreamType.INPUT+" streamType actual:"+Arrays.toString(this.resourceDescriptor.getSupportedStreamFormats(StreamType.INPUT)),new ResourceDescriptor.StreamFormat[]{StreamFormat.XML_BLOCK},this.resourceDescriptor.getSupportedStreamFormats(StreamType.INPUT));
        Assert.assertArrayEquals("Expected Stream formats are not correct for "+StreamType.OUTPUT+" streamType actual:"+Arrays.toString(this.resourceDescriptor.getSupportedStreamFormats(StreamType.OUTPUT)),new ResourceDescriptor.StreamFormat[]{StreamFormat.PROCESS},this.resourceDescriptor.getSupportedStreamFormats(StreamType.OUTPUT));
        Assert.assertArrayEquals("Expected Stream formats are not correct for "+StreamType.ERROR+" streamType actual:"+Arrays.toString(this.resourceDescriptor.getSupportedStreamFormats(StreamType.ERROR)),null,this.resourceDescriptor.getSupportedStreamFormats(StreamType.ERROR));
    }

    @Override
    public void testIsSupportedStreamFormat() throws Exception
    {
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.INPUT, StreamFormat.BLOCK) == false);
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.INPUT, StreamFormat.PROCESS) == false);
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.INPUT, StreamFormat.STREAM) == false);
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.INPUT, StreamFormat.XML_BLOCK) == true);
        
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.OUTPUT, StreamFormat.BLOCK) == false);
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.OUTPUT, StreamFormat.PROCESS) == true);
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.OUTPUT, StreamFormat.STREAM) == false);
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.OUTPUT, StreamFormat.XML_BLOCK) == false);
        
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.ERROR, StreamFormat.BLOCK) == false);
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.ERROR, StreamFormat.PROCESS) == false);
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.ERROR, StreamFormat.STREAM) == false);
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.ERROR, StreamFormat.XML_BLOCK) == false);
    }

    @Override
    @Test
    public void testReadXML() throws Exception
    {
        resourceDescriptor.addResourceParameters(null, new ResourceParameter("query","select * from systems"));
        super.testReadXML();
    }
    
    @Override
    @Test
    public void testReadBlock() throws Exception
    {
        resourceDescriptor.addResourceParameters(null, new ResourceParameter("query","select * from systems"));
        super.testReadBlock();
    }
    
    @Override
    @Test
    public void testNext() throws Exception
    {
        resourceDescriptor.addResourceParameters(null, new ResourceParameter("query","select * from systems"));
        super.testNext();
    }
    
    @Override
    @Test
    public void testProcessOutput() throws Exception
    {
        resourceDescriptor.addResourceParameters(null, new ResourceParameter("update","INSERT INTO SYSTEMS VALUES('BS-ID','BS-NAME','2012-06-22 10:33:11.840000','BS-OS')"));
        super.testProcessOutput();
        resourceDescriptor.addResourceParameters(null, new ResourceParameter("query","select * from systems"));
        Element readElement = resourceDescriptor.readXML(null);
        XMLDiff xmlDiff = new XMLDiff();
        xmlDiff.setAllowNamespaceMismatches(true);
        Document baseDocument = TestServer.getServerInstance().getDocumentBuilder().parse(new File("test-data/testdb/update_results.xml"));
        Element diffElement = xmlDiff.getDifferences(baseDocument.getDocumentElement(), readElement);
        if(XMLDiff.EQUALITY.equals(diffElement.getAttribute(XMLDiff.XDIFF_PREFIX+":"+XMLDiff.XDIFF_ELEMENT_ATTRIBUTE_NAME)) == false)
        {
            XPath.dumpNode(diffElement, System.err);
        }
        Assert.assertEquals(XMLDiff.EQUALITY, diffElement.getAttribute(XMLDiff.XDIFF_PREFIX+":"+XMLDiff.XDIFF_ELEMENT_ATTRIBUTE_NAME));
        
    }
    
    @Override
    @Test
    public void testGetIterationMetaData() throws Exception
    {
        resourceDescriptor.addResourceParameters(null, new ResourceParameter("query","select * from systems"));
        super.testGetIterationMetaData();
    }
}
