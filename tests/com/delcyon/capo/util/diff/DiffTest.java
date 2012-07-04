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
import java.io.FileInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import com.delcyon.capo.datastream.StreamUtil;
import com.delcyon.capo.tests.util.Util;
import com.delcyon.capo.util.diff.Diff.Side;
import com.delcyon.capo.util.diff.InputStreamTokenizer.TokenList;
import com.delcyon.capo.xml.XPath;

/**
 * @author jeremiah
 *
 */
public class DiffTest
{

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		System.setProperty("javax.xml.xpath.XPathFactory", "net.sf.saxon.xpath.XPathFactoryImpl");
		System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		
	}

	/**
	 * Test method for {@link com.delcyon.capo.util.diff.Diff#Diff(java.io.InputStream, java.io.InputStream, int)}.
	 */
	@Test
	public void testDiffInputStreamInputStreamInt() throws Exception
	{
		
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		
		
		
		
		DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = documentBuilder.newDocument();
		Element rootElement = document.createElement("root");
		document.appendChild(rootElement);
		
		Element baseElement = document.createElement("base");
		rootElement.appendChild(baseElement);
		FileInputStream baseFileInputStream = new FileInputStream("test-data/diff-testdata/base.txt");
		StreamUtil.readInputStreamIntoOutputStream(baseFileInputStream, byteArrayOutputStream,4096);
		baseElement.setTextContent(new String(byteArrayOutputStream.toByteArray()));
		byteArrayOutputStream.reset();
		
		Element otherElement = document.createElement("other");
		rootElement.appendChild(otherElement);
		FileInputStream otherFileInputStream = new FileInputStream("test-data/diff-testdata/other.txt");
		
		StreamUtil.readInputStreamIntoOutputStream(otherFileInputStream, byteArrayOutputStream,4096);
		otherElement.setTextContent(new String(byteArrayOutputStream.toByteArray()));
		byteArrayOutputStream.reset();
		
		Element diffElement = document.createElement("diff");
		rootElement.appendChild(diffElement);
		
		Diff diff = new Diff(baseElement.getTextContent(),otherElement.getTextContent(),TokenList.WORD_BOUNDRY);
		diff.addCustomTokenList('.');
		
		XMLTextDiff xmlDiffFormat = new XMLTextDiff();
		Element differenceElement = xmlDiffFormat.getDifferenceElement(diff);
		//Element differenceElement = xmlDiffFormat.getDifferenceElement((Text)baseElement.getFirstChild(),(Text)otherElement.getFirstChild());
		
		XPath.dumpNode(differenceElement, System.err);
		
		Text baseSideText = xmlDiffFormat.getTextForSide(differenceElement, Side.BASE);
		
		//XPath.dumpNode(baseSideDocument, System.out);
		
		Assert.assertEquals(baseElement.getTextContent(), baseSideText.getTextContent());
		
		
		Text otherSideText = xmlDiffFormat.getTextForSide(differenceElement, Side.MOD);
		//XPath.dumpNode(otherSideDocument, System.out);
		Assert.assertEquals(otherElement.getTextContent(), otherSideText.getTextContent());
		
		Text otherPatchText = xmlDiffFormat.getAlternateSideText(differenceElement,Side.BASE,baseSideText);
		//XPath.dumpNode(patchDocument, System.out);
		Assert.assertEquals(otherElement.getTextContent(), otherPatchText.getTextContent());
		
		Text basePatchText = xmlDiffFormat.getAlternateSideText(differenceElement,Side.MOD,otherSideText);
		//XPath.dumpNode(patchDocument, System.out);
		Assert.assertEquals(baseElement.getTextContent(), basePatchText.getTextContent());
		
//		StreamUtil.readInputStreamIntoOutputStream(diff.getInputStream(), byteArrayOutputStream,4096);
//		
//		System.out.println(new String(byteArrayOutputStream.toByteArray()));

		
		
	}

}
