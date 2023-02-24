package com.delcyon.capo.xml.cdom;

import java.util.Vector;

public interface CNodeDefinition extends NodeValidationUtilitesFI
{

    boolean isValid(CNode node, Vector<CValidationException> exceptionVector) throws Exception;

    static CNodeDefinition getDefinitionForNode(CNode cNode)
    {        
        throw new UnsupportedOperationException();
    }

}
