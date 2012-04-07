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
package com.delcyon.capo.protocol.client;

import java.io.BufferedInputStream;
import java.io.OutputStream;

/**
 * @author jeremiah
 * 
 */
public abstract class Request
{

    private OutputStream outputStream;
	private BufferedInputStream inputStream;

	
	public Request()
	{
		
	}
	
    public Request(OutputStream outputStream,BufferedInputStream inputStream)
    {
    	this.outputStream = outputStream;
    	this.inputStream = inputStream;
    }
    
    
    
    public abstract void send() throws Exception;
    
    public OutputStream getOutputStream()
    {
    	return outputStream;
    }
    
    public BufferedInputStream getInputStream()
    {
    	return inputStream;
    }

    public void setInputStream(BufferedInputStream inputStream) throws Exception
	{
		this.inputStream = inputStream;
	}

    public void setOutputStream(OutputStream outputStream) throws Exception
	{
		this.outputStream = outputStream;
	}
    
	
}
