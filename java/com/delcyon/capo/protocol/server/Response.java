package com.delcyon.capo.protocol.server;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface Response
{

	public abstract void setSessionID(String sessionID);

	public abstract String getSessionID();

	public abstract Document getResponseDocument();

	public abstract void appendElement(Element element);

}