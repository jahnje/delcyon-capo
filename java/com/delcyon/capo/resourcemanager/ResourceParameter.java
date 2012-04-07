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

import com.delcyon.capo.util.ReflectionUtility;

/**
 * @author jeremiah
 *
 */
public class ResourceParameter
{
	/**
	 * Used to determine where this parameter was declared, so we can change them depending on scope.
	 * @author jeremiah
	 *
	 */
	public enum Source
	{
		/** from a ResourceElement */
		DECLARATION,
		/** from an element where the resource is going to be used. */
		CALL
	}
	
	/**
	 * Used to determine when we should process any of the variables in this parameter
	 * @author jeremiah
	 *
	 */
	public enum EvaluationContext
	{
		/**
		 * Right now when it was declared. This is the default
		 */
		NOW,
		/**
		 * Evaluate when being used by the caller (getInputStream()|getOutputStream())
		 */
		DELAYED
	}
	
	public static final String ELEMENT_NAME = "parameter"; //TODO don't like this name, should we use a namespace on the resource/parameters?
	public static final String NAME_ATTRIBUTE_NAME = "name";
	public static final String VALUE_ATTRIBUTE_NAME = "value";
	
	private String name = null;
	private String value = null;
	private String lastValue = null;
	private Source source = null;
	private EvaluationContext evaluationContext = null;
	
	@SuppressWarnings("unused")
	private ResourceParameter(){}; //here for serialization
	
	@SuppressWarnings("unchecked")
	public ResourceParameter(String name, Enum value, Source source, EvaluationContext evaluationContext)
	{	
		this.name = name;
		this.value = value.toString();
		this.source = source;
		this.evaluationContext = evaluationContext;
	}
	
	@SuppressWarnings("unchecked")
	public ResourceParameter(Enum name, Enum value, Source source)
	{	
		this.name = name.toString();
		this.value = value.toString();
		this.source = source;
		this.evaluationContext = EvaluationContext.NOW;
	}
	
	@SuppressWarnings("unchecked")
	public ResourceParameter(Enum name, Enum value, Source source, EvaluationContext evaluationContext)
	{	
		this.name = name.toString();
		this.value = value.toString();
		this.source = source;
		this.evaluationContext = evaluationContext;
	}
	
	@SuppressWarnings("unchecked")
	public ResourceParameter(Enum name, String value, Source source, EvaluationContext evaluationContext)
	{	
		this.name = name.toString();
		this.value = value;
		this.source = source;
		this.evaluationContext = evaluationContext;
	}
	
	public ResourceParameter(String name, String value, Source source, EvaluationContext evaluationContext)
	{	
		this.name = name;
		this.value = value;
		this.source = source;
		this.evaluationContext = evaluationContext;
	}
	
	@SuppressWarnings("unchecked")
	public ResourceParameter(Enum name, String value)
	{	
		this.name = name.toString();
		this.value = value;
		this.source = Source.CALL;
		this.evaluationContext = EvaluationContext.NOW;
	}
	
	@SuppressWarnings("unchecked")
    public ResourceParameter(Enum name, Enum value)
    {   
        this.name = name.toString();
        this.value = value.toString();
        this.source = Source.CALL;
        this.evaluationContext = EvaluationContext.NOW;
    }
	
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public String getValue()
	{
		return value;
	}
	public void setValue(String value)
	{
		//only swap if there is something going on, and they are actually different
		if (this.value != null && value != null && this.value.equals(value) == false)
		{			
			this.lastValue = this.value;
		}		
		this.value = value;
	}
	
	public void resetValue()
	{
		if (lastValue != null)
		{
			this.value = lastValue;
		}
	}
	
	public Source getSource()
	{
		return source;
	}
	
	public void setSource(Source source)
	{
		this.source = source;
	}
	
	public EvaluationContext getEvaluationContext()
	{
		return evaluationContext;
	}
	
	public void setEvaluationContext(EvaluationContext evaluationContext)
	{
		this.evaluationContext = evaluationContext;
	}
	
	@Override
	public String toString()
	{
		return ReflectionUtility.processToString(this);
	}
}
