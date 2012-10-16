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
package com.delcyon.capo.controller.elements;

import java.io.OutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.AbstractControl;
import com.delcyon.capo.controller.ControlElementProvider;
import com.delcyon.capo.datastream.NullOutputStream;
import com.delcyon.capo.datastream.stream_attribute_filter.MD5FilterOutputStream;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceParameter;
import com.delcyon.capo.resourcemanager.ResourceParameterBuilder;
import com.delcyon.capo.xml.XPath;

/**
 * @author jeremiah
 *
 */
@ControlElementProvider(name="export")
public class ExportElement extends AbstractControl
{
	
	private enum Attributes
	{
		name,dest,ref,nodeset,output,trim,type
	}
	
	
	private static final String[] supportedNamespaces = {CapoApplication.SERVER_NAMESPACE_URI};
	
	
	private static final String[] namespacesToRemove = {CapoApplication.SERVER_NAMESPACE_URI,CapoApplication.CLIENT_NAMESPACE_URI};

	
	@Override
	public Attributes[] getAttributes()
	{
		return Attributes.values();
	}

	
	@Override
	public Attributes[] getRequiredAttributes()
	{
		return new Attributes[]{Attributes.dest};
	}

	
	@Override
	public String[] getSupportedNamespaces()
	{
		return supportedNamespaces;
	}

		
	@Override
	public Object processServerSideElement() throws Exception
	{
		
		String ref = getAttributeValue(Attributes.ref);
		String name = getAttributeValue(Attributes.name);
		String dest = getAttributeValue(Attributes.dest);
		String nodeset = getAttributeValue(Attributes.nodeset);
		String output = getAttributeValue(Attributes.output);
		String trim = getAttributeValue(Attributes.trim);
		String type = getAttributeValue(Attributes.type);
		if (type.isEmpty())
		{
			type = "XML";
		}
		
		//default the name to the element name
		if (name == null || name.trim().isEmpty())
		{
			name = getElementName();
		}
		
		
		
		//get the list of nodes, and add them to the document
		Node refNode = null;
		if (nodeset != null && nodeset.trim().isEmpty() == false)
		{
			
			Document exportDocument = CapoApplication.getDocumentBuilder().newDocument();
			exportDocument.appendChild(exportDocument.createElement(name));
			
			NodeList nodeList = XPath.selectNodes(getControlElementDeclaration(), nodeset);
			for(int index = 0;index < nodeList.getLength(); index++)
			{	

				Node node = nodeList.item(index);
				
				//strip off delcyon client and server namespace declarations ugh! We end up with multiple declarations of we don't
				XPath.removeNamespaceDeclarations(node, namespacesToRemove);
				
				exportDocument.getDocumentElement().appendChild(exportDocument.importNode(node, true));
			}
			refNode = exportDocument;
		}
		else if (ref.isEmpty() == false)
		{
			refNode = XPath.selectSingleNode(getControlElementDeclaration(), ref,getControlElementDeclaration().getPrefix());			
		}
		else
		{
			refNode = getControlElementDeclaration();
		}
		
		if (refNode == null)
		{
		    throw new Exception("Couldn't find anything at "+ref+" to export. Check your path.");
		}
		//trim all of the whitespace out of the text nodes
		if (trim != null && trim.equalsIgnoreCase("true"))
		{
			refNode = refNode.cloneNode(true);			
			NodeList nodeList = XPath.selectNodes(refNode, "descendant-or-self::*");
			for(int index = 0;index < nodeList.getLength(); index++)
			{
				Node node = nodeList.item(index);
				String text = node.getTextContent();
				if (text != null && text.isEmpty() == false)
				{
					node.setTextContent(text.trim());	
				}
			}
		}

		//escape all newline and carriage returns
		refNode = refNode.cloneNode(true);
		
		NodeList nodeList = XPath.selectNodes(refNode, "descendant-or-self::*");
		for(int index = 0;index < nodeList.getLength(); index++)
		{
			Node node = nodeList.item(index);
			//make sure we only mess around with text nodes, setting the text content on a parent node will wipe out all of the children.
			if (node.getNodeType() != Node.TEXT_NODE)
			{
			    continue;
			}
			
			String text = node.getTextContent();
			if (text != null)
			{
				String newText = text.replaceAll("\\\\n", "\n").replaceAll("\\\\r", "\r");
				node.setTextContent(newText);	
			}
			
		}
		
		refNode.normalize();
		
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer = tFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		
		//set any XSL output properties
		if (output != null && output.trim().isEmpty() == false)
		{
			String[] outputProperties = output.split(",");
			for (String outputProperty : outputProperties)
			{
				String propertyName = outputProperty.substring(0,outputProperty.indexOf(":"));
				String propertyValue = outputProperty.substring(outputProperty.indexOf(":")+1);				
				transformer.setOutputProperty(propertyName, propertyValue);	
			}
			
		}
		
		//save the file 
		ResourceDescriptor resourceDescriptor = getParentGroup().getResourceDescriptor(this, dest);		
		ResourceParameter[] resourceParameters = ResourceParameterBuilder.getResourceParameters(getControlElementDeclaration());
		//don't save anything if the data hasn't changed
		String md5 = resourceDescriptor.getContentMetaData(getParentGroup(),resourceParameters).getMD5();
		
		if (md5 != null)
		{
			//we are going to write this twice, but we don't know the size of the file that could be created, so this way we keep memory use to a minimum.
			MD5FilterOutputStream md5FilterOutputStream = new MD5FilterOutputStream(new NullOutputStream());
			transformer.transform(new DOMSource(refNode), new StreamResult(md5FilterOutputStream));
			if (md5.equalsIgnoreCase(md5FilterOutputStream.getMD5()) == false)
			{
			    
				OutputStream outputStream = resourceDescriptor.getOutputStream(getParentGroup(),resourceParameters);
				transformer.transform(new DOMSource(refNode), new StreamResult(outputStream));
				outputStream.flush();
				outputStream.close();
				//resourceDescriptor.getOutputStream(getParentGroup(),resourceParameters).close();
			}
			else
			{
				//they are the same, so don't do anything				
			}
		}
		else
		{	
			OutputStream outputStream = resourceDescriptor.getOutputStream(getParentGroup(),resourceParameters);
			transformer.transform(new DOMSource(refNode), new StreamResult(outputStream));
			outputStream.flush();
			outputStream.close();
			//resourceDescriptor.getOutputStream(getParentGroup(),resourceParameters).close();
		}
//		//cleanup and parameters we added when calling this.
		//resourceDescriptor.close(getParentGroup(),resourceParameters);
		
		return null;
	}

	
}
