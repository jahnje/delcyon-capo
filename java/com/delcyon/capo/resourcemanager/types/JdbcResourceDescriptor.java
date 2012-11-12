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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.ControlElement;
import com.delcyon.capo.resourcemanager.ContentFormatType;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceParameter;
import com.delcyon.capo.resourcemanager.ResourceParameterBuilder;
import com.delcyon.capo.resourcemanager.ResourceURI;
import com.delcyon.capo.resourcemanager.types.ContentMetaData.Attributes;
import com.delcyon.capo.xml.XPath;
import com.delcyon.capo.xml.cdom.CDocument;
import com.delcyon.capo.xml.cdom.CElement;
import com.delcyon.capo.xml.cdom.CNamedNodeMap;
import com.delcyon.capo.xml.cdom.VariableContainer;
import com.delcyon.capo.xml.dom.ResourceDeclarationElement;

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
	private HashMap<String, String> columnMap = new HashMap<String, String>();
	private HashMap<String, Boolean> includeMap = new HashMap<String, Boolean>();
	private HashMap<String, Boolean> excludeMap = new HashMap<String, Boolean>();
	
	private SimpleContentMetaData contentMetaData = null;
	private SimpleContentMetaData outputMetaData = null;
	private ResultSet resultSet;
	private Statement statement;
	private boolean table = false;
	private String rule = null;
	private CElement rowElement = null;
	private CDocument document = null;
	private boolean exists = true;
	
	@Override
	protected SimpleContentMetaData buildResourceMetaData(VariableContainer variableContainer,ResourceParameter... resourceParameters)
	{
		SimpleContentMetaData simpleContentMetaData  = new SimpleContentMetaData(getResourceURI());
		simpleContentMetaData.addSupportedAttribute(Attributes.exists,Attributes.readable,Attributes.writeable,Attributes.container);		
		simpleContentMetaData.setValue(Attributes.exists,exists);
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
	public void init(ResourceDeclarationElement declaringResourceElement,VariableContainer variableContainer, LifeCycle lifeCycle, boolean iterate, ResourceParameter... resourceParameters) throws Exception
	{

	    document = (CDocument) CapoApplication.getDocumentBuilder().newDocument();
	    document.setSilenceEvents(true);
	    super.init(declaringResourceElement,variableContainer, lifeCycle, iterate, resourceParameters);
	    
		if (getResourceURI().getChildResourceURI() != null)
		{
			this.table = true;
		}
		if(getDeclaringResourceElement() != null)
		{
			ResourceDeclarationElement resourceDeclarationElement = getDeclaringResourceElement();
		    Element controlElementDeclaration = resourceDeclarationElement.getDeclaration();
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
	    advanceState(State.INITIALIZED, variableContainer, resourceParameters);
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
	        if(getResourceURI().getChildResourceURI() != null)
	        {
	            setLocalName(getResourceURI().getChildResourceURI().getPath());
	        }
	        ResultSet tableResultSet = connection.getMetaData().getPrimaryKeys(null, null, getLocalName());	        
	        while(tableResultSet.next())
	        {	            
	            keyMap.put(tableResultSet.getString("COLUMN_NAME"), true);	            	            
	        }
	        tableResultSet.close();
	        tableResultSet = connection.getMetaData().getColumns(null, null, getLocalName(),null);          
            while(tableResultSet.next())
            {                                
                columnMap.put(tableResultSet.getString("COLUMN_NAME"), tableResultSet.getString("DATA_TYPE"));                              
            }
            if(columnMap.size() == 0)
            {
                exists = false;
                throw new Exception("table '"+getLocalName()+"' not found. Check your case");
            }
	    }
	    else
	    {
	        //got a connection so assume all is good
	        exists = true;
	    }
	}
	
	@Override
	protected void clearContent() throws Exception
	{
	    rowElement = null;
	    if(statement != null)
	    {
	        statement.close();
	    }
	}
	
	@Override
	public boolean next(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
	    
	    advanceState(State.OPEN, variableContainer, resourceParameters);
	    if (connection == null && connection.isClosed() == true)
        {
	        setResourceState(State.CLOSED);
	        return false;
        }
	    
	    contentMetaData = buildResourceMetaData(variableContainer,resourceParameters);
	    
	    if(table == true)
        {                            
            contentMetaData.setValue(Attributes.container,false);
        }
	    
	    //if we're open then we're going to be running a new query
	    if (getResourceState() == State.OPEN)
	    {	
	        statement = connection.createStatement();
	        
	        //Build the SQL by processing any rules
	        String sql = "select * from "+getLocalName();
	        
	        //see if the basic sql statement has been overridden	        
	        if(getVarValue(variableContainer,"query") != null && getVarValue(variableContainer,"query").isEmpty() == false)
	        {
	            sql = getVarValue(variableContainer,"query");
	            table = true;
	        }
	        
	        //see if there are any rules to apply to the sql statement
	        if (rule != null)
            {            
                String whereClause = processVars(variableContainer, rule);
                if (whereClause.trim().isEmpty() == false)
                {
                    sql+=" where "+whereClause;
                }
                CapoApplication.logger.log(Level.FINE, "RUNNING SQL = '"+sql+"'");
                System.err.println("RUNNING SQL = '"+sql+"'");
            }
	        
	        //see if we are running some sql against a table, or a DB
	        if(table == true)
            {                
                resultSet = statement.executeQuery(sql);                
            }
            //we're working with a DB, so give them the description
            else
            {
                resultSet = connection.getMetaData().getTables(null, null, null, null);
            }
	        
	        //call next to verify if we got any reults
	        if(resultSet.next())
	        {	            
	            rowElement = buildElementFromResultSet(resultSet);
	            setResourceState(State.STEPPING);
	            return true;
	        }
	        else //didn't get any, so bail out 
	        {
	            statement.close();
                resultSet = null;
                rowElement = null;
	            return false;
	        }
	         
	    }
	    //looks like were already in a query, so give them the next resultset. 
	    else if (getResourceState() == State.STEPPING && resultSet != null)
		{
			if(resultSet.next())
			{			    
			    rowElement = buildElementFromResultSet(resultSet);
				return true;
			}
			else
			{
				statement.close();
				resultSet = null;
				setResourceState(State.OPEN);
			}
		}
	    else //this just isn't valid, so make sure we don't give there user anything useful if they don't follow the rules.
	    {	        
	        contentMetaData = null;
	    }
		return false;
	}
	
	@Override
	public CElement readXML(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{
	    advanceState(State.STEPPING, variableContainer, resourceParameters);
		return rowElement;		
	}
	
	


	private CElement buildElementFromResultSet(ResultSet resultSet) throws Exception
	{
	    ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
	    CElement rowElement = (CElement) document.createElement(getLocalName());
		
		//rowElement.setAttribute("number", resultSet.getRow()+"");
		
		int columnCount = resultSetMetaData.getColumnCount();
		String keyString = "";
		
		for(int currentColumn = 1; currentColumn <= columnCount; currentColumn++)
		{
		    String columName = resultSetMetaData.getColumnName(currentColumn);
		    String value = resultSet.getString(currentColumn);
		    
		    //check to see if we're supposed to include this
		    if(includeMap.isEmpty() == false && includeMap.containsKey(columName))
		    {
		        rowElement.setAttribute(columName,value);
		    }
		    //if we don't have any includes and we're not supposed to exclude it then add it 
		    else if(includeMap.isEmpty() == true && excludeMap.isEmpty() == false && excludeMap.containsKey(columName) == false)
		    {
		        rowElement.setAttribute(columName,value);
		    }
		    //this is the fall through, but we're only going to add  non empty things
		    else if(includeMap.isEmpty() && excludeMap.isEmpty())
		    {
		        if(value != null && value.trim().isEmpty() == false)
	            {
		            rowElement.setAttribute(columName,value);
	            }		        
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
		if(table == true)
		{
		    contentMetaData.setResourceURI(new ResourceURI(getResourceURI().getResourceURIString()+"?"+keyString));		    
		}
		else // assume we're working with a DB result
		{
		    contentMetaData.setResourceURI(new ResourceURI(getResourceURI().getResourceURIString()+"!"+resultSet.getString("TABLE_NAME")));		    
		}
		return rowElement;
	}
	
	@Override
	public void processOutput(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{
	    advanceState(State.OPEN, variableContainer, resourceParameters);
		outputMetaData = buildResourceMetaData(variableContainer,resourceParameters);
		outputMetaData.addSupportedAttribute(LocalAttributes.values());
		try
			{
				Statement statement = connection.createStatement();
				int rowCount = statement.executeUpdate(getVarValue(variableContainer,"update"));
				outputMetaData.setValue(LocalAttributes.rowCount, rowCount); 				
			} 
			catch (Exception exception)
			{
				throw new IOException(exception);
			} 
	}
	
	@Override
	public void writeXML(VariableContainer variableContainer, CElement element, ResourceParameter... resourceParameters) throws Exception
	{
		advanceState(State.OPEN, variableContainer, resourceParameters);
		CNamedNodeMap cNamedNodeMap = (CNamedNodeMap) element.getAttributes();
		boolean isInsert = ResourceParameterBuilder.getBoolean(DefaultParameters.NEW, resourceParameters);
		String keyString = "";
		String whereClause = "";
		if (keyMap.size() > 0)
		{
		    whereClause += " where ";
		}
		
		String sql = null;
		String insertClause = " values(";
		if (isInsert == false)
		{
		    sql = "update "+getLocalName()+" set ";
		}
		else
		{
		    sql = "insert into "+getLocalName()+" (";
		}
		
		for (Node node : cNamedNodeMap)
        {
		    if(isInsert == false)
		    {
		        sql += node.getNodeName()+" = ?,";
		    }
		    else
		    {
		        sql += node.getNodeName()+",";
		        insertClause += "?,";
		    }
		    
            if(keyMap.containsKey(node.getNodeName()))
            {
                whereClause += node.getNodeName()+" = ? and";
                keyString += node.getNodeName()+"="+node.getNodeValue()+";";
            }
        }
		if(isInsert == false)
		{
		    sql = sql.substring(0, sql.length()-1)+whereClause.substring(0, whereClause.length()-3);
		}
		else
		{
		    sql = sql.substring(0, sql.length()-1)+")"+insertClause.substring(0,insertClause.length()-1)+")";
		}
		System.out.println(sql);
		PreparedStatement preparedStatement = connection.prepareStatement(sql);
		int index = 1;
		for (Node node : cNamedNodeMap)
        {
		    
		    preparedStatement.setObject(index, node.getNodeValue(),Integer.parseInt(columnMap.get(node.getNodeName())));
		    index++;
        }
		
		if(isInsert == false)
		{
		    for (Node node : cNamedNodeMap)
		    {            
		        if(keyMap.containsKey(node.getNodeName()))
		        {
		            preparedStatement.setObject(index, node.getNodeValue(),Integer.parseInt(columnMap.get(node.getNodeName())));
		            index++;
		        }            
		    }
		}
		System.out.println(preparedStatement);
		preparedStatement.executeUpdate();
		preparedStatement.close();		
		if(getResourceURI().getChildResourceURI() != null && getResourceURI().getChildResourceURI().getParameterMap().size() == 0)
		{
		    setResourceURI(new ResourceURI(getResourceURI().getResourceURIString()+"?"+keyString));
		    refreshResourceMetaData(variableContainer, resourceParameters);		    
		}
	}
 	

	@Override
	public ContentMetaData getContentMetaData(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{
		return contentMetaData;
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
			return new StreamFormat[]{StreamFormat.XML_BLOCK,StreamFormat.PROCESS};
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
		return new Action[]{Action.DELETE};
	}
	
	@Override
	public boolean performAction(VariableContainer variableContainer, Action action, ResourceParameter... resourceParameters) throws Exception
	{
	    if(action == Action.DELETE)
	    {
	        advanceState(State.OPEN, variableContainer, resourceParameters);
	        return delete(variableContainer, resourceParameters);
	    }
	    return false;
	}
	
	private boolean delete(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
    {
       String sql = "delete from "+getLocalName()+" where ";
       if(getResourceURI().getChildResourceURI() != null && getResourceURI().getChildResourceURI().getParameterMap().size() != 0)
       {
           HashMap<String, String> parameterMap = getResourceURI().getChildResourceURI().getParameterMap();
           Set<Entry<String, String>> parameterEntrySet = parameterMap.entrySet();
           for (Entry<String, String> entry : parameterEntrySet)
           {
               sql += " "+entry.getKey()+" = ? and";
           }
           sql = sql.substring(0, sql.length()-3);
           PreparedStatement preparedStatement = connection.prepareStatement(sql);
           int index = 1;
           for (Entry<String, String> entry : parameterEntrySet)
           {
               preparedStatement.setObject(index, entry.getValue(),Integer.parseInt(columnMap.get(entry.getKey())));
               index++;
           }
           preparedStatement.executeUpdate();
           preparedStatement.close();
           exists = false;
           refreshResourceMetaData(variableContainer, resourceParameters);
           return true;
       }
       return false;
    }

    @Override
	public void release(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
		if (connection != null)
		{
			CapoApplication.logger.log(Level.INFO, "Closing DB Connection");
			connection.close();
		}
		super.release(variableContainer, resourceParameters);
	}
	
	@Override
	public ResourceDescriptor getChildResourceDescriptor(ControlElement callingControlElement, String relativeURI) throws Exception
	{
	    advanceState(State.OPEN,null);
	    ResourceURI childResourceURI = new ResourceURI(getResourceURI().getBaseURI()+"!"+relativeURI);
	    JdbcResourceDescriptor jdbcResourceDescriptor = (JdbcResourceDescriptor) callingControlElement.getParentGroup().getResourceDescriptor(callingControlElement, childResourceURI.getResourceURIString());
	    jdbcResourceDescriptor.connection = this.connection;	    
	    jdbcResourceDescriptor.init(null, getDeclaringVariableContainer(), getLifeCycle(),isIterating(), ResourceParameterBuilder.getResourceParameters(callingControlElement.getControlElementDeclaration()));
	    jdbcResourceDescriptor.setLocalName(relativeURI);
	    jdbcResourceDescriptor.setResourceState(State.OPEN);
	    return jdbcResourceDescriptor;
	}
	
}
