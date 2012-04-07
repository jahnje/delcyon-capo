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
package com.delcyon.capo.http;

import java.util.HashMap;

import org.apache.commons.cli.ParseException;

public class SimpleHttpRequest
{
	private HashMap<String, String> requestHeaderHashMap = new HashMap<String, String>();
	private String path = null;
	private String method = null;
	private String version = null;
	private String requestString = null;
	
	public SimpleHttpRequest(String requestString) throws Exception
	{
		this.requestString = requestString;
		String[] lines = requestString.split("\\n");
		for(int currentLine = 0 ; currentLine < lines.length; currentLine++)
		
		{
			String[] split = null;
			if (currentLine == 0)
			{
				split = lines[currentLine].split(" ");
				if (split.length != 3)
				{
					throw new ParseException("Couldn't parse:"+requestString);
				}
				method = split[0].trim();
				path = split[1].trim();
				version = split[2].trim();
			}
			else
			{
				split = lines[currentLine].split(":");
				if (split.length == 2)
				{
					requestHeaderHashMap.put(split[0].trim(), split[1].trim());
				}
			}
			 
		}
		
	}
	
	public String getMethod()
	{
		return method;
	}
	
	public String getPath()
	{
		return path;
	}
	
	public String getRequestString()
	{
		return requestString;
	}
	
	public String getVersion()
	{
		return version;
	}
	
	public HashMap<String, String> getRequestHeaderHashMap()
	{
		return requestHeaderHashMap;
	}
	
	public String getRequestHeader(String header)
	{
		return requestHeaderHashMap.get(header);
	}
	
}
