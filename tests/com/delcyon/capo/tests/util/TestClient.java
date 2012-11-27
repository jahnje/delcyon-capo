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
package com.delcyon.capo.tests.util;

import java.util.concurrent.CopyOnWriteArrayList;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.CapoApplication.ApplicationState;
import com.delcyon.capo.client.CapoClient;



/**
 * @author jeremiah
 * This class will start up the server, and wait until it is ready.
 * If you want to start up the test server to test the client, use the ExternalTestServer class.
 *
 */
public class TestClient
{
	private static Thread clientThread = null;
	private static CapoClient capoClient = null;
	private static CopyOnWriteArrayList<Exception> exceptionList = null;
	
	public static void start(ApplicationState waitForApplicationState,final String... args) throws Exception
	{
		
		if (clientThread != null)
		{
			System.err.println("found an existing client" + clientThread);
			shutdown();
			//System.exit(0);
		}
		exceptionList = new CopyOnWriteArrayList<Exception>();
		clientThread = new Thread("TestClient")
		{
			@Override
			public void run()
			{

				try
				{					
					capoClient = new CapoClient();
					capoClient.setExceptionList(exceptionList);
					if (args.length == 0)
					{
					    capoClient.start(new String[]{"-CLIENT_AS_SERVICE","false"});
					}
					else
					{
					    capoClient.start(args);
					}
				}
				catch (Exception e)
				{					
					e.printStackTrace();
				}

			}
		};
		clientThread.start();
		
        
		while(CapoApplication.getApplication() == null || CapoApplication.getApplication().getApplicationState().ordinal() < waitForApplicationState.ordinal())
		{            
			Thread.sleep(1000);
		}
		//capoClient = (CapoClient) CapoApplication.getApplication();
		
	}
	
	
	public static void shutdown() throws Exception
	{
		if (capoClient != null)
		{
		    System.err.println("=====================Calling Client Shutdown========================================");
			capoClient.stop(0);
			capoClient = null;
		}
		if (clientThread != null)
		{
		    clientThread = null;
		}
	}
	
	public static CapoClient getClientInstance()
	{
	    return capoClient;
	}
	
	public static CopyOnWriteArrayList<Exception> getExceptionList()
	{
		return exceptionList;
	}
}
