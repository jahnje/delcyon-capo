package com.delcyon.capo.xml.cdom;

import com.delcyon.capo.xml.XPath;

public class CValidationException extends Exception
{

    private CNode source;

    public CValidationException(String message, CNode source)
    {
        super(message);
        this.source = source;
    }

    public CNode getSource()
    {
        return source;
    }
    
    @Override
    public String getMessage()
    {
        try
        {
            return super.getMessage()+" ["+XPath.getXPath(source)+"]";
        }
        catch (Exception e)
        {
            return super.getMessage();
        }
    }
    
}
