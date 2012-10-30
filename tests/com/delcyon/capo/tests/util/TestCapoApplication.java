package com.delcyon.capo.tests.util;

import java.io.File;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.resourcemanager.types.FileResourceType.Parameters;

public class TestCapoApplication extends CapoApplication
{
    
    public TestCapoApplication() throws Exception
    {
        System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "com.delcyon.capo.xml.cdom.CDocumentBuilderFactory");
        //System.clearProperty("javax.xml.parsers.DocumentBuilderFactory");
        setVariable(Parameters.ROOT_DIR.toString(), new File(".").getCanonicalPath());
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
