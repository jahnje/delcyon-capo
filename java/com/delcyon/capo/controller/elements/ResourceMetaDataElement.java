package com.delcyon.capo.controller.elements;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import org.w3c.dom.Element;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.AbstractControl;
import com.delcyon.capo.controller.ControlElementProvider;
import com.delcyon.capo.datastream.NullOutputStream;
import com.delcyon.capo.datastream.StreamUtil;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceParameter;
import com.delcyon.capo.resourcemanager.ResourceParameterBuilder;
import com.delcyon.capo.resourcemanager.types.ContentMetaData;
import com.delcyon.capo.server.CapoServer;

@ControlElementProvider(name="resourceMetaData") //TODO contentMetatDataElement
public class ResourceMetaDataElement extends AbstractControl
{

	
	
	public enum Attributes
	{
		name,
		resource,
		forceOpen,
		forceRead,
		useLastRead,
		depth,
		useRelativePaths,
		attributes, requiredAttributes
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
		
		String resource = getAttributeValue(Attributes.resource);
		
		ResourceParameterBuilder resourceParameterBuilder = new ResourceParameterBuilder();
		resourceParameterBuilder.addAll(getControlElementDeclaration());
		
		if (getAttributeValue(Attributes.depth).isEmpty() == false)
		{
			resourceParameterBuilder.addParameter(ContentMetaData.Parameters.DEPTH, getAttributeValue(Attributes.depth));
		}
		
		if (getAttributeValue(Attributes.useRelativePaths).equalsIgnoreCase("true"))
		{
			resourceParameterBuilder.addParameter(ContentMetaData.Parameters.USE_RELATIVE_PATHS, getAttributeValue(Attributes.useRelativePaths).toLowerCase());
			resourceParameterBuilder.addParameter(ContentMetaData.Parameters.ROOT_PATH, resource);
		}
		
		ResourceParameter[] resourceParameters = resourceParameterBuilder.getParameters();
		
		Boolean result = false;

		//get attributes to display
		String[] attributes = null;
		if (getAttributeValue(Attributes.attributes).isEmpty() == false)
		{
			attributes = getAttributeValue(Attributes.attributes).split(",");
		}

		ResourceDescriptor resourceDescriptor = null;
		if (getAttributeValue(Attributes.forceOpen).equalsIgnoreCase("true"))
		{
			resourceDescriptor = getParentGroup().openResourceDescriptor(this, resource);
		}
		else
		{
			resourceDescriptor = getParentGroup().getResourceDescriptor(this, resource);
		}
		if (resourceDescriptor != null)
		{
			if (getAttributeValue(Attributes.forceRead).equalsIgnoreCase("true"))
			{
				StreamUtil.readInputStreamIntoOutputStream(resourceDescriptor.getInputStream(getParentGroup(),resourceParameters),new NullOutputStream());
			}
			ContentMetaData contentMetaData = null;
			if (getAttributeValue(Attributes.useLastRead).equalsIgnoreCase("true"))
			{
				StreamUtil.readInputStreamIntoOutputStream(resourceDescriptor.getInputStream(getParentGroup(),resourceParameters),new NullOutputStream());
				contentMetaData = resourceDescriptor.getIterationMetaData(getParentGroup(),resourceParameters);
			}
			else
			{
				contentMetaData = resourceDescriptor.getContentMetaData(getParentGroup(),resourceParameters);
			}
			resourceDescriptor.close(getParentGroup(), resourceParameters);

			buildElementContent(contentMetaData,getControlElementDeclaration(),attributes);			
		}
		else
		{
			CapoServer.logger.log(Level.SEVERE," no resource found matching: "+getAttributeValue(Attributes.resource));
		}

		return result;

	}

	private void buildElementContent(ContentMetaData contentMetaData, Element containerElement, String... attributes)
	{
		
		
		List<String> supportedAttributeList = contentMetaData.getSupportedAttributes();
		for (String attributeName : supportedAttributeList)
		{
			if (attributes != null && Arrays.binarySearch(attributes, attributeName) < 0)
			{
				continue;
			}
			
			if (contentMetaData.getValue(attributeName) != null)
			{
				containerElement.setAttribute(attributeName, contentMetaData.getValue(attributeName));
				
			}
		}
		if (contentMetaData.isContainer())
		{
			for (ContentMetaData childContentMetaData : contentMetaData.getContainedResources())
			{
				if (getAttributeValue(Attributes.requiredAttributes).isEmpty() == false)
				{
					String[] requiredAttributes = getAttributeValue(Attributes.requiredAttributes).split(",");
					boolean foundAllAttributes = true;
					for (String requiredAttribute : requiredAttributes)
					{
						if (childContentMetaData.getValue(requiredAttribute) == null)
						{
							foundAllAttributes = false;
							break;
						}
					}
					if (foundAllAttributes == false)
					{
						continue;
					}
				}
				
				
				Element resourceElement = containerElement.getOwnerDocument().createElement("resource");
				if (childContentMetaData.getResourceURI() == null)
				{
					System.err.println("got here");
				}
				else
				{
					resourceElement.setAttribute("uri", childContentMetaData.getResourceURI().getResourceURIString());
				}
				//resourceElement.setAttribute("uri", resourceDescriptor.isRemoteResource() ? "remote:"+ childContentMetaData.getResourceURI().toString() : childContentMetaData.getResourceURI().toString());
				containerElement.appendChild(resourceElement);
				buildElementContent(childContentMetaData, resourceElement,attributes);
			}
		}
	}
}
