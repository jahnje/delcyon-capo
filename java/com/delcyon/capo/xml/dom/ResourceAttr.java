package com.delcyon.capo.xml.dom;

import java.lang.reflect.Modifier;

import com.delcyon.capo.controller.elements.ResourceControlElement;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.util.ControlledClone;
import com.delcyon.capo.util.ToStringControl;
import com.delcyon.capo.util.ToStringControl.Control;
import com.delcyon.capo.xml.cdom.CAttr;

@ToStringControl(control=Control.exclude,modifiers=Modifier.FINAL+Modifier.STATIC)
public class ResourceAttr extends CAttr implements ControlledClone,ResourceNode
{
    
	@SuppressWarnings("unused")
	private ResourceAttr(){}//serialization only
	
    public ResourceAttr(ResourceElement resourceElement, String attributeName, String value)
	{
		super(resourceElement,attributeName,value);
	}

	@Override
    public ResourceDescriptor getResourceDescriptor()
    {
        return ((ResourceNode) getParentNode()).getResourceDescriptor();
    }
    
    @Override
    public ResourceDescriptor getProxyedResourceDescriptor()
    {
    	return ((ResourceNode) getParentNode()).getProxyedResourceDescriptor();
    }
    
    @Override
    public ResourceControlElement getResourceControlElement()
    {
        return ((ResourceNode) getParentNode()).getResourceControlElement();
    }
   
    @Override
    public ResourceDocument getOwnerResourceDocument()
    {    	
    	return ((ResourceNode) getParentNode()).getOwnerResourceDocument();
    }
    
    
    
   
   
    
    
   
   
   
   
    
}
