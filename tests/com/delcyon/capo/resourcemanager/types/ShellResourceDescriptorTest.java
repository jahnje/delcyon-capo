package com.delcyon.capo.resourcemanager.types;


import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.delcyon.capo.controller.elements.StepElement;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.LifeCycle;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.State;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.StreamFormat;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.StreamType;
import com.delcyon.capo.resourcemanager.ResourceDescriptorTest;
import com.delcyon.capo.resourcemanager.ResourceParameter;
import com.delcyon.capo.tests.util.TestServer;

public class ShellResourceDescriptorTest extends ResourceDescriptorTest
{
    
    @Override
    protected ResourceDescriptor getResourceDescriptor() throws Exception
    {        
        ResourceDescriptor resourceDescriptor =  TestServer.getServerInstance().getApplication().getDataManager().getResourceDescriptor(null, "shell:/bin/bash -i -l");
        resourceDescriptor.addResourceParameters(null, new ResourceParameter(ShellResourceDescriptor.Parameter.DEBUG,"true"));
        resourceDescriptor.addResourceParameters(null, new ResourceParameter(ShellResourceDescriptor.Parameter.PRINT_BUFFER,"true"));
        resourceDescriptor.addResourceParameters(null, new ResourceParameter(StepElement.Parameters.TIMEOUT,"2"));
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
        Assert.assertArrayEquals("Expected Stream formats are not correct for "+StreamType.INPUT+" streamType actual:"+Arrays.toString(this.resourceDescriptor.getSupportedStreamFormats(StreamType.INPUT)),new ResourceDescriptor.StreamFormat[]{StreamFormat.BLOCK},this.resourceDescriptor.getSupportedStreamFormats(StreamType.INPUT));
        Assert.assertArrayEquals("Expected Stream formats are not correct for "+StreamType.OUTPUT+" streamType actual:"+Arrays.toString(this.resourceDescriptor.getSupportedStreamFormats(StreamType.OUTPUT)),new ResourceDescriptor.StreamFormat[]{StreamFormat.BLOCK},this.resourceDescriptor.getSupportedStreamFormats(StreamType.OUTPUT));
        Assert.assertArrayEquals("Expected Stream formats are not correct for "+StreamType.ERROR+" streamType actual:"+Arrays.toString(this.resourceDescriptor.getSupportedStreamFormats(StreamType.ERROR)),null,this.resourceDescriptor.getSupportedStreamFormats(StreamType.ERROR));
    }

    @Override
    public void testIsSupportedStreamFormat() throws Exception
    {
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.INPUT, StreamFormat.BLOCK) == true);
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.INPUT, StreamFormat.PROCESS) == false);
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.INPUT, StreamFormat.STREAM) == false);
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.INPUT, StreamFormat.XML_BLOCK) == false);
        
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.OUTPUT, StreamFormat.BLOCK) == true);
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.OUTPUT, StreamFormat.PROCESS) == false);
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.OUTPUT, StreamFormat.STREAM) == false);
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.OUTPUT, StreamFormat.XML_BLOCK) == false);
        
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.ERROR, StreamFormat.BLOCK) == false);
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.ERROR, StreamFormat.PROCESS) == false);
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.ERROR, StreamFormat.STREAM) == false);
        Assert.assertTrue(this.resourceDescriptor.isSupportedStreamFormat(StreamType.ERROR, StreamFormat.XML_BLOCK) == false);
    }

    @Test
    public void testWriteBlock() throws Exception
    {
        resourceDescriptor.reset(State.NONE);
        resourceDescriptor.init(null, null, LifeCycle.EXPLICIT, false);
        resourceDescriptor.open(null);
        if (resourceDescriptor.isSupportedStreamFormat(StreamType.OUTPUT,StreamFormat.BLOCK))
        {
            resourceDescriptor.writeBlock(null,"echo 'this is a test'\n".getBytes());
            String data = new String(resourceDescriptor.readBlock(null));
            System.err.println(data);
            //Assert.assertEquals("this is a test", data);
            resourceDescriptor.writeBlock(null,"echo 'this is also a test'\n".getBytes());
            data = new String(resourceDescriptor.readBlock(null));
            System.err.println(data);
            Assert.assertTrue(data.startsWith("this is also a test"));
        }
        
    }

    @Test
    public void testNext() throws Exception
    {
        resourceDescriptor.reset(State.NONE);
        resourceDescriptor.init(null, null, LifeCycle.EXPLICIT, true);
        resourceDescriptor.open(null);
        resourceDescriptor.next(null,new ResourceParameter(StepElement.Parameters.UNTIL,".*\\$ "),new ResourceParameter(StepElement.Parameters.TIMEOUT,"2500"));
        resourceDescriptor.writeBlock(null,"ls -l capo\n".getBytes());
        resourceDescriptor.next(null,new ResourceParameter(StepElement.Parameters.UNTIL,".*\\$ "),new ResourceParameter(StepElement.Parameters.TIMEOUT,"2500"));
        String data = new String(resourceDescriptor.readBlock(null));
        System.err.println(data);
        Assert.assertTrue(data.isEmpty() == false && data.matches("(?sm).*touch.test$.*") == false);
        resourceDescriptor.writeBlock(null,"touch capo/touch.test\n".getBytes());
        resourceDescriptor.writeBlock(null,"ls -l capo\n".getBytes());
        resourceDescriptor.next(null,new ResourceParameter(StepElement.Parameters.UNTIL,".*\\$ "),new ResourceParameter(StepElement.Parameters.TIMEOUT,"2500"));
        String read2 = new String(resourceDescriptor.readBlock(null));
        System.err.println(read2);
        Assert.assertTrue(read2.matches("(?sm).*touch.test$.*"));
    }
    
}
