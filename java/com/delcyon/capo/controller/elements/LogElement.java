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

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.logging.Level;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.delcyon.capo.controller.AbstractControl;
import com.delcyon.capo.controller.ControlElementProvider;
import com.delcyon.capo.server.CapoServer;
import com.delcyon.capo.xml.XPath;

/**
 * @author jeremiah
 *
 */
@ControlElementProvider(name="log")
public class LogElement extends AbstractControl
{

	private enum Attributes
	{
		ref, eval,level,output,message
	}
	
	private enum LogOutputStream
	{
		LOG,
		STDOUT,
		STDERR;				
	}
	
	private static final String[] supportedNamespaces = {GroupElement.SERVER_NAMESPACE_URI,GroupElement.CLIENT_NAMESPACE_URI};
	
	
	public LogElement()
	{	
	}
	
	@Override
	public String[] getSupportedNamespaces()
	{
		return supportedNamespaces;
	}

	@Override
	public Attributes[] getAttributes()
	{
		return Attributes.values();
	}
	
	
	@Override
	public Attributes[] getRequiredAttributes()
	{
		return new Attributes[]{};
	}
	
	
	@Override
	public Object processServerSideElement() throws Exception
	{
		Element logElement = getControlElementDeclaration();
		Level level = Level.INFO;
		LogOutputStream logOutputStream = LogOutputStream.STDOUT;
		String levelString = getAttributeValue(Attributes.level);
		if (levelString.isEmpty() == false)
		{			
			level = Level.parse(levelString);
		}
		
		String outputString = getAttributeValue(Attributes.output);
		if (outputString.isEmpty() == false)
		{
			logOutputStream = LogOutputStream.valueOf(outputString);
		}
		
		String message = getAttributeValue(Attributes.message);
		if (logElement.getTextContent() != null)
		{
			message += logElement.getTextContent();
		}
				
		String path = "Unknown Path";
		try
		{
			path = XPath.getXPath(logElement);
		}
		catch (Exception e)
		{	
			e.printStackTrace();
			if (getParentGroup() != null)
			{
				path = getParentGroup().getGroupPath();
			}			
		}
		String header = ""+new Date()+" ["+path+"]\n"+level.getName()+": ";
		
		
		if (logElement.hasAttribute(Attributes.ref.toString()))
		{
			Node referencedNode = XPath.selectSingleNode(logElement, getAttributeValue(Attributes.ref));
			if (referencedNode != null)
			{
				ByteArrayOutputStream referenceOutputStream = new ByteArrayOutputStream();
				XPath.dumpNode(referencedNode, referenceOutputStream);
				message += "\n"+referenceOutputStream.toString();
			}
		}
		
		if (logElement.hasAttribute(Attributes.eval.toString()))
		{
			Node referencedNode = XPath.selectSingleNode(logElement, getAttributeValue(Attributes.eval));
			if (referencedNode != null)
			{
				message += "\n"+referencedNode.getTextContent();
			}
		}
		
		switch (logOutputStream)
		{
			case LOG:				
				CapoServer.logger.log(level,message);
				break;
			case STDERR:
				
				if (level.intValue() >= CapoServer.LOGGING_LEVEL.intValue())
				{
					System.err.println(header+message);					
				}
				break;
			case STDOUT:
				if (level.intValue() >= CapoServer.LOGGING_LEVEL.intValue())
				{
					System.out.println(header+message);
				}
				break;			
		}
		return null;
	}
	
}
