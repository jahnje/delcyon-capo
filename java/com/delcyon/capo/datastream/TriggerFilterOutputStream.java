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
import java.util.logging.Level;

import com.delcyon.capo.server.CapoServer;



/**
 * @author jeremiah
 *
 */
public class TriggerFilterOutputStream extends FilterOutputStream
{
	private static final int SYMBOL_COUNT = 6;
	private int max_buffer_size = 50;
    private int symbol = '@';
    private ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private int symbolCount = 0;
	private String varname;
	private String replacement;
    
    public TriggerFilterOutputStream(OutputStream outputStream, String varname,String replacement )
    {
        super(outputStream);
        this.varname = varname;
        this.replacement = replacement;
        this.max_buffer_size = varname.length()+SYMBOL_COUNT;
        
    }
    
    public TriggerFilterOutputStream(OutputStream outputStream, String varname,String replacement, char symbol )
    {
        super(outputStream);
        this.varname = varname;
        this.replacement = replacement;
        this.max_buffer_size = varname.length()+SYMBOL_COUNT;
        this.symbol = (int)symbol;
    }
    
    /**
     * override write method  
     */
    @Override
    public void write(int b) throws IOException {
        
        
        if (b == symbol) //this could be a variable declaration, so start counting
        {
            if (symbolCount == 0)//gotten our first symbol, so increment the symbolCount se we start counting @'s
            {                
                symbolCount++;
            }
            else if (symbolCount == 1) //got another symbol, so increment the symbolCount, and prep the buffer 
            {                
                buffer.reset(); 
                symbolCount++;                
            }
            else if (symbolCount == 2) //got another symbol, so increment the symbolCount, we are on the other side of the trigger now
            {                                
                symbolCount++;                
            }
            else if (symbolCount == 3) //we've gotten a full variable declaration now, so see if it's something we know about
            {                
                //trigger matches so output the new value, and then reset
                if (varname.equals(buffer.toString()))
                {
                    byte[] replacement =  this.replacement.getBytes();
                    CapoServer.logger.log(Level.FINER, "Replacing '"+(char)symbol+(char)symbol+varname+(char)symbol+(char)symbol+"' with '"+this.replacement+"'");
                    out.write(replacement);                   
                    symbolCount = 0;
                }
                else //trigger doesn't match, so reset
                {                    
                    out.write(symbol);
                    out.write(symbol);
                    out.write(buffer.toByteArray());
                    out.write(symbol);
                    out.write(b);
                    symbolCount = 0;
                }
            }
        }
        else if (symbolCount == 1) //should have gotten another @ symbol, so reset
        {              
            out.write(symbol);
            out.write(b);
            symbolCount = 0;
        }
        else if (symbolCount == 2) //start buffering things
        {         
            buffer.write(b);
            if (b == '\n') //hit a new line, so reset
            {
                out.write(symbol);
                out.write(symbol);
                out.write(buffer.toByteArray());
                symbolCount = 0;
                CapoServer.logger.log(Level.FINER, "Reached newline flushing buffer");
            }
            else if (buffer.size() >= max_buffer_size)
            {
                out.write(symbol);
                out.write(symbol);
                out.write(buffer.toByteArray());
                symbolCount = 0;
                CapoServer.logger.log(Level.FINER, "Reached max buffer size :"+max_buffer_size+" flushing buffer");
            }
        }
        else if (symbolCount == 3) //should have gotten another @ symbol, so reset
        {           
            out.write(symbol);
            out.write(symbol);
            out.write(buffer.toByteArray());
            out.write(symbol);
            out.write(b);
            symbolCount = 0;
        }
        else //normal char, just write it out
        {         
            out.write(b);  
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
