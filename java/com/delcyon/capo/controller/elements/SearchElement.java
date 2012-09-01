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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.AbstractControl;
import com.delcyon.capo.controller.ControlElement;
import com.delcyon.capo.controller.ControlElementProvider;
import com.delcyon.capo.controller.Group;
import com.delcyon.capo.controller.elements.SearchTypeElement.Attributes;
import com.delcyon.capo.controller.server.ControllerClientRequestProcessor;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.xml.XPath;
import com.delcyon.capo.xml.dom.ResourceDocumentBuilder;

@ControlElementProvider(name="search")
public class SearchElement extends AbstractControl
{

	public enum Attributes
	{
		name,uri
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
		return Attributes.values();
	}

	
	@Override
	public String[] getSupportedNamespaces()
	{
		return supportedNamespaces;
	}

	private String groupName;
	private Group group;
	
	@Override
	public void init(Element controlElementDeclaration, ControlElement parentControlElement, Group parentGroup, ControllerClientRequestProcessor controllerClientRequestProcessor) throws Exception
	{
		super.init(controlElementDeclaration, parentControlElement, parentGroup, controllerClientRequestProcessor);
		this.groupName = controlElementDeclaration.getAttribute(Attributes.name.toString());
		this.group = new Group(groupName,parentGroup,null,controllerClientRequestProcessor);
	}
	
	@Override
	public Object processServerSideElement() throws Exception
	{
	    ResourceDescriptor resourceDescriptor = group.getResourceDescriptor(this, getAttributeValue(Attributes.uri));
        ResourceDocumentBuilder resourceDocumentBuilder = new ResourceDocumentBuilder();
        Document resourceDocument = resourceDocumentBuilder.buildDocument(resourceDescriptor);// <=== pass in any constraint nodes
        Element styleSheetElement = (Element) XPath.selectNSNode(getControlElementDeclaration(), "xsl:stylesheet", "xsl=http://www.w3.org/1999/XSL/Transform");
        Document transformDocument = null;
        if (styleSheetElement != null)
        {
            transformDocument = CapoApplication.getDocumentBuilder().newDocument();            
            Element transformStyleSheetElement = (Element) transformDocument.adoptNode(styleSheetElement);

            transformDocument.appendChild(transformStyleSheetElement);
            
          //for some reason, we always strip out the parent name space if it's declared in the child, which it has to be, so we add it back in.
            String parentNameSpace = styleSheetElement.getNamespaceURI();
            String parentPrefix = styleSheetElement.getPrefix();
            transformStyleSheetElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:"+parentPrefix, parentNameSpace);
            
        }
        
        if (transformDocument != null)
        {
            TransformerFactory tFactory = TransformerFactory.newInstance();            
            Transformer transformer = tFactory.newTransformer(new DOMSource(transformDocument));
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(resourceDocument), new DOMResult(getControlElementDeclaration()));
        }
        else
        {
            Element resourceElement = resourceDocument.getDocumentElement();
            resourceElement = (Element) getControlElementDeclaration().getOwnerDocument().importNode(resourceElement,true);
            getControlElementDeclaration().appendChild(resourceElement);

        }        
		
		return null;
	}

	
}
