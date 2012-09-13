/**
Copyright (c) 2012 Delcyon, Inc.
This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.delcyon.capo.util;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;

/**
 * @author jeremiah
 * This is used to control automated toString call using the {@link ReflectionUtility} processToString. 
 */
@Retention(value=RUNTIME)
@Target(value={FIELD,TYPE,METHOD})
public @interface ToStringControl
{
    public enum Control
    {
        /**
         * At the Class level this can be used to filter fields according to a modifier mask.
         * At the Method level this will cause the result of a zero parameter method to be invoked, and toString() to be called on the result.
         * At the Field level this will force an excluded field to be included.    
         */
        include,
        /**          
         * At the Class level you can use the modifiers to automatically include or exclude any filed matching the modifier map.
         * At the Method Level, this has no effect, as methods are not printed by default. 
         * At the Field level this will force a field to no be printed.
         */
        exclude
    }
    
    Control control();
    /**
     * This uses the modifier constants from {@link Modifier}. Just add them together using the + operator. This only has effect at the class level.
     * @return
     */
    int modifiers() default 0;
}
