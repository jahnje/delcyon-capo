package com.delcyon.capo.controller.elements;

import com.delcyon.capo.controller.AbstractControl;
import com.delcyon.capo.controller.ControlElementProvider;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceParameterBuilder;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.State;

@ControlElementProvider(name="step") 
public class StepElement extends AbstractControl
{

	private enum Attributes
	{
		resource,
		until,
		timeout
	}
	
	public enum Parameters
	{
		UNTIL,
		TIMEOUT
	}
	
	private static final String[] supportedNamespaces = {GroupElement.SERVER_NAMESPACE_URI};
		
	@Override
	public Attributes[] getAttributes()
	{
		return Attributes.values();
	}

	
	
	@Override
	public Attributes[] getRequiredAttributes()
	{
		return new Attributes[]{Attributes.resource};
	}

	
	@Override
	public String[] getSupportedNamespaces()
	{
		return supportedNamespaces;
	}

	
	@Override
	public Object processServerSideElement() throws Exception
	{
		Boolean result = false;
		
		String resource = getAttributeValue(Attributes.resource);
		if (resource.startsWith("resource:") == false)
		{
			resource = "resource:"+resource;
		}
		
		ResourceDescriptor resourceDescriptor = getParentGroup().getResourceDescriptor(this, resource);
		ResourceParameterBuilder resourceParameterBuilder = new ResourceParameterBuilder();
		resourceParameterBuilder.addAll(getControlElementDeclaration());
		
		String until = getAttributeValue(Attributes.until);
		if (until.isEmpty() == false)
		{
			resourceParameterBuilder.addParameter(Parameters.UNTIL, until);
		}
		
		String timeout = getAttributeValue(Attributes.timeout);
		if (timeout.isEmpty() == false && timeout.matches("\\d+"))
		{
			resourceParameterBuilder.addParameter(Parameters.TIMEOUT, timeout);
		}
		
		
		if (resourceDescriptor.getResourceState() == State.OPEN)
		{
			resourceDescriptor.open(getParentGroup(),resourceParameterBuilder.getParameters());
		}
		if (resourceDescriptor.getResourceState() == State.STEPPING)
		{			
			result = resourceDescriptor.next(getParentGroup(),resourceParameterBuilder.getParameters());
		}
		return result;

	}

}
