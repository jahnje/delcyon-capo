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
package com.delcyon.capo.client;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.SocketException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStore.TrustedCertificateEntry;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.HashMap;
import java.util.logging.Level;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.xml.bind.DatatypeConverter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.tanukisoftware.wrapper.WrapperManager;
import org.w3c.dom.Document;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.Configuration;
import com.delcyon.capo.Configuration.PREFERENCE;
import com.delcyon.capo.controller.LocalRequestProcessor;
import com.delcyon.capo.controller.client.ControllerRequest;
import com.delcyon.capo.crypto.CertificateRequest;
import com.delcyon.capo.crypto.CertificateRequest.CertificateRequestType;
import com.delcyon.capo.datastream.StreamHandler;
import com.delcyon.capo.datastream.StreamProcessor;
import com.delcyon.capo.preferences.Preference;
import com.delcyon.capo.preferences.PreferenceInfo;
import com.delcyon.capo.preferences.PreferenceInfoHelper;
import com.delcyon.capo.preferences.PreferenceProvider;
import com.delcyon.capo.protocol.client.CapoConnection;
import com.delcyon.capo.protocol.client.Request;
import com.delcyon.capo.resourcemanager.CapoDataManager;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceParameter;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.Action;
import com.delcyon.capo.resourcemanager.types.FileResourceType;
import com.delcyon.capo.server.CapoServer;
import com.delcyon.capo.tasks.TaskManagerThread;

/**
 * @author jeremiah
 *
 */
@PreferenceProvider(preferences=CapoClient.Preferences.class)
public class CapoClient extends CapoApplication
{
	
	public enum Preferences implements Preference
	{
		
		@PreferenceInfo(arguments={}, defaultValue="false", description="Run The Capo Client as a service [true|false] default is false", longOption="CLIENT_AS_SERVICE", option="CLIENT_AS_SERVICE")
		CLIENT_AS_SERVICE,
		@PreferenceInfo(arguments={}, defaultValue="capo.client.0", description="ID that this server will use when communicating with servers", longOption="CLIENT_ID", option="CLIENT_ID")
		CLIENT_ID,
		@PreferenceInfo(arguments={}, defaultValue="1024", description="Encryption key size", longOption="KEY_SIZE", option="KEY_SIZE")
		KEY_SIZE,
		@PreferenceInfo(arguments={}, defaultValue="36", description="Number of Months before key expires", longOption="KEY_MONTHS_VALID", option="KEY_MONTHS_VALID")
		KEY_MONTHS_VALID;
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
	
	private static final String APPLICATION_DIRECTORY_NAME = "client";
	
	
	private HashMap<String, String> idHashMap = new HashMap<String, String>();
    private boolean isReady = false;
    private boolean interupted = false;

	private boolean isDone = false;
	
	public CapoClient() throws Exception
	{
		super();
	}
	
	@Override
	public Integer start(String[] programArgs)
	{
		try
		{
			init(programArgs);			
			startup(programArgs);			
		} catch (Exception e)
		{
			e.printStackTrace();
			return 1;
		}
		return null;
	}
	
	/**
	 * @param programArgs
	 */
	public static void main(String[] programArgs)
	{
		try
		{		    		   
		    WrapperManager.start( new CapoClient(), programArgs );		    
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}

	protected void init(String[] programArgs) throws Exception
	{
	
		setConfiguration(new Configuration(programArgs));
		if (getConfiguration().hasOption(PREFERENCE.HELP))
		{
			getConfiguration().printHelp();
			System.exit(0);
		}

		//System.setProperty("javax.net.ssl.keyStore", getConfiguration().getValue(PREFERENCE.KEYSTORE));
		System.setProperty("javax.net.ssl.keyStorePassword", getConfiguration().getValue(PREFERENCE.KEYSTORE_PASSWORD));

		setDataManager(CapoDataManager.loadDataManager(getConfiguration().getValue(PREFERENCE.RESOURCE_MANAGER)));
		getDataManager().init();
		
		TaskManagerThread.startTaskManagerThread();
		
		runStartupScript(getConfiguration().getValue(PREFERENCE.STARTUP_SCRIPT));
		this.isReady = true;
	}

	private void runStartupScript(String startupScriptName) throws Exception
	{
		ResourceDescriptor startupScriptFile = getDataManager().getResourceDescriptor(null,startupScriptName);
		startupScriptFile.addResourceParameters(null,new ResourceParameter(FileResourceType.Parameters.PARENT_PROVIDED_DIRECTORY,PREFERENCE.CONFIG_DIR));
		if (startupScriptFile.getContentMetaData(null).exists() == false)
		{
		    startupScriptFile.performAction(null, Action.CREATE);
		    startupScriptFile.close(null);
            startupScriptFile.open(null);
			Document startupDocument = CapoApplication.getDefaultDocument("client_startup.xml");			
			OutputStream startupFileOutputStream = startupScriptFile.getOutputStream(null);
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(new DOMSource(startupDocument), new StreamResult(startupFileOutputStream));			
			startupFileOutputStream.close();
		}
		
		LocalRequestProcessor localRequestProcessor = new LocalRequestProcessor();
		localRequestProcessor.process(CapoApplication.getDocumentBuilder().parse(startupScriptFile.getInputStream(null)));
		startupScriptFile.close(null);
	}
	
	@Override
	protected void startup(String[] programArgs) throws Exception
	{
		start();
		//keep this thread running until the client thread is done. 
		while(isDone == false)
		{
			Thread.sleep(500);			
		}		
	}
	
	@Override
	public void run()
	{
		
		try 
		{
			
		    HashMap<String, String> sessionHashMap = new HashMap<String, String>();
			//get list of RequestProducers
			//get list of ServerResponseConsumers
			//get ordered list of initial requests


			CapoConnection capoConnection = new CapoConnection();


			ControllerRequest controllerRequest = new ControllerRequest(capoConnection.getOutputStream(),capoConnection.getInputStream());
			controllerRequest.setType("update");
			controllerRequest.loadSystemVariables();
			runRequest(capoConnection, controllerRequest,sessionHashMap);
			capoConnection.close();
			
			if (hasValidKeystore() == false)
			{
				//setup keystore
				capoConnection = new CapoConnection();
				setupKeystore(capoConnection);
				capoConnection.close();
			}
			else
			{
				loadKeystore();
			}

			setupSSL();

			//verify identity scripts

			//run identity scripts
			capoConnection = new CapoConnection();
			controllerRequest = new ControllerRequest(capoConnection.getOutputStream(),capoConnection.getInputStream());
            controllerRequest.setType("identity");
            controllerRequest.loadSystemVariables();
			runRequest(capoConnection, controllerRequest,sessionHashMap);
			capoConnection.close();


			capoConnection = new CapoConnection();
			controllerRequest = new ControllerRequest(capoConnection.getOutputStream(),capoConnection.getInputStream());
			//load client variables
			controllerRequest.loadSystemVariables();
			runRequest(capoConnection, controllerRequest,sessionHashMap);
		} catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		isDone  = true;
	}

	
	public void runRequest(CapoConnection capoConnection, Request request, HashMap<String, String> sessionHashMap) throws Exception
	{
	    String initialRequestType = null;
	    if (request instanceof ControllerRequest)
        {
            initialRequestType = ((ControllerRequest) request).getRequestType();
            if (initialRequestType == null)
            {
                initialRequestType = "default";
            }
        }	    
	    else
	    {
	        initialRequestType = request.getClass().getSimpleName();
	    }
		//send request
		try
		{					
			request.send();
		}
		catch (SocketException socketException)
		{
			//do nothing, let any errors be processed later, since there might be a message in the buffer
		}
		boolean isFinished = false;
		
		while(isFinished == false)
		{	
			byte[] buffer = getBuffer(capoConnection.getInputStream());
			//System.out.println(new String(buffer));
			//figure out the kind of response
			StreamProcessor streamProcessor = StreamHandler.getStreamProcessor(buffer);
			if (streamProcessor != null)
			{
				streamProcessor.init(sessionHashMap);
				streamProcessor.processStream(capoConnection.getInputStream(), capoConnection.getOutputStream());
			}
			else
			{
				//if we have no data, then we are finished, otherwise wait, then try again?
				if (buffer.length == 0)
				{
				    
				    
				    
				    CapoApplication.logger.log(Level.INFO, "Nothing left to process, finishing up "+initialRequestType+" request.");
				    
					isFinished = true;
				}
				
			}
		}
	}
	
	
	private byte[] getBuffer(BufferedInputStream inputStream) throws Exception
	{
		int bufferSize = getConfiguration().getIntValue(PREFERENCE.BUFFER_SIZE);
	    byte[] buffer = new byte[bufferSize];
	    inputStream.mark(bufferSize);
	    int bytesRead = inputStream.read(buffer);
	    inputStream.reset();
	    
	    //truncate the buffer so we can do accurate length checks on it
	    //totally pointless, but seems like a good idea at the time
	    if (bytesRead < 0)
	    {
	    	return new byte[0];
	    }
	    else if (bytesRead < bufferSize)
	    {
	    	byte[] shortenedBuffer = new byte[bytesRead];
	    	System.arraycopy(buffer, 0, shortenedBuffer, 0, bytesRead);
	    	return shortenedBuffer;
	    }
	    else
	    {
	    	return buffer;
	    }
	}
	
	@Override
	public String getApplicationDirectoryName()
	{
		return APPLICATION_DIRECTORY_NAME;
	}

	@Override
	public boolean isReady()
	{
	    return this.isReady;
	}
	
	public void clearIDMap()
	{
		idHashMap.clear();		
	}

	public void setID(String name, String value)
	{
		idHashMap.put(name, value);
		
	}

	public HashMap<String, String> getIDMap()
	{
		return idHashMap;
	}
	
	
	private boolean hasValidKeystore() throws Exception
	{
		boolean keyStoreIsValid = true;
		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		ResourceDescriptor keystoreFile = getDataManager().getResourceDescriptor(null,getConfiguration().getValue(PREFERENCE.KEYSTORE));
		keystoreFile.addResourceParameters(null,new ResourceParameter(FileResourceType.Parameters.PARENT_PROVIDED_DIRECTORY,PREFERENCE.CONFIG_DIR));
		if (keystoreFile.getContentMetaData(null).exists() == false)
		{
			return false;
		}
		
		char[] password = getConfiguration().getValue(PREFERENCE.KEYSTORE_PASSWORD).toCharArray();
		InputStream keyStoreFileInputStream = keystoreFile.getInputStream(null); 
		keyStore.load(keyStoreFileInputStream, password);		
		keyStoreFileInputStream.close();
		String clientID = getConfiguration().getValue(CapoClient.Preferences.CLIENT_ID);
		if (keyStore.containsAlias(clientID+".private") == false)
		{
			return false;
		}
		
		
		
		return keyStoreIsValid;
	}
	
	private void loadKeystore() throws Exception
	{
		
		// load the file
		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		ResourceDescriptor keystoreFile = getDataManager().getResourceDescriptor(null, getConfiguration().getValue(PREFERENCE.KEYSTORE));
		keystoreFile.addResourceParameters(null,new ResourceParameter(FileResourceType.Parameters.PARENT_PROVIDED_DIRECTORY,PREFERENCE.CONFIG_DIR));
		char[] password = getConfiguration().getValue(PREFERENCE.KEYSTORE_PASSWORD).toCharArray();
		InputStream keyStoreFileInputStream = keystoreFile.getInputStream(null);
		keyStore.load(keyStoreFileInputStream, password);
		keyStoreFileInputStream.close();
		setKeyStore(keyStore);
		//System.setProperty("javax.net.ssl.keyStore", keystoreFile.getCanonicalPath());
		System.setProperty("javax.net.ssl.keyStorePassword", getConfiguration().getValue(PREFERENCE.KEYSTORE_PASSWORD));
	}
	
	
	private void setupSSL() throws Exception
	{
				
		// set the ssl context to load using our newly created trustmanager which has our keys in it.
		SSLContext sslContext = SSLContext.getInstance("SSL");

		// initialize a key manager factory with our keystore
		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(getKeyStore(), getConfiguration().getValue(PREFERENCE.KEYSTORE_PASSWORD).toCharArray());
        
     // initialize a trust manager factory with our keystore
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());  
        trustManagerFactory.init(getKeyStore());
        
		sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new java.security.SecureRandom());		
		CapoConnection.sslSocketFactory = sslContext.getSocketFactory();
		
	}
	
	

	private void setupKeystore(CapoConnection capoConnection) throws Exception
	{
		 
		// load the file
		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		ResourceDescriptor keystoreFile = getDataManager().getResourceDescriptor(null, getConfiguration().getValue(PREFERENCE.KEYSTORE));
		keystoreFile.addResourceParameters(null,new ResourceParameter(FileResourceType.Parameters.PARENT_PROVIDED_DIRECTORY,PREFERENCE.CONFIG_DIR));
		char[] password = getConfiguration().getValue(PREFERENCE.KEYSTORE_PASSWORD).toCharArray();
		if (keystoreFile.getContentMetaData(null).exists() == false)
		{
			KeyPairGenerator rsakeyPairGenerator = KeyPairGenerator.getInstance("RSA");
            rsakeyPairGenerator.initialize(getConfiguration().getIntValue(Preferences.KEY_SIZE));
            KeyPair rsaKeyPair = rsakeyPairGenerator.generateKeyPair();
			
            //get certificate from server
            CertificateRequest certificateRequest = new CertificateRequest(capoConnection);
            certificateRequest.setCertificateRequestType(CertificateRequestType.DH);
            certificateRequest.loadDHPhase1();
            certificateRequest.init();
            certificateRequest.send();
            certificateRequest.parseResponse();
            
          //this is where the server assigns our client ID
            String clientID = certificateRequest.getParameter(CertificateRequest.Attributes.CLIENT_ID);
            getConfiguration().setValue(Preferences.CLIENT_ID,clientID);
            
            byte[] certificateEncoding = certificateRequest.getDecryptedPayload();
            
            Certificate certificate = CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(certificateEncoding));
            
            System.out.println("Enter Password:");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String serverPassword = br.readLine();
            certificateRequest.setPayload(serverPassword);
            certificateRequest.setParameter(CertificateRequest.Attributes.CLIENT_PUBLIC_KEY, DatatypeConverter.printBase64Binary(rsaKeyPair.getPublic().getEncoded()));
            certificateRequest.resend();
            
			keyStore.load(null, password);
			
			KeyStore.TrustedCertificateEntry trustedCertificateEntry = new TrustedCertificateEntry(certificate);

			keyStore.setEntry(certificateRequest.getParameter(CertificateRequest.Attributes.SERVER_ID), trustedCertificateEntry,null);
            KeyStore.PrivateKeyEntry privateKeyEntry = new PrivateKeyEntry(rsaKeyPair.getPrivate(), new Certificate[]{certificate});
            
            keyStore.setEntry(getConfiguration().getValue(Preferences.CLIENT_ID)+".private", privateKeyEntry,new KeyStore.PasswordProtection(password));
            if (keystoreFile.getContentMetaData(null).exists() == false)
            {
                keystoreFile.performAction(null, Action.CREATE);
                keystoreFile.close(null);
                keystoreFile.open(null);                
            }
            OutputStream keyStoreFileOutputStream = keystoreFile.getOutputStream(null);
            keyStore.store(keyStoreFileOutputStream, password);
            keyStoreFileOutputStream.close();
            setKeyStore(keyStore);
		}
		else
		{
			loadKeystore();
		}
		
	}

	public void shutdown() throws Exception
	{
		if (TaskManagerThread.getTaskManagerThread() != null)
		{
		    CapoApplication.logger.log(Level.INFO,"Stopping Task Manager");
			TaskManagerThread.getTaskManagerThread().interrupt();
			CapoApplication.logger.log(Level.INFO,"Done Stopping Task Manager");
		}
		
		CapoApplication.logger.log(Level.INFO,"Waiting for processing to finish.");
		while(isDone == false)
		{
			Thread.sleep(500);
		}
		CapoApplication.logger.log(Level.INFO,"Done.");
	}
	
	
}
