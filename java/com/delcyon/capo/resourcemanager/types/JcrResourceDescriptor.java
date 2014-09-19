/**
Copyright (c) 2014 Delcyon, Inc.
This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.delcyon.capo.resourcemanager.types;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.EnumSet;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.jackrabbit.value.BinaryValue;

import com.delcyon.capo.resourcemanager.ResourceParameter;
import com.delcyon.capo.server.jackrabbit.CapoJcrServer;
import com.delcyon.capo.xml.cdom.VariableContainer;
import com.delcyon.capo.xml.dom.ResourceDeclarationElement;

/**
 * @author jeremiah
 *
 */
public class JcrResourceDescriptor extends AbstractResourceDescriptor
{

	
	
	private Session session;
	private Node node;

	@Override
	public void init(ResourceDeclarationElement declaringResourceElement,VariableContainer variableContainer, LifeCycle lifeCycle,boolean iterate, ResourceParameter... resourceParameters) throws Exception
	{

		super.init(declaringResourceElement, variableContainer, lifeCycle, iterate,resourceParameters);
		this.session = CapoJcrServer.getRepository().login();
		this.node = session.getNode(getResourceURI().getResourceURIString());
	}
	
	
	@Override
	public StreamType[] getSupportedStreamTypes() throws Exception
	{
		return (StreamType[]) EnumSet.of(StreamType.INPUT, StreamType.OUTPUT).toArray();
	}

	@Override
	public StreamFormat[] getSupportedStreamFormats(StreamType streamType) throws Exception
	{
		return (StreamFormat[]) EnumSet.of(StreamFormat.XML_BLOCK).toArray();
	}

	@Override
	public boolean next(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{		
		return true;
	}

	@Override
	public ContentMetaData getContentMetaData(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{		
		return new SimpleContentMetaData(getResourceURI(), resourceParameters);
	}

	@Override
	public ContentMetaData getOutputMetaData(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{
		return new SimpleContentMetaData(getResourceURI(), resourceParameters);
	}

	@Override
	protected void clearContent() throws Exception
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	protected ContentMetaData buildResourceMetaData(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{
		// TODO Auto-generated method stub
		return new SimpleContentMetaData(getResourceURI(), resourceParameters);
	}

	@Override
	public OutputStream getOutputStream(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{
		//MimeTable mt = MimeTable.getDefaultTable();
	    //String mimeType = mt.getContentTypeFor(file.getName());
	    //if (mimeType == null) mimeType = "application/octet-stream";

	    Node fileNode = node.addNode("<name>", "nt:file");

	    System.out.println( fileNode.getName() );

	    Node resNode = fileNode.addNode("jcr:content", "nt:resource");
	    resNode.setProperty("jcr:mimeType", "<mimeType>");
	    resNode.setProperty("jcr:encoding", "");
	    resNode.setProperty("jcr:data",new BinaryValue( new FileInputStream("")));
	    Calendar lastModified = Calendar.getInstance();
	    //lastModified.setTimeInMillis(file.lastModified());
	    resNode.setProperty("jcr:lastModified", lastModified);
	    //Uti
	    return new ByteArrayOutputStream();
	}
	
	@Override
	public InputStream getInputStream(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{
		Node jcrContent = node.getNode("jcr:content");
		String fileName = node.getName();
		return jcrContent.getProperty("jcr:data").getBinary().getStream();
	}
	
	@Override
	protected Action[] getSupportedActions()
	{
		return (Action[]) EnumSet.allOf(Action.class).toArray();
	}

}
