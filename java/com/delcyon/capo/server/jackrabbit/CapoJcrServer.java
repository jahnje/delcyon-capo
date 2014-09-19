package com.delcyon.capo.server.jackrabbit;
import java.util.logging.Level;

import javax.jcr.Repository;

import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.config.RepositoryConfig;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.Configuration;

public class CapoJcrServer implements Runnable
{
	
	private String xml;
	private String dir;
	private RepositoryConfig config;
	private static Repository repository;
	
	public static Repository getRepository()
	{
		return repository;
	}
	
	public CapoJcrServer()
	{
		String configDir = CapoApplication.getConfiguration().getValue(Configuration.PREFERENCE.CONFIG_DIR);
		String capoDir = CapoApplication.getConfiguration().getValue(Configuration.PREFERENCE.CAPO_DIR);
		xml = capoDir+"/"+configDir+"/repository.xml";
		dir = capoDir;
	}
	
	@Override
	public void run()
	{
		try
		{
			config = RepositoryConfig.create(xml, dir);
			CapoJcrServer.repository = RepositoryImpl.create(config);
		} catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}
	
	public void start() throws Exception
	{
		Thread serverThread = new Thread(this, "JackrabbitServerThread");
		serverThread.start();
		CapoApplication.logger.log(Level.INFO, "Waiting for jackrabbit to start");
		while (repository == null)
		{
			Thread.sleep(1000);
		}
		CapoApplication.logger.log(Level.INFO, "Apache Jackrabbit started");
	}


	public void shutdown() throws Exception
	{
		((RepositoryImpl) repository).shutdown();
		
	}
	
}
