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
package com.delcyon.capo.datastream;

import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.util.HashMap;

/**
 * @author jeremiah
 *
 */
public interface StreamProcessor
{
	public abstract void init(HashMap<String, String> sessionHashMap) throws Exception;
	public abstract void processStream(BufferedInputStream bufferedInputStream, OutputStream outputStream) throws Exception;	
}
