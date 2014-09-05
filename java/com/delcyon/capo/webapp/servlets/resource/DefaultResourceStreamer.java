package com.delcyon.capo.webapp.servlets.resource;
/*
 * Unmodified:
 * Copyright (c) 2012. betterFORM Project - http://www.betterform.de
 * Licensed under the terms of BSD License
 */
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.delcyon.capo.CapoApplication;



/**
 * Default streamer for scripts, css, images and all other content
 */
public class DefaultResourceStreamer implements ResourceStreamer
{
	public boolean isAppropriateStreamer(String mimeType)
	{
		return (mimeType != null);
	}

	public void stream(HttpServletRequest request, HttpServletResponse response, InputStream inputStream) throws IOException
	{
		byte[] buffer = new byte[2048];

		int length = 0;
		boolean isEOF = false;
		while (isEOF == false)
		{
			try
			{
				isEOF = (length = inputStream.read(buffer)) < 0;
			}
			catch (IOException e)
			{
				throw new IOException("failed to read inputStream", e);
			}

			if (isEOF == false)
			{
				try
				{
					response.getOutputStream().write(buffer, 0, length);
				}
				catch (IOException e)
				{
					// swallow this exception and bail to avoid unnecessary logging; the output stream could be closed/unavailable for any number of reasons that we don't really care about
					CapoApplication.logger.log(Level.INFO, "Write to OutputStream failed; ignoring: \"" + e.getMessage() + "\"");
					isEOF = true;
				}
			}
		}
	}
}
