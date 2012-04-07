package com.delcyon.capo.controller.elements;

import java.util.HashMap;

import com.delcyon.capo.controller.server.ControllerClientRequestProcessor;

public class RemoteGroupMessage 
{
	private HashMap<String, String> variableHashMap;
	private ControllerClientRequestProcessor controllerClientRequestProcessor;
	private String sessionID;	
	
	public HashMap<String, String> getVariableHashMap()
	{
		return variableHashMap;
	}

	public void setVariableHashMap(HashMap<String, String> variableHashMap)
	{
		this.variableHashMap = variableHashMap;
	}

	public ControllerClientRequestProcessor getControllerClientRequestProcessor()
	{
		return controllerClientRequestProcessor;
	}

	public void setControllerClientRequestProcessor(ControllerClientRequestProcessor controllerClientRequestProcessor)
	{
		this.controllerClientRequestProcessor = controllerClientRequestProcessor;
	}

	public String getSessionID()
	{
		return sessionID;
	}

	public void setSessionID(String sessionID)
	{
		this.sessionID = sessionID;
	}

	
}