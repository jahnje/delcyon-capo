package com.delcyon.capo.tests.util;

import com.delcyon.capo.CapoApplication;

public class TestCapoApplication extends CapoApplication
{
    
    public TestCapoApplication() throws Exception
    {
        
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
    public boolean isReady()
    {
        // TODO Auto-generated method stub
        return false;
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
    
    public static void  cleanup() 
    {
    	CapoApplication.capoApplication = null;
    }
}