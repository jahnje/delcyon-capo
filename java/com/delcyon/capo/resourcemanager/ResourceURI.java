/**
Copyright (c) 2012 Delcyon, Inc.
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
package com.delcyon.capo.resourcemanager;

/**
 * @author jeremiah
 * <a href="http://en.wikipedia.org/wiki/URI_scheme">Based on</a>
 * 
 */
public class ResourceURI
{
	
	private String resourceURIString;

	public ResourceURI(String resourceURI)
	{
		this.resourceURIString = resourceURI;
		String [] uriSplit = resourceURI.split("!(?<!\\\\!)");
    	for (String uriSection : uriSplit)
    	{
    		System.out.println(uriSection);
    	}
    	String[] parameterSectionSplit = uriSplit[0].replaceAll("\\\\(?=!)", "").split("\\?(?<!\\\\\\?)");// now split off the parameter section of the first declaration of the URI
    	for (String uriSection : parameterSectionSplit)
    	{
    		System.out.println("ps==>"+uriSection);
    	}
    	if (parameterSectionSplit.length > 1)
    	{
    		String[] parameterSplit = parameterSectionSplit[1].replaceAll("\\\\(?=\\?)", "").split("&(?<!\\\\&)");// now split off the parameters from parameter section
    		for (String parameter : parameterSplit)
    		{
    			System.out.println("p==>"+parameter);
    			String[] avp = parameter.replaceAll("\\\\(?=&)", "").split("=(?<!\\\\=)");// now split off the parameters from parameter section
    			String parameterName = avp[0].replaceAll("\\\\(?==)", "");
    			String parameterValue = "";
    			if(avp.length > 1)
    			{
    				parameterValue = avp[1].replaceAll("\\\\(?==)", "");
    			}
    			System.out.println("\tname=> '"+parameterName+"'");
    			System.out.println("\tvalue=> '"+parameterValue+"'");
    		}
    	}
	}
	
	public static String getScheme(String capoURIString)
	{
		String scheme = null;
		
		int schemeDeliminatorIndex = capoURIString.indexOf(":");
		if(schemeDeliminatorIndex > 0)
		{
			scheme = capoURIString.substring(0, schemeDeliminatorIndex);
			if(scheme.toLowerCase().matches("[a-z0-9+\\-\\.]+") == false)
			{
				scheme = null;
			}
		}
		return scheme;
	}
	
	public static String getSchemeSpecificPart(String capoURIString)
	{
		String scheme = null;
		String uriRemainder = null;
		int schemeDeliminatorIndex = capoURIString.indexOf(":");
		if(schemeDeliminatorIndex > 0)
		{			
			scheme = capoURIString.substring(0, schemeDeliminatorIndex);
			//verify that what we have is actually a scheme
			if(scheme.toLowerCase().matches("[a-z0-9+\\-\\.]+") == false)
			{
				scheme = null;
			}
			else
			{
				uriRemainder = capoURIString.substring(schemeDeliminatorIndex+1);
			}
		}
		if (scheme == null)
		{
			return capoURIString;
		}
		else
		{
			return uriRemainder;
		}
	}
	
	public static String removeURN(String uriString)
	{
		while(getScheme(uriString) != null)
		{
			uriString = getSchemeSpecificPart(uriString); 
		}
		return uriString;
	}
	
	/**
	 * This is straight out of the Java File URL parsing code. 
	 * It may prove problematic as I'm not sure if this really conforms to the <a href="http://www.w3.org/DesignIssues/Axioms.html#opaque">Opacity Axiom</a>.
	 * Currently if the scheme specific part of the uri does NOT start with a '/' this will return true.
	 * @param resourceURI
	 * @return
	 */
	public static boolean isOpaque(String resourceURI)
	{
		return getSchemeSpecificPart(resourceURI).startsWith("/") == false;
	}

	public static String getAuthroity(String resourceURI)
	{
		if (resourceURI.matches(".+//.+/.+"))
		{
			return resourceURI.replaceFirst(".+//(.+?)/.+", "$1");
		}
		else
		{
			return null;
		}
	}

	public static String getUserInfo(String resourceURI)
	{
		String userInfo = null;
		String authority = getAuthroity(resourceURI);
		if(authority != null && authority.matches(".+@.*"))
		{
			userInfo = authority.replaceFirst("(.+)@.*", "$1");
		}
		return userInfo;
	}
	
	public static String getHostname(String resourceURI)
	{
		String hostname = null;
		String authority = getAuthroity(resourceURI);
		if(authority != null)
		{
			hostname = authority.replaceFirst(".+@", "").replaceFirst(":\\d+", "");
		}
		return hostname;
	}
	
	public static Integer getPort(String resourceURI)
	{
		Integer port = null;
		String authority = getAuthroity(resourceURI);
		if(authority != null && authority.matches(".+:\\d+"))
		{
			port = Integer.parseInt(authority.replaceAll(".+:(\\d+)", "$1"));
		}
		return port;
	}
	
//	public static String getHierarchy(String resourceURI)
//	{
//		String hostname = null;
//		String authority = getAuthroity(resourceURI);
//		if(authority != null)
//		{
//			hostname = authority.replaceFirst(".+@", "").replaceFirst(":\\d+", "");
//		}
//		return hostname;
//	}
//	
//	public static String getQuery(String resourceURI)
//	{
//		String hostname = null;
//		String authority = getAuthroity(resourceURI);
//		if(authority != null)
//		{
//			hostname = authority.replaceFirst(".+@", "").replaceFirst(":\\d+", "");
//		}
//		return hostname;
//	}
//	
//	public static String getPath(String resourceURI)
//	{
//		String hostname = null;
//		String authority = getAuthroity(resourceURI);
//		if(authority != null)
//		{
//			hostname = authority.replaceFirst(".+@", "").replaceFirst(":\\d+", "");
//		}
//		return hostname;
//	}
	
}
