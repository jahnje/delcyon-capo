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
package com.delcyon.capo.resourcemanager;

import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.delcyon.capo.resourcemanager.ResourceParameter.EvaluationContext;
import com.delcyon.capo.resourcemanager.ResourceParameter.Source;
import com.delcyon.capo.util.ReflectionUtility;
import com.delcyon.capo.xml.XPath;

/**
 * @author jeremiah
 *
 */
public class ResourceParameterBuilder
{

	private ArrayList<ResourceParameter> resourceParameterArrayList = new ArrayList<ResourceParameter>();
	private Source source = Source.CALL;
	private EvaluationContext evaluationContext = EvaluationContext.NOW;
	
	public static ResourceParameter[] getResourceParameters(Node parentNode) throws Exception
	{
		ResourceParameterBuilder parameterBuilder = new ResourceParameterBuilder();
		parameterBuilder.addAll(parentNode);
		return parameterBuilder.getParameters();		
	}
	
	public void addAll(Node node) throws Exception
	{
		NodeList nodeList = XPath.selectNSNodes(node, "descendant::resource:parameter", "resource=http://www.delcyon.com/capo/resource");
		for(int index = 0; index < nodeList.getLength(); index++)
		{
			Node parameterNode = nodeList.item(index);
			if (parameterNode instanceof Element)
			{
				Element parameterElement = (Element) parameterNode;
				if (parameterElement.hasAttribute("name") && parameterElement.hasAttribute("value"))
				{
					EvaluationContext evaluationContext = this.evaluationContext;
					if (parameterElement.hasAttribute("context"))
					{
						evaluationContext = EvaluationContext.valueOf(parameterElement.getAttribute("context"));
					}
						
					ResourceParameter resourceParameter = new ResourceParameter(parameterElement.getAttribute("name"),parameterElement.getAttribute("value"),source,evaluationContext);
					resourceParameterArrayList.add(resourceParameter);
				}
			}
		}
	}

	public void addParameter(ResourceParameter resourceParameter)
	{
		resourceParameterArrayList.add(resourceParameter);
	}
	
	public void addParameter(String name,String value)
	{
		resourceParameterArrayList.add(new ResourceParameter(name, value,source, evaluationContext));
	}
	
	@SuppressWarnings("unchecked")
	public void addParameter(Enum name, String value)
	{
		addParameter(name.toString(), value);
		
	}
	
	public ResourceParameter[] getParameters()
	{
		return resourceParameterArrayList.toArray(new ResourceParameter[]{});
	}
	
	@Override
	public String toString()
	{
		return ReflectionUtility.processToString(this);
	}

	public void setSource(Source source)
	{
		this.source  = source;
	}
	
	public Source getSource()
	{
		return source;
	}
	
	public EvaluationContext getEvaluationContext()
	{
		return evaluationContext;
	}
	
	public void setEvaluationContext(EvaluationContext evaluationContext)
	{
		this.evaluationContext = evaluationContext;
	}

	public void addAll(ResourceParameter... resourceParameters)
	{
		for (ResourceParameter resourceParameter : resourceParameters)
		{
			resourceParameterArrayList.add(resourceParameter);
		}
		
	}

	@SuppressWarnings("unchecked")
	public static boolean getBoolean(Enum parameter, ResourceParameter[] resourceParameters)
	{
		for (ResourceParameter resourceParameter : resourceParameters)
		{
			if (resourceParameter.getName().equals(parameter.toString()))
			{
				if(resourceParameter.getValue() != null && resourceParameter.getValue().equalsIgnoreCase("true"))
				{
					return true;
				}
				else
				{
					return false;
				}
			}
		}
		return false;
	}

	
}
