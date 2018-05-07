package com.delcyon.capo.webapp.servlets.resource;

/*
 * Derived from ResourceServlet.java:
 * Copyright (c) 2012. betterFORM Project - http://www.betterform.de
 * Licensed under the terms of BSD License
 */
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.delcyon.capo.CapoApplication;

public abstract class AbstractResourceServlet extends HttpServlet
{
	
    private static AbstractResourceServlet  resourceServlet = null;

	private boolean caching;	// allow the client to cache resources sent by this servlet? defaults to true but can be overridden by setting a 'caching' init-param with value 'false' in web.xml
	private long oneYear = 31363200000L;
	private String forwarder = null;

	@Override
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		if ("false".equals(config.getInitParameter("caching")))
		{
			caching = false;
			Logger.getGlobal().log(Level.FINEST, "Caching of Resources is disabled");
		}
		else
		{
			caching = true;
			Logger.getGlobal().log(Level.FINEST, "Caching of Resources is enabled - resources are loaded from classpath");
		}

		if(config.getInitParameter("missingResourceServlet") != null)
		{
			forwarder = config.getInitParameter("missingResourceServlet");
		}
		
		initMimeTypes();
		initResourceStreamers();
		if(AbstractResourceServlet.resourceServlet == null)
		{
		    AbstractResourceServlet.resourceServlet = this;
		}
	}

	public static AbstractResourceServlet getResourceServletInstance()
	{
	    return AbstractResourceServlet.resourceServlet;
	}
	
	@SuppressWarnings("rawtypes")
	/**
	 * This can be overridden to if your resource were loaded from a different jar file than the main class's   
	 * @return
	 */
    public Class getResourceClass()
	{
	    return AbstractResourceServlet.class;
	}
	
	/**
	 * define the collection of MimeTypes the servlet will serve up
	 */
	protected abstract void initMimeTypes();
	
	/**
	 * define any ResourceStreamers the servlet will use to stream content 
	 */
	protected abstract void initResourceStreamers();
	
	/**
	 * return a date (as long) to be used for the Last-Modified response header when caching is enabled
	 */
	protected abstract long getLastModifiedValue();
	
	/**
	 * return a MimeType string associated with the given fileExtension if the servlet is set up to stream files of that type
	 * @param fileExtension
	 * @return a MimeType string for the given extension if one can be found, otherwise null
	 */
	protected abstract String getMimeType(String fileExtension);
	
	/**
	 * return the resource path for a given resource request
	 * @param requestURI
	 * @return a String representing the absolute path for the requested resource, validity of path may or may not be checked
	 */
	public abstract String getResourcePath(String requestURI);
	
	/**
	 * Stream the resource with the appropriate ResourceStreamer for mimeType
	 * @param request
	 * @param response
	 * @param mimeType
	 * @param inputStream
	 */
	protected abstract void streamResource(HttpServletRequest request, HttpServletResponse response, String mimeType, InputStream inputStream) throws IOException;
	
	public boolean exists(String requestURI)
	{
	    return (getResourceClass().getResource(getResourcePath(requestURI)) != null);
	}
	/**
	 * Takes a request, attempts to locate the resource requested and stream it to the response 
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		String requestUri = request.getRequestURI();
		Logger.getGlobal().log(Level.FINEST, "Request URI: " + requestUri);
		String resourcePath = getResourcePath(requestUri);
		Logger.getGlobal().log(Level.FINEST, "Resource path: " + resourcePath);
		
		if (resourcePath == null)
		{
			if(forwarder != null)
			{
				request.getRequestDispatcher(forwarder).forward(request, response);
				return;
			}
		    Logger.getGlobal().log(Level.INFO, "resourcePath '" + resourcePath + "' does not map to a valid resource. Sending 'not found' response - ");
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		InputStream inputStream = null;

		inputStream = getResourceClass().getResourceAsStream(resourcePath);
		if (inputStream == null)
		{
			if(forwarder != null)
			{
				request.getRequestDispatcher(forwarder).forward(request, response);
				return;
			}
			if(inputStream == null)
			{
				Logger.getGlobal().log(Level.INFO, "Resource not found at '" + resourcePath + "'. Sending 'not found' response - ");
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
		}
		
		String mimeType = getResourceContentType(resourcePath);
		if (mimeType == null)
		{
		    Logger.getGlobal().log(Level.INFO, "MimeType for '" + resourcePath + "' not found. Sending 'not found' response - ");
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		response.setContentType(mimeType);
		response.setStatus(HttpServletResponse.SC_OK);
		setCaching(request, response);
		
		try
		{
			streamResource(request, response, mimeType, inputStream);
			Logger.getGlobal().log(Level.FINEST, "Resource '" + resourcePath + "' streamed succesfully");
		}
		catch (IOException e)
		{
		    Logger.getGlobal().log(Level.SEVERE, "Read exception occurred while streaming resource; path: " + resourcePath, e);
		}

		if (inputStream != null)
		{
			inputStream.close();
		}

		response.getOutputStream().flush();
		response.getOutputStream().close();
	}

	/**
	 * set the caching headers for the resource response. Caching can be disabled by adding an init-param of 'caching' with value 'false' to web.xml
	 * 
	 * @param request the http servlet request
	 * @param response the http servlet response
	 */
	protected void setCaching(HttpServletRequest request, HttpServletResponse response)
	{
		long now = System.currentTimeMillis();
		if (caching == true)
		{
			response.setHeader("Cache-Control", "max-age=3600, public");
			response.setDateHeader("Date", now);
			response.setDateHeader("Expires", now + this.oneYear);
			response.setDateHeader("Last-Modified", getLastModifiedValue());
		}
		else
		{
			// Set to expire far in the past.
			response.setHeader("Expires", "Sat, 6 May 1995 12:00:00 GMT");
			// Set standard HTTP/1.1 no-cache headers.
			response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
			// Set IE extended HTTP/1.1 no-cache headers (use addHeader).
			response.addHeader("Cache-Control", "post-check=0, pre-check=0");
			// Set standard HTTP/1.0 no-cache header.
			response.setHeader("Pragma", "no-cache");
		}
	}
	
	/**
	 * get Content-Type string (mime type) for the requested file's extension
	 * @param resourcePath
	 * @return
	 */
	protected String getResourceContentType(String resourcePath)
	{
		String resourceFileExtension = getResourceFileExtension(resourcePath);
		return getMimeType(resourceFileExtension);
	}

	/**
	 * get the file extension for file at resourcePath
	 * @param resourcePath
	 * @return
	 */
	protected String getResourceFileExtension(String resourcePath)
	{
		String parsed[] = resourcePath.split("\\.");

		return parsed[parsed.length - 1];
	}
}
