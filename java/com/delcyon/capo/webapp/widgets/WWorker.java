package com.delcyon.capo.webapp.widgets;

import eu.webtoolkit.jwt.WApplication;

/**
 * Takes care of some of the leg work for running long running processes in a different thread than the current event Thread of the WApplication.
 * Make a new Worker, and call run(()-{}); with runnable object; This will attach the new thread to the application. You can then call update at anytime in your thread to send updates back to the client.
 * 
 * @author jeremiah
 *
 */
public class WWorker implements Runnable
{
    private WApplication ownerApplication = WApplication.getInstance();
    private Runnable runnable;
    private Thread thread;
    private Boolean isRunning = false;

    /**
     * 
     */
    public WWorker()
    {
        
    }
    
    /**
     * code you want to run in separate thread.
     * @param runnable
     */
    public void run(Runnable runnable)
    {
        this.runnable = runnable;
        if(isRunning == false)
        {
            thread = new Thread(this, "WWorker for "+ownerApplication.getId());
            thread.start();
            this.isRunning = true;
        }
        else
        {
            throw new RuntimeException("WWorker for "+ownerApplication.getId()+" already has a running thread.");
        }
    }
    
    /**
     * Use this to trigger update events on the owner application. You can passing a comma separated list of Runnable objects, or none which will just call update
     * @param updateMethods
     */
    public void update(Runnable...updateMethods)
    {
        WApplication.UpdateLock lock = ownerApplication.getUpdateLock();
                
        if(updateMethods != null)
        {
            for (Runnable runnable : updateMethods)
            {
                runnable.run();    
            }            
        }
        
        if(lock != null)
        {
            ownerApplication.triggerUpdate();
            lock.release();
        }
    }

    /**
     * This is where the code it actually run
     */
    @Override
    public final void run()
    {
        ownerApplication.attachThread(true);
        try
        {
            runnable.run();
        }         
        finally
        {
            ownerApplication.attachThread(false);
            this.isRunning = false;
        }
    }
    
    /**
     * call this to interrupt the running thread. There must be some point in the code that is being run that will actually check Thread.interupted() for this to have an effect.
     */
    public void interrupt()
    {
        thread.interrupt();
        this.isRunning = false;
    }
    
    /**
     * 
     * @return where or not we think our thread is currently running
     */
    public boolean isRunning()
    {
        return this.isRunning;
    }
    
    /**
     * 
     * @return the WAplication that this thread was started with
     */
    public WApplication getOwnerApplication()
    {
        return ownerApplication;
    }
    
}
