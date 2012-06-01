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
package com.delcyon.capo.server;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStore.TrustedCertificateEntry;
import java.security.Security;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.xml.bind.DatatypeConverter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.tanukisoftware.wrapper.WrapperManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.CapoThreadFactory;
import com.delcyon.capo.Configuration;
import com.delcyon.capo.Configuration.PREFERENCE;
import com.delcyon.capo.controller.LocalRequestProcessor;
import com.delcyon.capo.datastream.BufferedSocket;
import com.delcyon.capo.datastream.SocketFinalizer;
import com.delcyon.capo.datastream.StreamFinalizer;
import com.delcyon.capo.datastream.StreamHandler;
import com.delcyon.capo.datastream.StreamProcessor;
import com.delcyon.capo.preferences.Preference;
import com.delcyon.capo.preferences.PreferenceInfo;
import com.delcyon.capo.preferences.PreferenceInfoHelper;
import com.delcyon.capo.preferences.PreferenceProvider;
import com.delcyon.capo.protocol.server.ClientRequestProcessorSessionManager;
import com.delcyon.capo.resourcemanager.CapoDataManager;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.Action;
import com.delcyon.capo.resourcemanager.ResourceParameter;
import com.delcyon.capo.resourcemanager.types.FileResourceType;
import com.delcyon.capo.tasks.TaskManagerThread;
import com.delcyon.capo.xml.XPath;

/**
 * @author jeremiah
 */
@PreferenceProvider(preferences=CapoServer.Preferences.class)
public class CapoServer extends CapoApplication
{
	
	public enum Preferences implements Preference
	{
		
		@PreferenceInfo(arguments={}, defaultValue="15", description="The number of seconds to tell a client to wait before attempting to connect again", longOption="INITIAL_CLIENT_RETRY_TIME", option="INITIAL_CLIENT_RETRY_TIME")
		INITIAL_CLIENT_RETRY_TIME,
		@PreferenceInfo(arguments={}, defaultValue="600000", description="The number of ms to keep an idle thread alive", longOption="THREAD_IDLE_TIME", option="THREAD_IDLE_TIME")
		THREAD_IDLE_TIME,
		@PreferenceInfo(arguments={}, defaultValue="30", description="The number of concurrent connection to allow", longOption="MAX_THREADPOOL_SIZE", option="MAX_THREADPOOL_SIZE")
		MAX_THREADPOOL_SIZE,
		@PreferenceInfo(arguments={}, defaultValue="10", description="The number of server threads to start with", longOption="START_THREADPOOL_SIZE", option="START_THREADPOOL_SIZE")
		START_THREADPOOL_SIZE,
		@PreferenceInfo(arguments={}, defaultValue="capo.server.0", description="ID that this server will use when communicating with clients", longOption="SERVER_ID", option="SERVER_ID")
		SERVER_ID,
		@PreferenceInfo(arguments={}, defaultValue="1024", description="Encryption key size", longOption="KEY_SIZE", option="KEY_SIZE")
		KEY_SIZE,
		@PreferenceInfo(arguments={}, defaultValue="36", description="Number of Months before key expires", longOption="KEY_MONTHS_VALID", option="KEY_MONTHS_VALID")
		KEY_MONTHS_VALID,
		@PreferenceInfo(arguments={}, defaultValue="clients", description="resource where client information is stored", longOption="CLIENTS_DIR", option="CLIENTS_DIR")
        CLIENTS_DIR;
		@Override
		public String[] getArguments()
		{
			return PreferenceInfoHelper.getInfo(this).arguments();
		}

		@Override
		public String getDefaultValue()
		{
		    return java.util.prefs.Preferences.systemNodeForPackage(CapoApplication.getApplication().getClass()).get(getLongOption(), PreferenceInfoHelper.getInfo(this).defaultValue());			
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
	
	private static final String BC = org.bouncycastle.jce.provider.BouncyCastleProvider.PROVIDER_NAME;
	private static final String APPLICATION_DIRECTORY_NAME = "server";
	
	private ServerSocket serverSocket;
	private boolean attemptSSL = true;
	private boolean isReady = false;
	private boolean isShutdown = false;
	private ThreadPoolExecutor threadPoolExecutor;
	private ClientRequestProcessorSessionManager clientRequestProcessorSessionManager;
    private SSLSocketFactory sslSocketFactory;

	public CapoServer() throws Exception
	{
		super();		
	}
	
	@Override
	public Integer start(String[] programArgs)
	{
		try
		{		    
			CapoServer capoServer = new CapoServer();			
			capoServer.init(programArgs);			
			capoServer.startup(programArgs);			
		} catch (Exception e)
		{
			e.printStackTrace();
			return 1;
		}
		return null;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
		{
		    WrapperManager.start( new CapoServer(), args );
//			CapoServer capoServer = new CapoServer();
//			capoServer.init(args);
//			capoServer.startup(args);
		} catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * @param programArgs
	 * @throws Exception
	 * @throws SecurityException
	 */
	public void init(String[] programArgs) throws SecurityException, Exception
	{
		setConfiguration(new Configuration(programArgs));

		if (getConfiguration().hasOption(PREFERENCE.HELP))
		{
			getConfiguration().printHelp();
			System.exit(0);
		}
		
		

		clientRequestProcessorSessionManager = new ClientRequestProcessorSessionManager();
		clientRequestProcessorSessionManager.start();
		
		//setup resource manager
		setDataManager(CapoDataManager.loadDataManager(getConfiguration().getValue(PREFERENCE.RESOURCE_MANAGER)));
		getDataManager().init();
		
		TaskManagerThread.startTaskManagerThread();	
		
		Security.addProvider(new BouncyCastleProvider());
		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		ResourceDescriptor keystoreFile = getDataManager().getResourceDescriptor(null, getConfiguration().getValue(PREFERENCE.KEYSTORE));
		keystoreFile.addResourceParameters(null,new ResourceParameter(FileResourceType.Parameters.PARENT_PROVIDED_DIRECTORY,PREFERENCE.CONFIG_DIR));
		char[] password = getConfiguration().getValue(PREFERENCE.KEYSTORE_PASSWORD).toCharArray();
		if (keystoreFile.getContentMetaData(null).exists() == false)
		{
            keyStore = buildKeyStore();
		}
		else
		{
		    keystoreFile.open(null);
			InputStream keyStoreFileInputStream = keystoreFile.getInputStream(null);
			keyStore.load(keyStoreFileInputStream, password);
			keyStoreFileInputStream.close();
			keystoreFile.close(null);
		}
		
		setKeyStore(keyStore);
		
		serverSocket = new ServerSocket(getConfiguration().getIntValue(PREFERENCE.PORT));
		
		runStartupScript(getConfiguration().getValue(PREFERENCE.STARTUP_SCRIPT));
		
		this.isReady = true;
	}

	

	private void runStartupScript(String startupScriptName) throws Exception
	{
	    ResourceDescriptor startupScriptFile = getDataManager().getResourceDescriptor(null,startupScriptName);
	    startupScriptFile.addResourceParameters(null,new ResourceParameter(FileResourceType.Parameters.PARENT_PROVIDED_DIRECTORY,PREFERENCE.CONTROLLER_DIR));
		if (startupScriptFile.getContentMetaData(null).exists() == false)
		{
		    startupScriptFile.performAction(null, Action.CREATE);
		    startupScriptFile.close(null);
		    startupScriptFile.open(null);
			
			Document startupDocument = CapoApplication.getDefaultDocument("server_startup.xml");			
			OutputStream startupFileOutputStream = startupScriptFile.getOutputStream(null);
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(new DOMSource(startupDocument), new StreamResult(startupFileOutputStream));			
			startupFileOutputStream.close();
		}
		
		LocalRequestProcessor localRequestProcessor = new LocalRequestProcessor();
		localRequestProcessor.process(CapoApplication.getDocumentBuilder().parse(startupScriptFile.getInputStream(null)));
		
	}

	
	public void shutdown() throws Exception
	{
		logger.log(Level.INFO, "Shuting Down Server");
		isShutdown = true;
		if (serverSocket != null)
		{
			logger.log(Level.INFO, "Closing Socket");
			serverSocket.close();
		}
		
		if (threadPoolExecutor != null)
		{
			logger.log(Level.INFO, "Stopping thread pool");
			threadPoolExecutor.shutdown();
			
			while(threadPoolExecutor.isTerminated() == false)
			{
				logger.log(Level.INFO, "Waiting for thread pool to shutdown...");
				sleep(1000);
			}
		}
		
		if (TaskManagerThread.getTaskManagerThread() != null)
		{
			logger.log(Level.INFO, "Stopping Task Manager");
			TaskManagerThread.getTaskManagerThread().interrupt();
			while(TaskManagerThread.getTaskManagerThread().isAlive())
			{
				logger.log(Level.INFO, "Waiting for Task Manager to shutdown...");
				sleep(1000);
			}
			
		}
		
		if (clientRequestProcessorSessionManager != null)
		{
			logger.log(Level.INFO, "Stopping Session Manager");
			clientRequestProcessorSessionManager.shutdown();
			while(clientRequestProcessorSessionManager.isAlive())
			{
				logger.log(Level.INFO, "Waiting for Session Manager to shutdown...");
				sleep(1000);
			}
		}
		
		while(isReady == true)
		{
			logger.log(Level.INFO, "Waiting for final shutdown...");
			Thread.sleep(100);
		}
		logger.log(Level.INFO, "Server Shutdown");
	}
	
	
	
	/**
	 * Start Server Listening
	 * 
	 * @param programArgs
	 * @throws Exception
	 */
	public void startup(String[] programArgs) throws Exception
	{
		SynchronousQueue<Runnable> synchronousQueue = new SynchronousQueue<Runnable>();
		threadPoolExecutor = new ThreadPoolExecutor(getConfiguration().getIntValue(Preferences.START_THREADPOOL_SIZE), getConfiguration().getIntValue(Preferences.MAX_THREADPOOL_SIZE), getConfiguration().getIntValue(Preferences.THREAD_IDLE_TIME), TimeUnit.MILLISECONDS, synchronousQueue);
		threadPoolExecutor.setThreadFactory(new CapoThreadFactory());
		
		SSLContext sslContext = SSLContext.getInstance("SSL");

		TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());  
		trustManagerFactory.init(getKeyStore());	
		
		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyManagerFactory.init(getKeyStore(), getConfiguration().getValue(PREFERENCE.KEYSTORE_PASSWORD).toCharArray());
		
		sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new java.security.SecureRandom());
		sslSocketFactory = sslContext.getSocketFactory();

		start();
	}

	@Override
    public void run()
	{
	    try
	    {
	        while (true)
	        {

	            logger.log(Level.FINER, "waiting for connection");
	            Socket socket = null;
	            try 
	            {
	                socket = serverSocket.accept();
	            } catch (SocketException socketException)
	            {
	                if (isShutdown == true && serverSocket.isClosed())
	                {
	                    logger.log(Level.INFO, "Shutting down server");
	                    isReady = false;
	                    return;
	                }
	            }
	            logger.log(Level.FINE, "got connection: "+socket);
	            socket = new BufferedSocket(socket);
	            InputStream inputStream = socket.getInputStream();

	            //figure out what kind of socket this is

	            inputStream.mark(getConfiguration().getIntValue(PREFERENCE.BUFFER_SIZE));
	            byte[] buffer = new byte[getConfiguration().getIntValue(PREFERENCE.BUFFER_SIZE)];   
	            inputStream.read(buffer);
	            inputStream.reset();

	            String message = new String(buffer).trim();

	            if (message.matches("CAPO_REQUEST"))
	            {
	                if (threadPoolExecutor.getActiveCount() < threadPoolExecutor.getMaximumPoolSize())
	                {                   
	                    writeOKMessage(inputStream, socket, message, buffer);
	                }
	                else
	                {                   
	                    writeBusyMessage(socket);
	                    continue;
	                }
	            }

	            StreamProcessor streamProcessor = StreamHandler.getStreamProcessor(buffer);
	            String clientID = null;
	            HashMap<String, String> sessionHashMap = new HashMap<String, String>();
	            //if the header is an unknown type assume its SSL and pass it on, since we can't read anything from it
	            if (streamProcessor == null && attemptSSL == true)
	            {
	                try
	                {

	                    socket = sslSocketFactory.createSocket(socket, socket.getLocalAddress().getHostAddress(), socket.getLocalPort(), true);

	                } 
	                catch (Exception exception)
	                {
	                    CapoApplication.logger.log(Level.WARNING, "Unknown Stream Type: "+exception.getMessage());
	                    socket.close();
	                    continue;
	                }

	                //keep the ssl socket so that we can use it's session id for validation
	                SSLSocket sslSocket = (SSLSocket) socket;
	                sslSocket.setUseClientMode(false);

	                //we effectively have a brand new socket, so we have to wrap it as well, so we can reset of checking it's content
	                socket = new BufferedSocket(socket);
	                inputStream = socket.getInputStream();
	                inputStream.mark(getConfiguration().getIntValue(PREFERENCE.BUFFER_SIZE));
	                inputStream.read(buffer);

	                String authMessage = new String(buffer).trim();
	                if (authMessage.matches("AUTH:CID=capo\\.(client|server)\\.\\d+:SIG=[A-F0-9]+.*:.*"))
	                {
	                    logger.finer("SSL SID:"+DatatypeConverter.printHexBinary(sslSocket.getSession().getId()));
	                    if (isValidAuthMessage(authMessage,sslSocket.getSession().getId()) == false)
	                    {
	                        CapoApplication.logger.log(Level.WARNING, "Invalid AUTH attempt "+authMessage+" from: "+socket);
	                        socket.close();
	                        continue;
	                    }
	                    else //got verified AUTH message
	                    {
	                        //the client is waiting for a response so send any single value;
	                        socket.getOutputStream().write(0);
	                        socket.getOutputStream().flush();
	                        //reset to the beginning
	                        inputStream.reset();
	                        //skip ahead to the end of our AUTH message
	                        inputStream.skip(authMessage.length());
	                        //mark the spot
	                        inputStream.mark(getConfiguration().getIntValue(PREFERENCE.BUFFER_SIZE));
	                        //not re-read the buffer from our newly reset and marked location
	                        Arrays.fill(buffer, (byte)0);
	                        inputStream.read(buffer);
	                        //store client id
	                        clientID = authMessage.replaceFirst("AUTH:CID=(capo\\.(client|server)\\.\\d+):SIG=[A-F0-9]+.*:.*", "$1");
	                        sessionHashMap.put("clientID", clientID);

	                        //check for a local client directory
	                        ResourceDescriptor clientResourceDescriptor = getDataManager().getResourceDescriptor(null, "clients:"+clientID);
	                        if (clientResourceDescriptor.getContentMetaData(null).exists() == false)
	                        {
	                            logger.log(Level.INFO, "Creating new clients resource for "+clientID);
	                            clientResourceDescriptor.performAction(null, Action.CREATE,new ResourceParameter(ResourceDescriptor.DefaultParameters.CONTAINER, "true"));
	                            clientResourceDescriptor.close(null);
	                            clientResourceDescriptor.open(null);
	                        }

	                        //check for a client resource directory
	                        ResourceDescriptor clientResourcesResourceDescriptor = clientResourceDescriptor.getChildResourceDescriptor(null,getConfiguration().getValue(PREFERENCE.RESOURCE_DIR));
	                        if (clientResourcesResourceDescriptor.getContentMetaData(null).exists() == false)
	                        {
	                            logger.log(Level.INFO, "Creating new resource dir for "+clientID);
	                            clientResourcesResourceDescriptor.performAction(null, Action.CREATE,new ResourceParameter(ResourceDescriptor.DefaultParameters.CONTAINER, "true"));
	                            clientResourcesResourceDescriptor.close(null);
	                            clientResourcesResourceDescriptor.open(null);
	                        }

	                        //update status information
	                        ResourceDescriptor statusResourceDescriptor = clientResourceDescriptor.getChildResourceDescriptor(null,"status.xml");
	                        Element statusRootElement = null;
	                        if (statusResourceDescriptor.getContentMetaData(null).exists() == false)
	                        {
	                            statusResourceDescriptor.performAction(null, Action.CREATE);
	                            statusRootElement = CapoApplication.getDefaultDocument("status.xml").getDocumentElement();
	                        }
	                        else
	                        {
	                            statusRootElement = getDocumentBuilder().parse(statusResourceDescriptor.getInputStream(null)).getDocumentElement();
	                        }
	                        statusRootElement.setAttribute("lastConnectTime", System.currentTimeMillis()+"");
	                        XPath.dumpNode(statusRootElement, statusResourceDescriptor.getOutputStream(null));
	                        statusResourceDescriptor.close(null);
	                    }


	                    inputStream.reset();

	                    message = new String(buffer).trim();

	                    if (message.matches("CAPO_REQUEST"))
	                    {
	                        if (threadPoolExecutor.getActiveCount() < threadPoolExecutor.getMaximumPoolSize())
	                        {
	                            writeOKMessage(inputStream, socket, message, buffer);
	                        }
	                        else
	                        {
	                            writeBusyMessage(socket);
	                            continue;
	                        }
	                    }                   
	                }

	                streamProcessor = StreamHandler.getStreamProcessor(buffer);

	                //reset the buffer
	                inputStream.reset();
	            }
	            logger.log(Level.FINE, "Request Buffer: '" + new String(buffer) +"'");

	            //we should have a stream handler by this point
	            if (streamProcessor == null)
	            {
	                CapoApplication.logger.log(Level.WARNING, "Unknown Stream Type from "+socket.getRemoteSocketAddress());
	                CapoApplication.logger.log(Level.WARNING, "Unknown Stream Type: "+new String(buffer));
	                socket.close();
	                continue;
	            }

	            StreamHandler streamHandler = new StreamHandler(streamProcessor);
	            StreamFinalizer streamFinalizer = new SocketFinalizer(socket);
	            streamHandler.add(streamFinalizer);
	            streamHandler.init((BufferedInputStream) socket.getInputStream(),socket.getOutputStream(),sessionHashMap);          
	            logger.log(Level.INFO, "Starting a "+streamProcessor.getClass().getSimpleName()+" Stream Handler for "+clientID+"@"+socket.getRemoteSocketAddress());
	            try
	            {
	                threadPoolExecutor.execute(streamHandler);
	            }
	            catch (RejectedExecutionException e) 
	            {
	                writeBusyMessage(socket);
	            }
	        }
	    }
	    catch (Exception exception)
	    {
	        CapoApplication.logger.log(Level.SEVERE, "Exiting due to uncaught exception.",exception);
	    }

    }
	
	private void writeOKMessage(InputStream inputStream,Socket socket, String message,byte[] buffer) throws Exception
	{
		inputStream.skip(message.length());
		inputStream.mark(getConfiguration().getIntValue(PREFERENCE.BUFFER_SIZE));
		socket.getOutputStream().write("OK".getBytes());
		socket.getOutputStream().flush();
		Arrays.fill(buffer, (byte)0);
		inputStream.read(buffer);
		inputStream.reset();
	}
	
	
	private void writeBusyMessage(Socket socket) throws Exception
	{
		String busyString = new String("BUSY "+(getConfiguration().getIntValue(Preferences.INITIAL_CLIENT_RETRY_TIME)*1000));
		logger.log(Level.WARNING, "Rejecting a request from "+socket.getRemoteSocketAddress()+" with "+busyString);
		socket.getOutputStream().write(busyString.getBytes());
		socket.getOutputStream().flush();
		socket.close();
	}
	
	
	private boolean isValidAuthMessage(String authMessage, byte[] sessionID)
	{
		if (authMessage == null)
		{
			return false;
		}
		if (authMessage.matches("AUTH:CID=capo\\.(client|server)\\.\\d+:SIG=[A-F0-9]+.*:.*") == false)
		{
			return false;
		}
		String[] splitAuthMessage = authMessage.split(":|=");
		if (splitAuthMessage.length != 5)
		{
			return false;
		}
		String clientID = splitAuthMessage[2];
		byte[] encodedSignature = DatatypeConverter.parseHexBinary(splitAuthMessage[4]);
		try
		{
			Certificate certificate = getKeyStore().getCertificate(clientID+".cert");
			if (certificate == null)
			{
				return false;
			}
			Signature signature = Signature.getInstance("SHA256withRSA");
			signature.initVerify(certificate);
			signature.update(clientID.getBytes());
			signature.update(sessionID);
			if (signature.verify(encodedSignature) == true)
			{
				return true;
			}
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public boolean isReady()
	{
	    return this.isReady;
	}
	
	@Override
	public String getApplicationDirectoryName()
	{
		return APPLICATION_DIRECTORY_NAME;
	}
	
	/**
	 * This loads an xml file, starts he server, and sends the document as a
	 * request, then returns an array of [requestDocument,responseDocument]
	 * TESTING ONLY
	 * 
	 * @param filename
	 * @return
	 * @throws Exception
	 */


	private KeyStore buildKeyStore() throws Exception
	{
		
		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());		
		char[] password = getConfiguration().getValue(PREFERENCE.KEYSTORE_PASSWORD).toCharArray();
		//generate keys
		KeyPairGenerator rsakeyPairGenerator = KeyPairGenerator.getInstance("RSA");
        rsakeyPairGenerator.initialize(getConfiguration().getIntValue(Preferences.KEY_SIZE));
        KeyPair rsaKeyPair = rsakeyPairGenerator.generateKeyPair();
		
        //begin bouncy castle crap
		X500NameBuilder x500NameBuilder = new X500NameBuilder(BCStyle.INSTANCE);			
		x500NameBuilder.addRDN(BCStyle.CN,getConfiguration().getValue(Preferences.SERVER_ID));

		ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSAEncryption").setProvider(BC).build(rsaKeyPair.getPrivate());

		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, getConfiguration().getIntValue(Preferences.KEY_MONTHS_VALID));
		
		X509v3CertificateBuilder certificateBuilder = new JcaX509v3CertificateBuilder(x500NameBuilder.build(), BigInteger.valueOf(System.currentTimeMillis()), new Date(System.currentTimeMillis() - 50000),calendar.getTime(),x500NameBuilder.build(), rsaKeyPair.getPublic());
		
		X509Certificate certificate = new JcaX509CertificateConverter().setProvider(BC).getCertificate(certificateBuilder.build(contentSigner));
		//end bouncy castle crap
		
		keyStore.load(null, password);
		KeyStore.TrustedCertificateEntry trustedCertificateEntry = new TrustedCertificateEntry(certificate);
		keyStore.setEntry(getConfiguration().getValue(Preferences.SERVER_ID), trustedCertificateEntry,null);
		keyStore.setEntry("capo.server.cert", trustedCertificateEntry,null);
        KeyStore.PrivateKeyEntry privateKeyEntry = new PrivateKeyEntry(rsaKeyPair.getPrivate(), new Certificate[]{certificate});
        keyStore.setEntry(getConfiguration().getValue(Preferences.SERVER_ID)+".private", privateKeyEntry,new KeyStore.PasswordProtection(password));

        
       writeKeyStore(keyStore);
        
        return keyStore;
	}
	
	public synchronized void writeKeyStore(KeyStore keyStore) throws Exception
	{
		ResourceDescriptor keystoreFile = getDataManager().getResourceDescriptor(null,getConfiguration().getValue(PREFERENCE.KEYSTORE));
		keystoreFile.addResourceParameters(null,new ResourceParameter(FileResourceType.Parameters.PARENT_PROVIDED_DIRECTORY,PREFERENCE.CONFIG_DIR));
		char[] password = getConfiguration().getValue(PREFERENCE.KEYSTORE_PASSWORD).toCharArray();
		 //save keystore
        OutputStream keyStoreFileOutputStream = keystoreFile.getOutputStream(null);
        keyStore.store(keyStoreFileOutputStream, password);
        keyStoreFileOutputStream.close();
        keystoreFile.close(null);
	}

	
	
}
