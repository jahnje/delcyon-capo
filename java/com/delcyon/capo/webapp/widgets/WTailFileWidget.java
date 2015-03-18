package com.delcyon.capo.webapp.widgets;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import com.delcyon.capo.parsers.GrammarParser;

import eu.webtoolkit.jwt.TextFormat;

public class WTailFileWidget extends WConsoleWidget
{
    private ArrayList<Object[]> grammerStyles = new ArrayList<>();
    private ArrayList<PatternHolder> testPatternList = new ArrayList<>();
	private TailingThread tailingThread;


	public WTailFileWidget(String filename)
	{
		super();		
		tailingThread = new TailingThread(new File(filename));		
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
		            Thread.sleep(1000);
		        }
		    }
		    catch (Exception e) {
		        e.printStackTrace();
		    	append("Fatal error reading log file, log tailing has stopped."+e.getMessage(),TextFormat.PlainText);
		    }
			
		}
		
	}

	@Override
	public void append(String message, TextFormat textFormat)
	{
	   
	    //must match all tests
	    for (PatternHolder patternHolder : testPatternList)
        {
            if(patternHolder.pattern.matcher(message).matches() != patternHolder.passingMatchRule)
            {
                return; 
            }
        }
	    
	   
	    for (Object[] objects : grammerStyles)
        {
	        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	        GrammarParser grammarParser = (GrammarParser) objects[0];
	        Transformer transformer = (Transformer) objects[1];
	        try
            {
                transformer.transform(new DOMSource(grammarParser.parse(new ByteArrayInputStream(message.getBytes()))), new StreamResult(byteArrayOutputStream));
                if(byteArrayOutputStream.size() > 0)
                {                    
                    super.append(new String(byteArrayOutputStream.toByteArray()), TextFormat.XHTMLText);
                    return;
                }
            }            
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }    
        }
	    
	    super.append(message, textFormat);
	}
	
	/**
	 * This takes a regex and will only append messages that match the regex
	 * @param regex
	 */
	public void addFilter(String regex)
	{
	    addFilter(regex, true);
	}
	

    public void addGrammerStyle(GrammarParser grammarParser, Document xslDocument) throws TransformerConfigurationException
    {   
        
        TransformerFactory tFactory = TransformerFactory.newInstance();                
        grammerStyles.add(new Object[]{grammarParser,tFactory.newTransformer(new DOMSource(xslDocument))});
    }

    /**
     * expected match can be used to invert the match as opposed to writing an complicated inverse regex
     * 
     * @param string
     * @param expected
     */
    public void addFilter(String regex, boolean match)
    {
        Pattern pattern = Pattern.compile(regex);        
        testPatternList.add(new PatternHolder(match, pattern));
        
    }
    
    private class PatternHolder
    {
        boolean passingMatchRule = true;
        Pattern pattern = null;
        /**
         * @param passingMatchRule
         * @param pattern
         */
        private PatternHolder(boolean passingMatchRule, Pattern pattern)
        {
            super();
            this.passingMatchRule = passingMatchRule;
            this.pattern = pattern;
        }
        
        
    }

    
}
