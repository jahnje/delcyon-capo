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

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.delcyon.capo.annotations.DefaultDocumentProvider;
import com.delcyon.capo.annotations.DirectoyProvider;
import com.delcyon.capo.annotations.DirectoyProvider.Location;
import com.delcyon.capo.preferences.Preference;
import com.delcyon.capo.preferences.PreferenceProvider;
import com.delcyon.capo.server.CapoServer;
import com.delcyon.capo.xml.XPath;

/**
 * @author jeremiah
 */
@DirectoyProvider(preferenceName="CONFIG_DIR",preferences=Configuration.PREFERENCE.class)
@DefaultDocumentProvider(directoryPreferenceName="CONFIG_DIR",preferences=Configuration.PREFERENCE.class,name="config.xml")
public class Configuration
{

	private static final String CONFIG_FILENAME = "config.xml";

	public enum PREFERENCE implements Preference
	{
		RETAIN("r", "retain", "overwrite values in configuration file with option from command line", null, null),
		PORT("port", "port", "port to listen on", "8080", new String[] { "portNumber" }),
		SERVER_PORT("sp", "SERVER_PORT", "server port to connect to. default 8080", "8080", new String[] { "portNumber" }),
		SERVER_LIST("sl", "SERVER_LIST", "server addresses. comma seperated. default 127.0.0.1", "127.0.0.1", new String[] { "address" }),
		HELP("h", "help", "print usage", null, null), 
		BUFFER_SIZE("bs", "BUFFER_SIZE", "Stream buffer size, default is 4096. ", "4096", new String[] { "int" }), 
		RESOURCE_MANAGER("rm", "RESOURCE_MANAGER", "class to use as a resource manager (default is com.delcyon.capo.resourcemanager.ResourceManager)", "com.delcyon.capo.resourcemanager.ResourceManager", new String[] { "class name" }),
		DATA_MANAGER_ARGUMENTS("dma", "DATA_MANAGER_ARGUMENTS", "arguments to pass to data manager, like location of the file store or url of an xml server", "capo-data", new String[] { "uri" }),
		CAPO_DIR("CAPO_DIR", "CAPO_DIR", "main capo directory on local machine", "capo", new String[] { "dir" }),
		CONFIG_DIR("CONFIG_DIR", "CONFIG_DIR", "directory where main config is stored, relative to root data dir", "config", new String[] { "dir" }),
		STATUS_DIR("STATUS_DIR", "STATUS_DIR", "directory where status information is stored, relative to root data dir", "status", new String[] { "dir" }),
		UPDATES_DIR("UPDATES_DIR", "UPDATES_DIR", "directory where updates are stored, relative to root data dir", "updates", new String[] { "dir" }),
		MODULE_DIR("MODULE_DIR", "MODULE_DIR", "directory where modules are stored, relative to root data dir", "modules", new String[] { "dir" }),
		RESOURCE_DIR("RESOURCE_DIR", "RESOURCE_DIR", "directory where misc resources are stored, relative to root data dir", "resources", new String[] { "dir" }),
		CONTROLLER_DIR("CONTROLLER_DIR", "CONTROLLER_DIR", "directory where controller scripts are stored, relative to root data dir", "controller", new String[] { "dir" }),		
		RESPONSE_DIR("RESPONSE_DIR", "RESPONSE_DIR", "directory where responses to requests are stored, relative to root data dir, server only", "responses", new String[] { "dir" }),
		KEYSTORE("KEYSTORE", "KEYSTORE", "location of java keystore default = keystore", "keystore", new String[] { "file" }),
		KEYSTORE_PASSWORD("KEYSTORE_PASSWORD", "KEYSTORE_PASSWORD", "password for java keystore. default is 'password'", "password", new String[] { "password" }),
		MODE("m", "MODE", "mode to run capo in client, server, hybrid. default 'client'", "client", new String[] { "mode" }),
		CLIENT_MODE("cm", "CLIENT_MODE", "what kind of client connection to use, persistant or dynamic. default is dynamic", "dynamic", new String[] { "client_mode" }), 
		STARTUP_SCRIPT("STARTUP_SCRIPT","STARTUP_SCRIPT","Capo formatted XML file, relative to CONFIG_DIR, containing controls to run at startup, before server becomes available.","startup.xml",new String[] { "file" }), 
		UPDATE_SCRIPT("UPDATE_SCRIPT","UPDATE_SCRIPT","Capo formatted XML file, relative to CONFIG_DIR, containing controls to run an update, before client control processing.","update.xml",new String[] { "file" }), 
		;

		private String option;
		private String longOption;
		private boolean hasArgument;
		private String description;
		private String defaultValue;
		private String[] arguments;

		PREFERENCE(String option, String longOption, String description, String defaultValue, String[] arguments)
		{
			this.option = option;
			this.longOption = longOption;
			if (arguments != null && arguments.length > 0)
			{
				this.hasArgument = true;
			}
			else
			{
				this.hasArgument = false;
			}
			this.description = description;
			this.defaultValue = defaultValue;
			this.arguments = arguments;
		}

		@Override
		public String toString()
		{
			return getLongOption();
		}

		public String getOption()
		{
			return option;
		}

		public String getLongOption()
		{
			return longOption;
		}

		public boolean hasArgument()
		{
			return hasArgument;
		}

		public String getDescription()
		{
			return description;
		}

		public String getDefaultValue()
		{
			return defaultValue;
		}

		public String[] getArguments()
		{
			return arguments;
		}

	}

	private CommandLine commandLine;
	private Options options;
	private Document configDocument;
	private DocumentBuilder documentBuilder;
	private File capoConfigFile;
	private HashMap<String, String> preferenceValueHashMap = new HashMap<String, String>();
	private HashMap<String, Preference> preferenceHashMap = new HashMap<String, Preference>();	
	

	public Configuration() throws Exception
	{
		this(null);
	}

	@SuppressWarnings({ "unchecked", "static-access" })
	public Configuration(String[] programArgs) throws Exception
	{

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		documentBuilder = documentBuilderFactory.newDocumentBuilder();
		
		options = new Options();
		// the enum this is a little complicated, but it gives us a nice
		// centralized place to put all of the system parameters
		// and lets us iterate of the list of options and preferences
		PREFERENCE[] preferences = PREFERENCE.values();
		for (PREFERENCE preference : preferences)
		{
			// not the most elegant, but there is no default constructor, but
			// the has arguments value is always there
			OptionBuilder optionBuilder = OptionBuilder.hasArg(preference.hasArgument);
			if (preference.hasArgument == true)
			{
				String[] argNames = preference.arguments;
				for (String argName : argNames)
				{
					optionBuilder = optionBuilder.withArgName(argName);
				}
			}

			optionBuilder = optionBuilder.withDescription(preference.getDescription());
			optionBuilder = optionBuilder.withLongOpt(preference.getLongOption());
			options.addOption(optionBuilder.create(preference.getOption()));
			
			preferenceHashMap.put(preference.toString(), preference);
		}

		//add dynamic options
		
		
		Set<String> preferenceProvidersSet = CapoApplication.getAnnotationMap().get(PreferenceProvider.class.getCanonicalName());
		if (preferenceProvidersSet != null)
		{
			for (String className : preferenceProvidersSet)
			{
				Class preferenceClass = Class.forName(className).getAnnotation(PreferenceProvider.class).preferences();
				if (preferenceClass.isEnum())
				{
					Object[] enumObjects = preferenceClass.getEnumConstants();
					for (Object enumObject : enumObjects)
					{
						
						Preference preference = (Preference) enumObject;
						preferenceHashMap.put(preference.toString(), preference);
						boolean hasArgument = false;
						if (preference.getArguments() == null || preference.getArguments().length == 0)
						{
							hasArgument = false;
						}
						else
						{
							hasArgument = true;
						}
						
						OptionBuilder optionBuilder = OptionBuilder.hasArg(hasArgument);
						if (hasArgument == true)
						{
							String[] argNames = preference.getArguments();
							for (String argName : argNames)
							{
								optionBuilder = optionBuilder.withArgName(argName);
							}
						}

						optionBuilder = optionBuilder.withDescription(preference.getDescription());
						optionBuilder = optionBuilder.withLongOpt(preference.getLongOption());
						options.addOption(optionBuilder.create(preference.getOption()));
					}
					
				}				
			}
		}
		
		
		// create parser
		CommandLineParser commandLineParser = new GnuParser();
		this.commandLine = commandLineParser.parse(options, programArgs);
		
		
		
		Preferences systemPreferences = Preferences.systemNodeForPackage(this.getClass());
		String capoDirString = systemPreferences.get(PREFERENCE.CAPO_DIR.longOption,null);
		if (capoDirString == null)
		{
			
			systemPreferences.put(PREFERENCE.CAPO_DIR.longOption, PREFERENCE.CAPO_DIR.defaultValue);
			capoDirString = PREFERENCE.CAPO_DIR.defaultValue;
			try
			{
				systemPreferences.sync();
			} catch (BackingStoreException e)
			{						
				e.printStackTrace();
			}				
		}
		
		File capoDirFile = new File(capoDirString);
		if (capoDirFile.exists() == false)
		{
			capoDirFile.mkdirs();
		}
		File configDir = new File(capoDirFile,CapoApplication.getApplication().getApplicationDirectoryName()+File.separatorChar+PREFERENCE.CONFIG_DIR.defaultValue);
		if (configDir.exists() == false)
		{
			configDir.mkdirs();
		}
			
		capoConfigFile = new File(configDir,CONFIG_FILENAME);
		if (capoConfigFile.exists() == false)
		{
			
			
			Document configDocument = CapoApplication.getDefaultDocument("config.xml");
			
			FileOutputStream configFileOutputStream = new FileOutputStream(capoConfigFile);
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(new DOMSource(configDocument), new StreamResult(configFileOutputStream));			
			configFileOutputStream.close();
		}
		
		configDocument = documentBuilder.parse(capoConfigFile);
		loadPreferences();
		//print out preferences
		//this also has the effect of persisting all of the default values if a values doesn't already exist
		for (PREFERENCE preference : preferences)
		{
			if (getValue(preference) != null)
			{
				CapoApplication.logger.log(Level.CONFIG, preference.longOption+"='"+getValue(preference)+"'");				
			}
		}
		
	}

	public boolean hasOption(Preference preference)
	{
		return commandLine.hasOption(preference.getOption());
	}

	/**
	 * command line values take precedence over saved preferences 
	 * @param preference
	 * @return
	 */
	public String getValue(Preference preference)
	{
		if (commandLine.hasOption(preference.getOption()))
		{
			//if the retain flag is specified persist the option or if the option exists and there is a default value, but no value in the preferences
			if (hasOption(PREFERENCE.RETAIN) || (getPref(preference.getLongOption(), null) == null && preference.getDefaultValue() != null))
			{
				putPref(preference.getLongOption(), commandLine.getOptionValue(preference.getOption()));
				try
				{
					sync();
				} catch (BackingStoreException e)
				{						
					e.printStackTrace();
				}				
			}
			return commandLine.getOptionValue(preference.getOption());
		}
		else
		{
			//if there isn't a preference set and there is a default value then store the default in the preferences
			//this is how we save the initial configuration
			if (getPref(preference.getLongOption(), null) == null && preference.getDefaultValue() != null)
			{
				putPref(preference.getLongOption(), preference.getDefaultValue());
				try
				{
					sync();
				} catch (BackingStoreException e)
				{						
					e.printStackTrace();
				}
			}
			return getPref(preference.getLongOption(), null);
		}
	}

	public void printHelp()
	{
		HelpFormatter formatter = new HelpFormatter();
		formatter.setWidth(200);
		formatter.printHelp("CapoServer", options);
	}

	public int getIntValue(Preference preference)
	{		
		return Integer.parseInt(getValue(preference));
		
	}

	public long getLongValue(Preference preference)
	{
	    return Long.parseLong(getValue(preference));
	}

	
	/**
	 * Encapsulation of XML preferences
	 * @param key
	 * @param defaultValue
	 * @return null, on no value
	 * @throws BackingStoreException 
	 */
	private String getPref(String key,String defaultValue)
	{
			String returnValue = defaultValue;
			if (preferenceValueHashMap.containsKey(key))
			{
				returnValue = preferenceValueHashMap.get(key);
			}			
			return returnValue;	
	}
	
	private void putPref(String key, String value)
	{
		preferenceValueHashMap.put(key, value);
	}
	
	/**
	 * Encapsulation of XML preferences
	 * @param key
	 * @param value
	 * @throws BackingStoreException 
	 */
	private void putXmlPref(String key, String value) throws BackingStoreException
	{
		try
		{
		synchronized (configDocument)
		{
			Element entryElement = (Element) XPath.selectSingleNode(configDocument, "//entry[@key = '"+key+"']");
			if (entryElement != null)
			{
				entryElement.setAttribute("value", value);
			}
			else
			{
				entryElement = configDocument.createElementNS(null, "entry");
				entryElement.setAttribute("key", key);
				entryElement.setAttribute("value", value);
				configDocument.getDocumentElement().appendChild(entryElement);
			}
		}
		}
		catch (Exception exception)
		{
			throw new BackingStoreException(exception.getMessage());
		}
	}
	
	private synchronized void sync() throws BackingStoreException
	{
		try
		{
			Set<Entry<String, String>> preferenceEntrySet = preferenceValueHashMap.entrySet();
			for (Entry<String, String> entry : preferenceEntrySet)
			{
				putXmlPref(entry.getKey(), entry.getValue());
			}
			FileOutputStream configFileOutputStream = new FileOutputStream(capoConfigFile);
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(new DOMSource(configDocument), new StreamResult(configFileOutputStream));			
			configFileOutputStream.close();
		}
		catch (Exception exception)
		{
			throw new BackingStoreException(exception.getMessage());
		}
	}
	
	private void loadPreferences() throws Exception
	{
		NodeList entryNodeList = XPath.selectNodes(configDocument, "//entry");
		for (int nodeIndex = 0; nodeIndex < entryNodeList.getLength(); nodeIndex++)
		{
			Element entryElement = (Element) entryNodeList.item(nodeIndex);
			preferenceValueHashMap.put(entryElement.getAttribute("key"), entryElement.getAttribute("value"));
		}
	}

	@SuppressWarnings("unchecked")
	public Preference[] getDirectoryPreferences()
	{
	    boolean isServer = CapoApplication.getApplication() instanceof CapoServer;
		Vector<Preference> preferenceVector = new Vector<Preference>();
		Set<String> directoryProvidersSet = CapoApplication.getAnnotationMap().get(DirectoyProvider.class.getCanonicalName());
		if (directoryProvidersSet != null)
		{
			for (String className : directoryProvidersSet)
			{
				try
				{
				    Location location = Class.forName(className).getAnnotation(DirectoyProvider.class).location();
				    Class preferenceClass = Class.forName(className).getAnnotation(DirectoyProvider.class).preferences();
                    String preferenceName = Class.forName(className).getAnnotation(DirectoyProvider.class).preferenceName();
				    if (location == Location.BOTH)
				    {					
				        preferenceVector.add((Preference)Enum.valueOf(preferenceClass, preferenceName));
				    }
				    else if (isServer == true && location == Location.SERVER)
				    {
				        preferenceVector.add((Preference)Enum.valueOf(preferenceClass, preferenceName));
				    }
				    else if (isServer == false && location == Location.CLIENT)
				    {
				        preferenceVector.add((Preference)Enum.valueOf(preferenceClass, preferenceName));
				    }
				} catch (ClassNotFoundException classNotFoundException)
				{
					CapoApplication.logger.log(Level.WARNING, "Error getting directory providers",classNotFoundException);
				}
			}
		}
		return preferenceVector.toArray(new Preference[]{});
	}
	
	public DefaultDocumentProvider[] getDefaultDocumentProviders()
	{
		Vector<DefaultDocumentProvider> defaultDocumentProviderVector = new Vector<DefaultDocumentProvider>();
		Set<String> defaultDocumentProviderSet = CapoApplication.getAnnotationMap().get(DefaultDocumentProvider.class.getCanonicalName());
		for (String className : defaultDocumentProviderSet)
		{
			try
			{
			defaultDocumentProviderVector.add(Class.forName(className).getAnnotation(DefaultDocumentProvider.class));
			} catch (ClassNotFoundException classNotFoundException)
			{
				CapoApplication.logger.log(Level.WARNING, "Error getting document providers",classNotFoundException);
			}
			
		}
		return defaultDocumentProviderVector.toArray(new DefaultDocumentProvider[]{});
	}

	public Preference getPreference(String preferenceName)
	{
		return preferenceHashMap.get(preferenceName);
	}

	public void setValue(Preference preference, String value)
	{
		putPref(preference.getLongOption(), value);
		try
		{
			sync();
		} catch (BackingStoreException e)
		{						
			e.printStackTrace();
		}
		
	}

   

	
	
}