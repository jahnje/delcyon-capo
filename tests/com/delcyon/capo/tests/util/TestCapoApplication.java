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
    protected void start(String[] programArgs) throws Exception
    {
        // TODO Auto-generated method stub

    }

    @Override
    public String getApplicationDirectoryName()
    {       
        return "test";
    }

    @Override
    public boolean isReady()
    {
        // TODO Auto-generated method stub
        return false;
    }

}
