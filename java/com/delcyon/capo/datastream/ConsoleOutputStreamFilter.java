package com.delcyon.capo.datastream;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import eu.webtoolkit.jwt.Signal1;

public class ConsoleOutputStreamFilter extends FilterOutputStream
{
    private Signal1<String> outputSignal = new Signal1<>();
    private StringBuffer buffer = new StringBuffer();
    
    public Signal1<String> output()
    {
        return outputSignal;
    }
    /**
     * @param out
     */
    public ConsoleOutputStreamFilter(OutputStream out)
    {
        super(out);        
    }
    
    
    /* (non-Javadoc)
     * @see java.io.FilterOutputStream#write(int)
     */
    @Override
    public void write(int b) throws IOException
    {   
        buffer.append(((char)b));
        if(b == 10)
        {
            outputSignal.trigger(buffer.toString());
            buffer.setLength(0);
        }            
        super.write(b);
    }
}
