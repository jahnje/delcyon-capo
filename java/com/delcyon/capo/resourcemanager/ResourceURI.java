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

import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * @author jeremiah
 * <a href="http://en.wikipedia.org/wiki/URI_scheme">Based on</a>
 * 
 */
public class ResourceURI
{
	//Pattern.compile(regex).split(this, limit)
    private static final Pattern parameterSplitPattern = Pattern.compile("&(?<!\\\\&)|;(?<!\\\\;)");
    private static final Pattern avpSplitPattern = Pattern.compile("=(?<!\\\\=)");
    private static final Pattern avpReplaceAllPattern = Pattern.compile("\\\\((?=&)|(?=;))");
    private static final Pattern avpParameterNameReplaceAllPattern = Pattern.compile("\\\\(?==)");
    private static final Pattern avpParameterValueReplaceAllPattern = Pattern.compile("\\\\(?==)");
    
    
    
	private String resourceURIString = null;
	private String scheme = null;
	private String schemeSpecificPart = null;
	private Boolean opaque = null;
	private String authority = null;
	private String hierarchy = null;
	private String userInfo = null;
	private String hostname = null;
	private Integer port = null;
	private String baseURI = null;
	private String query = null;
	private String fragment = null;
	private String path = null;
	private HashMap<String, String> parameterMap = null;//new HashMap<String, String>(0);
	private ResourceURI childResourceURI = null;
    private Boolean hasHierarchy = null;
	

	/** needed for serialization **/
	private ResourceURI(){};
	
	public ResourceURI(String resourceURI)
	{	    
	    this.baseURI = getBaseURI(resourceURI,this);
	    this.hasHierarchy = hasHierarchy(resourceURI, this);
	    this.hierarchy  = getHierarchy(resourceURI,this);
		this.resourceURIString = resourceURI;
		this.scheme = getScheme(resourceURI,this);
		this.schemeSpecificPart = getSchemeSpecificPart(resourceURI,this);
		this.opaque = isOpaque(resourceURI,this);
		this.authority  = getAuthroity(resourceURI,this);
		
		this.userInfo = getUserInfo(resourceURI,this);
		this.hostname = getHostname(resourceURI,this);
		this.port = getPort(resourceURI,this);
				
		this.query = getQuery(resourceURI,this);
		this.path = getPath(resourceURI,this);
		this.fragment = getFragment(resourceURI,this);
		
		if(getChildURI(resourceURI) != null)
		{
			
			childResourceURI = new ResourceURI(getChildURI(resourceURI));
		}
		
		if (this.query != null)
		{
		
			String[] parameterSplit = parameterSplitPattern.split(query);//, limit)query.split("&(?<!\\\\&)|;(?<!\\\\;)");// now split off the parameters from parameter section
			parameterMap = new HashMap<String, String>(parameterSplit.length);
			for (String parameter : parameterSplit)
			{
			    
				String[] avp = avpSplitPattern.split(avpReplaceAllPattern.matcher(parameter).replaceAll(""));//parameter.replaceAll("\\\\((?=&)|(?=;))", "").split("=(?<!\\\\=)");// now split off the parameters from parameter section
				String parameterName = avpParameterNameReplaceAllPattern.matcher(avp[0]).replaceAll("");//avp[0].replaceAll("\\\\(?==)", "");
				String parameterValue = "";
				if(avp.length > 1)
				{
					parameterValue = avpParameterValueReplaceAllPattern.matcher(avp[1]).replaceAll("");//avp[1].replaceAll("\\\\(?==)", "");
				}
				parameterMap.put(parameterName, parameterValue);
			
			}
		}
	}
	
	
	

    

    public String getResourceURIString()
	{
		return resourceURIString;
	}

	public String getScheme()
	{
		return scheme;
	}

	public String getSchemeSpecificPart()
	{
		return schemeSpecificPart;
	}

	public boolean isOpaque()
	{
		return opaque;
	}

	public String getAuthority()
	{
		return authority;
	}

	public String getHierarchy()
	{
		return hierarchy;
	}

	public String getUserInfo()
	{
		return userInfo;
	}

	public String getHostname()
	{
		return hostname;
	}

	public Integer getPort()
	{
		return port;
	}

	public String getBaseURI()
	{
		return baseURI;
	}

	public String getQuery()
	{
		return query;
	}

	public String getFragment()
	{
		return this.fragment;
	}

	public String getPath()
	{
		return path;
	}

	public HashMap<String, String> getParameterMap()
	{
	    if(parameterMap == null)
	    {
	        parameterMap = new HashMap<String, String>(0);
	    }
		return parameterMap;
	}

	public ResourceURI getChildResourceURI()
	{
		return childResourceURI;
	}

	@Override
	public boolean equals(Object obj)
	{		
		if (obj instanceof ResourceURI)
		{
			return resourceURIString.equals(((ResourceURI) obj).getResourceURIString());
		}
		else
		{
			return false;
		}
	}
	
	@Override
	public String toString()
	{
		return resourceURIString;
	}
	
//================================start static methods==============================================

	public static String getScheme(String resourceURI)
    {
            return getScheme(resourceURI,null);
    }
	
	private static String getScheme(String resourceURI,ResourceURI uri)
	{
	    if(uri != null && uri.scheme != null)
	    {
	        return uri.scheme;
	    }
	    
		String scheme = null;
		
		int schemeDeliminatorIndex = resourceURI.indexOf(":");
		if(schemeDeliminatorIndex > 0)
		{
			scheme = resourceURI.substring(0, schemeDeliminatorIndex);
			if(scheme.toLowerCase().matches("[a-z0-9+\\-\\.]+") == false)
			{
				scheme = null;
			}
			else //TODO expensive CPU wise when reading a file system
			{
			    scheme = scheme.intern();
			}
			
		}
		return scheme;
	}
	
	
	//Pattern.compile(regex).split(this, limit)
    private static final Pattern schemeSpecificPartMatchPattern = Pattern.compile("[a-z0-9+\\-\\.]+");
    
    public static String getSchemeSpecificPart(String resourceURI)
    {
        return getSchemeSpecificPart(resourceURI, null);
    }
    
	private static String getSchemeSpecificPart(String resourceURI,ResourceURI uri)
	{
	    if(uri != null && uri.schemeSpecificPart != null)
        {
            return uri.schemeSpecificPart;
        }
	    
		String scheme = null;
		String uriRemainder = null;
		int schemeDeliminatorIndex = resourceURI.indexOf(':');
		if(schemeDeliminatorIndex > 0)
		{			
			scheme = resourceURI.substring(0, schemeDeliminatorIndex);
			//verify that what we have is actually a scheme
			if(schemeSpecificPartMatchPattern.matcher(scheme.toLowerCase()).matches() == false)//scheme.toLowerCase().matches("[a-z0-9+\\-\\.]+") == false)
			{
				scheme = null;
			}
			else
			{
				uriRemainder = resourceURI.substring(schemeDeliminatorIndex+1);
			}
		}
		if (scheme == null)
		{
			return resourceURI;
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
		return isOpaque(resourceURI, null);
	}
	private static boolean isOpaque(String resourceURI,ResourceURI uri)
    {
	    if(uri != null && uri.opaque != null)
        {
            return uri.opaque;
        }
        return getSchemeSpecificPart(resourceURI,uri).startsWith("/") == false;
    }

	public static String getAuthroity(String resourceURI)
    {
	    return getAuthroity(resourceURI,null);
    }
	
	private static String getAuthroity(String resourceURI,ResourceURI uri)
	{

	    if(uri != null && uri.authority != null)
        {
            return uri.authority;
        }
	    
		String hierarchy = getHierarchy(resourceURI,uri);
		if (hierarchy != null && hierarchy.matches(".+[/:]{0,1}.*"))
		{
			String authority = hierarchy.split("/")[0]; 
			return authority;
		}
		else
		{
			return null;
		}
	}

	public static String getUserInfo(String resourceURI)
    {
	    return getUserInfo(resourceURI,null);
    }
	
	private static String getUserInfo(String resourceURI,ResourceURI uri)
	{
	    if(uri != null && uri.userInfo != null)
        {
            return uri.userInfo;
        }
	    
		String userInfo = null;
		String authority = getAuthroity(resourceURI,uri);
		if(authority != null && authority.matches(".+@.*"))
		{
			userInfo = authority.replaceFirst("(.+)@.*", "$1");
		}
		return userInfo;
	}
	
	public static String getHostname(String resourceURI)
    {
	    return getHostname(resourceURI,null);
    }
	public static String getHostname(String resourceURI,ResourceURI uri)
	{
	    if(uri != null && uri.hostname != null)
        {
            return uri.hostname;
        }
	    
		String hostname = null;
		String authority = getAuthroity(resourceURI,uri);
		if(authority != null)
		{
			hostname = authority.replaceFirst(".+@", "").replaceFirst(":\\d+", "");
		}
		return hostname;
	}
	
	public static Integer getPort(String resourceURI)
    {
	    return getPort(resourceURI, null);
    }
	
	private static Integer getPort(String resourceURI,ResourceURI uri)
	{
	    if(uri != null && uri.port != null)
        {
            return uri.port;
        }
	    
		Integer port = null;
		String authority = getAuthroity(resourceURI,uri);
		if(authority != null && authority.matches(".+:\\d+"))
		{
			port = Integer.parseInt(authority.replaceAll(".+:(\\d+)", "$1"));
		}
		return port;
	}
	
	
	/**
     * This returns the part of the URI with any sub/content URI's removed.
     * Content URI's are deliminated by a '!'. For example 'file:some.jar!something.class'
     * This would return file:some.jar  
     * @param resourceURI
     */
	public static String getBaseURI(String resourceURI)
    {
	    
	    return getBaseURI(resourceURI,null);
        
    }
	
	private static String getBaseURI(String resourceURI, ResourceURI uri)
	{
	    if(uri != null && uri.baseURI != null)
        {
            return uri.baseURI;
        }
		//split on the '!' char
		String baseURI = baseURISplitPattern.split(resourceURI)[0];//resourceURI.split("!(?<!\\\\!)")[0];
		//remove any escape chars that were used in the URL
		return baseURIReplaceAllPattern.matcher(baseURI).replaceAll("");//baseURI.replaceAll("\\\\(?=!)", "");
	}
	

    private static final Pattern baseURISplitPattern = Pattern.compile("!(?<!\\\\!)");
    private static final Pattern baseURIReplaceAllPattern = Pattern.compile("\\\\(?=!)");
	
	public static String getChildURI(String resourceURI)
    {
	    
        //split on the '!' char
	    String[] splitURI = resourceURI.split("!(?<!\\\\!)");
	    if(splitURI.length == 1)
	    {
	        return null;
	    }
	    String childURI = "";
        for(int index = 1; index < splitURI.length;index++)
        {
            childURI+= splitURI[index]+"!";
        }
        return childURI.substring(0,childURI.length()-1);
    }
	
	
    private static final Pattern hasHierarchySplitPattern = Pattern.compile("\\?(?<!\\\\\\?)");
    private static final Pattern hasHierarchyMatchesPattern = Pattern.compile(".+://.+/.*");
	
    public static boolean hasHierarchy(String resourceURI)
    {
        return hasHierarchy(resourceURI,null);
    }
    
	private static boolean hasHierarchy(String resourceURI,ResourceURI uri)
	{
	    if(uri != null && uri.hasHierarchy  != null)
        {
            return uri.hasHierarchy;
        }
	    
		String hierarchy = hasHierarchySplitPattern.split(getBaseURI(resourceURI,uri))[0];//getBaseURI(resourceURI).split("\\?(?<!\\\\\\?)")[0];
		if (hasHierarchyMatchesPattern.matcher(hierarchy).matches())//hierarchy.matches(".+://.+/.*"))
		{
			return true;
		}
		else
		{
			return false;
		}
		
	}
	
	public static String getHierarchy(String resourceURI)
	{
	    return getHierarchy(resourceURI,null);
	}
	
	private static String getHierarchy(String resourceURI,ResourceURI uri)
	{
		if(hasHierarchy(resourceURI,uri) == false)
		{
			return null;
		}
		
		if(uri != null && uri.hierarchy != null)
        {
            return uri.hierarchy;
        }
		
		String hierarchy = getBaseURI(resourceURI,uri).split("\\?(?<!\\\\\\?)")[0];// now split off the parameter section of the first declaration of the URI
		return getSchemeSpecificPart(hierarchy,uri).replaceFirst("//(.*)", "$1");
	}
	
	public static String getQuery(String resourceURI)
    {
	    return getQuery(resourceURI,null);
    }

	private static String getQuery(String resourceURI,ResourceURI uri)
	{
	    if(uri != null && uri.query != null)
        {
            return uri.query;
        }
	    
		String query = null;
		String[] querySplit = getBaseURI(resourceURI,uri).split("\\?(?<!\\\\\\?)");
		if(querySplit.length > 1)
		{
			query = querySplit[1].replaceAll("\\\\(?=\\?)", "");
			if (query.matches(".+#.*"))
			{
				query = query.split("#(?<!\\\\#)")[0].replaceAll("\\\\(?=#)", "");
			}
		}
		return query;
	}
	
	public static String getFragment(String resourceURI)
    {
	    return getFragment(resourceURI,null);
    }
	
	private static String getFragment(String resourceURI,ResourceURI uri)
	{
	    
	    if(uri != null && uri.fragment != null)
        {
            return uri.fragment;
        }
	    
		String fragment = null;
		String[] querySplit = getBaseURI(resourceURI,uri).split("\\?(?<!\\\\\\?)");
		if(querySplit.length > 1)
		{
			fragment = querySplit[1].replaceAll("\\\\(?=\\?)", "");
			if (fragment.matches(".+#.+"))
			{
				fragment = fragment.split("#(?<!\\\\#)")[1].replaceAll("\\\\(?=#)", "");
			}
			else
			{
				fragment = null;
			}
		}
		return fragment;
	}
	
	private static final Pattern pathSplitPattern = Pattern.compile("\\?(?<!\\\\\\?)");
    private static final Pattern pathReplaceAllPattern1 = Pattern.compile("([^/]*:)?(.+)");//strip off urn
    private static final Pattern pathReplaceAllPattern2 = Pattern.compile("\\\\(?=:)"); //hmmm... not sure
	
    public static String getPath(String resourceURI)
    {
        return getPath(resourceURI,null);
    }
    
	private static String getPath(String resourceURI,ResourceURI uri)
	{
	    if(uri != null && uri.path != null)
        {
            return uri.path;
        }
	    
		String path = null;
		String baseURI = getBaseURI(resourceURI,uri);
		if(hasHierarchy(baseURI,uri))
		{
			String authority = getAuthroity(baseURI,uri);
			path = getHierarchy(baseURI,uri);
			if (authority != null)
			{
				path = path.substring(authority.length());
			}
		}
		else
		{			
			path = pathSplitPattern.split(getSchemeSpecificPart(getBaseURI(resourceURI,uri),uri))[0];//getSchemeSpecificPart(getBaseURI(resourceURI).split("\\?(?<!\\\\\\?)")[0]);
			int firstSlashIndex = path.indexOf('/');
			if (firstSlashIndex >= 0)//matches(".*/.*")) //see if this path is a urn path or a conventional path
			{
			    int lastIndexOfColon = path.lastIndexOf(":");
			    if (lastIndexOfColon <= firstSlashIndex)
			    {
			        path = path.substring(lastIndexOfColon+1);
			    }
			    else //fall back to regex
			    {
			        path = pathReplaceAllPattern2.matcher(pathReplaceAllPattern1.matcher(path).replaceAll("$2")).replaceAll("");//path.replaceAll(".+:(?<!\\\\:)(.+)", "$1").replaceAll("\\\\(?=:)", "");
			    }
			}			
		}
		
		
		return path;
	}
	
	public static String[] getParts(String resourceURI)
    {
        String[] parts = resourceURI.split("&(?<!\\\\&)|;(?<!\\\\;)|\\?(?<!\\\\\\?)|#(?<!\\\\#)|!(?<!\\\\!)|:(?<!\\\\:)|/(?<!\\\\/)");        
        return parts;
    }
	
	
	
}
