package com.delcyon.capo.controller.elements;

import com.delcyon.capo.controller.AbstractControl;
import com.delcyon.capo.controller.ControlElementProvider;

@ControlElementProvider(name="open") 
public class OpenElement extends AbstractControl
{

	private enum Attributes
	{
		resource
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
		getParentGroup().openResourceDescriptor(this, resource);
		return result;

	}

}
