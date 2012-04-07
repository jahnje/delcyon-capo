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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;


public class CommandExecution extends Thread
{
    private String command;
    private String stdout = null;
    private String stderr = null;
    private int exitCode = -1;
    private boolean isFinished = false;
    private Exception exception = null;
    private static final long MAX_RUNTIME = 250000l;
    private Process process;
	private Long timeout; 
   
    
    public CommandExecution(String command,Long timeout)
    {
        super(command);
        this.command = command;
        if (timeout == null || timeout == 0l)
        {
        	this.timeout = MAX_RUNTIME;
        }
        else
        {
        	this.timeout = timeout;
        }
    }
    
    public CommandExecution(String command, String timeoutString)
	{
    	super(command);
        this.command = command;
		if (timeoutString == null || timeoutString.trim().isEmpty() || timeoutString.matches("\\d+") == false)
		{
			this.timeout = MAX_RUNTIME;
		}
		else
		{
			Long timeout = Long.valueOf(timeoutString);
			if (timeout == null || timeout == 0l)
	        {
	        	this.timeout = MAX_RUNTIME;
	        }
	        else
	        {
	        	this.timeout = timeout;
	        }
		}
	}

	public void executeCommand() throws Exception
    {
        long startTime = System.currentTimeMillis();
        start();
        while(isFinished == false)
        {
            sleep(100);
            if (System.currentTimeMillis() - startTime > timeout)
            {
                process.destroy();
                throw new Exception("command timed out: '"+command+"'");
            }
        }
        if (exception != null)
        {
            throw exception;
        }
    }
    
    public void run()
    {
        try
        {
        
   
            
            String[] commandArray = {"/bin/sh","-c",command};
            process = Runtime.getRuntime().exec(commandArray,null);
            
            InputStream stdoutInputStream = process.getInputStream();
            InputStream stderrInputStream = process.getErrorStream();
            
            
            ByteArrayOutputStream errorByteArrayOutputStream = new ByteArrayOutputStream();
            ByteArrayOutputStream outputByteArrayOutputStream = new ByteArrayOutputStream();
            
            int errorValue = 0;
            int outputValue = 0;
            
            
            byte[] buffer = new byte[4096];
            while (outputValue >= 0 && errorValue >= 0)
            {
                
                //see if we have anything to read from stdout
                if (outputValue >= 0 && stdoutInputStream.available() > 0)
                {
                    outputValue = stdoutInputStream.read(buffer);                   
                    outputByteArrayOutputStream.write(buffer,0,outputValue);
                }
                else
                {
                    outputValue = 0;
                }
                
                //see if we have anything to read from stderr
                if (errorValue >= 0 && stderrInputStream.available() > 0)
                {
                    errorValue = stderrInputStream.read(buffer);
                    errorByteArrayOutputStream.write(buffer,0,errorValue);
                }
                else
                {
                    errorValue = 0;
                }
                //we aren't closed, but we didn't read anything either
                if (outputValue == 0 && errorValue == 0)
                {
                    //this is arbitrary, but should keep us from running the cpu to hot
                    sleep(50);
                    try
                    {
                        process.exitValue();
                        //just because the process has finished doesn't mean we've gotten all of the data from the buffers yet
                        //so double check to make sure we don't having anything available
                        if (stderrInputStream.available() == 0 && stdoutInputStream.available() == 0)
                        {
                            break;
                        }
                    }
                    catch (IllegalThreadStateException e) {
                        //thread not finished
                    }
                    
                }
                
            }
            
            
            stderr = new String(errorByteArrayOutputStream.toByteArray()).trim();
            stdout = new String(outputByteArrayOutputStream.toByteArray()).trim();
            
            exitCode = process.waitFor();
            
            stderrInputStream.close();
            stdoutInputStream.close();                        
        }
        catch (Exception exception)
        {
            this.exception = new Exception("error running command: "+command,exception);            
        }
        finally
        {
            process.destroy();
            isFinished = true;
        }
    }
    
    /**
     * @return the stderr    
     */
    public String getStderr()
    {
        return stderr;
    }
    
    /**
     * @return the stdout    
     */
    public String getStdout()
    {
        return stdout;
    }
    
    /**
     * @return the exitCode    
     */
    public int getExitCode()
    {
        return exitCode;
    }

    /**
     * @return
     */
    public String getCommand()
    {
        return command;
    }
}
