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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.AbstractClientSideControl;
import com.delcyon.capo.controller.ControlElementProvider;
import com.delcyon.capo.controller.client.ClientSideControl;
import com.delcyon.capo.resourcemanager.ContentFormatType;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceParameter;
import com.delcyon.capo.resourcemanager.ResourceParameterBuilder;
import com.delcyon.capo.util.diff.Diff;
import com.delcyon.capo.util.diff.XMLTextDiff;
import com.delcyon.capo.xml.XMLDiff;

/**
 * @author jeremiah
 *
 */
@ControlElementProvider(name="diff")
public class DiffElement extends AbstractClientSideControl implements ClientSideControl
{

	
	
	private enum Attributes
	{
		name,base, mod,dst;
	}
	
	
	private static final String[] supportedNamespaces = {CapoApplication.SERVER_NAMESPACE_URI,CapoApplication.CLIENT_NAMESPACE_URI};
	
	private static final String[] functionNames = {};
	
	@Override
	public Attributes[] getAttributes()
	{
		return Attributes.values();
	}
	
	@Override
	public Attributes[] getRequiredAttributes()
	{
		return new Attributes[]{Attributes.base,Attributes.mod};
	}

	
	@Override
	public String[] getSupportedNamespaces()
	{
		return supportedNamespaces;
	}

	
	

	@Override
	public Element processClientSideElement() throws Exception
	{
//		CommandExecution commandExecution = new CommandExecution(controlElementDeclaration.getAttribute(Attributes.exec.toString()), controlElementDeclaration.getAttribute(Attributes.timeout.toString()));
//		commandExecution.executeCommand();
//		controlElementDeclaration.setAttribute(Attributes.exitCode.toString(), commandExecution.getExitCode()+"");
//		Element stdoutElement = controlElementDeclaration.getOwnerDocument().createElement(Children.stdout.toString());
//		stdoutElement.setTextContent(commandExecution.getStdout());
//		controlElementDeclaration.appendChild(stdoutElement);
//		Element stderrElement = controlElementDeclaration.getOwnerDocument().createElement(Children.stderr.toString());
//		stderrElement.setTextContent(commandExecution.getStderr());
//		controlElementDeclaration.appendChild(stderrElement);
//		return (Element)(controlElementDeclaration.cloneNode(true));
		return null;
	}


	
	@Override
	public Object processServerSideElement() throws Exception
	{
//		if (controlElementDeclaration.getNamespaceURI().equals(GroupElement.CLIENT_NAMESPACE_URI))
//		{
//			
//			Element newCommandElement = controllerClientRequestProcessor.sendServerSideClientElement((Element) parentGroup.replaceVarsInAttributeValues(controlElementDeclaration.cloneNode(true)));
//			//replace our original element in the document with the newly returned element
//			//this is a shortcut, perhaps we should just merge all of the children into the old element
//			controlElementDeclaration.getParentNode().replaceChild(newCommandElement, controlElementDeclaration);
//			//set our field with the old element to the new element, so nothing goes wonky, and so that any XPath's will have the correct reference
//			controlElementDeclaration = newCommandElement;
//			
//			if (controlElementDeclaration.hasAttribute(Attributes.name.toString()))
//			{
//				parentGroup.set(controlElementDeclaration.getAttribute(Attributes.name.toString()), XPath.getXPath(controlElementDeclaration));
//			}
//		}
//		else
//		{
//			//TODO run server side command
//		}
		
		
		ResourceDescriptor baseResourceDescriptor = getParentGroup().openResourceDescriptor(this, getAttributeValue(Attributes.base));
		
		ResourceDescriptor modResourceDescriptor = getParentGroup().openResourceDescriptor(this, getAttributeValue(Attributes.mod));
		ResourceParameter[] resourceParameters = ResourceParameterBuilder.getResourceParameters(getControlElementDeclaration());
		//we always have to search for the lowest common denominator BIN=>TEXT=>XML
		if (baseResourceDescriptor.getResourceMetaData(getParentGroup(),resourceParameters).getContentFormatType() == ContentFormatType.XML || modResourceDescriptor.getResourceMetaData(getParentGroup(),resourceParameters).getContentFormatType() == ContentFormatType.XML)
		{
			
			Document baseDocument = CapoApplication.getDocumentBuilder().parse(baseResourceDescriptor.getInputStream(getParentGroup(),resourceParameters));
			Document modDocument = CapoApplication.getDocumentBuilder().parse(modResourceDescriptor.getInputStream(getParentGroup(),resourceParameters));
			XMLDiff xmlDiff = new XMLDiff();
			Document differenceDocument = xmlDiff.getDifferences(baseDocument,modDocument);
			getControlElementDeclaration().appendChild(getControlElementDeclaration().getOwnerDocument().adoptNode(differenceDocument.getDocumentElement()));
		}
		else if (baseResourceDescriptor.getResourceMetaData(getParentGroup(),resourceParameters).getContentFormatType() == ContentFormatType.TEXT || modResourceDescriptor.getResourceMetaData(getParentGroup(),resourceParameters).getContentFormatType() == ContentFormatType.TEXT)
		{						
			Diff diff = new Diff(baseResourceDescriptor.getInputStream(getParentGroup(),resourceParameters), modResourceDescriptor.getInputStream(getParentGroup(),resourceParameters));
			XMLTextDiff xmlTextDiff = new XMLTextDiff();
			Element differenceElement = xmlTextDiff.getDifferenceElement(diff);
			getControlElementDeclaration().appendChild(getControlElementDeclaration().getOwnerDocument().adoptNode(differenceElement));
		}
		else //assume both are BINARY at this point
		{
			//TODO going to need to set a token list for this
			Diff diff = new Diff(baseResourceDescriptor.getInputStream(getParentGroup(),resourceParameters), modResourceDescriptor.getInputStream(getParentGroup(),resourceParameters));
		}
		baseResourceDescriptor.close(getParentGroup(),resourceParameters);
		modResourceDescriptor.close(getParentGroup(),resourceParameters);
		
		return null;

	}

	
}
