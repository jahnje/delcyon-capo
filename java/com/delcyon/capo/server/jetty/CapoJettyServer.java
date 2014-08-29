/**
Copyright (c) 2014 Delcyon, Inc.
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
package com.delcyon.capo.server.jetty;

import java.security.KeyStore;
import java.util.logging.Level;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.Configuration.PREFERENCE;
import com.delcyon.capo.server.CapoServer;
import com.delcyon.capo.webapp.servlets.CapoWebWTServlet;

import eu.webtoolkit.jwt.ServletInit;


/**
 * @author jeremiah
 *
 */
public class CapoJettyServer implements Runnable
{


	private KeyStore keyStore = null;
	private String keyStorePassowrd = null;
	private Server server = null;
	private int port;
	
	public CapoJettyServer(KeyStore keyStore, String keyStorePassowrd, int port)
	{
		this.keyStore = keyStore;
		this.keyStorePassowrd = keyStorePassowrd;
		this.port = port;
	}
	
	
	@Override
	public void run()
	{
		//mostly copied from http://www.eclipse.org/jetty/documentation/current/embedding-jetty.html
		// === jetty.xml ===
        // Setup Threadpool
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setMaxThreads(500);
 
        // Server
        this.server = new Server();
         // Extra options
        //server.setDumpAfterStart(true);
        //server.setDumpBeforeStop(true);
        server.setStopAtShutdown(true);

    	//Handler Structure
        HandlerCollection handlers = new HandlerCollection();
        
        NCSARequestLog requestLog = new NCSARequestLog();
        requestLog.setFilename("request.log");
        requestLog.setFilenameDateFormat("yyyy_MM_dd");
        requestLog.setRetainDays(90);
        requestLog.setAppend(true);
        requestLog.setExtended(true);
        requestLog.setLogCookies(false);
        requestLog.setLogTimeZone("GMT");
        RequestLogHandler requestLogHandler = new RequestLogHandler();
        requestLogHandler.setRequestLog(requestLog);
        handlers.addHandler(requestLogHandler);
        
        
     // === jetty-https.xml ===
        // SSL Context Factory
        SslContextFactory sslContextFactory = new SslContextFactory();
       
        sslContextFactory.setKeyStore(keyStore);
        sslContextFactory.setKeyStorePassword(keyStorePassowrd);
        sslContextFactory.setKeyManagerPassword(keyStorePassowrd);
        sslContextFactory.setTrustStore(keyStore);
        sslContextFactory.setTrustStorePassword(keyStorePassowrd);
//        sslContextFactory.setExcludeCipherSuites(
//                "SSL_RSA_WITH_DES_CBC_SHA",
//                "SSL_DHE_RSA_WITH_DES_CBC_SHA",
//                "SSL_DHE_DSS_WITH_DES_CBC_SHA",
//                "SSL_RSA_EXPORT_WITH_RC4_40_MD5",
//                "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA",
//                "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA",
//                "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA");
 
        // SSL HTTP Configuration     
        HttpConfiguration https_config = new HttpConfiguration();
        https_config.setSecureScheme("https");
        https_config.setSecurePort(port);
        https_config.setOutputBufferSize(32768);
        https_config.setRequestHeaderSize(8192);
        https_config.setResponseHeaderSize(8192);
        https_config.setSendServerVersion(true);
        https_config.setSendDateHeader(false);       
        https_config.addCustomizer(new SecureRequestCustomizer());
 
        // SSL Connector
        ServerConnector sslConnector = new ServerConnector(server, new SslConnectionFactory(sslContextFactory,"http/1.1"),new HttpConnectionFactory(https_config));
        sslConnector.setPort(port);
        
        server.setConnectors(new Connector[]{sslConnector});
        
        
        // The WebAppContext is the entity that controls the environment in which a web application lives and
        // breathes. In this example the context path is being set to "/" so it is suitable for serving root context
        // requests and then we see it setting the location of the war. A whole host of other configurations are
        // available, ranging from configuring to support annotation scanning in the webapp (through
        // PlusConfiguration) to choosing where the webapp will unpack itself.
        WebAppContext webapp = new WebAppContext();//"capo/server/webapp","/");
        webapp.setContextPath("/");
        webapp.addServlet(DefaultServlet.class, "/img/*");
        webapp.addServlet(CapoWebWTServlet.class, "/*");        
        webapp.setResourceBase(CapoApplication.getDataManager().getResourceDirectory(PREFERENCE.WEB_DIR.toString()).getResourceURI().getResourceURIString());
        webapp.getSessionHandler().getSessionManager().setMaxInactiveInterval(CapoApplication.getConfiguration().getIntValue(CapoServer.Preferences.WEB_SESSION_TIMEOUT));        
        webapp.addEventListener(new ServletInit());
 
        // A WebAppContext is a ContextHandler as well so it needs to be set to the server so it is aware of where to
        // send the appropriate requests.       
        handlers.addHandler(webapp);
        
        
        try
        {
        	server.setHandler(handlers);
        	// Start the server
        	server.start();
        	server.join();
        } catch (Exception exception)
        {
        	exception.printStackTrace();
        	
        }
	}

	public void start() throws Exception
	{
		Thread jettyServerThread = new Thread(this, "JettyServerThread");
		jettyServerThread.start();
		CapoApplication.logger.log(Level.INFO, "Waiting for jetty to start");
		while (server == null || server.isRunning() == false)
		{
			Thread.sleep(1000);
		}
		CapoApplication.logger.log(Level.INFO, "Jetty Started on port "+port);
	}


	public void shutdown() throws Exception
	{
		server.stop();
		
	}
	
}
