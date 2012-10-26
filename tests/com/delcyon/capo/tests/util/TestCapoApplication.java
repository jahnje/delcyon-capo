package com.delcyon.capo.tests.util;

import com.delcyon.capo.CapoApplication;

public class TestCapoApplication extends CapoApplication
{
    
    public TestCapoApplication() throws Exception
    {
        System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "com.delcyon.capo.xml.cdom.CDocumentBuilderFactory");
        //System.clearProperty("javax.xml.parsers.DocumentBuilderFactory");
    }
    
    @Override
    protected void init(String[] programArgs) throws Exception
    {
        // TODO Auto-generated method stub

    }

    @Override
    protected void startup(String[] programArgs) throws Exception
    {
        // TODO Auto-generated method stub

    }

    @Override
    public String getApplicationDirectoryName()
    {       
        return "TestApp";
    }

    

    @Override
    public void shutdown() throws Exception
    {
    	// TODO Auto-generated method stub
    	
    }
    
    @Override
    public Integer start(String[] programArgs)
    {
    	// TODO Auto-generated method stub
    	return null;
    }
    
    
}
