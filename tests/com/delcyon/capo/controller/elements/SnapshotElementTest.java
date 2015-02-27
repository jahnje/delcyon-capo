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


import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayDeque;
import java.util.Arrays;

import org.junit.After;
import org.junit.Test;
import org.w3c.dom.Document;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.LocalRequestProcessor;
import com.delcyon.capo.tests.util.TestServer;
import com.delcyon.capo.tests.util.Util;

public class SnapshotElementTest
{
   
    @After
    public void teardown() throws Exception
    {        
        TestServer.shutdown();       
    }
    
    

    @Test
    public void takeSnapshot() throws Exception
    {
    	
    	Util.copyTree("test-data/capo", "capo", true, true);
    	Util.deleteTree("testdb");
    	Util.copyTree("test-data/testdb", "testdb", true, true);
    	//Util.copyTree("test-data/parser_test_data/SIMPLE.grammer", "capo/server/resources", true, true);
    	Util.copyTree("test-data/parser_test_data", "capo/server/resources", true, true);
    	
    	
    	//File rootFile = new File(".");
    	//walkTree(rootFile);
    	
    	
        TestServer.start();
        Document document = CapoApplication.getDocumentBuilder().parse(new FileInputStream("test-data/snapshot_element_testdata/snapshot-element-test.xml"));
        LocalRequestProcessor localRequestProcessor = new LocalRequestProcessor();
        localRequestProcessor.process(document);
        
//        Document expectedDocument = CapoApplication.getDocumentBuilder().parse(new FileInputStream("test-data/import_element_tests/import_element_test_output.xml"));
//        Document resultDocument = CapoApplication.getDocumentBuilder().parse(new FileInputStream("capo/server/testImportOutput.xml"));
//        
//        XMLDiff xmlDiff = new XMLDiff();
//        xmlDiff.setIgnoreNamespaceDeclarations(true);
//        xmlDiff.setIgnoreContentDifferences(true);
//        Document xmlDiffDocument = xmlDiff.getDifferences(expectedDocument, resultDocument);
//        
//        if(XMLDiff.EQUALITY.equals(xmlDiffDocument.getDocumentElement().getAttribute(XMLDiff.XDIFF_PREFIX+":"+XMLDiff.XDIFF_ELEMENT_ATTRIBUTE_NAME)) == false)
//        {
//            XPath.dumpNode(xmlDiffDocument.getDocumentElement(), System.err);
//        }
//        Assert.assertEquals(XMLDiff.EQUALITY, xmlDiffDocument.getDocumentElement().getAttribute(XMLDiff.XDIFF_PREFIX+":"+XMLDiff.XDIFF_ELEMENT_ATTRIBUTE_NAME));
    }
    
    private void walkTree(File rootFile) throws Exception
    {
    	ArrayDeque<File> stack = new ArrayDeque<File>(50);
    	System.out.println(getIndentation(stack)+"<"+rootFile.getName()+">");
    	stack.push(rootFile);
    	File currentFile = null;
    	
    	while(stack.isEmpty() == false)
    	{
    		currentFile = getNextChild(stack.peek(),currentFile); 
    		if(currentFile == null) //no more kids
    		{
    			currentFile = stack.pop();
    			System.out.println(getIndentation(stack)+"</"+currentFile.getName()+">");
    		}
    		else if(getNextChild(currentFile, null) != null)//has more/next kids 
    		{
    			System.out.println(getIndentation(stack)+"<"+currentFile.getName()+">");
    			stack.push(currentFile);
    			currentFile = null;//getNextChild(currentFile, null);
    		}
    		else //no kids
    		{
    			System.out.println(getIndentation(stack)+"<"+currentFile.getName()+"/>");
    		}
    	}
    }
   
    private String getIndentation(ArrayDeque stack)
    {
    	StringBuilder builder = new StringBuilder(stack.size());
    	for(int depth =0; depth < stack.size(); depth++)
    	{
    		builder.append("\t");
    	}
    	return builder.toString();
    }
    private File getNextChild(File parent, File child) throws Exception
    {
    	File[] children = parent.listFiles();
    	if(children == null || children.length == 0)
    	{
    		return null;
    	}
    	else
    	{
    		Arrays.sort(children);
    		if(child == null)
    		{
    			return children[0];
    		}
    		for(int index =0; index < children.length;index++)
    		{
    			if(children[index].getCanonicalPath().equals(child.getCanonicalPath()))
    			{
    				if(children.length > index+1)
    				{
    					return children[index+1];
    				}
    				else
    				{
    					return null;
    				}
    			}    					
    		}
    		return null;
    	}
    	
    }
    
    
}
