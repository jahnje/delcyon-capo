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
package com.delcyon.capo.controller.elements;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.logging.Level;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.delcyon.capo.controller.ControlElement;
import com.delcyon.capo.controller.ControlElementProvider;
import com.delcyon.capo.controller.Group;
import com.delcyon.capo.controller.VariableContainer;
import com.delcyon.capo.controller.client.ClientSideControl;
import com.delcyon.capo.controller.client.ServerControllerResponse;
import com.delcyon.capo.controller.server.ControllerClientRequestProcessor;
import com.delcyon.capo.datastream.StreamUtil;
import com.delcyon.capo.protocol.client.CapoConnection;
import com.delcyon.capo.protocol.server.ClientRequest;
import com.delcyon.capo.protocol.server.ClientRequestProcessor;
import com.delcyon.capo.protocol.server.ClientRequestProcessorSessionManager;
import com.delcyon.capo.protocol.server.ClientRequestXMLProcessor;
import com.delcyon.capo.resourcemanager.remote.RemoteResourceRequest;
import com.delcyon.capo.resourcemanager.remote.RemoteResourceDescriptorMessage.MessageType;
import com.delcyon.capo.server.CapoServer;
import com.delcyon.capo.util.XMLSerializer;
import com.delcyon.capo.xml.XPath;

/**
 * @author jeremiah
 *
 */
@ControlElementProvider(name="remoteGroup")
public class RemoteGroupElement extends GroupElement implements ClientSideControl,VariableContainer,ClientRequestProcessor
{
	
	public enum Attributes
	{		
		name,
		returns
	}
	
	@Override
	public Attributes[] getAttributes()
	{		
		return Attributes.values();
	}

	@Override
	public Attributes[] getRequiredAttributes()
	{
		return new Attributes[]{Attributes.name};
	}

	@Override
	public String[] getSupportedNamespaces()
	{		
		return null;
	}

	private String groupName;
	private Group group;
	private ServerControllerResponse serverControllerResponse;
	private String sessionID = null;
	
	public Group getGroup()
	{
		return group;
	}

	
	//Server side init
	@Override
	public void init(Element controlElementDeclaration, ControlElement parentControlElement, Group parentGroup,ControllerClientRequestProcessor controllerClientRequestProcessor) throws Exception
	{	
		sessionID = ClientRequestProcessorSessionManager.generateSessionID();
		ClientRequestProcessorSessionManager.registerClientRequestProcessor(this,sessionID);
		super.init(controlElementDeclaration, parentControlElement, parentGroup, controllerClientRequestProcessor);
		this.groupName = controlElementDeclaration.getAttribute(Attributes.name.toString());		
		this.group = new Group(groupName,parentGroup,this,controllerClientRequestProcessor);
		CapoServer.logger.log(Level.FINE, "init remote group = "+groupName);
	}
	

	@Override
	public ServerControllerResponse getServerControllerResponse()
	{
		return this.serverControllerResponse;
	}

	@Override
	public Object processServerSideElement() throws Exception
	{
		
		Element groupElementCopy = (Element) getControlElementDeclaration().cloneNode(true);
		
		//make a message
		RemoteGroupMessage remoteGroupMessage = new RemoteGroupMessage();
		remoteGroupMessage.setSessionID(sessionID);
		remoteGroupMessage.setControllerClientRequestProcessor(getControllerClientRequestProcessor());
		
		//create an xml representation of our message object
		XMLSerializer xmlSerializer = new XMLSerializer();
		xmlSerializer.export(remoteGroupMessage, groupElementCopy, 0);
	
		//send it to the server and WAIT for a response
		groupElementCopy = getControllerClientRequestProcessor().sendServerSideClientElement(groupElementCopy);
		
		//marshall the XML back into an instance of the message
		xmlSerializer.marshall(groupElementCopy, remoteGroupMessage);
		getGroup().setVariableHashMap(remoteGroupMessage.getVariableHashMap());
		
		//then see if we have a returns attribute, and if so pull any matching vars from this group to the parent group
		if (getControlElementDeclaration().hasAttribute(Attributes.returns.toString()) && getParentGroup() != null)
		{			
			String[] varnnames = getControlElementDeclaration().getAttribute(Attributes.returns.toString()).split(",");
			for (String varName : varnnames)
			{
				if (group.containsLocalKey(varName))
				{
					getParentGroup().set(varName, group.getLocalValue(varName));
				}				
			}
		}
		
		//cleanup serialized XML
		XPath.removeNodes(groupElementCopy, "child::*[local-name() = 'controllerClientRequestProcessor' or local-name() = 'variableHashMap']");	
		getControlElementDeclaration().getParentNode().replaceChild(groupElementCopy, getControlElementDeclaration());
		
		return null;
	}

	@Override
	public void destroy() throws Exception
	{
		//cleanup after ourselves
		ClientRequestProcessorSessionManager.removeClientRequestProcessor(getSessionId());
		super.destroy();
	}
	
	@Override
	public String getSessionId()
	{
		return sessionID;
	}

	
	//Client side init
	@Override
	public void init(Element controlElementDeclaration, ControlElement parentControlElement, Group parentGroup, ServerControllerResponse serverControllerResponse) throws Exception
	{
		
		RemoteGroupMessage remoteGroupMessage = new RemoteGroupMessage();
		XMLSerializer xmlSerializer = new XMLSerializer();
		xmlSerializer.marshall(controlElementDeclaration, remoteGroupMessage);
		sessionID = remoteGroupMessage.getSessionID();
		//initialize this as a normal group
		super.init(controlElementDeclaration, parentControlElement, parentGroup, remoteGroupMessage.getControllerClientRequestProcessor());
		
		//then initialize this as a client side group
		setParentGroup(parentGroup);
		
		setOriginalControlElementDeclaration(controlElementDeclaration);
		if (parentGroup != null)
		{
			parentGroup.setVariableContainer(this);
			setControlElementDeclaration((Element)parentGroup.replaceVarsInAttributeValues((controlElementDeclaration.cloneNode(true))));
		}
		else
		{
			setControlElementDeclaration((Element) (controlElementDeclaration.cloneNode(true)));
		}
		setParentControlElement(parentControlElement);
		
		this.serverControllerResponse = serverControllerResponse;
		this.groupName = controlElementDeclaration.getAttribute(Attributes.name.toString());
		this.group = new Group(groupName, parentGroup, this, getControllerClientRequestProcessor());
		
	}
	

	@Override
	public Element processClientSideElement() throws Exception
	{
		//need session id
		//create a child group based on this group, and run it		
		GroupElement groupElement = new GroupElement();		
		groupElement.init(getControlElementDeclaration(), this, getGroup(), getControllerClientRequestProcessor());
		groupElement.processServerSideElement();	
		
		//get any variables that were made and send them back with the response
		XMLSerializer xmlSerializer = new XMLSerializer();
		RemoteGroupMessage remoteGroupMessage = new RemoteGroupMessage();
		remoteGroupMessage.setVariableHashMap(getGroup().getVariableHashMap());
		xmlSerializer.export(remoteGroupMessage, getControlElementDeclaration(), 0);
			
		return getControlElementDeclaration();
	}
	
	


	

	

	
	@Override
	public String getVarValue(String varName)
	{
		try
		{
			CapoConnection capoConnection = new CapoConnection();
			RemoteResourceRequest request = new RemoteResourceRequest(capoConnection.getOutputStream(),capoConnection.getInputStream());
			request.setType(MessageType.GET_VAR_VALUE);
			request.setVarName(varName);
			request.setSessionId(sessionID);
			request.send();
			capoConnection.getInputStream();			
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			StreamUtil.readInputStreamIntoOutputStream(capoConnection.getInputStream(), byteArrayOutputStream);
			return new String(byteArrayOutputStream.toByteArray()); 
		} catch (Exception exception)
		{
			exception.printStackTrace();
			return null;
		}
	}

	@Override
	public void process(ClientRequest clientRequest) throws Exception
	{		
		MessageType messageType = RemoteResourceRequest.getType(clientRequest);
		
		//process the request		
		switch (messageType)
		{
			
			case GET_VAR_VALUE:
				String varName = RemoteResourceRequest.getVarName(clientRequest);
				if (getParentGroup() != null)
				{
					String value = getParentGroup().getVarValue(varName);
					if (value != null)
					{
						clientRequest.getOutputStream().write(value.getBytes());
					}					
				}
				break;
			default:
				throw new UnsupportedOperationException(messageType.toString());
		}
		
		
	}

	//DO NOTHINGS

	@Override
	public Document readNextDocument() throws Exception
	{		
		return null;
	}
	
	@Override
	public void init(ClientRequestXMLProcessor clientRequestXMLProcessor, String sessionID,HashMap<String, String> sessionHashMap,String requestName) throws Exception{}
}
