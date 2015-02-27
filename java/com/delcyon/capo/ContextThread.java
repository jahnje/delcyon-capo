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
package com.delcyon.capo;

import java.util.logging.Level;

import javax.jcr.Session;


/**
 * @author jeremiah
 *
 */
public class ContextThread extends Thread
{

	private Object context = null;	
	private InterruptibleRunnable interruptibleRunnable = null;
	
	public byte[] hugeBuffer = null;
    private Session session;
	
	public ContextThread()
	{

	}
	
	public ContextThread(String threadName)
	{
		super(threadName);
	}
	
	public ContextThread(Runnable runnable,String threadName)
    {
        super(runnable,threadName);
    }
	
	public ContextThread(ThreadGroup threadGroup, Runnable runnable)
	{
		super(threadGroup, runnable, "ContextThread - "+CapoApplication.getApplication().getApplicationDirectoryName().toUpperCase());		  
		this.interruptibleRunnable = null;
	}

	public Object getContext()
	{
		return context;
	}
	
	public void setContext(Object context)
	{
		this.context = context;
	}
	
	@Override
	public void interrupt()
	{
		if (interruptibleRunnable != null)
		{
			interruptibleRunnable.interrupt();
		}
		super.interrupt();

	}

	/**
	 * Make sure you set this to null once you're about to stop so that intterupt doesn't get called on already dead threads. 
	 * @param interruptibleRunnable
	 */
	public void setInterruptible(InterruptibleRunnable interruptibleRunnable)
	{
		this.interruptibleRunnable = interruptibleRunnable;
		
	}

    public Session getSession()
    {
        return this.session;
    }
    
    public void setSession(Session session)
    {
        if(session != null)
        {
            if(this.session != null && this.session.isLive())
            {                
                Thread.dumpStack();                
                CapoApplication.logger.log(Level.WARNING, "SetSession OVERLAP: T="+this+" S="+this.session+" with S="+session);
            }
            CapoApplication.logger.log(Level.FINE, "SetSession: T="+this+" S="+session);
        }
        else
        {
            if(this.session != null)
            {
                CapoApplication.logger.log(Level.FINER,"DropSession: T="+this+" S="+this.session);
            }
        }
        //Thread.dumpStack();
        this.session = session;
    }
    
    @Override
    protected void finalize() throws Throwable
    {
        this.session = null;
        super.finalize();
    }
}
