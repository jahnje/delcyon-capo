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
package com.delcyon.capo;

import java.net.URL;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.scannotation.AnnotationDB;
import org.scannotation.ClasspathUrlFinder;
import org.w3c.dom.Document;

import com.delcyon.capo.resourcemanager.CapoDataManager;
import com.delcyon.capo.server.CapoServer;
import com.delcyon.capo.util.LeveledConsoleHandler;
import com.delcyon.capo.util.LeveledConsoleHandler.Output;

import eu.medsea.mimeutil.MimeUtil;

/**
 * @author jeremiah
 * This class pieces together the common functionality between the client and the server programs.
 * It takes care of logging, annotation processing, system properties, and always contains a reference to the running application so that decisions can be made based on client or server contexts. 
 */
public abstract class CapoApplication extends ContextThread
{
	public enum DefaultDocument
	{
		default_capo,
		default_response, 
		default_resourceMonitors,
		default_request,
		config
	}
	
	private static CapoApplication capoApplication;
	public static Level LOGGING_LEVEL = Level.CONFIG;
	public static Logger logger = null;
	private static LeveledConsoleHandler leveledConsoleHandler;
	private static FileHandler fileHandler;
	private static String logFileName = null;	
	private Transformer transformer;
	private Map<String, Set<String>> annotaionMap;
	private CopyOnWriteArrayList<Exception> exceptionList;
	private static HashMap<String, String> applicationVariableHashMap = new HashMap<String, String>();
	private static KeyStore keyStore = null;
	private static CapoDataManager dataManager = null;
	private static Configuration configuration = null;
    private static DocumentBuilderFactory documentBuilderFactory;
	
	public CapoApplication() throws Exception
	{
		capoApplication = this;
		System.setProperty("java.util.prefs.syncInterval", "2000000");
		System.setProperty("javax.xml.xpath.XPathFactory", "net.sf.saxon.xpath.XPathFactoryImpl");
		System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
		//System.setProperty("javax.net.debug", "ssl");
		logger = Logger.getLogger(this.getClass().getName());
		logger.setLevel(LOGGING_LEVEL);
		if (leveledConsoleHandler == null)
		{
			leveledConsoleHandler = new LeveledConsoleHandler();
			leveledConsoleHandler.setLevel(LOGGING_LEVEL);
			leveledConsoleHandler.setOutputForLevel(Output.STDERR, Level.FINER);
			logger.setUseParentHandlers(false);
			logger.addHandler(leveledConsoleHandler);			
		}
		logger.log(Level.INFO, "Starting Capo "+getApplication().getApplicationDirectoryName());

		if (fileHandler == null)
		{
			logFileName = getApplication().getApplicationDirectoryName().replaceAll(" ", "_").toLowerCase() + ".log";
			logger.log(Level.FINE, "Opening LogElement File:" + logFileName);
			fileHandler = new FileHandler(logFileName);
			fileHandler.setLevel(LOGGING_LEVEL);
			logger.addHandler(fileHandler);
		}
		
		//setup Mime Type Processing
		MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
		MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.ExtensionMimeDetector");
		documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		
		
		//setup xml output
		TransformerFactory tFactory = TransformerFactory.newInstance();
		transformer = tFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		
		//find annotations
		URL[] urls = ClasspathUrlFinder.findClassPaths();
		AnnotationDB annotationDB = new AnnotationDB();
		
		annotationDB.scanArchives(urls);
		annotaionMap = annotationDB.getAnnotationIndex();
		
	}
	
	protected abstract void init(String[] programArgs) throws Exception;
	
	protected abstract void start(String[] programArgs) throws Exception;
	
	public static Configuration getConfiguration()
	{
	    return configuration;
	}
	
	public static void setConfiguration(Configuration configuration)
	{
	    CapoApplication.configuration = configuration;
	}
	
	public static CapoApplication getApplication()
	{
		return capoApplication;
	}
	
	public static CapoDataManager getDataManager()
	{
	    return dataManager;
	}
	
	public static boolean isServer()
	{
	    return getApplication() instanceof CapoServer;
	}
	
	public static void setDataManager(CapoDataManager dataManager)
	{
	    CapoApplication.dataManager = dataManager;
	}
	
	public static DocumentBuilder getDocumentBuilder() throws Exception
	{
	    return documentBuilderFactory.newDocumentBuilder();
	}
	
	public static Document getDefaultDocument(String defaultDocumentName) throws Exception
	{
		return getDocumentBuilder().parse(ClassLoader.getSystemResource("defaults/"+defaultDocumentName).openStream());
	}
	
	public abstract String getApplicationDirectoryName();

	public static Map<String, Set<String>> getAnnotationMap()
	{
		if (getApplication() != null)
		{
			return getApplication().annotaionMap;
		}
		else
		{
			return null;
		}
		
	}

	public static void setVariable(String varName, String value)
	{
		logger.log(Level.INFO, "Set application var "+varName+" to "+value);
		applicationVariableHashMap.put(varName, value);			
	}

	public static String getVariableValue(String varName)
	{
		return applicationVariableHashMap.get(varName);
	}
	
	public static String removeVariableValue(String varName)
	{
		return applicationVariableHashMap.remove(varName);
	}

	public static void setKeyStore(KeyStore keyStore)
	{
		CapoApplication.keyStore = keyStore;
	}
	
	public static KeyStore getKeyStore()
	{
		return keyStore;
	}
	
	public static byte[] getCeritifcate() throws Exception
	{
		if (keyStore != null)
		{
			return keyStore.getCertificate("capo.server.cert").getEncoded();
		}
		else
		{
			return null;
		}
	}

    public abstract boolean isReady();

    public void setExceptionList(CopyOnWriteArrayList<Exception> exceptionList)
    {
    	this.exceptionList = exceptionList;
    }
    
	public CopyOnWriteArrayList<Exception> getExceptionList()
	{
		return exceptionList;
	}

	
	
}
