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

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.annotations.XmlMappedArrays;
import com.delcyon.capo.datastream.NullOutputStream;
import com.delcyon.capo.datastream.OutputStreamAttributeFilterProvider;
import com.delcyon.capo.datastream.StreamUtil;
import com.delcyon.capo.datastream.stream_attribute_filter.InputStreamAttributeFilterProvider;
import com.delcyon.capo.datastream.stream_attribute_filter.MD5FilterInputStream;
import com.delcyon.capo.datastream.stream_attribute_filter.SizeFilterInputStream;
import com.delcyon.capo.datastream.stream_attribute_filter.StreamAttributeFilter;
import com.delcyon.capo.resourcemanager.ContentFormatType;
import com.delcyon.capo.resourcemanager.ResourceParameter;
import com.delcyon.capo.resourcemanager.ResourceURI;
import com.delcyon.capo.util.CloneControl;
import com.delcyon.capo.util.CloneControl.Clone;
import com.delcyon.capo.util.ControlledClone;
import com.delcyon.capo.util.ReflectionUtility;
import com.delcyon.capo.util.ToStringControl;
import com.delcyon.capo.util.ToStringControl.Control;

/**
 * @author jeremiah
 *
 */
@SuppressWarnings("unchecked")
@ToStringControl(control=Control.exclude,modifiers=Modifier.STATIC)
@XmlMappedArrays(name="MetaDataAttributes",keys="attributeKeys", values="attributeValues")
public abstract class AbstractContentMetaData implements ContentMetaData, ControlledClone
{
    //this is to lower memory usage on common attribute values, slows things a bit, but can drastically reduce memory usage by using constants 
    private static final String[] CommonStrings = new String[]{"0","false","true"};
    
	private static LinkedList<Class> inputStreamAttributeFilterLinkedList = null;
	static
	{
		inputStreamAttributeFilterLinkedList = new LinkedList<Class>();
		Set<String> inputStreamAttributeFilterProviderSet = CapoApplication.getAnnotationMap().get(InputStreamAttributeFilterProvider.class.getCanonicalName());
		if (inputStreamAttributeFilterProviderSet != null) //this is only null during testing
		{
			for (String className : inputStreamAttributeFilterProviderSet)
			{
				try
				{
					Class inputStreamAttributeFilterProviderClass = Class.forName(className);
					InputStreamAttributeFilterProvider inputStreamAttributeFilterProvider = (InputStreamAttributeFilterProvider) inputStreamAttributeFilterProviderClass.getAnnotation(InputStreamAttributeFilterProvider.class);
					if (FilterInputStream.class.isAssignableFrom(inputStreamAttributeFilterProviderClass) == false)
					{
						CapoApplication.logger.log(Level.WARNING, inputStreamAttributeFilterProviderClass.getCanonicalName()+" is not a filterInputStream. SKIPPING");
					}
					else if (StreamAttributeFilter.class.isAssignableFrom(StreamAttributeFilter.class) == false)
					{
						CapoApplication.logger.log(Level.WARNING, inputStreamAttributeFilterProviderClass.getCanonicalName()+" is not a StreamAttributeFilter. SKIPPING");
					}
					else
					{
						inputStreamAttributeFilterLinkedList.add(inputStreamAttributeFilterProviderClass);
						CapoApplication.logger.log(Level.CONFIG, "Loaded InputStreamAttributeFilterProvider "+inputStreamAttributeFilterProvider.name()+" from "+className);
					}
				} catch (ClassNotFoundException classNotFoundException)
				{
					CapoApplication.logger.log(Level.WARNING, "Error getting StreamAttributeFilter providers",classNotFoundException);
				}
			}
		}
	}
	
	private static Vector<Class> outputStreamAttributeFilterVector = null;
	static
	{
		outputStreamAttributeFilterVector = new Vector<Class>();
		Set<String> outputStreamAttributeFilterProviderSet = CapoApplication.getAnnotationMap().get(OutputStreamAttributeFilterProvider.class.getCanonicalName());
		if (outputStreamAttributeFilterProviderSet != null) //this is only null during testing, since we have built-in providers
		{
			for (String className : outputStreamAttributeFilterProviderSet)
			{
				try
				{
					Class outputStreamAttributeFilterProviderClass = Class.forName(className);
					OutputStreamAttributeFilterProvider outputStreamAttributeFilterProvider = (OutputStreamAttributeFilterProvider) outputStreamAttributeFilterProviderClass.getAnnotation(OutputStreamAttributeFilterProvider.class);
					if (FilterOutputStream.class.isAssignableFrom(outputStreamAttributeFilterProviderClass) == false)
					{
						CapoApplication.logger.log(Level.WARNING, outputStreamAttributeFilterProviderClass.getCanonicalName()+" is not a FilterOutputStream. SKIPPING");
					}
					else if (StreamAttributeFilter.class.isAssignableFrom(outputStreamAttributeFilterProviderClass) == false)
					{
						CapoApplication.logger.log(Level.WARNING, outputStreamAttributeFilterProviderClass.getCanonicalName()+" is not a StreamAttributeFilter. SKIPPING");
					}
					else
					{
						outputStreamAttributeFilterVector.add(outputStreamAttributeFilterProviderClass);
						CapoApplication.logger.log(Level.CONFIG, "Loaded OutputStreamAttributeFilterProvider "+outputStreamAttributeFilterProvider.name()+" from "+className);
					}
				} catch (ClassNotFoundException classNotFoundException)
				{
					CapoApplication.logger.log(Level.WARNING, "Error getting OutputStreamAttributeFilter providers",classNotFoundException);
				}
			}
		}
	}
	
	private boolean includeStreamAttributes = false;
	private boolean isInitialized = false;
	private ResourceURI resourceURI = null;
	
	
	@CloneControl(filter=Clone.exclude)
	private transient LinkedList<StreamAttributeFilter> streamAttributeFilterLinkedList = new LinkedList<StreamAttributeFilter>();
	protected LinkedList<ContentMetaData> childContentMetaDataLinkedList = new LinkedList<ContentMetaData>();
	
	
	private String[] attributeKeys = null;
	private String[] attributeValues = null;
	
    private boolean attributesLoaded = false;

    @Override
    public void preClone(Object parentClonedObject, Object clonedObject) throws Exception {};
    @Override
    public void postClone(Object parentClonedObject, Object clonedObject) throws Exception
    {
        internAttributes();
    }
	
    //minimize memory by making sure attribute names, and common values are interned
    private void internAttributes()
    {
        initializeAttributeStorage();
        for (int index = 0; index < attributeKeys.length; index++)
        {
            attributeKeys[index] = attributeKeys[index].intern();
        }
        
        
        for (String commonString : CommonStrings)
        {
            for (int index = 0; index < attributeKeys.length; index++)
            {                
                if(commonString.equals(attributeValues[index]))
                {
                    attributeValues[index] = commonString;
                    break;
                }
            }   
            
        }   
    }
    
	private void initializeAttributeStorage()
	{
	    if(attributeKeys == null)
	    {
	        //make sure we include initial room for the stream attributes 
	        boolean _includeStreamAttributes = includeStreamAttributes;
	        includeStreamAttributes = true;
	        List<String> attributes = getSupportedAttributes();
	        includeStreamAttributes = _includeStreamAttributes;
	        
	        attributeKeys = new String[attributes.size()];
	        attributeValues = new String[attributeKeys.length];
	        for (int index = 0; index < attributeKeys.length; index++)
	        {
	            attributeKeys[index] = attributes.get(index).intern();
	        }
	    }
	}
	
	/**
	 * this will try to initialize md5 and length fields by reading the input stream
	 * If you want to use a byte[] wrap it in a ByteArrayInputStream
	 * This WILL close the stream
	 * @param inputStream
	 * @throws Exception 
	 */
	protected byte[] readInputStream(InputStream inputStream, boolean returnContent) throws Exception
	{
	    this.includeStreamAttributes = true;
	    boolean useHugeBuffer = false;
	    OutputStream outputStream = null;
	    if(returnContent == true)
	    {
	        outputStream = new ByteArrayOutputStream();
	    }
	    else
	    {
	    	useHugeBuffer = true;
	        outputStream = new NullOutputStream();
	    }
		inputStream =  wrapInputStream(inputStream);
		if(useHugeBuffer == false)
		{
			StreamUtil.readInputStreamIntoOutputStream(inputStream, outputStream);
		}
		else
		{
			StreamUtil.readInputStreamIntoOutputStream(inputStream, outputStream,1024000);
		}
		inputStream.close();
		loadAttributes(); //once we've read the file, load up he attribute map with any stream attributes.
		this.isInitialized = true;
		if (returnContent == true)
		{
		    return ((ByteArrayOutputStream) outputStream).toByteArray();
		}
		else
		{
		    return null;
		}
	}
	
	/**
	 * this will try to initialize md5 and length fields by reading the input stream
	 * If you want to use a byte[] wrap it in a ByteArrayInputStream
	 * This does NOT close the stream
	 * @param inputStream
	 * @throws Exception 
	 */
	protected InputStream wrapInputStream(InputStream inputStream) throws Exception
	{
	    this.includeStreamAttributes = true;
		streamAttributeFilterLinkedList.clear();
		for (Class inputStreamAttributeFilterProviderClass : inputStreamAttributeFilterLinkedList)
		{			
			inputStream = (FilterInputStream) inputStreamAttributeFilterProviderClass.getConstructor(InputStream.class).newInstance(inputStream);
			streamAttributeFilterLinkedList.add((StreamAttributeFilter) inputStream);
		}		
		return inputStream;
	}
	
	protected OutputStream wrapOutputStream(OutputStream outputStream) throws Exception
	{
	    this.includeStreamAttributes = true;
		streamAttributeFilterLinkedList.clear();
		setValue(MD5FilterInputStream.ATTRIBUTE_NAME,null);
		setValue(SizeFilterInputStream.ATTRIBUTE_NAME,null);
		setValue(ContentFormatType.ATTRIBUTE_NAME,null);
		for (Class outputStreamAttributeFilterProviderClass : outputStreamAttributeFilterVector)
		{			
			outputStream = (FilterOutputStream) outputStreamAttributeFilterProviderClass.getConstructor(OutputStream.class).newInstance(outputStream);
			streamAttributeFilterLinkedList.add((StreamAttributeFilter) outputStream);
		}		
		return outputStream;
	}
	
	@Override
    public boolean isDynamic()
    {       
        if(includeStreamAttributes == true)
        {
            return false;
        }
        else
        {
            return true;
        }
    }
    
	
	@Override
	public boolean isInitialized()
	{
		return this.isInitialized;
	}
	
	@Override
	public void setInitialized(boolean isInitialized)
	{
		this.isInitialized = isInitialized;		
	}
	
	@Override
	public void clearAttributes()
	{
	    if(attributesLoaded != false)
	    {
	        attributeValues = new String[attributeKeys.length];
	        attributesLoaded = false;
	    }
	}
	
	private int getAttributeIndex(String attributeName)
	{
	    initializeAttributeStorage();
	    attributeName = attributeName.intern();
        for (int index = 0; index < attributeKeys.length; index++)
        {
            if(attributeKeys[index] == attributeName)
            {
                return index;
            }
        }
        
        //somewhere, somehow it's possible for keys to be un-interned, so if we didn't find the attribute key, we use the slower code, and if we then find it, something has been messing with us and we intern everything  
        for (int index = 0; index < attributeKeys.length; index++)
        {
            if(attributeKeys[index].equals(attributeName))
            {
                internAttributes();
                return index;
            }
        }
        
        return -1;
	}
	
	@Override
	public boolean hasAttribute(String attributeName)
	{
	   if(getAttributeIndex(attributeName) >= 0)
	   {
	       return true;
	   }
	   else
	   {
	       return false;
	   }
	}
	

	@Override
	public boolean areDynamicAttributeLoaded()
	{
	    return attributesLoaded;
	}
	
	private void loadAttributes()
	{
	    if (attributesLoaded  == false)
	    {
	        attributesLoaded = true;
	        for (StreamAttributeFilter streamAttributeFilter : streamAttributeFilterLinkedList)
	        {
	            int attributeIndex = getAttributeIndex(streamAttributeFilter.getName());
	            if (attributeIndex < 0 || (attributeValues[attributeIndex] == null && streamAttributeFilter.getValue() != null))
	            {
	                setValue(streamAttributeFilter.getName(), streamAttributeFilter.getValue());
	            }
	        }
	        
	        //once we've gotten the values, release the memory used by the list
	        streamAttributeFilterLinkedList.clear();
	        
	        setValue(Attributes.path.toString(), getResourceURI().getPath());
	        setValue(Attributes.uri.toString(), getResourceURI().getBaseURI());
	        
	    }
		
	}
	
	public ContentFormatType getContentFormatType()
	{
    	if(isInitialized() == false)
    	{
    		init();
    	}

	    loadAttributes();
	    String contentFormatTypeString = getValue(ContentFormatType.ATTRIBUTE_NAME);
		if (contentFormatTypeString != null)
		{			
			return ContentFormatType.valueOf(contentFormatTypeString);
		}
		else
		{
			return null;
		}
	}

	public void setContentFormatType(ContentFormatType contentFormatType)
	{
		setValue(ContentFormatType.ATTRIBUTE_NAME, contentFormatType.toString());
	}
	
	public String getMD5()
	{
    	if(isInitialized() == false)
    	{
    		init();
    	}

	    loadAttributes();
		return getValue(MD5FilterInputStream.ATTRIBUTE_NAME);		
	}
	
	public void setMD5(String md5)
	{		
	    setValue(MD5FilterInputStream.ATTRIBUTE_NAME, md5);
	}
	
	public Long getLength()
	{
    	if(isInitialized() == false)
    	{
    		init();
    	}

	    loadAttributes();
		String length = getValue(SizeFilterInputStream.ATTRIBUTE_NAME);
		if (length.matches("\\d+"))
		{
			return Long.parseLong(length);
		}
		return null;
	}
	public void setLength(Long length)
	{
	    setValue(SizeFilterInputStream.ATTRIBUTE_NAME, length+"");
	}
	
	@Override
	public String getValue(String name) throws RuntimeException
	{
	    if (isInitialized == false)
	    {
	        init();
	    }
	    initializeAttributeStorage();
	    loadAttributes();
	    int attributeIndex = getAttributeIndex(name);
	    if(attributeIndex >= 0)
	    {
	        return attributeValues[attributeIndex];
	    }
		
		return null;
	}
	
	abstract public void init() throws RuntimeException;
	
    @Override
	public String getValue(Enum name) throws RuntimeException
	{
	    return getValue(name.toString());
	}
	
	@Override
	public void setValue(String name, String value)
	{		
		name = name.intern();
		
		for (String commonString : CommonStrings)
        {
            if(commonString.equals(value))
            {
                value = commonString;
                break;
            }
        }
		
		initializeAttributeStorage();
		
		boolean foundAttribute = false;
        for (int index = 0; index < attributeKeys.length; index++)
        {
            if(attributeKeys[index] == name)
            {
                attributeValues[index] = value;
                foundAttribute = true;
                break;
            }
        }
        
        if (foundAttribute == false)
        {
            
            
            //expand attribute storage to handle new value 
            String[] newAttributeKeys = new String[attributeKeys.length+1];
            
            for (int index = 0; index < attributeKeys.length; index++)
            {
                //check to see if we need to intern the attributes 
                if(attributeKeys[index].intern() == name)
                {
                    //looks like we do, so intern them, then set our value, since we should have picked it up the first time, and just finish up.
                    internAttributes();
                    attributeValues[index] = value;
                    newAttributeKeys = null;                    
                    return;
                }
                newAttributeKeys[index] = attributeKeys[index];
                
            }
            newAttributeKeys[attributeKeys.length] = name;
            
            String[] newAttributeValues = new String[attributeValues.length+1];
            for (int index = 0; index < attributeValues.length; index++)
            {
                newAttributeValues[index] = attributeValues[index];
            }
            newAttributeValues[attributeValues.length] = value;
            
            //set new arrays into place
            attributeValues = newAttributeValues;
            attributeKeys = newAttributeKeys;
            
            
            
        }
	}
	
	
	
	/**
	 * Convince Method
	 * @param enumclass
	 * @param object
	 */
	public void setValue(Enum enumclass, Object object)
	{
		if (object != null)
		{
			setValue(enumclass.toString(), object.toString());
		}
		else
		{
			setValue(enumclass.toString(), null);
		}
		
	}
	
	public void setValue(String name, Object object)
	{
		if (object != null)
		{
			setValue(name, object.toString());
		}
		else
		{
			setValue(name, null);
		}
		
	}
	
	@Override
	public String toString()
	{
		return ReflectionUtility.processToString(this);
	}

	@Override
	public boolean isSupported(String attributeName)
	{
		List<String> supportedAttributeList = getSupportedAttributes();
		if(includeStreamAttributes == true)
		{
		    for (Class inputStreamAttributeFilterProviderClass : inputStreamAttributeFilterLinkedList)
		    {			
		        supportedAttributeList.add(((InputStreamAttributeFilterProvider) inputStreamAttributeFilterProviderClass.getAnnotation(InputStreamAttributeFilterProvider.class)).name());
		    }	
		}
		return supportedAttributeList.contains(attributeName);
	}

	@Override
	public List<String> getSupportedAttributes()
	{
		LinkedList<String>  supportedAttributeLinkedList = new LinkedList<String>();
		Enum[] supportedAttributeList = getAdditionalSupportedAttributes();
		for (Enum attributeEnum : supportedAttributeList)
		{
			supportedAttributeLinkedList.add(attributeEnum.toString());
		}
		
		if(includeStreamAttributes == true)
		{
		    for (Class inputStreamAttributeFilterProviderClass : inputStreamAttributeFilterLinkedList)
		    {	
		        String streamAttributeName = ((InputStreamAttributeFilterProvider) inputStreamAttributeFilterProviderClass.getAnnotation(InputStreamAttributeFilterProvider.class)).name();
		        if(supportedAttributeLinkedList.contains(streamAttributeName) == false)
		        {
		            supportedAttributeLinkedList.add(streamAttributeName);
		        }
		    }
		}
		supportedAttributeLinkedList.add(Attributes.path.toString());
		return supportedAttributeLinkedList;
	}
	
	@Override
	public List<ContentMetaData> getContainedResources()
	{
	    if(isInitialized == false)
	    {
	        init();
	    }
		return childContentMetaDataLinkedList;
	}
	
	@Override
	public void addContainedResource(ContentMetaData contentMetaData)
	{
		childContentMetaDataLinkedList.add(contentMetaData);
	}
	
	public abstract Enum[] getAdditionalSupportedAttributes();

	public int getIntValue(Enum name, int defaultValue, ResourceParameter... resourceParameters)
	{
		return ContentMetaData.getIntValue(name, defaultValue, resourceParameters);	
	}
	
	
	public boolean getBoolean(Enum name, boolean defaultValue, ResourceParameter... resourceParameters)
	{
		for (ResourceParameter resourceParameter : resourceParameters)
		{
			if (resourceParameter.getName().equals(name.toString()))
			{
				if(resourceParameter.getValue().equalsIgnoreCase("true"))
				{
					return true;
				}				
			}
		}
		return defaultValue;
		
	}
	
	public String getString(Enum name, String defaultValue, ResourceParameter... resourceParameters)
	{
		for (ResourceParameter resourceParameter : resourceParameters)
		{
			if (resourceParameter.getName().equals(name.toString()))
			{
				return resourceParameter.getValue();				
			}
		}
		return defaultValue;
	}
	
	public void setResourceURI(ResourceURI resourceURI)
	{
		this.resourceURI = resourceURI;
	}
	
	@Override
	public ResourceURI getResourceURI()
	{
		return resourceURI;
	}
}
