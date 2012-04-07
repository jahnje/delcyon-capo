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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

import org.junit.Test;


/**
 * @author jeremiah
 *
 */
public class RegexFilterOutputStreamTest
{

	@Test
	public void runTest() throws Exception
	{
		URL url = ClassLoader.getSystemResource("regex-testdata.txt");
		System.out.println(url);
		InputStream inputStream = url.openStream();
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		
		RegexFilterOutputStream regexFilterOutputStream2 = new RegexFilterOutputStream(byteArrayOutputStream, "goodbye", "hello", 1);
		RegexFilterOutputStream regexFilterOutputStream = new RegexFilterOutputStream(regexFilterOutputStream2, "test\nthis", "goodbye", 0);
		while(true)
		{
			int value = inputStream.read();
		
			if (value >= 0)
			{
				regexFilterOutputStream.write(value);
			}
			else
			{
				regexFilterOutputStream.close();
				break;
			}
		}		
	}
}
