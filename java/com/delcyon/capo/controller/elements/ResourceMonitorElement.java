package com.delcyon.capo.controller.elements;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.AbstractControl;
import com.delcyon.capo.controller.ControlElementProvider;

@ControlElementProvider(name="resourceMonitor") //TODO resourceMonitorElement
public class ResourceMonitorElement extends AbstractControl
{
	private enum Attributes
	{
		ID,expirationInterval,lastAccessTime,pollInterval,type
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
		return new Attributes[]{Attributes.ID};
	}

	
	@Override
	public String[] getSupportedNamespaces()
	{
		return supportedNamespaces;
	}

	
	@Override
	public Object processServerSideElement() throws Exception
	{
		Boolean result = false; //TODO doesn't do anything yet
		
		return result;

	}
	
}
