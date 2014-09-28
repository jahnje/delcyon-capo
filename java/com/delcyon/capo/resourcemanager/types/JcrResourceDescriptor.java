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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.EnumSet;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.Value;

import com.delcyon.capo.ContextThread;
import com.delcyon.capo.datastream.stream_attribute_filter.ContentFormatTypeFilterInputStream;
import com.delcyon.capo.datastream.stream_attribute_filter.MD5FilterInputStream;
import com.delcyon.capo.datastream.stream_attribute_filter.MimeTypeFilterInputStream;
import com.delcyon.capo.datastream.stream_attribute_filter.SizeFilterInputStream;
import com.delcyon.capo.resourcemanager.ResourceParameter;
import com.delcyon.capo.server.jackrabbit.CapoJcrServer;
import com.delcyon.capo.webapp.servlets.CapoWebApplication;
import com.delcyon.capo.xml.cdom.VariableContainer;
import com.delcyon.capo.xml.dom.ResourceDeclarationElement;

import eu.webtoolkit.jwt.WApplication;

/**
 * @author jeremiah
 *
 */
public class JcrResourceDescriptor extends AbstractResourceDescriptor
{

	
	
	private Session session;
	private Node node;
	private String absPath = null;
	private JcrContentMetaData jcrResourceMetatData = null;
	private boolean isLocalSession = false;
	private volatile Boolean isWriting = false;
	private Boolean hasPipeThreadStarted = false;

	 
	
	@Override
	public void init(ResourceDeclarationElement declaringResourceElement,VariableContainer variableContainer, LifeCycle lifeCycle,boolean iterate, ResourceParameter... resourceParameters) throws Exception
	{
		this.absPath = getResourceURI().getPath();
		super.init(declaringResourceElement, variableContainer, lifeCycle, iterate, resourceParameters);
	}
	
	
	@Override
	public void open(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{
		advanceState(State.INITIALIZED, variableContainer, resourceParameters);
	    if (getResourceState().ordinal() < State.OPEN.ordinal())
	    {
	    	
	    	this.isLocalSession = false;
			
			System.out.println("login");
			
			if(Thread.currentThread() instanceof ContextThread)
			{
			    this.session = ((ContextThread)Thread.currentThread()).getSession();
			}
			else if(WApplication.getInstance() != null)
			{
			    this.session = ((CapoWebApplication)WApplication.getInstance()).getJcrSession();
			}
			else if(CapoJcrServer.getRepository() != null && getLifeCycle() == LifeCycle.EXPLICIT)
	        {
	            this.session = CapoJcrServer.getRepository().login(new SimpleCredentials("admin","admin".toCharArray()));
	            this.isLocalSession = true;
	        }
			if(this.session == null)
			{
			    throw new Exception("Can't use JCR resources without a Session");
			}
			
			if(session.nodeExists(absPath) == true)
			{
				this.node = session.getNode(absPath);
			}
	        super.open(variableContainer,resourceParameters);
	        ((JcrContentMetaData) getResourceMetaData(variableContainer, resourceParameters)).setNode(this.node);
	    }
	}
	
	@Override
	public StreamType[] getSupportedStreamTypes() throws Exception
	{
		return  EnumSet.of(StreamType.INPUT, StreamType.OUTPUT).toArray(new StreamType[]{});
	}

	@Override
	public StreamFormat[] getSupportedStreamFormats(StreamType streamType) throws Exception
	{
	    if(streamType == StreamType.INPUT || streamType == StreamType.OUTPUT)
	    {
	        return EnumSet.of(StreamFormat.XML_BLOCK,StreamFormat.STREAM).toArray(new StreamFormat[]{});
	    }
	    else
	    {
	        return null;
	    }
	}

	@Override
	public boolean next(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{		
	    advanceState(State.OPEN, variableContainer, resourceParameters);
        if(getResourceState() == State.OPEN)
        {
            
           // contentMetaData = new SimpleContentMetaData(getResourceURI());
//            URL url = new URL(getResourceURI().getBaseURI());   
//            InputStream inputStream = contentMetaData.wrapInputStream(url.openConnection().getInputStream());
//            content = contentMetaData.readInputStream(inputStream,true);
            setResourceState(State.STEPPING);
            return true;
        }
        else if (getResourceState() == State.STEPPING)
        {
            setResourceState(State.OPEN);
            return false;
        }
        return false;
	}

	@Override
	public ContentMetaData getContentMetaData(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{		
	    return buildResourceMetaData(variableContainer, resourceParameters);
	}

	@Override
	public ContentMetaData getOutputMetaData(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{
	    return buildResourceMetaData(variableContainer, resourceParameters);
	}

	@Override
	protected void clearContent() throws Exception
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	protected ContentMetaData buildResourceMetaData(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{
		if(jcrResourceMetatData == null)
		{
		    this.jcrResourceMetatData = new JcrContentMetaData(getResourceURI(), node);; 
		}
		return this.jcrResourceMetatData;
	}

	@Override
	public OutputStream getOutputStream(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{
		
	    synchronized (isWriting)
		{
	    	isWriting = true;
	    	final PipedInputStream pipedInputStream = new PipedInputStream(){
	    		@Override
	    		public void close() throws IOException
	    		{
	    			System.out.println("piped input close attempt: "+System.currentTimeMillis());
	    			// TODO Auto-generated method stub
	    			super.close();
	    			System.out.println("piped input closed: "+System.currentTimeMillis());

	    		}
	    	};

	    	PipedOutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream){
	    		@Override
	    		public void close() throws IOException
	    		{	
	    			System.out.println("piped output close attempt: "+System.currentTimeMillis());
	    			
	    			
	    			super.close();
	    			System.out.println("isWaiting = "+isWriting);
	    			Boolean _isWaiting = null;
	    			synchronized (isWriting)
	    			{
	    				_isWaiting = isWriting;
	    			}
	    				//don't let this close while we're still dealing with threaded output
	    				System.out.println("isWaiting = "+_isWaiting);
	    				while(_isWaiting == true )
	    				{
	    					try
	    					{
	    						System.out.println("waiting for pipe thread to finish");
	    						Thread.sleep(100);
	    						synchronized (isWriting)
								{
	    							_isWaiting = isWriting;
								}
	    					} catch (InterruptedException e)
	    					{					
	    						e.printStackTrace();
	    					}
	    				}
	    			
	    			System.out.println("piped output closed: "+System.currentTimeMillis());
	    			
	    			
	    		}
	    	};


	    	//MimeTable mt = MimeTable.getDefaultTable();
	    	//String mimeType = mt.getContentTypeFor(file.getName());
	    	//if (mimeType == null) mimeType = "application/octet-stream";

	    	// Node fileNode = node.addNode("<name>", "nt:file");

	    	//System.out.println( fileNode.getName() );

	    	//final Node resNode = fileNode.addNode("jcr:content", "nt:resource");
	    	//resNode.setProperty("jcr:mimeType", "<mimeType>");
	    	//resNode.setProperty("jcr:encoding", "");

	    	Runnable pipe = new Runnable()
	    	{

	    		@Override
	    		public void run()
	    		{
	    			

	    				try
	    				{
	    					

	    					MD5FilterInputStream md5FilterInputStream = new MD5FilterInputStream(pipedInputStream);
	    					ContentFormatTypeFilterInputStream contentFormatTypeFilterInputStream = new ContentFormatTypeFilterInputStream(md5FilterInputStream);
	    					MimeTypeFilterInputStream mimeTypeFilterInputStream = new MimeTypeFilterInputStream(contentFormatTypeFilterInputStream);
	    					SizeFilterInputStream sizeFilterInputStream = new SizeFilterInputStream(mimeTypeFilterInputStream);
	    					System.out.println("pipe thread start read: "+System.currentTimeMillis());
	    					synchronized (hasPipeThreadStarted)
							{
	    						hasPipeThreadStarted.notify();
							}	    					
	    					Binary binary = node.getSession().getValueFactory().createBinary( sizeFilterInputStream );
	    					node.setProperty("jcr:data",binary);
	    					binary.dispose();                    
	    					node.setProperty(contentFormatTypeFilterInputStream.getName(),contentFormatTypeFilterInputStream.getValue());
	    					node.setProperty(sizeFilterInputStream.getName(),sizeFilterInputStream.getValue());                    
	    					node.setProperty(mimeTypeFilterInputStream.getName(),mimeTypeFilterInputStream.getValue());
	    					node.setProperty(md5FilterInputStream.getName(),md5FilterInputStream.getValue());                    
	    					System.out.println("pipe thread done: "+System.currentTimeMillis());
	    				}
	    				catch (Exception e)
	    				{
	    					e.printStackTrace();            		
	    				}
	    				finally
	    				{
	    					//isWriting.notify();
	    					synchronized (isWriting)
	    	    			{
	    						isWriting = false;
	    	    			}
	    				}
	    			}
	    		
	    	};

	    	synchronized (hasPipeThreadStarted)
			{
	    		new Thread(pipe, node.getName()+" pipeThread-"+System.currentTimeMillis()).start();
	    		//Calendar lastModified = Calendar.getInstance();
	    		//lastModified.setTimeInMillis(file.lastModified());
	    		//resNode.setProperty("jcr:lastModified", lastModified);
	    		//Uti


				System.out.println("waiting for pipe thread to start");
				hasPipeThreadStarted.wait(1500);
			}
			
			
	    	return pipedOutputStream;
		}
	}
	
	@Override
	public InputStream getInputStream(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{	
	    if(node.hasProperty("jcr:data"))
	    {
	        return node.getProperty("jcr:data").getBinary().getStream();
	    }
	    else
	    {
	        return new ByteArrayInputStream(new byte[]{});
	    }
	}
	
//	@Override
//	public void writeXML(VariableContainer variableContainer, CElement element, ResourceParameter... resourceParameters) throws Exception
//	{
//	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//	    XPath.dumpNode(element, outputStream);
//	    session.importXML(getResourceURI().getResourceURIString().replaceFirst("^repo:", ""), new ByteArrayInputStream(outputStream.toByteArray()), 0);
//	    dump(session.getRootNode());
//	    session.save();
//	    String[] langs = session.getWorkspace().getQueryManager().getSupportedQueryLanguages();
//	    String[] prefixes = session.getNamespacePrefixes();
//	    
//	    Query query = session.getWorkspace().getQueryManager().createQuery("SELECT * FROM [nt:unstructured] where NAME([nt:unstructured]) = 'server:log' order by message", "JCR-SQL2");
//	    QueryResult result = query.execute();
//
//
//	    // Iterate over the nodes in the results ...
//
//	    NodeIterator nodeIter = result.getNodes();
//	    System.out.println("=============================");
//	    while ( nodeIter.hasNext() ) {
//
//	        Node _node = nodeIter.nextNode();
//	        System.out.println("===>"+_node.getName()+" type:"+_node.getPrimaryNodeType().getName());
//	        dump(_node);
//
//	    }
//	    System.out.println("=============================");
//	    session.logout();
//	}
	
	
	@Override
	public boolean performAction(VariableContainer variableContainer,Action action,ResourceParameter... resourceParameters) throws Exception
	{
	    super.addResourceParameters(variableContainer, resourceParameters);
	    advanceState(State.OPEN, variableContainer, resourceParameters);
        
        boolean success = false;

        if (action == Action.CREATE)
        {
        	if(session.nodeExists(absPath) == false)
    		{
    		    String[] relPath = absPath.split("/");
    		    Node currentNode = session.getRootNode();
    		    for (String nodeName : relPath)
                {
    		        if(nodeName.isEmpty())
    		        {
    		            currentNode = session.getRootNode();
    		            continue;
    		        }
                    if(currentNode.hasNode(nodeName) == false)
                    {
                        currentNode.addNode(nodeName);//,"nt:folder");
                    }
                    currentNode = currentNode.getNode(nodeName);
                }
    		    //session.getRootNode().addNode(getResourceURI().getResourceURIString().replaceFirst("^repo:/", ""));
    		    this.node = session.getNode(absPath);
    		    ((JcrContentMetaData) getResourceMetaData(variableContainer, resourceParameters)).setNode(this.node);
    		    refreshResourceMetaData(variableContainer, resourceParameters);
    		    success = true;
    		}
        }
        else if (action == Action.DELETE)
        {
            if (node != null)
            {
                node.remove();
                this.node = null;
    		    ((JcrContentMetaData) getResourceMetaData(variableContainer, resourceParameters)).setNode(this.node);
    		    refreshResourceMetaData(variableContainer, resourceParameters);
    		    success = true;
            }
        }
        else if (action == Action.COMMIT)
        {
            if (node != null)
            {
                node.getSession().save();
    		    success = true;
            }
        }
	    return success;
	}
	
	@Override
	protected Action[] getSupportedActions()
	{
		return (Action[]) EnumSet.allOf(Action.class).toArray(new Action[]{});
	}

	/** Recursively outputs the contents of the given node. */ 
	public static void dump(Node node) throws RepositoryException { 
	    // First output the node path 
	    if(node == null)
	    {
	        return;
	    }
	    // Skip the virtual (and large!) jcr:system subtree 
	    if (node.getPath().startsWith("/jcr:system")) { 
	        return; 
	    } 
	    System.out.println(node.getPath()); 
	    // Then output the properties 
	    PropertyIterator properties = node.getProperties(); 
	    while (properties.hasNext()) { 
	        Property property = properties.nextProperty(); 
	        if (property.getDefinition().isMultiple()) { 
	            // A multi-valued property, print all values 
	            Value[] values = property.getValues(); 
	            for (int i = 0; i < values.length; i++) { 
	                System.out.println( 
	                        property.getPath() + " = " + values[i].getString()); 
	            } 
	        } else { 
	            // A single-valued property 
	            System.out.println( 
	                    property.getPath() + " = " + property.getString()); 
	        } 
	    } 

	    // Finally output all the child nodes recursively 
	    NodeIterator nodes = node.getNodes(); 
	    while (nodes.hasNext()) { 
	        dump(nodes.nextNode()); 
	    } 
	} 

	@Override
	public void close(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{	 
		super.close(variableContainer, resourceParameters);
	    if(isLocalSession == true && session != null && session.isLive())
	    {
	        session.logout();
	        ((JcrContentMetaData) getResourceMetaData(variableContainer, resourceParameters)).setNode(null);
		    this.session = null;
	    }
	    
	    
	}
	
 
}
