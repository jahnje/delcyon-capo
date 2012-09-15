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
import java.util.HashMap;
import java.util.logging.Level;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.ControlElement;
import com.delcyon.capo.controller.VariableContainer;
import com.delcyon.capo.resourcemanager.ContentFormatType;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceParameter;
import com.delcyon.capo.resourcemanager.ResourceParameterBuilder;
import com.delcyon.capo.resourcemanager.ResourceURI;
import com.delcyon.capo.resourcemanager.types.ContentMetaData.Attributes;
import com.delcyon.capo.xml.XPath;
import com.delcyon.capo.xml.dom.ResourceElement;

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
	private HashMap<String, Boolean> keyMap = new HashMap<String, Boolean>();
	private HashMap<String, Boolean> includeMap = new HashMap<String, Boolean>();
	private HashMap<String, Boolean> excludeMap = new HashMap<String, Boolean>();
	private SimpleContentMetaData contentMetaData;
	private SimpleContentMetaData iterationContentMetaData;
	private ResultSet resultSet;
	private Statement statement;
	private boolean table = false;
	private String rule = null;
	
	
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
	public void init(ResourceElement declaringResourceElement,VariableContainer variableContainer, LifeCycle lifeCycle, boolean iterate, ResourceParameter... resourceParameters) throws Exception
	{

	    super.init(declaringResourceElement,variableContainer, lifeCycle, iterate, resourceParameters);
	    
		if (getResourceURI().getChildResourceURI() != null)
		{
			this.table = true;
		}
		if(getDeclaringResourceElement() != null)
		{
		    ResourceElement resourceElement = getDeclaringResourceElement();
		    Element controlElementDeclaration = resourceElement.getResourceControlElement().getControlElementDeclaration();
		    NodeList includeNodeList = XPath.selectNodes(controlElementDeclaration, "resource:include");
		    for(int index = 0; index < includeNodeList.getLength(); index++)
		    {
		        includeMap.put(((Element) includeNodeList.item(index)).getAttribute("name"), true);
		    }
		    NodeList excludeNodeList = XPath.selectNodes(controlElementDeclaration, "resource:exclude");
            for(int index = 0; index < excludeNodeList.getLength(); index++)
            {
                excludeMap.put(((Element) excludeNodeList.item(index)).getAttribute("name"), true);
            }
            
            StringBuffer buffer = new StringBuffer();
            processRules(controlElementDeclaration, buffer);
            this.rule = buffer.toString();
            
		}

		
		contentMetaData = buildContentMetatData();
	}
	
	
	private void processRules(Element ruleContainingElement,StringBuffer buffer) throws Exception
	{
		NodeList ruleNodeList = XPath.selectNodes(ruleContainingElement, "resource:rule");
		for(int index = 0; index < ruleNodeList.getLength(); index++)
        {
            Element ruleElement = (Element) ruleNodeList.item(index);
            if(ruleElement.hasAttribute("match"))
            {
            	if(ruleElement.getAttribute("match").equalsIgnoreCase("all"))
            	{
            		//(...and...)
            		buffer.append("(");
            		NodeList childRuleNodeList = XPath.selectNodes(ruleElement, "resource:rule");
            		for(int childIndex = 0; childIndex < childRuleNodeList.getLength(); childIndex++)
                    {
            			if(childIndex != 0)
            			{
            				buffer.append(" and ");
            			}
                        Element childRuleElement = (Element) childRuleNodeList.item(childIndex);
                        processRules(childRuleElement, buffer);                        
                    }
            		buffer.append(")");
            	}
            	else if (ruleElement.getAttribute("match").equalsIgnoreCase("any"))
            	{
            		//(...or...)
            		buffer.append("(");
            		NodeList childRuleNodeList = XPath.selectNodes(ruleElement, "resource:rule");
            		for(int childIndex = 0; childIndex < childRuleNodeList.getLength(); childIndex++)
                    {
            			if(childIndex != 0)
            			{
            				buffer.append(" or ");
            			}
                        Element childRuleElement = (Element) childRuleNodeList.item(childIndex);
                        processRules(childRuleElement, buffer);                        
                    }
            		buffer.append(")");
            	}
            }
            else if(ruleElement.hasAttribute("value"))
            {
            	if(index != 0)
    			{
    				buffer.append(" and ");
    			}
            	buffer.append(ruleElement.getAttribute("value"));
            }
        }
		if (ruleContainingElement.hasAttribute("value"))
        {
        	buffer.append(ruleContainingElement.getAttribute("value"));
        }		
	}
	
	@Override
	public void open(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{
	    if (getResourceState().ordinal() < State.OPEN.ordinal())
	    {
	        connection = DriverManager.getConnection(getResourceURI().getBaseURI(), getVarValue(getDeclaringVariableContainer(),"user"), getVarValue(getDeclaringVariableContainer(),"password"));
	        if (isIterating() && getResourceState() == State.OPEN)
	        {
	            readXML(variableContainer,resourceParameters);
	        }
	        super.open(variableContainer,resourceParameters);
	    }
	    if (table == true)
	    {
	        ResultSet tableResultSet = connection.getMetaData().getPrimaryKeys(null, null, getLocalName());	        
	        while(tableResultSet.next())
	        {
	            keyMap.put(tableResultSet.getString("COLUMN_NAME"), true);	            	            
	        }	        
	    }
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
			if(getVarValue(variableContainer,"query") != null && getVarValue(variableContainer,"query").isEmpty() == false)
			{
			    resultSet = statement.executeQuery(getVarValue(variableContainer,"query"));
			}
			else
			{
				if (rule == null)
				{
					resultSet = statement.executeQuery("select * from "+getLocalName());
				}
				else
				{
					String sql = "select * from "+getLocalName()+" where "+processVars(variableContainer, rule);					
					resultSet = statement.executeQuery(sql);					 
				}
			}
			
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
	    ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
		Element rowElement = document.createElement(getLocalName());
		rowElement.setAttribute("number", resultSet.getRow()+"");
		
		int columnCount = resultSetMetaData.getColumnCount();
		String keyString = "";
		
		for(int currentColumn = 1; currentColumn <= columnCount; currentColumn++)
		{
		    String columName = resultSetMetaData.getColumnName(currentColumn);
		    String value = resultSet.getString(currentColumn);
		    
		    if(includeMap.isEmpty() == false && includeMap.containsKey(columName))
		    {
		        rowElement.setAttribute(columName,value);
		    }
		    else if(includeMap.isEmpty() == true && excludeMap.containsKey(columName) == false)
		    {
		        rowElement.setAttribute(columName,value);
		    }
		    else if(includeMap.isEmpty() && excludeMap.isEmpty())
		    {
		        rowElement.setAttribute(columName,value);
		    }
		    
		    if(keyMap.size() > 0 && keyMap.containsKey(columName))
		    {
		        keyString += columName+"="+value+";"; 
		    }
		    else if(keyMap.size() == 0)//if there are no primary keys, then we have to use all of the results for the key
		    {
		        keyString += columName+"="+value+";";
		    }
		}
		rowElement.setAttribute("uri", getResourceURI().getResourceURIString()+"?"+keyString);
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
	
	@Override
	public ResourceDescriptor getChildResourceDescriptor(ControlElement callingControlElement, String relativeURI) throws Exception
	{
	    JdbcResourceDescriptor jdbcResourceDescriptor = new JdbcResourceDescriptor();
	    jdbcResourceDescriptor.connection = this.connection;
	    jdbcResourceDescriptor.setResourceType(getResourceType());
	    jdbcResourceDescriptor.setResourceURI(new ResourceURI(getResourceURI().getBaseURI()+"!"+relativeURI));
	    jdbcResourceDescriptor.init(null, getDeclaringVariableContainer(), getLifeCycle(),isIterating(), ResourceParameterBuilder.getResourceParameters(callingControlElement.getControlElementDeclaration()));
	    jdbcResourceDescriptor.setLocalName(relativeURI);
	    jdbcResourceDescriptor.setResourceState(State.OPEN);
	    return jdbcResourceDescriptor;
	}
	
}
