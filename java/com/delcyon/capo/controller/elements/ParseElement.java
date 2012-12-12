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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.Configuration.PREFERENCE;
import com.delcyon.capo.controller.AbstractControl;
import com.delcyon.capo.controller.ControlElementProvider;
import com.delcyon.capo.parsers.GrammerParser;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceParameter;
import com.delcyon.capo.resourcemanager.ResourceParameter.Source;
import com.delcyon.capo.resourcemanager.ResourceParameterBuilder;
import com.delcyon.capo.resourcemanager.types.FileResourceType;
import com.delcyon.capo.resourcemanager.types.RefResourceDescriptor;

/**
 * @author jeremiah
 *
 */
@ControlElementProvider(name="parse")
public class ParseElement extends AbstractControl
{

	
	
	private enum Attributes
	{
		name,src,grammar, srcRef
	}
	
	
	
	private static final String[] supportedNamespaces = {CapoApplication.SERVER_NAMESPACE_URI};

	
	
	
	@Override
	public Attributes[] getAttributes()
	{
		return Attributes.values();
	}
	
	@Override
	public Attributes[] getRequiredAttributes()
	{
		return new Attributes[]{Attributes.name,Attributes.src,Attributes.grammar};
	}
	
	@Override
	public String[] getSupportedNamespaces()
	{
		return supportedNamespaces;
	}

	
	@Override
	public Object processServerSideElement() throws Exception
	{
		ResourceParameter[] resourceParameters = ResourceParameterBuilder.getResourceParameters(getControlElementDeclaration());
		String src = getAttributeValue(Attributes.src);		   
		String grammar = getAttributeValue(Attributes.grammar);		
		String name = getAttributeValue(Attributes.name);		
		String type = null;
		
		ResourceDescriptor grammarResourceDescriptor = getParentGroup().getResourceDescriptor(this, grammar);
        if (grammarResourceDescriptor == null)
        {
            throw new Exception("grammar="+grammar+" not found");
        }
        grammarResourceDescriptor.addResourceParameters(getParentGroup(), new ResourceParameter(FileResourceType.Parameters.PARENT_PROVIDED_DIRECTORY,PREFERENCE.RESOURCE_DIR,Source.CALL));
        grammarResourceDescriptor.open(getParentGroup());
        if(grammarResourceDescriptor.getResourceMetaData(getParentGroup()).exists() == false)
        {
            throw new Exception("no resource found at "+grammarResourceDescriptor.getResourceURI().getResourceURIString());
        }
		
		
		ResourceDescriptor srcResourceDescriptor = getParentGroup().getResourceDescriptor(this, src);
		if (srcResourceDescriptor == null)
		{
			throw new Exception("src="+src+" not found");
		}
		srcResourceDescriptor.addResourceParameters(getParentGroup(), new ResourceParameter(FileResourceType.Parameters.PARENT_PROVIDED_DIRECTORY,PREFERENCE.RESOURCE_DIR,Source.CALL));
		srcResourceDescriptor.open(getParentGroup());
		if(srcResourceDescriptor.getResourceMetaData(getParentGroup()).exists() == false)
		{
		    throw new Exception("no resource found at "+srcResourceDescriptor.getResourceURI().getResourceURIString());
		}
		
		
		
		GrammerParser grammerParser = new GrammerParser();
		grammerParser.loadGrammer(grammarResourceDescriptor.getInputStream(getParentGroup()));
		InputStream srcInputStream = null;
		if(srcResourceDescriptor instanceof RefResourceDescriptor)
		{
		    srcInputStream = new ByteArrayInputStream(srcResourceDescriptor.readXML(getParentGroup()).getTextContent().getBytes());
		}
		else
		{
		  srcInputStream = srcResourceDescriptor.getInputStream(getParentGroup());
		}
		Document parsedDocument =  grammerParser.parse(srcInputStream);
		
		
		if (parsedDocument != null)
		{
			Element parentElement = getControlElementDeclaration();
					
			parentElement.appendChild(getControlElementDeclaration().getOwnerDocument().importNode(parsedDocument.getDocumentElement(),true));			
		}
		
		
		return null;
	}

	
}
