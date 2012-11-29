/**
Copyright (c) 2011 Delcyon, Inc.
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
package com.delcyon.capo.util.diff;


import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.delcyon.capo.util.diff.Diff.Side;
import com.delcyon.capo.util.diff.InputStreamTokenizer.TokenList;
import com.delcyon.capo.xml.XMLDiff;
import com.delcyon.capo.xml.XPath;

/**
 * @author jeremiah
 *
 */
public class XMLDiffTest
{

	

	private Document baseDocument;
	private Document changeDocument;
	private Document baseDiffOnlyDocument;
	private Document changeDiffOnlyDocument;
	private String baseOutputString;
	private String changeOutputString;
	private String baseDiffOnlyOutputString;
	private String changeDiffOnlyOutputString;
	private DocumentBuilder documentBuilder;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
	    System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "com.delcyon.capo.xml.cdom.CDocumentBuilderFactory");
	    //System.clearProperty("javax.xml.parsers.DocumentBuilderFactory");
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		documentBuilder = documentBuilderFactory.newDocumentBuilder();

		
		
		baseDocument = documentBuilder.parse(new File("test-data/diff-testdata/baseDocument.xml"));
		//XPath.dumpNode(baseDocument, System.out);
		System.out.println();
		changeDocument = documentBuilder.parse(new File("test-data/diff-testdata/changeDocument.xml"));
		ByteArrayOutputStream baseDocumentByteArrayOutputStream = new ByteArrayOutputStream();
		ByteArrayOutputStream changeDocumentByteArrayOutputStream = new ByteArrayOutputStream();
		XPath.dumpNode(baseDocument, baseDocumentByteArrayOutputStream);
		XPath.dumpNode(changeDocument, changeDocumentByteArrayOutputStream);
		baseOutputString = baseDocumentByteArrayOutputStream.toString();
		changeOutputString = changeDocumentByteArrayOutputStream.toString();
		System.out.println("============================================================");
		
//		documentBuilder = new CDocumentBuilder();
//		baseDocument = documentBuilder.parse("test-data/diff-testdata/baseDocument.xml");
//		XPath.dumpNode(baseDocument, System.out);
//		System.out.println();
		
		//baseDiffOnlyDocument = documentBuilder.parse("test-data/diff-testdata/baseDiffOnlyDocument.xml");
		//changeDiffOnlyDocument = documentBuilder.parse("test-data/diff-testdata/changeDiffOnlyDocument.xml");
//		ByteArrayOutputStream baseDocumentDiffOnlyByteArrayOutputStream = new ByteArrayOutputStream();
//		ByteArrayOutputStream changeDocumentDiffOnlyByteArrayOutputStream = new ByteArrayOutputStream();
//		XPath.dumpNode(baseDiffOnlyDocument, baseDocumentDiffOnlyByteArrayOutputStream);
//		XPath.dumpNode(changeDiffOnlyDocument, changeDocumentDiffOnlyByteArrayOutputStream);
		//baseDiffOnlyOutputString = baseDocumentDiffOnlyByteArrayOutputStream.toString();
		//changeDiffOnlyOutputString = changeDocumentDiffOnlyByteArrayOutputStream.toString();
		
	}

	
	/**
	 * This test calculates the differences between the baseDocument and the changeDocument.
	 * It then uses the produced difference document to get the original document for the base side.
	 * It then compares the generated base document against the original bas document.
	 * 
	 * Then It gets the change (Side.MOD) document using the xdiff document and compares it to the original changeDocument
	 * @throws Exception
	 */
	@Test
	public void testSimpleGetDifferenceCall() throws Exception
	{
		XMLDiff xmlDiff = new XMLDiff();
		Element differenceElement = xmlDiff.getDifferences(baseDocument.getDocumentElement(), changeDocument.getDocumentElement());
		System.err.println("DIFFERENCE ELEMENT");
		XPath.dumpNode(differenceElement, System.err);
		System.out.println("BASE SIDE ELEMENT EXPECTED");
        System.out.println(baseOutputString);
        Element diffElementClone = (Element) differenceElement.cloneNode(true);
        
		Element baseSideElement = xmlDiff.getElementForSide(diffElementClone, Side.BASE);
		System.out.println("BASE SIDE ELEMENT");
		XPath.dumpNode(baseSideElement, System.err);
		ByteArrayOutputStream xmlDiffByteArrayOutputStream = new ByteArrayOutputStream();
		Document baseSideDocument = documentBuilder.newDocument();
		baseSideDocument.appendChild(baseSideDocument.adoptNode(baseSideElement));
		System.out.println("BASE SIDE DOCUMENT");
		XPath.dumpNode(baseSideDocument, System.err);		
		XPath.dumpNode(baseSideDocument, xmlDiffByteArrayOutputStream);		
		Assert.assertEquals(baseOutputString, xmlDiffByteArrayOutputStream.toString());
		
		System.out.println("MOD SIDE ELEMENT EXPECTED");
		System.out.println(changeOutputString);
		System.out.println("MOD SIDE ELEMENT");		
		Element modSideElement = xmlDiff.getElementForSide((Element) differenceElement.cloneNode(true), Side.MOD);
		XPath.dumpNode(modSideElement, System.err);
		System.out.println("MOD SIDE ELEMENT AFTER ADOPTION");
		xmlDiffByteArrayOutputStream.reset();
		Document modSideDocument = documentBuilder.newDocument();
		modSideDocument.appendChild(modSideDocument.adoptNode(modSideElement));
		XPath.dumpNode(modSideElement, System.err);
		System.out.println("===============================================================");
		XPath.dumpNode(modSideDocument, xmlDiffByteArrayOutputStream);
		Assert.assertEquals(changeOutputString, xmlDiffByteArrayOutputStream.toString());
	}
	
	
	
	@Test
	public void testDiffOnlyGetDifferenceCall() throws Exception
	{
		XMLDiff xmlDiff = new XMLDiff();
		xmlDiff.setTokenLists(TokenList.NEW_LINE.getTokenLists());
		Element differenceElement = xmlDiff.getDifferences(baseDocument.getDocumentElement(), changeDocument.getDocumentElement());
		XPath.dumpNode(differenceElement, System.err);
		
		System.out.println("\n\n-------------------After Trim dump-------------------");
		xmlDiff.trimDifferenceToSide(differenceElement,Side.MOD);
		XPath.dumpNode(differenceElement, System.out);
		//TODO un-trim
		
		
		System.out.println("\n\n-------------------After UN-Trim dump-------------------------");
		xmlDiff.repopulate((Element) baseDocument.getDocumentElement().cloneNode(true),differenceElement,Side.MOD);
		XPath.dumpNode(differenceElement, System.out);
		
		Element sideElement = xmlDiff.getElementForSide((Element) differenceElement.cloneNode(true), Side.MOD);
		System.err.println("\n\nMOD dump===============================================================");
		XPath.dumpNode(sideElement, System.err);
		ByteArrayOutputStream xmlDiffByteArrayOutputStream = new ByteArrayOutputStream();
		Document sideDocument = documentBuilder.newDocument();
		sideDocument.appendChild(sideDocument.adoptNode(sideElement));
		XPath.dumpNode(sideDocument, xmlDiffByteArrayOutputStream);
		Assert.assertEquals(changeOutputString, xmlDiffByteArrayOutputStream.toString());

		//-----------------------------------now test the other side---------------------------------------------------

		xmlDiff = new XMLDiff();
		differenceElement = xmlDiff.getDifferences(baseDocument.getDocumentElement(), changeDocument.getDocumentElement());
		XPath.dumpNode(differenceElement, System.err);

		System.out.println("\n\n-------------------After Trim dump-------------------");
		xmlDiff.trimDifferenceToSide(differenceElement,Side.BASE);
		XPath.dumpNode(differenceElement, System.out);
		//TODO un-trim


		System.out.println("\n\n-------------------After UN-Trim dump-------------------------");
		xmlDiff.repopulate((Element) changeDocument.getDocumentElement().cloneNode(true),differenceElement,Side.BASE);
		XPath.dumpNode(differenceElement, System.out);
		
		sideElement = xmlDiff.getElementForSide((Element) differenceElement.cloneNode(true), Side.BASE);
		System.err.println("\n\nBASE dump===============================================================");
		XPath.dumpNode(sideElement, System.err);
		xmlDiffByteArrayOutputStream = new ByteArrayOutputStream();
		sideDocument = documentBuilder.newDocument();
		sideDocument.appendChild(sideDocument.adoptNode(sideElement));
		XPath.dumpNode(sideDocument, xmlDiffByteArrayOutputStream);
		Assert.assertEquals(baseOutputString, xmlDiffByteArrayOutputStream.toString());
		

	}
	
	@Test
	public void testSimpleXmlDiff() throws Exception
	{
		XMLDiff xmlDiff = new XMLDiff();
		Document simpleXmlDiffDocument = documentBuilder.parse(new File("test-data/diff-testdata/simpleXmlDiff.xml"));
		Element sideElement = xmlDiff.getElementForSide(baseDocument,(Element) simpleXmlDiffDocument.getDocumentElement().cloneNode(true), Side.MOD);
		System.err.println("\n\nBASE dump");
		XPath.dumpNode(sideElement, System.err);
	}
	
}
