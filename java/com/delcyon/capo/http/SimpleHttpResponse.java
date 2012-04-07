package com.delcyon.capo.http;

import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

public class SimpleHttpResponse
{

	private int responseCode = 200;
	private HashMap<String, String> responseHeaderHashMap = new HashMap<String, String>();
	private String responseMessage = "OK";
	
	public void setResponseCode(int responseCode, String responseMessage)
	{
		this.responseCode  = responseCode;
		this.responseMessage = responseMessage;
	}

	
	public void setHeader(String headerName,String headerValue)
	{
		responseHeaderHashMap.put(headerName, headerValue);
	}
	
	public byte[] getBytes()
	{
		StringBuilder stringBuilder = new StringBuilder();
		//add required first line
		stringBuilder.append("HTTP/1.1 "+responseCode+" "+responseMessage+"\r\n");
		
		//iterate through headers
		Set<Entry<String, String>> entrySet = responseHeaderHashMap.entrySet();
		for (Entry<String, String> entry : entrySet)
		{
			stringBuilder.append(entry.getKey()+": "+entry.getValue()+"\r\n");
		}
		//finish with empty line
		stringBuilder.append("\r\n");
		return stringBuilder.toString().getBytes();
	}

}
