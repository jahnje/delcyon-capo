package com.delcyon.capo.xml.cdom;

import java.util.Vector;

public interface NodeValidationUtilitesFI
{

    /**
     * If the exception vector is null, it will throw the create exception, otherwise it will add the exception to the vector  and return the created exception 
     * @param message
     * @param source
     * @param exceptionVector
     * @return
     * @throws CValidationException
     */
    public default CValidationException nodeInvalid(String message, CNode source, Vector<CValidationException> exceptionVector) throws CValidationException
    {
        CValidationException validationException = new CValidationException(message,source);
        if(exceptionVector != null)
        {
            exceptionVector.add(validationException);
        }
        else
        {
            throw validationException;
        }
        return validationException;
    }
    
    
}
