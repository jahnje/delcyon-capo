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

/**
 * @author jeremiah
 *  Used to control cloning by the {@link EqualityProcessor} clone method.
 *  @see CloneControl  
 */
public interface ControlledClone
{
    /**
     * This is called after all fields in the class have been cloned.
     * @param parentClonedObject
     * @param clonedObject
     * @throws Exception
     */
    public void postClone(Object parentClonedObject, Object clonedObject) throws Exception;
    
    /**
     * This is called directly after the cloned object has been instantiated, but before any fields in the class have been cloned. 
     * @param parentClonedObject
     * @param clonedObject
     * @throws Exception
     */
    public void preClone(Object parentClonedObject, Object clonedObject) throws Exception;
}
