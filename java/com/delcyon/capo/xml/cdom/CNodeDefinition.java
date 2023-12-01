package com.delcyon.capo.xml.cdom;

import java.util.Vector;

public interface CNodeDefinition extends NodeValidationUtilitesFI
{

    default boolean isValid(CNode node, Vector<CValidationException> exceptionVector) throws Exception
    {
        return isValid(node.getNodeValue(), exceptionVector);
    }
    
    default boolean isValid(String nodeValue, Vector<CValidationException> exceptionVector) throws Exception
    {
        throw new UnsupportedOperationException();
    }

    static CNodeDefinition getDefinitionForNode(CNode cNode)
    {        
        throw new UnsupportedOperationException();
    }

}
