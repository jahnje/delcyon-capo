/**
Copyright (C) 2012  Delcyon, Inc.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.delcyon.capo.resourcemanager.types;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.logging.Level;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.VariableContainer;
import com.delcyon.capo.resourcemanager.ContentFormatType;
import com.delcyon.capo.resourcemanager.ResourceParameter;
import com.delcyon.capo.resourcemanager.types.ContentMetaData.Attributes;

/**
 * @author jeremiah
 */
public class JdbcResourceDescriptor extends AbstractResourceDescriptor
{
	
	
	private enum LocalAttributes
	{
		rowCount,updateCount
	}
	
	private Connection connection = null;
	
	private SimpleContentMetaData contentMetaData;
	private SimpleContentMetaData iterationContentMetaData;
	private ResultSet resultSet;
	private Statement statement;
	
	
	
	
	private SimpleContentMetaData buildContentMetatData()
	{
		SimpleContentMetaData simpleContentMetaData  = new SimpleContentMetaData(getResourceURI());
		simpleContentMetaData.addSupportedAttribute(Attributes.exists,Attributes.readable,Attributes.writeable,Attributes.container);		
		simpleContentMetaData.setValue(Attributes.exists,true);
		simpleContentMetaData.setValue(Attributes.readable,true);
		simpleContentMetaData.setValue(Attributes.writeable,true);
		simpleContentMetaData.setValue(Attributes.container,true);
		simpleContentMetaData.setValue("mimeType","aplication/xml");
		simpleContentMetaData.setValue("MD5","");
		simpleContentMetaData.setValue("contentFormatType",ContentFormatType.XML);
		simpleContentMetaData.setValue("size","0");
		return simpleContentMetaData;
	}
	
	@Override
	public void init(VariableContainer variableContainer,LifeCycle lifeCycle, boolean iterate, ResourceParameter... resourceParameters) throws Exception
	{
	
		super.init(variableContainer,lifeCycle, iterate, resourceParameters);
		
		contentMetaData = buildContentMetatData();
	}
	
	@Override
	public void open(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{
		connection = DriverManager.getConnection(getResourceURI().toString(), getVarValue(getDeclaringVariableContainer(),"user"), getVarValue(getDeclaringVariableContainer(),"password"));
		if (isIterating() && getResourceState() == State.OPEN)
		{
			readXML(variableContainer,resourceParameters);
		}
		super.open(variableContainer,resourceParameters);
	}
	
	
	@Override
	public boolean next(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
	    if (getResourceState() == State.OPEN)
	    {
	        readXML(variableContainer, resourceParameters);
	    }
		if (getResourceState() == State.STEPPING && resultSet != null)
		{
			if(resultSet.next())
			{
				return true;
			}
			else
			{
				statement.close();
				resultSet = null;
				setResourceState(State.STEPPING);
			}
		}
		return false;
	}
	
	@Override
	public Element readXML(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{
		iterationContentMetaData = buildContentMetatData();
		
		Document document = CapoApplication.getDocumentBuilder().newDocument();
		Element rootElement = document.createElement("ResultSet");
		rootElement.setAttribute("sql", getVarValue(variableContainer,"query"));
		document.appendChild(rootElement);
		
		if (getResourceState() == State.STEPPING && resultSet != null)
		{
			Element rowElement = buildElementFromResultSet(document,resultSet);
			rootElement.appendChild(rowElement);
			return document.getDocumentElement();
		}
		
		if (connection != null && connection.isClosed() == false)
		{			
			statement = connection.createStatement();
			resultSet = statement.executeQuery(getVarValue(variableContainer,"query"));
			
			if (isIterating() == false)
			{
				while(resultSet.next())
				{
					Element rowElement = buildElementFromResultSet(document,resultSet);
					rootElement.appendChild(rowElement);
					
				}
				statement.close();
			}
			else
			{
				
				setResourceState(State.STEPPING);
			}
			
			return document.getDocumentElement();
			
		}
		else
		{
			return null;
		}
	}
	
	


	private Element buildElementFromResultSet(Document document,ResultSet resultSet) throws Exception
	{
		Element rowElement = document.createElement("Row");
		rowElement.setAttribute("number", resultSet.getRow()+"");
		
		ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
		int columnCount = resultSetMetaData.getColumnCount();
		for(int currentColumn = 1; currentColumn <= columnCount; currentColumn++)
		{
			Element columnElement = document.createElement("Column");
			rowElement.appendChild(columnElement);
			columnElement.setAttribute("name", resultSetMetaData.getColumnName(currentColumn));
			columnElement.setAttribute("type", resultSetMetaData.getColumnTypeName(currentColumn));
			columnElement.setTextContent(resultSet.getString(currentColumn));
		}
		return rowElement;
	}
	
	@Override
	public void processOutput(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{
		
		iterationContentMetaData = buildContentMetatData();
		iterationContentMetaData.addSupportedAttribute(LocalAttributes.values());
		try
			{
				Statement statement = connection.createStatement();
				int rowCount = statement.executeUpdate(getVarValue(variableContainer,"update"));
				iterationContentMetaData.setValue(LocalAttributes.rowCount, rowCount); 				
			} 
			catch (Exception exception)
			{
				throw new IOException(exception);
			} 
	}
	
	
 	@Override
	public ContentMetaData getContentMetaData(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{
		return contentMetaData;
	}

	@Override
	public ContentMetaData getIterationMetaData(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{
		return iterationContentMetaData;
	}

	

	

	@Override
	public StreamFormat[] getSupportedStreamFormats(StreamType streamType)
	{
		if (streamType == StreamType.INPUT)
		{
			return new StreamFormat[]{StreamFormat.XML_BLOCK};
		}
		else if(streamType == StreamType.OUTPUT)
		{
			return new StreamFormat[]{StreamFormat.PROCESS};
		}
		else
		{
			return null;
		}
	}

	@Override
	public StreamType[] getSupportedStreamTypes()
	{
		return new StreamType[]{StreamType.INPUT,StreamType.OUTPUT};
	}

	@Override
	public Action[] getSupportedActions()
	{		
		return new Action[]{};
	}
	
	@Override
	public void release(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		if (connection != null)
		{
			CapoApplication.logger.log(Level.INFO, "Closeing DB Connection");
			connection.close();
		}
		super.release(variableContainer, resourceParameters);
	}
	
}
