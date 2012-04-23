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
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

/**
 * @author jeremiah
 */
public class ReflectionUtility
{

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
	@SuppressWarnings("unchecked")
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
	@SuppressWarnings("unchecked")
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
				return valueString;
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
	 * creates a vector of declared fields where the first filed in the first
	 * field in the super most class
	 */
	@SuppressWarnings("unchecked")
	public static Vector<Field> getFieldVector(Object object)
	{
		Vector<Class> classVector = new Vector<Class>();
		Vector<Field> fieldVector = new Vector<Field>();

		Class currentClass = object.getClass();
		while (currentClass != null)
		{
			classVector.insertElementAt(currentClass, 0);
			Class[] interfaceClasses = currentClass.getInterfaces();
			for (Class interfaceClass : interfaceClasses)
			{
				classVector.insertElementAt(interfaceClass, 0);
			}
			currentClass = currentClass.getSuperclass();
		}

		for (Class clazz : classVector)
		{
			Field[] fields = clazz.getDeclaredFields();
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
		for (int currentField = 0; currentField < fieldVector.size(); currentField++)
		{
			Field field = fieldVector.get(currentField);
			field.setAccessible(true);
			if (currentField != 0)
			{
				stringBuffer.append(seperator);
			}
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
							arrayStringBuilder.append(Array.get(fieldValue, index).toString());
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

		stringBuffer.append("]");

		return stringBuffer.toString();

	}

	@SuppressWarnings("unchecked")
	public static Object getComplexInstance(Class type) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException
	{
		Constructor[] constructors = type.getDeclaredConstructors();
		
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
			throw new InstantiationException("Couldn't find a default contructor for "+type.getCanonicalName());
		}
	}

	@SuppressWarnings("rawtypes")
    public static boolean hasDefaultContructor(Class type)
	{
        for (Constructor constructor : type.getDeclaredConstructors())
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