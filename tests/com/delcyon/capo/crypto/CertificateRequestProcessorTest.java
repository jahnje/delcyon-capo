package com.delcyon.capo.crypto;

import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.delcyon.capo.client.CapoClient;
import com.delcyon.capo.tests.util.ExternalTestServer;
import com.delcyon.capo.tests.util.TestCapoApplication;
import com.delcyon.capo.tests.util.TestClient;
import com.delcyon.capo.tests.util.external.Util;
import com.delcyon.capo.xml.XPath;

public class CertificateRequestProcessorTest
{

	private Exception exception;
	private CapoClient capoClient;
	private String persistantPassword;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
	}

	@Before
	public void setUp() throws Exception
	{
		exception = null;
		
		persistantPassword = "This is a test password";
	}

	@AfterClass
	public static void tearDownAfterClass()
	{
		//TestCapoApplication.stop(0);
	}
	
	@Test
	public void testServerEmptyClientEmpty() throws Exception
	{
		//setup
		Util.deleteTree("capo");
	    Util.copyTree("test-data/capo/server", "capo/server");
	    Util.deleteTree("capo/server/clients");
	    Util.setDefaultPreferences();	    
		ExternalTestServer externalTestServer = new ExternalTestServer();
		externalTestServer.startServer();
		
		
		PrintStream oldSystemOutPrintStream = System.out;
		
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		PrintStream newSystemOutPrintStream = new PrintStream(byteArrayOutputStream);
		System.setOut(newSystemOutPrintStream);
		
		InputStream oldSystmIn = System.in;
		PipedInputStream testInPipe = new PipedInputStream();
		PipedOutputStream testOutPipe = new PipedOutputStream();
		testInPipe.connect(testOutPipe);
		System.setIn(testInPipe);
		
		TestClient.start();
		
		long waitTime = 0;
		while(true)
		{
			
			String output = new String(byteArrayOutputStream.toByteArray());
			
			
			if (output.matches("(?smi).*Enter Password:*.*"))
			{
				oldSystemOutPrintStream.println("Found Password Question!");
				break;
			}
			else
			{
				Thread.sleep(500);
				waitTime += 500;
				if (waitTime > 10000)
				{
					oldSystemOutPrintStream.println("==============FROM PIPE===============");					
					oldSystemOutPrintStream.write(byteArrayOutputStream.toByteArray());
					oldSystemOutPrintStream.println("==============END PIPE================");
					
					System.setOut(oldSystemOutPrintStream);
					fail("Never found password question");
				}
			}
		}
		
		//reset stdout
		System.setOut(oldSystemOutPrintStream);
		
		String[] lines = new String(byteArrayOutputStream.toByteArray()).split("\n");
		System.out.println(lines[lines.length-1]);
		System.out.println(lines[lines.length-2]);
		
		String password = lines[lines.length-2].replaceAll(".*'(\\d+)'.*", "$1");
		System.out.println("password = "+password);
		Assert.assertTrue("This isn't an expected password '"+password+"'", password.matches("\\d+"));
		
		
		testOutPipe.write((password+"\n").getBytes());
		testOutPipe.flush();
		
		
		TestClient.shutdown();
		//shutdown
		System.setIn(oldSystmIn);
		externalTestServer.shutdown();
		CopyOnWriteArrayList<Exception> exceptionList = externalTestServer.getExceptionList();
		if (exceptionList.isEmpty() == false)
		{
			throw exceptionList.get(0);
		}
		
		Document configDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File("capo/client/config/config.xml"));
		String clientID = XPath.selectSingleNodeValue(configDocument.getDocumentElement(), "//entry[@key = 'CLIENT_ID']/@value");
		Assert.assertEquals("client id not right!", "capo.client.2", clientID);
	}

	@Test
	public void testServerSetClientEmpty() throws Exception
	{
		String password = "This is a test password";
		//setup
		Util.deleteTree("capo");
	    Util.copyTree("test-data/capo/server", "capo/server");
	    Util.deleteTree("capo/server/clients");
	    Util.setDefaultPreferences();
	    Document serverConfigDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File("capo/server/config/config.xml"));
	    Element entryElement = serverConfigDocument.createElement("entry");
	   
	    entryElement.setAttribute("key", "CLIENT_VERIFICATION_PASSWORD");
	    entryElement.setAttribute("value", password);
	    serverConfigDocument.getDocumentElement().appendChild(entryElement);
	    XPath.dumpNode(serverConfigDocument, new FileOutputStream("capo/server/config/config.xml"));
		ExternalTestServer externalTestServer = new ExternalTestServer();
		externalTestServer.startServer();
		
		
		PrintStream oldSystemOutPrintStream = System.out;
		
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		PrintStream newSystemOutPrintStream = new PrintStream(byteArrayOutputStream);
		System.setOut(newSystemOutPrintStream);
		
		InputStream oldSystmIn = System.in;
		PipedInputStream testInPipe = new PipedInputStream();
		PipedOutputStream testOutPipe = new PipedOutputStream();
		testInPipe.connect(testOutPipe);
		System.setIn(testInPipe);
		
		TestClient.start();
		
		long waitTime = 0;
		while(true)
		{
			
			String output = new String(byteArrayOutputStream.toByteArray());
			
			
			if (output.matches("(?smi).*Enter Password:*.*"))
			{
				oldSystemOutPrintStream.println("Found Password Question!");
				break;
			}
			else
			{
				Thread.sleep(500);
				waitTime += 500;
				if (waitTime > 10000)
				{
					oldSystemOutPrintStream.println("==============FROM PIPE===============");					
					oldSystemOutPrintStream.write(byteArrayOutputStream.toByteArray());
					oldSystemOutPrintStream.println("==============END PIPE================");
					
					System.setOut(oldSystemOutPrintStream);
					fail("Never found password question");
				}
			}
		}
		
		//reset stdout
		System.setOut(oldSystemOutPrintStream);
		
		
		
		
		testOutPipe.write((password+"\n").getBytes());
		testOutPipe.flush();
		
		
		TestClient.shutdown();
		//shutdown
		System.setIn(oldSystmIn);
		externalTestServer.shutdown();
		CopyOnWriteArrayList<Exception> exceptionList = externalTestServer.getExceptionList();
		if (exceptionList.isEmpty() == false)
		{
			throw exceptionList.get(0);
		}
		
		Document configDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File("capo/client/config/config.xml"));
		String clientID = XPath.selectSingleNodeValue(configDocument.getDocumentElement(), "//entry[@key = 'CLIENT_ID']/@value");
		Assert.assertEquals("client id not right!", "capo.client.2", clientID);
	}

	@Test
	public void testServerEmptyClientSet() throws Exception
	{
		//setup
		Util.deleteTree("capo");
	    Util.copyTree("test-data/capo/server", "capo/server");
	    Util.deleteTree("capo/server/clients");
	    Util.setDefaultPreferences();

		ExternalTestServer externalTestServer = new ExternalTestServer();
		externalTestServer.startServer();
		
		TestClient.start("-CLIENT_VERIFICATION_PASSWORD",persistantPassword);
        TestClient.shutdown();
		CopyOnWriteArrayList<Exception> exceptionList = TestClient.getExceptionList();
		
		Assert.assertEquals("Expecting one wrong password exception",1, exceptionList.size());
		Assert.assertEquals("Expecting wrong password exception","Wrong Password.", exceptionList.get(0).getMessage());
		
		//shutdown
		
		externalTestServer.shutdown();
		exceptionList = externalTestServer.getExceptionList();
		if (exceptionList.isEmpty() == false)
		{
			throw exceptionList.get(0);
		}
		
		Document configDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File("capo/client/config/config.xml"));
		String clientID = XPath.selectSingleNodeValue(configDocument.getDocumentElement(), "//entry[@key = 'CLIENT_ID']/@value");
		Assert.assertEquals("client id not right!", "capo.client.2", clientID);
	}

	@Test
	public void testServerSetClientSet() throws Exception
	{
		
		//setup
		Util.deleteTree("capo");
	    Util.copyTree("test-data/capo/server", "capo/server");
	    Util.deleteTree("capo/server/clients");
	    Util.setDefaultPreferences();
	    Document serverConfigDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File("capo/server/config/config.xml"));
	    Element entryElement = serverConfigDocument.createElement("entry");
	   
	    entryElement.setAttribute("key", "CLIENT_VERIFICATION_PASSWORD");
	    entryElement.setAttribute("value", persistantPassword);
	    serverConfigDocument.getDocumentElement().appendChild(entryElement);
	    XPath.dumpNode(serverConfigDocument, new FileOutputStream("capo/server/config/config.xml"));
		ExternalTestServer externalTestServer = new ExternalTestServer();
		externalTestServer.startServer();
		
		TestClient.start("-CLIENT_VERIFICATION_PASSWORD",persistantPassword);
		TestClient.shutdown();
		
		
		externalTestServer.shutdown();
		CopyOnWriteArrayList<Exception> exceptionList = externalTestServer.getExceptionList();
		if (exceptionList.isEmpty() == false)
		{
			throw exceptionList.get(0);
		}
		
		Document configDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File("capo/client/config/config.xml"));
		String clientID = XPath.selectSingleNodeValue(configDocument.getDocumentElement(), "//entry[@key = 'CLIENT_ID']/@value");
		Assert.assertEquals("client id not right!", "capo.client.2", clientID);
	}
}
