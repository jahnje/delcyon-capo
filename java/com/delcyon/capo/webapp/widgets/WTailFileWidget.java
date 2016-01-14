package com.delcyon.capo.webapp.widgets;

import java.io.File;
import java.io.RandomAccessFile;

import eu.webtoolkit.jwt.TextFormat;

/**
 * this classes just watches a file, and appends lines to a wConsole widget.
 * Care must be taken to start and stop it appropriately. 
 * @author jeremiah
 *
 */
public class WTailFileWidget extends WConsoleWidget
{
    
	private TailingThread tailingThread;
	private boolean limitInitialReadSize = false;
	private long limitMultiplier = 120l;
	
	/**
	 * File to tail
	 * @param filename
	 */
	public WTailFileWidget(String filename)
	{
		super();		
		tailingThread = new TailingThread(new File(filename));		
	}
	
	/**
	 * 
	 * @param filename
	 * @param limitInitialReadSize default to false, can be used against large files to only scan against end of file. Will use limitMultiplier  to determines number of bytes from end to start showing
	 */
	public WTailFileWidget(String filename, boolean limitInitialReadSize)
    {
        super();        
        this.limitInitialReadSize = limitInitialReadSize;
        tailingThread = new TailingThread(new File(filename));      
    }
	
	public long getLimitMultiplier()
    {
        return limitMultiplier;
    }
	
	/**
	 * multipler to use against buffer size control initial size of dump of tailed file default = 120
	 * @param limitMultiplier
	 */
	public void setLimitMultiplier(long limitMultiplier)
    {
        this.limitMultiplier = limitMultiplier;
    }
	
	/**
	 * terminate the file tailing
	 */	
	public void stop()
	{
		tailingThread.interrupt();
	}
	
	/**
	 * start the file tailing
	 */
	public void start()
	{
		tailingThread.start();
	}
	
	
	private class TailingThread extends Thread
	{
		private boolean isRunning = true;
		private File file;
		private long filePosition = 0l;
		
		public TailingThread(File file)
		{
			this.file = file;
		}
		
		@Override
		public void interrupt()
		{
			isRunning = false;
		}
		
		
		@Override
		public void run()
		{
			try {
			    
			    /**
			     * stolen from stack exchange somewhere 
			     */
			    while (isRunning) 
			    {

			        long len = file.length();
			        //don't bother sending anything that would just be scrolled off the buffer, so just skip ahead
			        //assuming 120 char per line
			        if(limitInitialReadSize && filePosition == 0l && len > (((long)getBufferSize())*limitMultiplier))
			        {
			            filePosition = len - (((long)getBufferSize())*limitMultiplier);
			        }
			        
			        if (len < filePosition) {
			            // Log must have been jibbled or deleted.
			            append("Log file was reset. Restarting logging from start of file.",TextFormat.PlainText);
			            filePosition = len;
			        }
			        else if (len > filePosition) {
			            // File must have had something added to it!
			            RandomAccessFile raf = new RandomAccessFile(file, "r");
			            raf.seek(filePosition);
			            String line = null;
			            while ((line = raf.readLine()) != null)
			            {		                    
			                append(line,TextFormat.PlainText);		                	
			            }
			            filePosition = raf.getFilePointer();
			            raf.close();
			        }		            
			        Thread.sleep(1000);
			    }
			}
			catch (Exception e) {
			    e.printStackTrace();
			    append("Fatal error reading log file, log tailing has stopped."+e.getMessage(),TextFormat.PlainText);
			}
			
		}
		
	}

	
}
