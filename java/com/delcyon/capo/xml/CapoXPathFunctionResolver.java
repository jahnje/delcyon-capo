package com.delcyon.capo.xml;

import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathFunction;

import com.delcyon.capo.CapoApplication;

public class CapoXPathFunctionResolver implements javax.xml.xpath.XPathFunctionResolver
{
	private HashMap<String, XPathFunctionProcessor> xpathFunctionProcessorHashMap = null;
	public CapoXPathFunctionResolver()
	{
		
		this.xpathFunctionProcessorHashMap = new HashMap<String, XPathFunctionProcessor>();
		
		if (CapoApplication.getAnnotationMap() != null)
		{
			Set<String> xPathFunctionProviderSet =  CapoApplication.getAnnotationMap().get(XPathFunctionProvider.class.getCanonicalName());
			for (String className : xPathFunctionProviderSet)
			{
				try
				{
					Object object = Class.forName(className).newInstance();
					XPathFunctionProcessor xPathFunctionProcessor = (XPathFunctionProcessor) object;
					String[] functionNames = xPathFunctionProcessor.getXPathFunctionNames();
					for (String functionName : functionNames)
					{
						xpathFunctionProcessorHashMap.put(functionName, xPathFunctionProcessor);
						CapoApplication.logger.log(Level.CONFIG, "Loaded XPathFunctionProcessor "+functionName+"() from "+xPathFunctionProcessor.getClass().getSimpleName());
					}
				} catch (Exception e)
				{
					CapoApplication.logger.log(Level.WARNING, "Couldn't load "+className+" as an XPathFunctionProcessor", e);
				}
			}
			
			
			
		}
	}

	public XPathFunction resolveFunction(QName functionName, int arity)
	{
		if (xpathFunctionProcessorHashMap.containsKey(functionName.getLocalPart()))
		{
			return new CapoXPathFunction(functionName.getLocalPart(),xpathFunctionProcessorHashMap.get(functionName.getLocalPart()));
		}
		return null;
	}

}
