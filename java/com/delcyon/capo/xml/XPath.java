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
package com.delcyon.capo.xml;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionResolver;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.datastream.NullOutputStream;
import com.delcyon.capo.datastream.stream_attribute_filter.MD5FilterOutputStream;
import com.delcyon.capo.server.CapoServer;
import com.delcyon.capo.util.NamespaceContextMap;

/**
 * @author jeremiah
 *
 */
public class XPath
{

	private static final XPathFactory xPathFactory = XPathFactory.newInstance();
	
	public static void setXPathFunctionResolver(XPathFunctionResolver xPathFunctionResolver)
	{
		xPathFactory.setXPathFunctionResolver(xPathFunctionResolver);
	}
	
	public static Node selectSingleNode(Node node, String path) throws Exception
	{
		return selectSingleNode(node, path, null);
	}
	
	public static Node selectSingleNode(Node node, String path,String prefix) throws Exception
	{
		try 
		{
			//dumpNode(node.getOwnerDocument(), System.err);
			javax.xml.xpath.XPath xPath = xPathFactory.newXPath();
			NamespaceContextMap namespaceContextMap = new NamespaceContextMap();
			namespaceContextMap.addNamespace("server", CapoApplication.SERVER_NAMESPACE_URI);
			namespaceContextMap.addNamespace("client", CapoApplication.CLIENT_NAMESPACE_URI);
			xPath.setNamespaceContext(namespaceContextMap);
			//String parsedXpath = processFunctions(path,prefix);
			XPathExpression xPathExpression = xPath.compile(path);
			return (Node) xPathExpression.evaluate(node,XPathConstants.NODE);
			
		} catch (Exception exception)
		{
			CapoServer.logger.log(Level.SEVERE, "Error evaluating '"+path+"' on "+getPathToRoot(node));
			throw exception;
		}
	}

	public static NodeList selectNodes(Node node, String path) throws Exception
	{
		return selectNodes(node, path, null);
	}
	
	public static Node selectNSNode(Node node, String path,String... namespaces) throws Exception
	{
		try 
		{
			
			javax.xml.xpath.XPath xPath = xPathFactory.newXPath();
			NamespaceContextMap namespaceContextMap = new NamespaceContextMap();			
			for (String namespace : namespaces)
			{
				String[] namespaceDecl = namespace.split("=");
				namespaceContextMap.addNamespace(namespaceDecl[0], namespaceDecl[1]);
			}
			xPath.setNamespaceContext(namespaceContextMap);
			XPathExpression xPathExpression = xPath.compile(path);
			return (Node) xPathExpression.evaluate(node,XPathConstants.NODE);
			
		} catch (Exception exception)
		{
			if (CapoServer.logger != null)
			{
				CapoServer.logger.log(Level.SEVERE, "Error evaluating xpath '"+path+"' on "+getPathToRoot(node));
			}
			throw exception;
		}
	}
	
	public static NodeList selectNSNodes(Node node, String path,String... namespaces) throws Exception
	{
		try 
		{
			
			javax.xml.xpath.XPath xPath = xPathFactory.newXPath();
			NamespaceContextMap namespaceContextMap = new NamespaceContextMap();			
			for (String namespace : namespaces)
			{
				String[] namespaceDecl = namespace.split("=");
				namespaceContextMap.addNamespace(namespaceDecl[0], namespaceDecl[1]);
			}
			xPath.setNamespaceContext(namespaceContextMap);
			XPathExpression xPathExpression = xPath.compile(path);
			return (NodeList) xPathExpression.evaluate(node,XPathConstants.NODESET);
			
		} catch (Exception exception)
		{
			if (CapoServer.logger != null)
			{
				CapoServer.logger.log(Level.SEVERE, "Error evaluating xpath '"+path+"' on "+getPathToRoot(node));
			}
			throw exception;
		}
	}
	
	public static void removeNamespaceDeclarations(Node node,String... namespaces)
	{
		NamedNodeMap namedNodeMap = ((Element)node).getAttributes();
		for(int nameIndex = 0; nameIndex < namedNodeMap.getLength(); nameIndex++)
		{
			Node namedNode = namedNodeMap.item(nameIndex);
			String uri = namedNode.getNamespaceURI();
			String localName = namedNode.getLocalName();
			if (uri != null && uri.equals("http://www.w3.org/2000/xmlns/"))
			{
				for (String removeableNamespace : namespaces)
				{
					if (namedNode.getNodeValue().equals(removeableNamespace))
					{								
						((Element)node).removeAttributeNS("http://www.w3.org/2000/xmlns/",localName);
						nameIndex--;								
					}
				}
			}
		}
	}
	
	public static NodeList selectNodes(Node node, String path,String prefix) throws Exception
	{
		try 
		{
			
			javax.xml.xpath.XPath xPath = xPathFactory.newXPath();
			NamespaceContextMap namespaceContextMap = new NamespaceContextMap();
			namespaceContextMap.addNamespace("server", CapoApplication.SERVER_NAMESPACE_URI);
			namespaceContextMap.addNamespace("client", CapoApplication.CLIENT_NAMESPACE_URI);
			xPath.setNamespaceContext(namespaceContextMap);
			XPathExpression xPathExpression = xPath.compile(path);
			return (NodeList) xPathExpression.evaluate(node,XPathConstants.NODESET);
			
		} catch (Exception exception)
		{
			if (CapoServer.logger != null) //TODO this shouldn't have a dependency on CapoServer!
			{
				CapoServer.logger.log(Level.SEVERE, "Error evaluating xpath '"+path+"' on "+getPathToRoot(node));
			}
			throw exception;
		}
	}

	public static String selectSingleNodeValue(Element node, String path,String... namespaces) throws Exception
	{
		return selectSingleNodeValue(node, path, null,namespaces);
	}
	
	public static String selectSingleNodeValue(Element node, String path, String prefix,String... namespaces) throws Exception
	{
		try 
		{
			
			javax.xml.xpath.XPath xPath = xPathFactory.newXPath();
			NamespaceContextMap namespaceContextMap = new NamespaceContextMap();
			namespaceContextMap.addNamespace("server", CapoApplication.SERVER_NAMESPACE_URI);
			namespaceContextMap.addNamespace("client", CapoApplication.CLIENT_NAMESPACE_URI);
			namespaceContextMap.addNamespace("resource", CapoApplication.RESOURCE_NAMESPACE_URI);
			for (String namespace : namespaces)
			{
				String[] namespaceDecl = namespace.split("=");
				namespaceContextMap.addNamespace(namespaceDecl[0], namespaceDecl[1]);
			}
			xPath.setNamespaceContext(namespaceContextMap);
			XPathExpression xPathExpression = xPath.compile(path);
			return  xPathExpression.evaluate(node);
			
		} catch (Exception exception)
		{	
			//keep this from looping out of control when things are weird
			if (exception.getCause() == null || exception.getCause().getMessage() == null)
			{
				CapoServer.logger.log(Level.SEVERE, "Error evaluating xpath '"+path+"'");
				throw exception;
			}
			if (exception.getCause().getMessage().matches(".*context.*item.*for.*axis.*step.*is.*undefined.*"))
			{
				CapoServer.logger.log(Level.SEVERE, "Error evaluating xpath '"+path+"'");
				throw exception;
			}
			CapoServer.logger.log(Level.SEVERE, "Error evaluating xpath '"+path+"' on "+getPathToRoot(node));
			throw exception;
		}
	}

	public static String getXPath(Node node) throws Exception
	{
		//example
		// /server:Capo/server:group[1]/server:choose[1]/server:when[1]
		return getPathToRoot(node);		
	}
	
	private static String getPathToRoot(Node node) throws Exception
	{
		
		String name = node.getNodeName();
		if (node instanceof Element)
		{
			String nameAttributeValue = ((Element) node).getAttribute("name");
			
			
			if (nameAttributeValue.isEmpty() == true )
			{	
				if (node.getParentNode() != null)
				{
					String position = selectSingleNodeValue((Element) node, "count(preceding-sibling::"+name+")+1");
					name += "["+position+"]";
				}
				else //this node does not belong to a document. It must have just been created.
				{
					name += "[ORPHAN_NODE]";
				}
			}
			else
			{
				name += "[@name = '"+nameAttributeValue+"']";
			}
		}
		if (node.getParentNode() != null && node.getParentNode().getNodeName() != null && node.getParentNode() instanceof Element)
		{
			
			return getPathToRoot(node.getParentNode())+"/"+name;
		}
		else
		{
			return "/"+name;
		}
			
	}
	
//	private static String processFunctions(String varString,String prefix)
//	{
//		StringBuffer stringBuffer = new StringBuffer(varString);
//		processFunctions(stringBuffer,prefix);
//		return stringBuffer.toString();
//	}
	
//	private static void processFunctions(StringBuffer varStringBuffer,String prefix)
//	{
//		if (prefix == null)
//		{
//			prefix = "";
//		}
//		else
//		{
//			prefix = prefix +":";
//		}
//		
//		Set<String> keySet = xpathFunctionProcessorHashMap.keySet();
//		
//		for (String functionName : keySet)
//		{
//			//TODO deal with nested functions and quotes in quotes
//			//TODO look into cache this matches function, since it's got the greatest number of allocations in the system. 
//			if (varStringBuffer != null)
//			{
//				String varString = varStringBuffer.toString();
//				if( varString.matches(".*"+functionName.toString()+functionMatcher))
//				{
//					String argument = varString.replaceFirst(".*"+functionName.toString()+functionMatcher, "$1");
//					String originalFunctionDeclaration = functionName.toString()+"("+argument+")";
//					//strip of any quotes
//					argument = argument.replaceAll("'", "");
//					int startIndex = varStringBuffer.indexOf(originalFunctionDeclaration);
//					String[] arguments = argument.split(",");
//
//					String value = xpathFunctionProcessorHashMap.get(functionName).processFunction(prefix, functionName, arguments);
//
//					varStringBuffer.replace(startIndex, startIndex+originalFunctionDeclaration.length(), value);
//				}
//			}
//		}
//		
//		if (CapoServer.logger != null)
//		{
//			CapoServer.logger.log(Level.FINE,"final function replacement =  '"+varStringBuffer+"'");
//		}
//		
//	}

	public static void dumpNode(Node node, OutputStream outputStream) throws Exception
	{
		TransformerFactory tFactory = TransformerFactory.newInstance();
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();		
		Document indentityTransforDocument = documentBuilder.parse(ClassLoader.getSystemResource("defaults/identity_transform.xsl").openStream());
		Transformer transformer = tFactory.newTransformer(new DOMSource(indentityTransforDocument));
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		//transformer.setOutputProperty(SaxonOutputKeys.INDENT_SPACES,"4");
		
		transformer.transform(new DOMSource(node), new StreamResult(outputStream));		
	}

	/**
	 * 
	 * @param node
	 * @param xpath
	 * @return list of nodes removed
	 * @throws Exception
	 */
	public static NodeList removeNodes(Node node, String xpath) throws Exception
	{
		NodeList nodeList = selectNodes(node, xpath);
		for (int nodeListIndex = 0; nodeListIndex < nodeList.getLength(); nodeListIndex++)
		{
			Node removeableNode = nodeList.item(nodeListIndex);
			Node parentNode = removeableNode.getParentNode();
			if (parentNode != null)
			{
				parentNode.removeChild(removeableNode);
			}
		}
		return nodeList;
	}

	public static Document unwrapDocument(Document wrappedDocument) throws Exception
	{
		//unwrap document
		
		Document unwrappedDocument = CapoApplication.getDocumentBuilder().newDocument();
		
		NodeList nodeList = wrappedDocument.getDocumentElement().getElementsByTagName("*");
		if (nodeList.getLength() != 0)
		{
			unwrappedDocument.appendChild(unwrappedDocument.importNode(nodeList.item(0),true));	
		}
		else
		{
			throw new Exception("No root element child found");
		}
		return unwrappedDocument;
	}

	public static Document wrapDocument(Document parentDocument, Document childDocument) throws Exception
	{		
		parentDocument.getDocumentElement().appendChild(parentDocument.importNode(childDocument.getDocumentElement(), true));		
		return parentDocument;
	}

	public static void removeContent(Element element)
	{
		NodeList nodeList = element.getChildNodes();
		while(nodeList.getLength() > 0)
		{
			element.removeChild(nodeList.item(0));			
		}
		
	}

	public static void removeAttributes(Element element)
	{
		NamedNodeMap namedNodeMap = element.getAttributes();
		while (namedNodeMap.getLength() > 0)
		{
			element.removeAttributeNode((Attr) namedNodeMap.item(0));
		}
	}
	
	public static String getElementMD5(Element element) throws Exception
	{
	    String md5 = "";
        MD5FilterOutputStream md5FilterOutputStream = new MD5FilterOutputStream(new NullOutputStream());
        getElementMD5(element, md5FilterOutputStream);
        md5FilterOutputStream.close();
        md5 = md5FilterOutputStream.getMD5();           
        return md5;
	}
	
	
    private static void getElementMD5(Element element, MD5FilterOutputStream md5FilterOutputStream) throws Exception
    {

        //process attributes first
        NamedNodeMap attributeList = element.getAttributes();
        for (int currentAttribute = 0; currentAttribute < attributeList.getLength(); currentAttribute++)
        {
            Node attributeNode = attributeList.item(currentAttribute);
            if (attributeNode instanceof Attr)
            {
                Attr attr = (Attr) attributeNode;
                md5FilterOutputStream.write(attr.getName());
                md5FilterOutputStream.write(attr.getValue());
            }
        }
        
        //then process children
        NodeList nodeList = element.getChildNodes();
        for (int currentNode = 0; currentNode < nodeList.getLength(); currentNode++)
        {
            Node node = nodeList.item(currentNode);
            if (node instanceof Element)
            {
                getElementMD5((Element) node, md5FilterOutputStream);
            }
            else if (node instanceof Text)
            {
                if (node.getNodeValue().trim().isEmpty() == false)
                {
                    md5FilterOutputStream.write(node.getNodeValue());
                }
            }
        }
    }
}
