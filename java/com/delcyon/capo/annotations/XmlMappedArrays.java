package com.delcyon.capo.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author jeremiah
 * This is a class level annotation that takes effect on XMLSerialization. 
 * It's purpose is to make sure that when we are using a pair of arrays as a simple map, that they get added 
 * to the serialized XML in a logical way as attributes, and not as to separate arrays, that could get our of order.
 * Null values will NOT be serialized, so the placeholder for a key with a null value will be removed. The mapping code should handle this anyway 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value={ElementType.TYPE})
public @interface XmlMappedArrays
{
	/**
	 * Name of the xml Element to contain the attributes
	 * @return
	 */
	String name();

	/**
	 * Name of the array that contains the keys
	 * @return
	 */
	String keys();
	
	/**
	 * Name of the array that contains the values
	 * @return
	 */
	String values();

}
