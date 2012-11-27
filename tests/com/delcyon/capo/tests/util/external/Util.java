package com.delcyon.capo.tests.util.external;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Vector;
import java.util.prefs.Preferences;

import org.junit.Assert;
import org.junit.Test;

import com.delcyon.capo.client.CapoClient;
import com.delcyon.capo.server.CapoServer;

public class Util
{

	private static URLClassLoader independentClassLoader = null;
	private static final ClassLoader parentClassLoader = Util.class.getClassLoader(); 
    @SuppressWarnings("deprecation")
    public static URLClassLoader getIndependentClassLoader() throws Exception
    {
    	if(independentClassLoader == null)
    	{
    		
    		Vector<URL> classPathURLVector = new Vector<URL>();
    		classPathURLVector.add(new File("build").toURL());
    		File libFile = new File("lib");
    		File[] libFiles = libFile.listFiles();
    		for (File file : libFiles)
    		{
    			classPathURLVector.add(file.toURL());
    		}
    		URL[] classpathURLs = classPathURLVector.toArray(new URL[]{});

    		independentClassLoader =  new URLClassLoader(classpathURLs,null){
    			@Override
    			protected Class<?> findClass(String name) throws ClassNotFoundException
    			{
    				
    				if(name.startsWith("com.delcyon.capo.xml.cdom."))
    				{
    					//System.out.println("looking for class '"+name+"'");
    					return parentClassLoader.loadClass(name);
    				}
    				if(name.startsWith("com.delcyon.capo.util.CloneControl"))
    				{
    					System.out.println("looking for class '"+name+"'");
    					return parentClassLoader.loadClass(name);
    				}
    				return super.findClass(name);
    			}
    		};
    	}
    	return independentClassLoader;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static void copyTree(String src, String dest) throws Exception
    {
        
        Class utilClass = getIndependentClassLoader().loadClass("com.delcyon.capo.tests.util.Util");
        utilClass.getMethod("copyTree", String.class,String.class,boolean.class,boolean.class).invoke(null, src,dest,true,true);        
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static void deleteTree(String dest) throws Exception
    {       
        Class utilClass = getIndependentClassLoader().loadClass("com.delcyon.capo.tests.util.Util");
        utilClass.getMethod("deleteTree", String.class).invoke(null, dest);
    }
    
    @Test
    public void  testCopyTree() throws Exception
    {
        deleteTree("capo");
        copyTree("test-data/capo","capo");
        Assert.assertTrue("Copied Tree are not the same",areSame("test-data/capo","capo"));
    }

	public static void setDefaultPreferences() throws Exception
	{
		Preferences preferences = Preferences.systemNodeForPackage(CapoServer.class);
		preferences.put("CAPO_DIR", "capo/server");
		preferences.sync();
		preferences = Preferences.systemNodeForPackage(CapoClient.class);
		preferences.put("CAPO_DIR", "capo/client");
		preferences.sync();		
	}

    public static boolean areSame(String src, String dest) throws Exception
    {
        Class utilClass = getIndependentClassLoader().loadClass("com.delcyon.capo.tests.util.Util");
        return (Boolean) utilClass.getMethod("areSame", String.class, String.class).invoke(null, src,dest);
    }
    
    
    
    
}
