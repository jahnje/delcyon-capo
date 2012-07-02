package com.delcyon.capo.server;


import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.delcyon.capo.tests.util.TestServer;

public class CapoServerTest
{

	
	
	
	/**
	 * This makes sure that all of the threads have been shutdown
	 * @throws Exception
	 */
	@Test
	public void testShutdown() throws Exception
	{
		
		Map<Thread, StackTraceElement[]> stackTraceMap = Thread.getAllStackTraces();
		Set<Thread> threadSet = stackTraceMap.keySet();
		ThreadGroup ourThreadGroup = Thread.currentThread().getThreadGroup();
		
		//loop through all of the currently running threads in our thread group, and remove any active daemons from the active count. 
		//We only want to count NEW threads started in the server, and previous tests can leave them laying about
		int localDeamonCount = 0;
		for (Thread thread : threadSet)
		{
			if (thread.getThreadGroup().equals(ourThreadGroup))
			{
				
				if (thread.isDaemon())
				{					
					System.out.println("ignoring id="+thread.toString()+" name="+ thread.getName()+" d="+thread.isDaemon()+" a="+thread.isAlive()+""+thread.getThreadGroup().toString());
					localDeamonCount++;
				}
			}
		}
		
		//store our active count before
		int activeCount = Thread.activeCount()-localDeamonCount;
		
		TestServer.start();
		TestServer.shutdown();
		
		//do the same as above to remove any daemon threads from our counting
		stackTraceMap = Thread.getAllStackTraces();
		threadSet = stackTraceMap.keySet();
		

		 localDeamonCount = 0;
		for (Thread thread : threadSet)
		{
			if (thread.getThreadGroup().equals(ourThreadGroup))
			{
				
				if (thread.isDaemon())
				{					
					System.out.println("ignoring id="+thread.toString()+" name="+ thread.getName()+" d="+thread.isDaemon()+" a="+thread.isAlive()+""+thread.getThreadGroup().toString());
					localDeamonCount++;
				}
			}
		}
		
		
		Assert.assertEquals(activeCount, Thread.activeCount()-localDeamonCount);		
		
	}
	
}
