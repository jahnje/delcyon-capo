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

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.Configuration.PREFERENCE;
import com.delcyon.capo.controller.AbstractControl;
import com.delcyon.capo.controller.ControlElementProvider;
import com.delcyon.capo.modules.ModuleProvider;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceManager;
import com.delcyon.capo.resourcemanager.ResourceParameter;
import com.delcyon.capo.resourcemanager.ResourceParameterBuilder;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.StreamFormat;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.StreamType;
import com.delcyon.capo.resourcemanager.ResourceParameter.Source;
import com.delcyon.capo.resourcemanager.types.FileResourceType;
import com.delcyon.capo.resourcemanager.types.RefResourceType;
import com.delcyon.capo.xml.XPath;
import com.delcyon.capo.xml.XPathFunctionProcessor;
import com.delcyon.capo.xml.XPathFunctionProvider;
import com.delcyon.capo.xml.XPathFunctionUtility;

/**
 * @author jeremiah
 *
 */
@ControlElementProvider(name="transform")
@XPathFunctionProvider
public class TransformElement extends AbstractControl implements XPathFunctionProcessor
{
	
	private enum Attributes
	{
		name,ref,stylesheet,type
	}
	
	private static final String[] supportedNamespaces = {GroupElement.SERVER_NAMESPACE_URI};	
	private static final String[] functionNames = {"transform"};

	
	@Override
	public Attributes[] getAttributes()
	{
		return Attributes.values();
	}

	@Override
	public Attributes[] getRequiredAttributes()
	{
		return new Attributes[]{Attributes.name,Attributes.ref,Attributes.stylesheet};
	}

	
	@Override
	public String[] getSupportedNamespaces()
	{
		return supportedNamespaces;
	}

	
	@Override
	public String[] getXPathFunctionNames()
	{
		
		return functionNames;
	}
	
	@Override
	public Object processFunction(String functionName, Object... arguments) throws Exception
	{
		Node contextNode = getContextNode();
		String prefix = XPathFunctionUtility.getPrefix(contextNode, arguments, 1);
		if (functionName.equals("transform"))
		{
			return "//"+prefix+"transform[@name = '"+arguments[0]+"']";
		}
		else
		{		
			return null;
		}
	}
	
	@Override
	public Object processServerSideElement() throws Exception
	{
		
		String ref = getAttributeValue(Attributes.ref);
		
		String stylesheet = getAttributeValue(Attributes.stylesheet);
		String src = null;
		//see if we are making a specific reference to something, and if so ues it, as opposed to the module system.
		if (ResourceManager.getScheme(stylesheet) != null)
		{
			src = stylesheet;
		}
		Node refNode = XPath.selectSingleNode(getControlElementDeclaration(), ref);
		TransformerFactory tFactory = TransformerFactory.newInstance();
		
		Transformer transformer = null;
		if (src != null)
		{
			ResourceDescriptor resourceDescriptor = getParentGroup().getResourceDescriptor(this, src);
			resourceDescriptor.addResourceParameters(getParentGroup(),new ResourceParameter(FileResourceType.Parameters.PARENT_PROVIDED_DIRECTORY,PREFERENCE.MODULE_DIR,Source.CALL));
			resourceDescriptor.addResourceParameters(getParentGroup(),new ResourceParameter(RefResourceType.Parameters.XMLNS,"xsl=http://www.w3.org/1999/XSL/Transform"));
			if (resourceDescriptor.isSupportedStreamFormat(StreamType.INPUT, StreamFormat.XML_BLOCK))
			{
				Element styleSheetElement = resourceDescriptor.readXML(getParentGroup(),ResourceParameterBuilder.getResourceParameters(getControlElementDeclaration()));

				Document transformDocument = CapoApplication.getDocumentBuilder().newDocument();			
				Element transformStyleSheetElement = (Element) transformDocument.adoptNode(styleSheetElement);

				transformDocument.appendChild(transformStyleSheetElement);

				//for some reason, we always strip out the parent name space if it's declared in the child, which it has to be, so we add it back in.
				String parentNameSpace = getControlElementDeclaration().getNamespaceURI();
				String parentPrefix = getControlElementDeclaration().getPrefix();
				transformStyleSheetElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:"+parentPrefix, parentNameSpace);

				transformer = tFactory.newTransformer(new DOMSource(transformDocument));
			}
			else
			{
				transformer = tFactory.newTransformer(new StreamSource(resourceDescriptor.getInputStream(getParentGroup(),ResourceParameterBuilder.getResourceParameters(getControlElementDeclaration()))));
			}
			//cleanup and parameters we added when calling this.
			resourceDescriptor.close(getParentGroup(),ResourceParameterBuilder.getResourceParameters(getControlElementDeclaration()));
		}
		else if (stylesheet.isEmpty() == false)
		{
			Element moduleElement = ModuleProvider.getModuleElement(stylesheet);
			if (moduleElement == null)
			{
				throw new Exception ("Couldn't locate a style sheet named "+stylesheet+" in any of the module directories");
			}
			Document transformDocument = CapoApplication.getDocumentBuilder().newDocument();			
			Element transformStyleSheetElement = (Element) transformDocument.adoptNode(moduleElement);

			transformDocument.appendChild(transformStyleSheetElement);

			//for some reason, we always strip out the parent name space if it's declared in the child, which it has to be, so we add it back in.
			String parentNameSpace = getControlElementDeclaration().getNamespaceURI();
			String parentPrefix = getControlElementDeclaration().getPrefix();
			transformStyleSheetElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:"+parentPrefix, parentNameSpace);
			transformer = tFactory.newTransformer(new DOMSource(transformDocument));
		}
		
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.transform(new DOMSource(refNode), new DOMResult(getControlElementDeclaration()));
		if (getControlElementDeclaration().hasAttribute(Attributes.name.toString()))
		{
			getParentGroup().set(getAttributeValue(Attributes.name), XPath.getXPath(getControlElementDeclaration()));
		}
		
		return null;
	}

	
}
