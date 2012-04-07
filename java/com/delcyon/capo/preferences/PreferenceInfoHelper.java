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
package com.delcyon.capo.preferences;

import java.util.logging.Level;

import com.delcyon.capo.CapoApplication;


/**
 * @author jeremiah
 *
 */
public abstract class PreferenceInfoHelper
{
	@SuppressWarnings("unchecked")
	public static PreferenceInfo getInfo(Enum clazz)
	{
		try
		{
			return clazz.getClass().getField(clazz.toString()).getAnnotation(PreferenceInfo.class);
		} catch (Exception exception)
		{
			CapoApplication.logger.log(Level.WARNING, "PreferenceInfo Problem", exception);
		}
		return null;
	}
}
