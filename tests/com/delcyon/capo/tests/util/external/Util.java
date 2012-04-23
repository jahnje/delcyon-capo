package com.delcyon.capo.tests.util.external;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Vector;

import org.junit.Test;

public class Util
{

    @SuppressWarnings("deprecation")
    public static URLClassLoader getIndependentClassLoader() throws Exception
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

        return new URLClassLoader(classpathURLs,null);
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
        copyTree("capo/server", "temp");
    }

    
    
    
    
    
}
