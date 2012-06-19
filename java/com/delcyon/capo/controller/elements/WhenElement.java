package com.delcyon.capo.controller.elements;

import java.util.logging.Level;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.AbstractControl;
import com.delcyon.capo.controller.ControlElementProvider;
import com.delcyon.capo.server.CapoServer;
import com.delcyon.capo.xml.XPath;

@ControlElementProvider(name="when")
public class WhenElement extends AbstractControl
{

	private enum Attributes
	{
		test
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

	@Override
	public Object processServerSideElement() throws Exception
	{
		Boolean result = false;
		Object testResult = XPath.selectSingleNode(getControlElementDeclaration().getParentNode(), getParentGroup().processVars(getAttributeValue(Attributes.test)),getControlElementDeclaration().getPrefix());
		if (testResult != null)
		{
			CapoServer.logger.log(Level.FINE,"Test returned true ");
			processChildren(getControlElementDeclaration().getChildNodes(), getParentGroup(), this, getControllerClientRequestProcessor());			
			result = true;
		}
		return result;

	}

}
