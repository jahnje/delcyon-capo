package com.delcyon.capo.util;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import org.w3c.dom.Element;

import com.delcyon.capo.util.CloneControl.Clone;




public abstract class EqualityProcessor {

	/**
	 * Compares to objects to see if they are equal. It also does null comparisons
	 * @param object1
	 * @param object2
	 * @return
	 */
	public static boolean areSame(Object object1, Object object2) {
		if (object1 == null ^ object2 == null){
			return false;
		}
		else if (object1 == null & object2 == null){
			return true;
		}
		else return object1.equals(object2);
	}
	
	/**
	 * This is a utility method that can be used to clone anything, regardless of whether or not that object implements the clone interface.
	 * @see CloneControl CloneControl for more information, on controlling what gets cloned.  
	 * @param cloneable, any object, does NOT have to implement clonable
	 * @return cloned object, unless it couldn't be cloned in which case it will return null;
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> T  clone(T cloneable) throws Exception
	{
		if(cloneable == null)
		{
			return null;
		}
		
		
		
		if(ReflectionUtility.isPrimitive(cloneable.getClass()))
		{	
			return  (T) ReflectionUtility.getPrimitiveInstance(cloneable.getClass(), ReflectionUtility.getSerializedString(cloneable));
		}
		
		Constructor<T> constructor = ReflectionUtility.getDefaultConstructor(cloneable.getClass());
		T clone = (T) constructor.newInstance();
		Vector<Field> fieldVector = ReflectionUtility.getFieldVector(cloneable);
		
		CloneControl classCloneControl = cloneable.getClass().getAnnotation(CloneControl.class);
        if(classCloneControl != null)
        {
            if(classCloneControl.filter() == Clone.exclude && classCloneControl.modifiers() == 0)
            {
                fieldVector.clear();                
            }
        }
		
		for (Field field : fieldVector)
		{
			if (Modifier.isStatic(field.getModifiers()) == false)
			{
				field.setAccessible(true);
				
				CloneControl fieldToStringControl = field.getAnnotation(CloneControl.class);
	            boolean forceInclude = false;
	            if(fieldToStringControl != null)
	            {
	                if(fieldToStringControl.filter() == Clone.exclude)
	                {
	                    continue;    
	                }
	                else
	                {
	                    forceInclude = true;
	                }
	            }
	            
	            if(classCloneControl != null && forceInclude == false)
	            {
	                if(classCloneControl.filter() == Clone.exclude)
	                {
	                    if((field.getModifiers() & classCloneControl.modifiers()) != 0) //exclude if the modifiers match
	                    {
	                        continue;
	                    }
	                }
	                else
	                {
	                    if((field.getModifiers() & classCloneControl.modifiers()) == 0) //exclude if the modifiers don't match
	                    {
	                        continue;
	                    }
	                }
	            }
				
				Object cloneableFieldInstance = field.get(cloneable);
				if(cloneableFieldInstance != null)
				{
					if(ReflectionUtility.isPrimitive(field.getType()))
					{	
						field.set(clone, ReflectionUtility.getPrimitiveInstance(field.getType(), ReflectionUtility.getSerializedString(cloneableFieldInstance)));
					}
					else
					{
						Method cloneMethod = null;
						//check to see if this complex type implements clone
						Vector<Method> childMethodVector = ReflectionUtility.getMethodVector(cloneableFieldInstance);
						
						for (Method childMethod : childMethodVector)
						{
							if(childMethod.getName().equals("clone") && childMethod.getParameterTypes().length == 0 && Modifier.isPublic(childMethod.getModifiers()) == true)
							{
								cloneMethod = childMethod;								
							}
						}
						
						if (cloneMethod != null)
						{
							field.set(clone,cloneMethod.invoke(cloneableFieldInstance));
						}
						else //we have to do this the hard way
						{
							//do a standard element clone
							if (Element.class.isAssignableFrom(field.getType()))
							{								
								field.set(clone, ((Element)cloneableFieldInstance).cloneNode(true));
							}
							//implement a collection clone
							else if (Collection.class.isAssignableFrom(field.getType()))
							{
								Collection clonableCollection = (Collection) cloneableFieldInstance;
								Collection clonedCollection = (Collection) field.getType().newInstance();
								field.set(clone, clonedCollection);								
								
								for (Object object : clonableCollection)
								{
									clonedCollection.add(clone(object));
								}
								
							}
							//do an array clone
							else if (field.getType().isArray() == true)
							{
								int length = Array.getLength(cloneableFieldInstance);
								Object arrayObject = Array.newInstance(field.getType().getComponentType(), length);
								field.set(clone, arrayObject);
								for(int index = 0; index < length; index++)
								{
									Array.set(arrayObject, index, clone(Array.get(cloneableFieldInstance, index)));
								}
							}
							else if (Map.class.isAssignableFrom(field.getType()))
							{
								Map clonableMap = (Map) cloneableFieldInstance;
								Map clonedMap = (Map) field.getType().newInstance();
								field.set(clone, clonedMap);

								Set<Entry>  entrySet = clonableMap.entrySet();
								for (Entry entry : entrySet)
								{
									clonedMap.put(clone(entry.getKey()), clone(entry.getValue()));
								}
							}
							else
							{
								field.set(clone,clone(cloneableFieldInstance));
							}
							
						}
					}
				}
			}
		}
		
		//process any CloneControl methods
		Vector<Method> methodVector = ReflectionUtility.getMethodVector(cloneable);
		for (Method method : methodVector)
        {
		   
            CloneControl cloneControl = method.getAnnotation(CloneControl.class);
            if (cloneControl != null && cloneControl.filter() == Clone.include)
            {
                method.setAccessible(true);
                method.invoke(cloneable, clone);
            }
        }
		return clone;
	}
	
}
