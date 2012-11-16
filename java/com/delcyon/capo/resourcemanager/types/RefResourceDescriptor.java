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
package com.delcyon.capo.resourcemanager.types;

import com.delcyon.capo.ContextThread;
import com.delcyon.capo.controller.ControlElement;
import com.delcyon.capo.resourcemanager.ContentFormatType;
import com.delcyon.capo.resourcemanager.ResourceParameter;
import com.delcyon.capo.resourcemanager.types.ContentMetaData.Attributes;
import com.delcyon.capo.resourcemanager.types.RefResourceType.Parameters;
import com.delcyon.capo.xml.XPath;
import com.delcyon.capo.xml.cdom.CElement;
import com.delcyon.capo.xml.cdom.VariableContainer;
import com.delcyon.capo.xml.dom.ResourceDeclarationElement;

/**
 * @author jeremiah
 *
 */
public class RefResourceDescriptor extends AbstractResourceDescriptor
{

	
	private ControlElement contextControlElement = null;
	private CElement refElement = null;

	@Override
	protected SimpleContentMetaData buildResourceMetaData(VariableContainer variableContainer,ResourceParameter... resourceParameters)
	{
		SimpleContentMetaData simpleContentMetaData  = new SimpleContentMetaData(getResourceURI());
		simpleContentMetaData.addSupportedAttribute(Attributes.exists,Attributes.readable,Attributes.writeable,Attributes.container);		
		simpleContentMetaData.setValue(Attributes.exists,true);
		simpleContentMetaData.setValue(Attributes.readable,true);
		simpleContentMetaData.setValue(Attributes.writeable,false);
		simpleContentMetaData.setValue(Attributes.container,false);
		simpleContentMetaData.setContentFormatType(ContentFormatType.XML);
		simpleContentMetaData.setValue("mimeType","aplication/xml");
        simpleContentMetaData.setValue("MD5","");        
        simpleContentMetaData.setValue("size","0");
		return simpleContentMetaData;
	}
	
	@Override
	public void init(ResourceDeclarationElement declaringResourceElement, VariableContainer variableContainer, LifeCycle lifeCycle, boolean iterate, ResourceParameter... resourceParameters) throws Exception
	{		
		super.init(declaringResourceElement, variableContainer, lifeCycle, iterate, resourceParameters);
		
		
		if(Thread.currentThread() instanceof ContextThread && ((ContextThread)Thread.currentThread()).getContext() != null && ((ContextThread)Thread.currentThread()).getContext() instanceof ControlElement)
		{
			contextControlElement = (ControlElement) ((ContextThread)Thread.currentThread()).getContext();
			
		}
		
	}
	
	@Override
	protected Action[] getSupportedActions()
	{
		return new Action[]{};
	}

	
	@Override
	public ContentMetaData getContentMetaData(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		return buildResourceMetaData(variableContainer,resourceParameters);
	}

	@Override
	public ContentMetaData getOutputMetaData(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{	 
	    return null;
	}
	
	@Override
	public StreamFormat[] getSupportedStreamFormats(StreamType streamType) throws Exception
	{
		if (streamType == StreamType.INPUT)
		{
			return new StreamFormat[]{StreamFormat.XML_BLOCK};
		}		
		else
		{
			return null;
		}
	}

	@Override
	public StreamType[] getSupportedStreamTypes() throws Exception
	{
		return new StreamType[]{StreamType.INPUT};
	}

	public void setContextControlElement(ControlElement conextControlElement)
	{
	    this.contextControlElement = conextControlElement;
	}
	
	@Override
	protected void clearContent()
	{
	   refElement = null;	    
	}
	
	@Override
	public boolean next(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
	    advanceState(State.OPEN, variableContainer, resourceParameters);
        addResourceParameters(variableContainer, resourceParameters);
        
        if(contextControlElement != null && getResourceState() == State.OPEN)
        {
            setResourceState(State.STEPPING);
            if (getVarValue(variableContainer, Parameters.XMLNS) != null)
            {
                refElement =  (CElement) XPath.selectNSNode(contextControlElement.getControlElementDeclaration(), getResourceURI().getSchemeSpecificPart(),getVarValue(variableContainer, Parameters.XMLNS).split(","));
                return true;
            }
            else
            { 
                refElement =  (CElement) XPath.selectSingleNode(contextControlElement.getControlElementDeclaration(), getResourceURI().getSchemeSpecificPart());
                return true;
            }
        }
        else
        {
            setResourceState(State.OPEN);
            refElement = null;
            return false;
        }
	}
	
	@Override
	public CElement readXML(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{	
	    advanceState(State.STEPPING, variableContainer, resourceParameters);
		return refElement;
	}
	
}
