package com.delcyon.capo.tests.util;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.Test;

import com.delcyon.capo.CapoApplication.ApplicationState;
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
public class ExternalTestClient extends ClassLoader
{
	
	private URLClassLoader clientClassLoader;
	@SuppressWarnings({"rawtypes" })
	private Class testClientClass;

	public ExternalTestClient() throws Exception
	{
		clientClassLoader = Util.getIndependentClassLoader();
		testClientClass = clientClassLoader.loadClass("com.delcyon.capo.tests.util.TestClient");
	}
	
	
	@SuppressWarnings({ "unchecked" })
	public  void startClient(ApplicationState waitForApplicationState,String... args) throws Exception
	{
	    
	    Class enumClass = clientClassLoader.loadClass("com.delcyon.capo.CapoApplication$ApplicationState");
	    Object object = enumClass.getMethod("valueOf", String.class).invoke(null, waitForApplicationState.toString());
		testClientClass.getMethod("start",enumClass,args.getClass()).invoke(null,object,(Object)args);
	}
	
	@SuppressWarnings({ "unchecked" })
	public  void shutdown() throws Exception
	{
		testClientClass.getMethod("shutdown").invoke(null);
	}
	
	@SuppressWarnings({ "unchecked" })
	public  CopyOnWriteArrayList<Exception> getExceptionList() throws Exception
	{
		return (CopyOnWriteArrayList<Exception>) testClientClass.getMethod("getExceptionList").invoke(null);
	}
	
	@Test
	public void testExternalClient() throws Exception{
		Util.deleteTree("capo");
	    Util.copyTree("test-data/capo", "capo");
		TestServer.start();
		ExternalTestClient externalTestClient = new ExternalTestClient();
		externalTestClient.startClient(ApplicationState.RUNNING);
		
		//Thread.sleep(10000);
		externalTestClient.shutdown();
		TestServer.shutdown();
		CopyOnWriteArrayList<Exception> exceptionList = externalTestClient.getExceptionList();
		if (exceptionList.isEmpty() == false)
		{
			throw exceptionList.get(0);
		}
		
		exceptionList = TestServer.getExceptionList();
		if (exceptionList.isEmpty() == false)
		{
			throw exceptionList.get(0);
		}
		
	}
}
 
 

