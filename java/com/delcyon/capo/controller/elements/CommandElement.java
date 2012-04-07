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

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.delcyon.capo.controller.AbstractClientSideControl;
import com.delcyon.capo.controller.ControlElementProvider;
import com.delcyon.capo.controller.client.ClientSideControl;
import com.delcyon.capo.util.CommandExecution;
import com.delcyon.capo.xml.XPath;
import com.delcyon.capo.xml.XPathFunctionProcessor;
import com.delcyon.capo.xml.XPathFunctionProvider;
import com.delcyon.capo.xml.XPathFunctionUtility;

/**
 * @author jeremiah
 *
 */
@ControlElementProvider(name="command")
@XPathFunctionProvider
public class CommandElement extends AbstractClientSideControl implements ClientSideControl, XPathFunctionProcessor
{

	private enum Children
	{
		stdout,
		stderr
	}
	
	private enum Attributes
	{
		name,exec, timeout,exitCode
	}
	
	
	private static final String[] supportedNamespaces = {GroupElement.SERVER_NAMESPACE_URI,GroupElement.CLIENT_NAMESPACE_URI};
	
	private static final String[] functionNames = {"command"};
	
	@Override
	public Attributes[] getAttributes()
	{
		return Attributes.values();
	}
	
	@Override
	public Attributes[] getRequiredAttributes()
	{
		return new Attributes[]{Attributes.exec};
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
		if (functionName.equals("command"))
		{
			return XPath.selectSingleNode(contextNode, "//"+prefix+":command[@name = '"+arguments[0]+"']");
		}
		else
		{
			return null;
		}
	}

	@Override
	public Element processClientSideElement() throws Exception
	{
		CommandExecution commandExecution = new CommandExecution(getAttributeValue(Attributes.exec), getAttributeValue(Attributes.timeout));
		commandExecution.executeCommand();
		getControlElementDeclaration().setAttribute(Attributes.exitCode.toString(), commandExecution.getExitCode()+"");
		Element stdoutElement = getControlElementDeclaration().getOwnerDocument().createElement(Children.stdout.toString());
		stdoutElement.setTextContent(commandExecution.getStdout());
		getControlElementDeclaration().appendChild(stdoutElement);
		Element stderrElement = getControlElementDeclaration().getOwnerDocument().createElement(Children.stderr.toString());
		stderrElement.setTextContent(commandExecution.getStderr());
		getControlElementDeclaration().appendChild(stderrElement);
		return (Element)(getControlElementDeclaration().cloneNode(true));
	}
	
	
	@Override
	public Object processServerSideElement() throws Exception
	{
		if (getControlElementDeclaration().getNamespaceURI().equals(GroupElement.CLIENT_NAMESPACE_URI))
		{
			
			Element newCommandElement = getControllerClientRequestProcessor().sendServerSideClientElement((Element) getParentGroup().replaceVarsInAttributeValues(getControlElementDeclaration().cloneNode(true)));
			//replace our original element in the document with the newly returned element
			//this is a shortcut, perhaps we should just merge all of the children into the old element
			getOriginalControlElementDeclaration().getParentNode().replaceChild(newCommandElement, getOriginalControlElementDeclaration());
			//set our field with the old element to the new element, so nothing goes wonky, and so that any XPath's will have the correct reference
			setControlElementDeclaration(newCommandElement);
			
			if (getControlElementDeclaration().hasAttribute(Attributes.name.toString()))
			{
				getParentGroup().set(getAttributeValue(Attributes.name), XPath.getXPath(getControlElementDeclaration()));
			}
		}
		else
		{
			CommandExecution commandExecution = new CommandExecution(getAttributeValue(Attributes.exec), getAttributeValue(Attributes.timeout));
			commandExecution.executeCommand();
			getControlElementDeclaration().setAttribute(Attributes.exitCode.toString(), commandExecution.getExitCode()+"");
			Element stdoutElement = getControlElementDeclaration().getOwnerDocument().createElement(Children.stdout.toString());
			stdoutElement.setTextContent(commandExecution.getStdout());
			getControlElementDeclaration().appendChild(stdoutElement);
			Element stderrElement = getControlElementDeclaration().getOwnerDocument().createElement(Children.stderr.toString());
			stderrElement.setTextContent(commandExecution.getStderr());
			getControlElementDeclaration().appendChild(stderrElement);
			return (Element)(getControlElementDeclaration().cloneNode(true));
		}
		
		return null;

	}

	
}
