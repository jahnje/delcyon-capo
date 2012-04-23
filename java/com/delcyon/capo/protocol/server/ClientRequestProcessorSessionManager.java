/**
Copyright (C) 2012  Delcyon, Inc.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.delcyon.capo.protocol.server;

import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.preferences.Preference;
import com.delcyon.capo.preferences.PreferenceInfo;
import com.delcyon.capo.preferences.PreferenceInfoHelper;
import com.delcyon.capo.preferences.PreferenceProvider;

/**
 * @author jeremiah
 *
 */
@PreferenceProvider(preferences=ClientRequestProcessorSessionManager.Preferences.class)
public class ClientRequestProcessorSessionManager extends Thread
{
	
	public enum Preferences implements Preference
	{
		
		@PreferenceInfo(arguments={}, defaultValue="5", description="The number of minutes before an inactive session timesout", longOption="DEFAULT_SESSION_TIMEOUT", option="DEFAULT_SESSION_TIMEOUT")
		DEFAULT_SESSION_TIMEOUT
		;
		@Override
		public String[] getArguments()
		{
			return PreferenceInfoHelper.getInfo(this).arguments();
		}

		@Override
		public String getDefaultValue()
		{
			return PreferenceInfoHelper.getInfo(this).defaultValue();
		}

		@Override
		public String getDescription()
		{
			return PreferenceInfoHelper.getInfo(this).description();
		}

		@Override
		public String getLongOption()
		{
			return PreferenceInfoHelper.getInfo(this).longOption();
		}

		@Override
		public String getOption()
		{
		
			return PreferenceInfoHelper.getInfo(this).option();
		}
		
	}
	
	
	private static Random random = new Random();
	private static volatile ConcurrentHashMap<String, ClientRequestProcessorSession> clientRequestProcessorSessionHashtable = new ConcurrentHashMap<String, ClientRequestProcessorSession>();
	
	/**
	 * Allow a client request processor to register under a different session than its own.
	 * @param clientRequestProcessor
	 * @param sessionID
	 */
	public static void registerClientRequestProcessor(ClientRequestProcessor clientRequestProcessor, String sessionID)
	{
		ClientRequestProcessorSession clientRequestProcessorSession = clientRequestProcessorSessionHashtable.get(sessionID);
		if (clientRequestProcessorSession == null)
		{
			clientRequestProcessorSession = new ClientRequestProcessorSession(clientRequestProcessor, sessionID);
			clientRequestProcessorSessionHashtable.put(sessionID,clientRequestProcessorSession);			
		}
		else
		{
			clientRequestProcessorSession.setClientRequestProcessor(clientRequestProcessor);
			
		}
		clientRequestProcessorSession.updateLastAactivityTime();
				
	}

	public static void removeClientRequestProcessor(ClientRequestProcessor clientRequestProcessor)
	{
		removeClientRequestProcessor(clientRequestProcessor.getSessionId());
	}
	
	public static void removeClientRequestProcessor(String sessionID)
	{
		clientRequestProcessorSessionHashtable.remove(sessionID);		
	}
	//SS
	public static ClientRequestProcessor getClientRequestProcessor(String sessionID)
	{
		ClientRequestProcessorSession clientRequestProcessorSession = clientRequestProcessorSessionHashtable.get(sessionID);
		if (clientRequestProcessorSession == null)
		{
			return null;
		}
		else
		{
			clientRequestProcessorSession.updateLastAactivityTime();
			return clientRequestProcessorSession.getClientRequestProcessor();
		}		
	}

	public static String generateSessionID()
	{		
		return System.nanoTime()+""+random.nextLong();
	}

	
	public static void registerClientRequestProcessor(ClientRequestProcessor clientRequestProcessor)
	{
		registerClientRequestProcessor(clientRequestProcessor,clientRequestProcessor.getSessionId());
	}
	

	public ClientRequestProcessorSessionManager()
	{
		super("Client Request Processor Session Manager Thread");
	}
	
	@Override
	public void run()
	{
		while(true)
		{
			try
			{
				
				sleep(1000);
				Set<Entry<String, ClientRequestProcessorSession>>  clientRequestProcessorSessionHashtableEntrySet = clientRequestProcessorSessionHashtable.entrySet();
				for (Entry<String, ClientRequestProcessorSession> entry : clientRequestProcessorSessionHashtableEntrySet)
				{
					if (entry.getValue().isTimedOut())
					{
						CapoApplication.logger.log(Level.INFO, "Removing expired session: "+entry.getKey()+" - "+entry.getValue().getClientRequestProcessor().getClass().getCanonicalName());
						clientRequestProcessorSessionHashtable.remove(entry.getKey());
					}
					else
					{
						//do nothing
					}
				}
			} 
			catch (Exception exception)
			{
				CapoApplication.logger.log(Level.WARNING, "Error in SessionManager", exception);
			}
		}
	}

	
	
	
}