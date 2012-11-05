package com.delcyon.capo.xml.dom;

import com.delcyon.capo.controller.elements.ResourceControlElement;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;

public interface ResourceNode 
{
    public abstract ResourceDescriptor getResourceDescriptor();
    public abstract ResourceDescriptor getProxyedResourceDescriptor();
    public abstract ResourceControlElement getResourceControlElement();   
    public abstract ResourceDocument getOwnerResourceDocument();

}
