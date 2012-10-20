package com.delcyon.capo.xml.dom;

import org.w3c.dom.Node;

import com.delcyon.capo.controller.elements.ResourceControlElement;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;

public abstract class ResourceNode implements Node
{
    public abstract ResourceDescriptor getResourceDescriptor();
    public abstract ResourceDescriptor getProxyedResourceDescriptor();
    public abstract ResourceControlElement getResourceControlElement();
    public abstract ResourceDocument getOwnerResourceDocument();

}
