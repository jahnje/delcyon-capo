/*
 * Unmodified:
 * Copyright (c) 2012. betterFORM Project - http://www.betterform.de
 * Licensed under the terms of BSD License
 */
package com.delcyon.capo.webapp.servlets.resource;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ResourceStreamer
{
	public void stream(HttpServletRequest request, HttpServletResponse response, InputStream inputStream) throws IOException;

	public boolean isAppropriateStreamer(String mimeType);
}
