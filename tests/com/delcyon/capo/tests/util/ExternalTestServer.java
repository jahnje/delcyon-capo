package com.delcyon.capo.tests.util;
import java.net.URLClassLoader;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.Test;

import com.delcyon.capo.client.CapoClient;
import com.delcyon.capo.tests.util.external.Util;

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
public class ExternalTestServer
{
	
	private URLClassLoader serverClassLoader;
	@SuppressWarnings("rawtypes")
    private Class testServerClass;

	
	public ExternalTestServer() throws Exception
	{
		serverClassLoader = Util.getIndependentClassLoader();
		testServerClass = serverClassLoader.loadClass("com.delcyon.capo.tests.util.TestServer");
	}
	
	
	@SuppressWarnings({ "unchecked" })
	public  void startServer(String... args) throws Exception
	{	   
		testServerClass.getMethod("start",args.getClass()).invoke(null,(Object)args);
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
	public void testExternalServer() throws Exception
	{
		Util.deleteTree("capo");
	    Util.copyTree("test-data/capo", "capo");
	    Util.copyTree("lib", "capo/server/lib");
	    Util.setDefaultPreferences();	    
		ExternalTestServer externalTestServer = new ExternalTestServer();
		externalTestServer.startServer();
		TestClient.start();
		TestClient.shutdown();		
		//CapoClient.main(new String[]{"-CLIENT_AS_SERVICE","false"});
//		CapoClient capoClient = new CapoClient();
//		capoClient.start(new String[]{});
//		capoClient.shutdown();
		//CapoClient.main();
		//Thread.sleep(10000);
		externalTestServer.shutdown();
		
		CopyOnWriteArrayList<Exception> exceptionList = externalTestServer.getExceptionList();
		if (exceptionList.isEmpty() == false)
		{
			throw exceptionList.get(0);
		}
	}
}
 
 

