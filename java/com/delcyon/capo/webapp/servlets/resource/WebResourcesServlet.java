package com.delcyon.capo.webapp.servlets.resource;
/*
 * Derived from ResourceServlet.java:
 * Copyright (c) 2012. betterFORM Project - http://www.betterform.de
 * Licensed under the terms of BSD License
 */
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author jeremiah
 * init parameters
 * defaultVersion = add version number to path. Probably doesn't really work
 * resourceJARPath = locate of jar file containing data. Only used for caching default = ""
 * resourcePattern = should match servlet request path. default = "/wr/" 
 * resourceFolder = actual internal path of resources. default = "/web_resources/"
 */
public class WebResourcesServlet extends AbstractResourceServlet
{
	private  String resourceJARPath = "";
	
	private  String resourcePattern = "/wr/";
	private  String resourceFolder = "/web_resources/";


	private static Map<String, String> mimeTypes;
	private static List<ResourceStreamer> resourceStreamers;
	private static String defaultVersion;
	private long lastModified = 0;
	
	private ServletConfig servletConfig;
	
	@Override
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		defaultVersion = getInitParameter("defaultVersion");
		if (defaultVersion == null || defaultVersion.trim().length() == 0)
		{
			defaultVersion = "";
		}
		
		resourceJARPath = getInitParameter("resourceJARPath");
		if (resourceJARPath == null || resourceJARPath.trim().length() == 0)
		{
			resourceJARPath = "";
		}
		
		resourcePattern = getInitParameter("resourcePattern");
		if (resourcePattern == null || resourcePattern.trim().length() == 0)
		{
			resourcePattern = "/wr/";
		}
		
		resourceFolder = getInitParameter("resourceFolder");
		if (resourceFolder == null || resourceFolder.trim().length() == 0)
		{
			resourceFolder = "/web_resources/";
		}
		
		Enumeration<String> parameterNames = getInitParameterNames();
		while(parameterNames.hasMoreElements())
		{
			String parameterName = parameterNames.nextElement();
			if(parameterName.startsWith("mimetype+"))
			{
				String key = parameterName.replaceFirst("mimetype\\+", "");
				mimeTypes.put(key, getInitParameter(parameterName));
			}
		}
		
		Logger.getGlobal().log(Level.FINEST, "Using default version: '" + defaultVersion+"'");
		
		servletConfig = config;
		lastModified = getLastModifiedValue();
		
		Logger.getGlobal().log(Level.FINEST, "Using last modified: " + new SimpleDateFormat("MM/dd/yyyy hh:mm a").format(lastModified));
	}
	
	@Override
	protected void initMimeTypes()
	{
		mimeTypes = new HashMap<String, String>();
        mimeTypes.put("css", "text/css");
        mimeTypes.put("js", "text/javascript");
        mimeTypes.put("jpg", "image/jpeg");
        mimeTypes.put("jpeg", "image/jpeg");
        mimeTypes.put("png", "image/png");
        mimeTypes.put("gif", "image/gif");
        mimeTypes.put("gif", "image/gif");
        mimeTypes.put("html", "text/html");
	}

	@Override
	protected void initResourceStreamers()
	{
		resourceStreamers = new ArrayList<ResourceStreamer>();
		resourceStreamers.add(new DefaultResourceStreamer());
	}
	
	@Override
	public String getResourcePath(String requestURI)
	{
		String path = null;
		int queryIndex = requestURI.indexOf("?");
		if (queryIndex != -1)
		{
			requestURI = requestURI.substring(0, queryIndex);
		}
		
		// if the request specifies a particular version (/js/dojo/x.y.z/dojo...) try that first
		if (requestURI.matches("^" + resourcePattern + ".*") == true)
		{
			path = resourceFolder + requestURI.substring(resourcePattern.length());
			
			// if path doesn't map to an actual resource, we'll blank it out and try to find it in defaultVersion below
			if (AbstractResourceServlet.class.getResource(path) == null)
			{
				path = null;
			}
		}
		
		// use defaultVersion to try to locate the resource
		if (path == null)
		{
			path = resourceFolder + defaultVersion + "/" + requestURI.substring(resourcePattern.length());
		}
		
		return path;
	}
	
	/**
	 * Return a MimeType String for the given fileExtension or null if fileExtension is not one we handle here
	 */
	@Override
	protected String getMimeType(String fileExtension)
	{
		return mimeTypes.get(fileExtension);
	}

	/**
	 * Return the lastModified date for our dojo.jar. Used to set response header when caching is enabled (see ResourceServlet.java)
	 */
	@Override
	protected long getLastModifiedValue()
	{
		if (this.lastModified == 0)
		{
		    String realPath = servletConfig.getServletContext().getRealPath(resourceJARPath);
		    if(realPath == null)
		    {
		        return System.currentTimeMillis();
		    }
			File file = new File(realPath);
			if (file.exists() == true)
			{
				this.lastModified = file.lastModified();
			}
		}
		return lastModified;
	}

	/**
	 * Takes an input stream and MimeType string, attempts to locate a suitable ResourceStreamer for the MimeType and, if one is found, uses it to stream the input stream to the response
	 */
	@Override
	protected void streamResource(HttpServletRequest request, HttpServletResponse response, String mimeType, InputStream inputStream) throws IOException
	{
		for (ResourceStreamer streamer : resourceStreamers)
		{
			if (streamer.isAppropriateStreamer(mimeType))
			{
				streamer.stream(request, response, inputStream);
			}
		}
	}	
}