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
package com.delcyon.capo.controller.elements;


import java.io.FileInputStream;

import org.junit.After;
import org.junit.Test;
import org.w3c.dom.Document;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.LocalRequestProcessor;
import com.delcyon.capo.tests.util.TestServer;
import com.delcyon.capo.tests.util.Util;

public class SearchElementTest
{
   
    @After
    public void teardown() throws Exception
    {        
        TestServer.shutdown();       
    }
    
    

    @Test
    public void simpleTest() throws Exception
    {
    	String test = "jdbc:hsqldb:file:testdb/testdb?user=user\\?&password&\\&\\!systems=\\=";
    	String [] uriSplit = test.split("!(?<!\\\\!)");
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
    	
    	Util.copyTree("test-data/capo", "capo", true, true);
    	Util.copyTree("test-data/testdb", "testdb", true, true);
        TestServer.start();
        Document document = CapoApplication.getDocumentBuilder().parse(new FileInputStream("test-data/test-search.xml"));
        LocalRequestProcessor localRequestProcessor = new LocalRequestProcessor();
        localRequestProcessor.process(document);
    }
    
}
