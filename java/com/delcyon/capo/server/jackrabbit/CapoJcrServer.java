package com.delcyon.capo.server.jackrabbit;
import java.util.HashMap;
import java.util.logging.Level;

import javax.jcr.LoginException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.PropertyDefinitionTemplate;

import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.config.RepositoryConfig;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.Configuration;
import com.delcyon.capo.ContextThread;
import com.delcyon.capo.webapp.servlets.CapoWebApplication;

import eu.webtoolkit.jwt.WApplication;

public class CapoJcrServer implements Runnable
{
	
	private String xml;
	private String dir;
	private RepositoryConfig config;
	private static Repository repository;
    private static Session applicationSession;
	
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
			repository = RepositoryImpl.create(config);
			Session session = createSession();
			NamespaceRegistry namespaceRegistry = session.getWorkspace().getNamespaceRegistry();
			String[] uris = namespaceRegistry.getURIs();
			HashMap<String, String> uriHashMap = new HashMap<>(uris.length);
			for (String uri : uris)
            {
                uriHashMap.put(uri, "");
            }
			if(uriHashMap.containsKey(CapoApplication.SERVER_NAMESPACE_URI) == false)
			{
			    namespaceRegistry.registerNamespace("server", CapoApplication.SERVER_NAMESPACE_URI);
			}
			if(uriHashMap.containsKey(CapoApplication.CLIENT_NAMESPACE_URI) == false)
			{
			    namespaceRegistry.registerNamespace("client", CapoApplication.CLIENT_NAMESPACE_URI);
			}
			if(uriHashMap.containsKey(CapoApplication.RESOURCE_NAMESPACE_URI) == false)
			{
			    namespaceRegistry.registerNamespace("resource", CapoApplication.RESOURCE_NAMESPACE_URI);
			}
			
//			NodeTypeManager nodeTypeManager = session.getWorkspace().getNodeTypeManager();
//			
//			// Create a template for the node type ...
//			PropertyDefinitionTemplate type = nodeTypeManager.createPropertyDefinitionTemplate();
//			type.setName("ns:NodeType");
//			type.setDeclaredSuperTypeNames(new String[]{"nt:resource"});
//			type.setAbstract(true);
//			type.setOrderableChildNodes(true);
//			type.setMixin(true);
//			type.setQueryable(true);
//			
//			
//			nodeTypeManager.registerNodeType(type, true);
			
			session.logout();			
			CapoJcrServer.applicationSession = createSession();
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
		while (applicationSession == null)
        {
            Thread.sleep(1000);
        }
		CapoApplication.logger.log(Level.INFO, "Apache Jackrabbit started");
	}


	public void shutdown() throws Exception
	{
		((RepositoryImpl) repository).shutdown();
		CapoJcrServer.repository = null;
        CapoJcrServer.applicationSession = null;
		
	}

    public static Session getApplicationSession()
    {
        return CapoJcrServer.applicationSession;
    }
	
    public static Session createSession() throws LoginException, RepositoryException
    {
        Session session = repository.login(new SimpleCredentials("admin","admin".toCharArray()));
        CapoApplication.logger.log(Level.FINE, "Opened: T="+Thread.currentThread()+" S="+session);        
        return session;
    }
    
    public static Session getSession() throws Exception
    {
        Session localSession = null;
        
        if(Thread.currentThread() instanceof ContextThread)
        {
            localSession = ((ContextThread)Thread.currentThread()).getSession();
        }
        else if(WApplication.getInstance() != null)
        {
            localSession = ((CapoWebApplication)WApplication.getInstance()).getJcrSession();
        }
        
        if(localSession == null)
        {
            if(CapoApplication.isServer()) //one last ditch effort
            {
                localSession = CapoJcrServer.getApplicationSession();                   
            }
            if(localSession == null)
            {
                throw new Exception("Can't use JCR resources without a Session");
            }
        }
        return localSession;
    }
    
}
