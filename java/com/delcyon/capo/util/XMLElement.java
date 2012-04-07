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

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author jeremiah
 *
 */
@Retention(value=RUNTIME)
@Target(value={FIELD,TYPE})
public @interface XMLElement
{
	String defaultValue() default XMLSerializer.DEFAULT_STRING;
	String name() default XMLSerializer.DEFAULT_STRING;
	String namespace() default XMLSerializer.DEFAULT_STRING;
	String format() default XMLSerializer.DEFAULT_STRING;
	boolean unique() default true;
	boolean nillable() default false;
	@SuppressWarnings("unchecked")
	Class type() default DEFAULT.class;
	boolean doNotIncludeParent() default false;	
	public class DEFAULT {}
	boolean required() default false;
}
