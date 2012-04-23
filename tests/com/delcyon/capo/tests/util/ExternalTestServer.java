package com.delcyon.capo.tests.util;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.Test;

import com.delcyon.capo.client.CapoClient;

/**
Copyright (c) 2012 Delcyon, Inc.
This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

/**
 * @author jeremiah
 * Starts up the test server in a different class loader to make testing of clients results possible 
 */
public class ExternalTestServer extends ClassLoader
{
	
	private URLClassLoader serverClassLoader;
	private Class testServerClass;

	@SuppressWarnings({ "deprecation", "unchecked" })
	public ExternalTestServer() throws Exception
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

		serverClassLoader = new URLClassLoader(classpathURLs,null);
		testServerClass = serverClassLoader.loadClass("com.delcyon.capo.tests.util.TestServer");
	}
	
	
	@SuppressWarnings({ "unchecked" })
	public  void startServer() throws Exception
	{
		testServerClass.getMethod("start").invoke(null);
	}
	
	@SuppressWarnings({ "unchecked" })
	public  void shutdown() throws Exception
	{
		testServerClass.getMethod("shutdown").invoke(null);
	}
	
	@SuppressWarnings({ "unchecked" })
	public  CopyOnWriteArrayList<Exception> getExceptionList() throws Exception
	{
		return (CopyOnWriteArrayList<Exception>) testServerClass.getMethod("getExceptionList").invoke(null);
	}
	
	@Test
	public void testExternalServer() throws Exception{
		
		ExternalTestServer externalTestServer = new ExternalTestServer();
		externalTestServer.startServer();
		CapoClient.main(new String[]{});
		//Thread.sleep(10000);
		externalTestServer.shutdown();
		CopyOnWriteArrayList<Exception> exceptionList = externalTestServer.getExceptionList();
		if (exceptionList.isEmpty() == false)
		{
			throw exceptionList.get(0);
		}
	}
}
 
 

