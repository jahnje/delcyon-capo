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
package com.delcyon.capo.webapp.servlets.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.delcyon.capo.datastream.stream_attribute_filter.MimeTypeFilterInputStream;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;

import eu.webtoolkit.jwt.WResource;
import eu.webtoolkit.jwt.servlet.WebRequest;
import eu.webtoolkit.jwt.servlet.WebResponse;
import eu.webtoolkit.jwt.utils.StreamUtils;

/**
 * @author jeremiah
 *
 */
public class WResourceDescriptor extends WResource
{

	
	
	private ResourceDescriptor resourceDescriptor;

	public WResourceDescriptor(ResourceDescriptor resourceDescriptor)
	{
		this.resourceDescriptor = resourceDescriptor;
	}
	
	@Override
	protected void handleRequest(WebRequest request, WebResponse response) throws IOException
	{
		

		try {
			response.setContentType(resourceDescriptor.getResourceMetaData(null).getValue(MimeTypeFilterInputStream.MIME_TYPE_ATTRIBUTE));	
			
			try {
				StreamUtils.copy(resourceDescriptor.getInputStream(null), response.getOutputStream());
				response.getOutputStream().flush();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				resourceDescriptor.close(null);
			}
		} catch (Exception e) {
			System.err.println("Could not find resource: " + resourceDescriptor.getResourceURI().getBaseURI());
			response.setStatus(404);
		}

	}

}
