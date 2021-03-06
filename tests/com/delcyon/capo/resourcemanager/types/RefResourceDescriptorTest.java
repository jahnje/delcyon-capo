package com.delcyon.capo.resourcemanager.types;


import java.io.File;
import java.util.Arrays;

import org.junit.Assert;
import org.w3c.dom.Document;

import com.delcyon.capo.controller.elements.GroupElement;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.StreamFormat;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.StreamType;
import com.delcyon.capo.resourcemanager.ResourceDescriptorTest;
import com.delcyon.capo.tests.util.TestServer;

public class RefResourceDescriptorTest extends ResourceDescriptorTest
{

    @Override
    protected ResourceDescriptor getResourceDescriptor() throws Exception
    {  
        //load element
        Document configDocument = TestServer.getServerInstance().getDocumentBuilder().parse(new File("capo/server/config/config.xml"));
        GroupElement groupElement = new GroupElement();
        groupElement.setControlElementDeclaration(configDocument.getDocumentElement());
        //set context
        
        ResourceDescriptor resourceDescriptor =  TestServer.getServerInstance().getApplication().getDataManager().getResourceDescriptor(null, "ref:.");
        ((RefResourceDescriptor)resourceDescriptor).setContextControlElement(groupElement);
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
      Assert.assertTrue("Expected Stream types are not the same",Arrays.equals(new ResourceDescriptor.StreamType[]{StreamType.INPUT},this.resourceDescriptor.getSupportedStreamTypes()));
    }

    @Override
    public void testIsSupportedStreamType() throws Exception
    {
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamType(StreamType.ERROR) == false);
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamType(StreamType.INPUT));
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamType(StreamType.OUTPUT) == false);
    }

    @Override
    public void testGetSupportedStreamFormats() throws Exception
    {
        Assert.assertArrayEquals("Expected Stream formats are not correct for "+StreamType.INPUT+" streamType actual:"+Arrays.toString(this.resourceDescriptor.getSupportedStreamFormats(StreamType.INPUT)),new ResourceDescriptor.StreamFormat[]{StreamFormat.XML_BLOCK},this.resourceDescriptor.getSupportedStreamFormats(StreamType.INPUT));
        Assert.assertArrayEquals("Expected Stream formats are not correct for "+StreamType.OUTPUT+" streamType actual:"+Arrays.toString(this.resourceDescriptor.getSupportedStreamFormats(StreamType.OUTPUT)),null,this.resourceDescriptor.getSupportedStreamFormats(StreamType.OUTPUT));
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
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.OUTPUT, StreamFormat.PROCESS) == false);
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.OUTPUT, StreamFormat.STREAM) == false);
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.OUTPUT, StreamFormat.XML_BLOCK) == false);
        
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.ERROR, StreamFormat.BLOCK) == false);
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.ERROR, StreamFormat.PROCESS) == false);
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.ERROR, StreamFormat.STREAM) == false);
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.ERROR, StreamFormat.XML_BLOCK) == false);
    }

    

}
