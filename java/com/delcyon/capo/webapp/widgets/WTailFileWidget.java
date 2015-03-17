package com.delcyon.capo.webapp.widgets;

import java.io.File;
import java.io.RandomAccessFile;

import eu.webtoolkit.jwt.TextFormat;

public class WTailFileWidget extends WConsoleWidget
{
	private TailingThread tailingThread;


	public WTailFileWidget(String filename)
	{
		super();		
		tailingThread = new TailingThread(new File(filename));
		tailingThread.start();
	}
	
	public void stop()
	{
		tailingThread.interrupt();
	}
	
	public void start()
	{
		tailingThread.start();
	}
	
	
	private class TailingThread extends Thread
	{
		private boolean _running = true;
		private File _file;
		private long _filePointer = 0l;
		
		public TailingThread(File file)
		{
			this._file = file;
		}
		
		@Override
		public void interrupt()
		{
			_running = false;
		}
		
		
		@Override
		public void run()
		{
			try {
		        while (_running) {
		            Thread.sleep(1000);
		            long len = _file.length();
		            if (len < _filePointer) {
		                // Log must have been jibbled or deleted.
		                append("Log file was reset. Restarting logging from start of file.",TextFormat.PlainText);
		                _filePointer = len;
		            }
		            else if (len > _filePointer) {
		                // File must have had something added to it!
		                RandomAccessFile raf = new RandomAccessFile(_file, "r");
		                raf.seek(_filePointer);
		                String line = null;
		                while ((line = raf.readLine()) != null) {
		                	append(line,TextFormat.PlainText);
		                }
		                _filePointer = raf.getFilePointer();
		                raf.close();
		            }
		        }
		    }
		    catch (Exception e) {
		    	append("Fatal error reading log file, log tailing has stopped.",TextFormat.PlainText);
		    }
			
		}
		
	}
}
