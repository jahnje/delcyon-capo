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
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.delcyon.capo.xml.XPath;
import com.delcyon.capo.xml.cdom.CDocumentBuilder;

/**
 * @author jeremiah
 *
 */
public class XMLSerializerTest
{
	private transient DocumentBuilder documentBuilder;
	private transient Transformer transformer;
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
	    System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "com.delcyon.capo.xml.cdom.CDocumentBuilderFactory");
	    //System.clearProperty("javax.xml.parsers.DocumentBuilderFactory");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		documentBuilder = documentBuilderFactory.newDocumentBuilder();
		//setup xml output
		TransformerFactory tFactory = TransformerFactory.newInstance();
		transformer = tFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	}

	/**
	 * Test method for {@link com.delcyon.capo.util.XMLSerializer#export(java.lang.Object, org.w3c.dom.Element, int)}.
	 * @throws Exception 
	 */
	@Test
	public void testExport() throws Exception
	{
		XMLSerializerTestData xmlSerializerTestData = new XMLSerializerTestData(new XMLSerializerTestData(null));
		xmlSerializerTestData.setKeyArray(new String[]{"key1","key2","NullKey3"});
		xmlSerializerTestData.setValueArray(new String[]{"value1","value2",""});
		Document document = documentBuilder.newDocument();
		Element rootElement = document.createElement("root");
		XMLSerializer serializer = new XMLSerializer();
		serializer.export(xmlSerializerTestData, rootElement, 0);
		transformer.transform(new DOMSource(rootElement), new StreamResult(System.out));
	}

	
	
	/**
	 * Test method for {@link com.delcyon.capo.util.XMLSerializer#marshall(org.w3c.dom.Element, java.lang.Object)}.
	 * @throws Exception 
	 */
	@Test
	public void testMarshall() throws Exception
	{
		
		 boolean primBoolean = false;
		 Boolean objBoolean = true;	
		 
		 
		 
		//END TEST VALUES
		
		
		XMLSerializerTestData xmlSerializerTestData = new XMLSerializerTestData(null);
		xmlSerializerTestData.setObjBoolean(objBoolean);
		
		
		
		XMLSerializerTestData xmlSerializerTestDataMarshalled = new XMLSerializerTestData(null);
		XMLSerializer serializer = new XMLSerializer();
		serializer.marshall(getSerializedRootElement(xmlSerializerTestData), xmlSerializerTestDataMarshalled);
		
		try
		{
			Assert.assertEquals("objString not equal:", xmlSerializerTestData.getObjString(), xmlSerializerTestDataMarshalled.getObjString());
			
			serializer.marshall(getSerializedRootElement(xmlSerializerTestData), xmlSerializerTestDataMarshalled);
			Assert.assertEquals("not equals:", xmlSerializerTestData.toString(), xmlSerializerTestDataMarshalled.toString());
			
			xmlSerializerTestData.setObjBoolean(objBoolean);
			serializer.marshall(getSerializedRootElement(xmlSerializerTestData), xmlSerializerTestDataMarshalled);
			Assert.assertEquals("not equals:", xmlSerializerTestData.toString(), xmlSerializerTestDataMarshalled.toString());
			
			xmlSerializerTestData.setPrimBoolean(primBoolean);
			serializer.marshall(getSerializedRootElement(xmlSerializerTestData), xmlSerializerTestDataMarshalled);
			Assert.assertEquals("not equals:", xmlSerializerTestData.toString(), xmlSerializerTestDataMarshalled.toString());
			
			
			
			xmlSerializerTestData.setXmlSerializerTestData(new XMLSerializerTestData(null));
			serializer.marshall(getSerializedRootElement(xmlSerializerTestData), xmlSerializerTestDataMarshalled);
			Assert.assertEquals("not equals:", xmlSerializerTestData.toString(), xmlSerializerTestDataMarshalled.toString());
			
			//test Typed collections
			Vector<TestInterface> xmlSerializerTestDatasVector = new Vector<TestInterface>();
			xmlSerializerTestDatasVector.add(new XMLSerializerTestData(null));
			xmlSerializerTestDatasVector.add(new XMLSerializerTestData(null));
			xmlSerializerTestDatasVector.add(new XMLSerializerTestData(null));
			xmlSerializerTestData.setXmlSerializerTestDataVector(xmlSerializerTestDatasVector);
			serializer.marshall(getSerializedRootElement(xmlSerializerTestData), xmlSerializerTestDataMarshalled);
			Assert.assertEquals("not equals:", xmlSerializerTestData.toString(), xmlSerializerTestDataMarshalled.toString());
			Assert.assertEquals("not equals:", xmlSerializerTestData.getXmlSerializerTestDataVector().size(), xmlSerializerTestDataMarshalled.getXmlSerializerTestDataVector().size());
			
			//TODO test UnTyped collection
			
			
			//test arrays
			XMLSerializerTestData[] xmlSerializerTestDataArray = new XMLSerializerTestData[]{new XMLSerializerTestData(null),new XMLSerializerTestData(null),new XMLSerializerTestData(null)};
			xmlSerializerTestData.setXmlSerializerTestDatasArray(xmlSerializerTestDataArray);
			Element serializedRootElement = getSerializedRootElement(xmlSerializerTestData); 
			//XPath.dumpNode(serializedRootElement, System.out);
			serializer.marshall(serializedRootElement, xmlSerializerTestDataMarshalled);
			Assert.assertEquals("not equals:", xmlSerializerTestData.toString(), xmlSerializerTestDataMarshalled.toString());
			
			//test Maps
			HashMap<String, TestInterface> xmlSerializerTestDataHashMap = new HashMap<String, TestInterface>();
			xmlSerializerTestDataHashMap.put("one", new XMLSerializerTestData(null));
			xmlSerializerTestDataHashMap.put("two", new XMLSerializerTestData(null));
			xmlSerializerTestDataHashMap.put("three", new XMLSerializerTestData(null));
			xmlSerializerTestData.setXmlSerializerTestDataHashMap(xmlSerializerTestDataHashMap);
			serializer.marshall(getSerializedRootElement(xmlSerializerTestData), xmlSerializerTestDataMarshalled);
			Assert.assertEquals("not equals:", xmlSerializerTestData.toString(), xmlSerializerTestDataMarshalled.toString());
			
			//keep these last, as the array order can become different, but it's not relevant to array maps
			
			xmlSerializerTestData.setKeyArray(new String[]{"key1","key2","NullKey3"});
			xmlSerializerTestData.setValueArray(new String[]{"value1","value2",""});
			System.out.println(xmlSerializerTestData.toString());
			serializer.marshall(getSerializedRootElement(xmlSerializerTestData), xmlSerializerTestDataMarshalled);
			
			Assert.assertEquals("not equals:", xmlSerializerTestData.getKeyArray().length, xmlSerializerTestDataMarshalled.getKeyArray().length);
			Assert.assertEquals("not equals:", xmlSerializerTestData.getValueArray().length, xmlSerializerTestDataMarshalled.getValueArray().length);
			for (int index = 0; index < xmlSerializerTestData.getKeyArray().length; index++)
			{
				String key = xmlSerializerTestData.getKeyArray()[index];
				String value = xmlSerializerTestData.getValueArray()[index];
				String marshalledKey = null;
				String marshalledValue = null;
				boolean foundKey = false;
				for(int marshalIndex = 0; marshalIndex < xmlSerializerTestData.getKeyArray().length; marshalIndex++)
				{
					if (key.equals(xmlSerializerTestDataMarshalled.getKeyArray()[marshalIndex]))
					{
						marshalledKey = xmlSerializerTestDataMarshalled.getKeyArray()[marshalIndex];
						marshalledValue = xmlSerializerTestDataMarshalled.getValueArray()[marshalIndex];
						foundKey = true;
						break;
					}
				}
				Assert.assertTrue("didn't find key: "+key,foundKey);
				Assert.assertEquals(value,marshalledValue);
			}
			
		}
		catch (AssertionError e) {
			printOnFailure(xmlSerializerTestData, xmlSerializerTestDataMarshalled);
			throw e;
		}
		
	}
	
	private Element getSerializedRootElement(Object xmlSerializerTestData) throws Exception
	{
		XMLSerializer serializer = new XMLSerializer();
		Document document = documentBuilder.newDocument();
		Element rootElement = document.createElement("root");
		serializer.export(xmlSerializerTestData, rootElement, 0);
		return rootElement;
	}

	private String printOnFailure(Object xmlSerializerTestData, Object xmlSerializerTestDataMarshalled) throws TransformerException, Exception
	{
		transformer.transform(new DOMSource(getSerializedRootElement(xmlSerializerTestData)), new StreamResult(System.out));
		System.err.println("\n"+xmlSerializerTestDataMarshalled);
		transformer.transform(new DOMSource(getSerializedRootElement(xmlSerializerTestDataMarshalled)), new StreamResult(System.out));
		return "";
	}
	
}
