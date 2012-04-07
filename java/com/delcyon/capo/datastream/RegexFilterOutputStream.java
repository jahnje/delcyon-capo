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
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 * @author jeremiah
 *
 */
public class RegexFilterOutputStream extends FilterOutputStream
{
    
    private ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private Vector<Integer> lineVector = new  Vector<Integer>();
	private Pattern regex;
	private String replacement;
	private int lineBufferCount;
	private StringBuilder stringBuilder = new StringBuilder();
	private boolean isCloseing = false;
    
	
	/**
	 * remember, this filter will run BEFORE the outputStream parameter does. 
	 * So always make sure that you reverse the list of filters before creating them.
	 * @param outputStream
	 * @param regex
	 * @param replacement
	 * @param lineBufferCount how many lines we should have buffered before we start searching things. 
	 */
    public RegexFilterOutputStream(OutputStream outputStream, String regex,String replacement,int lineBufferCount)
    {
        super(outputStream);
        if (lineBufferCount == 1)
        {
        	this.regex = Pattern.compile(regex);
        }
        else
        {
        	//if we are buffering more than one line at a time, then enable multi-line support, otherwise there is no point.  
        	this.regex = Pattern.compile(regex,Pattern.MULTILINE);	
        }       
        this.replacement = replacement;
        this.lineBufferCount = lineBufferCount;
    }
    
   
    @Override
    public void close() throws IOException
    {    	
    	isCloseing = true;
    	processBuffer();
    	out.write(stringBuilder.toString().getBytes());
    	out.flush();
    	super.close();
    }
    
    /**
     * override write method  
     */
    @Override
    public void write(int b) throws IOException {

    	if (b == '\n')
    	{
    		//got new line
    		buffer.write(b);
    		processBuffer();    		
    	}    	
    	else
    	{
    		buffer.write(b);
    	}        
    }


    private void processBuffer() throws IOException
    {    	
    	String workingLine = buffer.toString();
		buffer.reset();
		lineVector.add(workingLine.length());
		stringBuilder.append(workingLine);
		//slid the buffer window 
		if (lineBufferCount != 0 && lineVector.size() > lineBufferCount)
		{
			//release head of vector
			workingLine = stringBuilder.substring(0, lineVector.remove(0));
			stringBuilder.delete(0, workingLine.length());
			out.write(workingLine.getBytes());
		}
		
		//make sure the buffer is full before running any tests, I think this will result in more expected output than any other solution
		//the corner case is when the stream is closing, the buffer may not be full, but we should run it anyway
		if (isCloseing  == false && lineVector.size() != lineBufferCount)
		{
			return;
		}
		//this does not do in line replacement, but instead returns a result
		//so we need to determine if something changed
		//and how that impacts our lineLengthVector
		//should we just flush the buffers and start over?
		//or should we do some serious math, and resize everything accordingly? 
		//being a lazy programmer, I've opted for flushing everything on a find/replace
		//you can always set the window size to 0 if you want perfect multi-line regex 
		Matcher matcher = regex.matcher(stringBuilder);//replaceAll(replacement);
		
		boolean foundMatch = matcher.find();
        if (foundMatch) {
            StringBuffer stringBuffer = new StringBuffer();
            
            while(foundMatch == true)            
            {
            	matcher.appendReplacement(stringBuffer, replacement);
            	foundMatch = matcher.find();
            }
            matcher.appendTail(stringBuffer);
            out.write(stringBuffer.toString().getBytes());
            stringBuilder = new StringBuilder();
            lineVector.clear();            
        }        		
    }
    
    /**
     * override write method  
     */
    @Override
    public void write(byte[] data, int offset, int length) throws IOException
    {
        for (int i = offset; i < offset + length; i++)
        {
            this.write(data[i]);
        }
    }
    
    /**
     * override write method  
     */
    @Override
    public void write(byte[] b) throws IOException
    {
        write(b, 0, b.length);
    }
}
