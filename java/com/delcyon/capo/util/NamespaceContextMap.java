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
package com.delcyon.capo.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.namespace.NamespaceContext;

/**
 * @author jeremiah
 *
 */
public class NamespaceContextMap implements NamespaceContext
{

	private HashMap<String, String> prefixHashMap = new HashMap<String, String>();
	private HashMap<String, Vector<String>> uriHashMap = new HashMap<String, Vector<String>>();
	
	
	public NamespaceContextMap()
	{
	
	}
	
	public void addNamespace(String prefix,String uri)
	{
		if (prefixHashMap.containsKey(prefix) == false)
		{
			Vector<String> prefixVector = uriHashMap.get(uri);
			if (prefixVector == null)
			{
				prefixVector = new Vector<String>();
				uriHashMap.put(uri,prefixVector);
			}
			prefixVector.add(prefix);
			prefixHashMap.put(prefix, uri);
			
		}
		
	}
	/* (non-Javadoc)
	 * @see javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String)
	 */
	public String getNamespaceURI(String prefix)
	{
		return prefixHashMap.get(prefix);
	}

	/* (non-Javadoc)
	 * @see javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String)
	 */
	public String getPrefix(String namespaceURI)
	{
		if (uriHashMap.containsKey(namespaceURI))
		{
			Vector<String> prefixeVector = uriHashMap.get(namespaceURI);
			if (prefixeVector != null && prefixeVector.size() > 0)
			{
				return prefixeVector.firstElement();
			}
			else
			{
				return null;
			}
		}
		else
		{
			return null;
		}
		
	}

	/* (non-Javadoc)
	 * @see javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public Iterator getPrefixes(String namespaceURI)
	{
		if (uriHashMap.containsKey(namespaceURI))
		{
			Vector<String> prefixeVector = uriHashMap.get(namespaceURI);
			if (prefixeVector != null)
			{
				return prefixeVector.iterator();
			}
			else
			{
				return null;
			}
		}
		return null;
	}

}
