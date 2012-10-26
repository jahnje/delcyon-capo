package com.delcyon.capo.xml.dom;

import java.util.ArrayList;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.delcyon.capo.util.ControlledClone;

public class ResourceNodeList extends ArrayList<Node> implements NodeList, ControlledClone
{

    private ResourceNode parentNode = null;
    
    protected ResourceNode getParentNode()
    {        
        return parentNode;
    }
    
    @Override
    public void preClone(Object parentClonedObject, Object clonedObject) throws Exception
    {        
        ((ResourceNodeList)clonedObject).parentNode = ((ResourceNodeList)parentClonedObject).parentNode;        
    }
    
    @Override
    public void postClone(Object parentClonedObject, Object clonedObject) throws Exception {}//not used
    
    @Override
    public Node item(int index)
    {
        return get(index);
    }

    @Override
    public int getLength()
    {
        return size();
    }

    public void addAll(NodeList childNodes)
    {
        for(int index = 0; index < childNodes.getLength(); index++)
        {
            add(childNodes.item(index));
        }
        
    }
    
    public void addAll(ResourceNodeList childNodes)
    {        
        for(int index = 0; index < childNodes.getLength(); index++)
        {
            add(childNodes.item(index));
        }
        
    }

    

}
