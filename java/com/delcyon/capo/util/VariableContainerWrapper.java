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

import java.util.HashMap;
import java.util.Stack;
import java.util.logging.Level;

import com.delcyon.capo.controller.VariableContainer;
import com.delcyon.capo.server.CapoServer;

/**
 * @author jeremiah
 *
 */
public class VariableContainerWrapper implements VariableContainer
{

	private VariableContainer parentVariableContainer;
	private HashMap<String, String> localVarHashMap = new HashMap<String, String>();
	
	@SuppressWarnings("unused")
	private VariableContainerWrapper(){};
	
	public VariableContainerWrapper(VariableContainer parentVariableContainer)
	{
		this.parentVariableContainer = parentVariableContainer;
	}
	
	/* (non-Javadoc)
	 * @see com.delcyon.capo.controller.VariableContainer#getVarValue(java.lang.String)
	 */
	@Override
	public String getVarValue(String varName)
	{
		if(localVarHashMap.containsKey(varName))
		{
			return localVarHashMap.get(varName);
		}
		else
		{
			return parentVariableContainer.getVarValue(varName);
		}
	}

	public void setVar(String name, String value)
	{
		localVarHashMap.put(name, value);
	}
	
	public String processVars(String varString)
	{
		StringBuffer stringBuffer = new StringBuffer(varString);
		processVars(stringBuffer);
		return stringBuffer.toString();
	}
	
	/**
	 * Check String for variables and replace them with the value of the var
	 * @param varStringBuffer
	 */
	private void processVars(StringBuffer varStringBuffer)
	{
	    while (varStringBuffer != null && varStringBuffer.toString().matches(".*\\$\\{.+}.*"))
        {
           
            CapoServer.logger.log(Level.FINE,"found var in '"+varStringBuffer+"'");
            Stack<StringBuffer> stack = new Stack<StringBuffer>();
            StringBuffer currentStringBuffer = new StringBuffer();
            for (int index = 0; index < varStringBuffer.length(); index++) 
            {
                 
                if (varStringBuffer.charAt(index) == '$' && varStringBuffer.charAt(index+1) == '{')
                {
                    stack.push(currentStringBuffer);
                    currentStringBuffer = new StringBuffer();
                    currentStringBuffer.append(varStringBuffer.charAt(index));
                }
                else if (varStringBuffer.charAt(index) == '}' && varStringBuffer.charAt(index-1) != '\\' && stack.empty() == false)
                {
                    //pop, and evaluate
                    currentStringBuffer.append(varStringBuffer.charAt(index));
                    String varName = currentStringBuffer.toString().replaceFirst(".*\\$\\{(.+)}.*", "$1");
                    String value = getVarValue(varName);
                    if (value == null) 
                    {
                        value = "";//TODO make this configurable to null,exception,or empty
                        CapoServer.logger.log(Level.WARNING,"var '"+varName+"' not found replaced with empty string");
                    }
                    currentStringBuffer = stack.pop();
                    currentStringBuffer.append(value);
                }
                else
                {
                    currentStringBuffer.append(varStringBuffer.charAt(index));    
                }

                
            }
            
            varStringBuffer.replace(0, varStringBuffer.length(), currentStringBuffer.toString());
            
        }
        CapoServer.logger.log(Level.FINE,"final replacement =  '"+varStringBuffer+"'");
		
	}
	
	
	
}
