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
package com.delcyon.capo.util;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.util.ToStringControl.Control;

/**
 * @author jeremiah
 */
public class ReflectionUtility
{
    @SuppressWarnings("rawtypes")
    private static volatile ConcurrentHashMap<Class, Method[]> systemClassDeclaredMethodsHashMap = new ConcurrentHashMap<Class, Method[]>();
    @SuppressWarnings("rawtypes")
    private static volatile ConcurrentHashMap<Class, Field[]> systemClassDeclaredFieldsHashMap = new ConcurrentHashMap<Class, Field[]>();
    @SuppressWarnings("rawtypes")
    private static volatile ConcurrentHashMap<Class, Class[]> systemClassIneterfacesHashMap = new ConcurrentHashMap<Class, Class[]>();
    @SuppressWarnings("rawtypes")
    private static volatile ConcurrentHashMap<Class, Constructor[]> systemClassConstructorsHashMap = new ConcurrentHashMap<Class, Constructor[]>();
    @SuppressWarnings("rawtypes")
    private static volatile ConcurrentHashMap<Class, Vector<Method>> systemClassMethodVectorHashMap = new ConcurrentHashMap<Class, Vector<Method>>();
    
	public static final String DEFAULT_DATE_FORMAT = "MM/dd/yyyy";
	public static final String DEFAULT_DATE_TIME_FORMAT = "MM/dd/yyyy HH:mm";

	/**
	 * checks to see if a class is assignable from a primitive type This has to
	 * be extended beyond the standard isPrimitive call to handle "primitive"
	 * objects like Strings and Booleans
	 * 
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static boolean isPrimitive(Class clazz)
	{
		if (clazz.isPrimitive())
		{
			return true;
		}
		else if (Boolean.class.isAssignableFrom(clazz))
		{
			return true;
		}
		else if (String.class.isAssignableFrom(clazz))
		{
			return true;
		}
		else if (Date.class.isAssignableFrom(clazz))
		{
			return true;
		}
		else if (Number.class.isAssignableFrom(clazz))
		{
			return true;
		}
		else if (Enum.class.isAssignableFrom(clazz))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * @param type
	 * @param valueString
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Object getPrimitiveInstance(Type type, String valueString) throws Exception
	{

		Object typeInstanceObject = null;
		try
		{
			if (valueString == null)
			{
				return null;
			}
			else if (type == int.class || type == Integer.class)
			{
				typeInstanceObject = Integer.parseInt(valueString);
			}
			else if (type == float.class || type == Float.class)
			{
				typeInstanceObject = Float.parseFloat(valueString);
			}
			else if (type == double.class || type == Double.class)
			{
				typeInstanceObject = Double.parseDouble(valueString);
			}
			else if (type == long.class || type == Long.class)
			{
				typeInstanceObject = Long.parseLong(valueString);
			}
			else if (type == short.class || type == Short.class)
			{
				typeInstanceObject = Short.parseShort(valueString);
			}
			else if (type == byte.class || type == Byte.class)
			{
				typeInstanceObject = Byte.parseByte(valueString);
			}
			else if (type == boolean.class || type == Boolean.class)
			{
				typeInstanceObject = Boolean.parseBoolean(valueString);
			}
			else if (type == String.class)
			{
				return new String(valueString);
			}
			else if (((Class) type).isEnum())
			{
				Class enumClass = (Class) type;
				Enum[] objects = (Enum[]) enumClass.getEnumConstants();
				for (Enum object : objects)
				{
					if (object.toString().equals(valueString))
					{
						typeInstanceObject = object;
						break;
					}
				}
			}
			else if (type == Date.class)
			{
				if (valueString.matches("\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}") == true)
				{
					typeInstanceObject = new SimpleDateFormat(DEFAULT_DATE_TIME_FORMAT).parse(valueString);
				}
				else if (valueString.matches("\\d{2}/\\d{2}/\\d{4}") == true)
				{
					typeInstanceObject = new SimpleDateFormat(DEFAULT_DATE_FORMAT).parse(valueString);
				}
				else
				{
					throw new Exception("bad date '" + valueString + "'");
				}

			}
			else if (type == java.sql.Date.class)
			{
				if (valueString.matches("\\d{2}/\\d{2}/\\d{4}") == true)
				{
					typeInstanceObject = new SimpleDateFormat(DEFAULT_DATE_FORMAT).parse(valueString);
				}
				else
				{
					throw new Exception("bad date '" + valueString + "'");
				}
			}

			else if (type == Timestamp.class)
			{
				if (valueString.matches("\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}") == true)
				{
					Date timestampDate = new SimpleDateFormat(DEFAULT_DATE_TIME_FORMAT).parse(valueString);

					if (timestampDate != null)
					{
						typeInstanceObject = new Timestamp(timestampDate.getTime());
					}
					else
					{
						throw new Exception("bad timestamp '" + valueString + "'");
					}
				}
				else
				{
					throw new Exception("bad timestamp '" + valueString + "'");
				}

			}
		}
		//return null for bad number strings
		catch (NumberFormatException numberFormatException)
		{
			return null;
		}
		return typeInstanceObject;
	}

	/**
     * This walks all of the super classes and interfaces of an object and
     * creates a vector of declared methods where the first method in the vector is the first
     * method in the super most class
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Vector<Method> getMethodVector(Object object)
    {
        Vector<Class> classVector = new Vector<Class>();
        

        Class currentClass = object.getClass();
        Vector<Method> methodVector = systemClassMethodVectorHashMap.get(currentClass);
        if(methodVector != null)
        {
            return methodVector;
        }
        else
        {
            methodVector = new Vector<Method>();
        }
        while (currentClass != null)
        {
            classVector.insertElementAt(currentClass, 0);
            Class[] interfaceClasses = null;
            
            if(systemClassIneterfacesHashMap.containsKey(currentClass))
            {
                interfaceClasses = systemClassIneterfacesHashMap.get(currentClass);
            }
            else
            {
                interfaceClasses = currentClass.getInterfaces();
                systemClassIneterfacesHashMap.put(currentClass,interfaceClasses);
            }
            
            for (Class interfaceClass : interfaceClasses)
            {
                classVector.insertElementAt(interfaceClass, 0);
            }
            currentClass = currentClass.getSuperclass();
        }

        for (Class clazz : classVector)
        {
            Method[] methods = null;
            if(systemClassDeclaredMethodsHashMap.containsKey(clazz))
            {
                methods = systemClassDeclaredMethodsHashMap.get(clazz);
            }
            else
            {
                methods = clazz.getDeclaredMethods();
                systemClassDeclaredMethodsHashMap.put(clazz,methods);
            }
            for (Method method : methods)
            {
                methodVector.add(method);
            }
        }
        systemClassMethodVectorHashMap.put(object.getClass(), methodVector);
        return methodVector;
    }
	
	
	/**
	 * This walks all of the super classes and interfaces of an object and
	 * creates a vector of declared fields where the first field in the vector is the first
	 * field in the super most class
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Vector<Field> getFieldVector(Object object)
	{
		Vector<Class> classVector = new Vector<Class>();
		Vector<Field> fieldVector = new Vector<Field>();

		Class currentClass = object.getClass();
		while (currentClass != null)
		{
			classVector.insertElementAt(currentClass, 0);
			
			Class[] interfaceClasses = null;
            
            if(systemClassIneterfacesHashMap.containsKey(currentClass))
            {
                interfaceClasses = systemClassIneterfacesHashMap.get(currentClass);
            }
            else
            {
                interfaceClasses = currentClass.getInterfaces();
                systemClassIneterfacesHashMap.put(currentClass,interfaceClasses);
            }
			
			for (Class interfaceClass : interfaceClasses)
			{
				classVector.insertElementAt(interfaceClass, 0);
			}
			currentClass = currentClass.getSuperclass();
		}

		for (Class clazz : classVector)
		{
			Field[] fields = null;
			
			if(systemClassDeclaredFieldsHashMap.containsKey(clazz))
            {
			    fields = systemClassDeclaredFieldsHashMap.get(clazz);
            }
            else
            {
                fields = clazz.getDeclaredFields();
                systemClassDeclaredFieldsHashMap.put(clazz,fields);
            }
			
			for (Field field : fields)
			{
				fieldVector.add(field);
			}
		}

		return fieldVector;
	}

	/**
	 * This calls toString() on most things, but converts Date 's and whatnot to
	 * specific formats.
	 * 
	 * @param object
	 * @return
	 */
	public static String getSerializedString(Object object)
	{
		if (object == null)
		{
			return null;
		}
		else if (object instanceof Date)
		{
			return new SimpleDateFormat(DEFAULT_DATE_TIME_FORMAT).format((Date) object);
		}
		else
		{
			return object.toString();// TODO use string utils to make this safe?
		}
	}

	public static String processToString(Object toStringObject)
	{
		return processToString(toStringObject, ",");
	}

	/**
	 * Simple method for printing out all of the fields in an object
	 * 
	 * @param toStringObject
	 * @return
	 */
	public static String processToString(Object toStringObject, String seperator)
	{
		if (seperator == null)
		{
			seperator = ",";
		}
		StringBuilder stringBuffer = new StringBuilder(toStringObject.getClass().getSimpleName() + "[");
		
		Vector<Field> fieldVector = ReflectionUtility.getFieldVector(toStringObject);
		Vector<Method> methodVector = ReflectionUtility.getMethodVector(toStringObject);
		
		
		ToStringControl classToStringControl = toStringObject.getClass().getAnnotation(ToStringControl.class);
        if(classToStringControl != null)
        {
            if(classToStringControl.control() == Control.exclude && classToStringControl.modifiers() == 0)
            {
                fieldVector.clear();
                methodVector.clear();
            }
        }
        int actualFieldCount = 0;
        
		for (int currentField = 0; currentField < fieldVector.size(); currentField++)
		{
			Field field = fieldVector.get(currentField);
			field.setAccessible(true);
			ToStringControl fieldToStringControl = field.getAnnotation(ToStringControl.class);
			boolean forceInclude = false;
			if(fieldToStringControl != null)
			{
			    if(fieldToStringControl.control() == Control.exclude)
			    {
			        continue;    
			    }
			    else
			    {
			        forceInclude = true;
			    }
			}
			
			if(classToStringControl != null && forceInclude == false)
	        {
	            if(classToStringControl.control() == Control.exclude)
	            {
	                if((field.getModifiers() & classToStringControl.modifiers()) != 0) //exclude if the modifiers match
	                {
	                    continue;
	                }
	            }
	            else
	            {
	                if((field.getModifiers() & classToStringControl.modifiers()) == 0) //exclude if the modifiers don't match
                    {
                        continue;
                    }
	            }
	        }
			
			
			
			if (actualFieldCount != 0)
			{
				stringBuffer.append(seperator);
			}
			actualFieldCount++;
			
			try
			{
				Object fieldValue = field.get(toStringObject);
				String value = "null";
				if (fieldValue != null)
				{
					// start array processing
					if (field.getType().isArray())
					{
						int length = Array.getLength(fieldValue);
						StringBuilder arrayStringBuilder = new StringBuilder("{");
						for (int index = 0; index < length; index++)
						{
							if (index != 0)
							{
								arrayStringBuilder.append(',');
							}
							if (Array.get(fieldValue, index) != null)
							{
								arrayStringBuilder.append(Array.get(fieldValue, index).toString());
							}
							else
							{
								arrayStringBuilder.append("");
							}
						}
						arrayStringBuilder.append('}');
						value = arrayStringBuilder.toString();
					} // end array processing
					else
					{
						value = fieldValue.toString();
					}
				}
				stringBuffer.append(field.getName() + "='" + value + "'");

			} catch (Exception e)
			{ // Don't care
			}
		}

		if (actualFieldCount != 0)
        {
            stringBuffer.append(seperator);
        }
		
		int actualMethodCount = 0;
		for (Method method : methodVector)
        {
		    
		    
            if(method.getAnnotation(ToStringControl.class) != null)
            {
                if(method.getAnnotation(ToStringControl.class).control() == Control.include)
                {
                    method.setAccessible(true);
                    if(method.getParameterTypes().length == 0)
                    {
                        try
                        {
                            if(actualMethodCount > 0)
                            {
                                stringBuffer.append(seperator);
                            }
                            stringBuffer.append(method.getName() + "='" + method.invoke(toStringObject, new Object[]{}) + "'");
                            actualMethodCount++;
                        }
                        catch (Exception e)
                        {                            
                            Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.WARNING, "Error invoking "+method.getName()+" on "+toStringObject.getClass().getCanonicalName(), e);
                        }                        
                    }
                }
            }
        }
		
		stringBuffer.append("]");

		return stringBuffer.toString();

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Object getComplexInstance(Class type,Object...cloneableFieldInstances) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException
	{
	    
	    Constructor[] constructors = null;
        if(systemClassConstructorsHashMap.containsKey(type))
        {
            constructors = systemClassConstructorsHashMap.get(type);
        }
        else
        {
            constructors = type.getDeclaredConstructors();
            systemClassConstructorsHashMap.put(type, constructors);
        }
	    
		
		Constructor defaultConstructor = null;
		for (Constructor constructor : constructors)
		{
			constructor.setAccessible(true);
			if (constructor.getParameterTypes().length == 0)
			{
				defaultConstructor = constructor;
				break;
			}
		}
		if (defaultConstructor != null)
		{
			return defaultConstructor.newInstance(new Object[0]);
		}
		else
		{
		    Object instanceObject =  ReflectionUtility.getMarshalWrapperInstance(type.getCanonicalName());
		    if (instanceObject == null)
		    {
		        Method cloneMethod = null;
                //check to see if this complex type implements clone
                Vector<Method> childMethodVector = ReflectionUtility.getMethodVector(type);
                
                for (Method childMethod : childMethodVector)
                {
                    if(childMethod.getName().equals("clone") && childMethod.getParameterTypes().length == 0 && Modifier.isPublic(childMethod.getModifiers()) == true)
                    {
                        cloneMethod = childMethod;
                        break;
                    }
                }
                if(cloneMethod != null && cloneableFieldInstances.length > 0)
                {
                    return cloneMethod.invoke(cloneableFieldInstances[0]);
                }
		        throw new InstantiationException("Couldn't find a default contructor for "+type.getCanonicalName());
		    }
		    else
		    {
		        return instanceObject;
		    }
		}
	}

	public static Object getMarshalWrapperInstance(String className) throws InstantiationException, IllegalAccessException
    {

	    Object instanceObject = null;
	    if(CapoApplication.getAnnotationMap() != null)
	    {
	        Set<String> marshalWrapperSet = CapoApplication.getAnnotationMap().get(MarshalWrapper.class.getCanonicalName());
	        for (String wrapperClassName : marshalWrapperSet)
	        {
	            try
	            {
	                MarshalWrapper marshalWrapper = Class.forName(wrapperClassName).getAnnotation(MarshalWrapper.class);
	                if(marshalWrapper.marshalledClass().getCanonicalName().equals(className))
	                {
	                    MarshalWrapperInterface marshalWrapperInterface = (MarshalWrapperInterface) Class.forName(wrapperClassName).newInstance();
	                    return marshalWrapperInterface.getInstance();
	                }

	            } catch (ClassNotFoundException classNotFoundException)
	            {
	                CapoApplication.logger.log(Level.WARNING, "Error getting document providers",classNotFoundException);
	            }

	        }
	    }
        return instanceObject;
    }
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Constructor getDefaultConstructor(Class type)
	{
	    
	    Constructor[] constructors = null;
        if(systemClassConstructorsHashMap.containsKey(type))
        {
            constructors = systemClassConstructorsHashMap.get(type);
        }
        else
        {
            constructors = type.getDeclaredConstructors();
            systemClassConstructorsHashMap.put(type, constructors);
        }
	    
		for (Constructor constructor : constructors)
        {
            constructor.setAccessible(true);
            if (constructor.getParameterTypes().length == 0)
            {
                return constructor;
            }
        }                               
        return null;
	}
	
	
	@SuppressWarnings("rawtypes")
    public static boolean hasDefaultContructor(Class type)
	{
	    Constructor[] constructors = null;
	    if(systemClassConstructorsHashMap.containsKey(type))
	    {
	        constructors = systemClassConstructorsHashMap.get(type);
	    }
	    else
	    {
	        constructors = type.getDeclaredConstructors();
	        systemClassConstructorsHashMap.put(type, constructors);
	    }
	    
        for (Constructor constructor : constructors)
        {
            constructor.setAccessible(true);
            if (constructor.getParameterTypes().length == 0)
            {
                return true;
            }
        }                               
        return false;
        
	}
	
}
