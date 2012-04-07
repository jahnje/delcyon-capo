package com.delcyon.capo.resourcemanager;

import org.w3c.dom.Element;

public interface ResourceListener
{
	public String getResourceListenerID();
	public void setResourceListenerID(String ID);
	public void resourceModified(Element modifiedElement) throws Exception;
}
