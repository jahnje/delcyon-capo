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
import java.io.ByteArrayOutputStream;
import java.util.EnumSet;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import com.delcyon.capo.ContextThread;
import com.delcyon.capo.resourcemanager.ResourceParameter;
import com.delcyon.capo.webapp.servlets.CapoWebApplication;
import com.delcyon.capo.xml.XPath;
import com.delcyon.capo.xml.cdom.CElement;
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

	@Override
	public void init(ResourceDeclarationElement declaringResourceElement,VariableContainer variableContainer, LifeCycle lifeCycle,boolean iterate, ResourceParameter... resourceParameters) throws Exception
	{

		super.init(declaringResourceElement, variableContainer, lifeCycle, iterate,resourceParameters);
		System.out.println("login");
		
		if(Thread.currentThread() instanceof ContextThread)
		{
		    this.session = ((ContextThread)Thread.currentThread()).getSession();
		}
		else if(WApplication.getInstance() != null)
		{
		    this.session = ((CapoWebApplication)WApplication.getInstance()).getJcrSession();
		}

		if(this.session == null)
		{
		    throw new Exception("Can't use JCR resources without a Session");
		}
		
		this.absPath = getResourceURI().getPath();
		//dump(session.getRootNode());		
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
//		    session.getRootNode().addNode(getResourceURI().getResourceURIString().replaceFirst("^repo:/", ""));
		}
		
		this.node = session.getNode(absPath);
	}
	
	
	@Override
	public StreamType[] getSupportedStreamTypes() throws Exception
	{
		return  EnumSet.of(StreamType.INPUT, StreamType.OUTPUT).toArray(new StreamType[]{});
	}

	@Override
	public StreamFormat[] getSupportedStreamFormats(StreamType streamType) throws Exception
	{
		return EnumSet.of(StreamFormat.XML_BLOCK).toArray(new StreamFormat[]{});
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
        else
        {
            setResourceState(State.OPEN);
            return false;
        }
	}

	@Override
	public ContentMetaData getContentMetaData(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{		
	    return new JcrContentMetaData(getResourceURI(), node);
	}

	@Override
	public ContentMetaData getOutputMetaData(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{
	    return new JcrContentMetaData(getResourceURI(), node);
	}

	@Override
	protected void clearContent() throws Exception
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	protected ContentMetaData buildResourceMetaData(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{
		
		return new JcrContentMetaData(getResourceURI(), node);
	}

//	@Override
//	public OutputStream getOutputStream(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
//	{
//		//MimeTable mt = MimeTable.getDefaultTable();
//	    //String mimeType = mt.getContentTypeFor(file.getName());
//	    //if (mimeType == null) mimeType = "application/octet-stream";
//
//	    Node fileNode = node.addNode("<name>", "nt:file");
//
//	    System.out.println( fileNode.getName() );
//
//	    Node resNode = fileNode.addNode("jcr:content", "nt:resource");
//	    resNode.setProperty("jcr:mimeType", "<mimeType>");
//	    resNode.setProperty("jcr:encoding", "");
//	    resNode.setProperty("jcr:data",new BinaryValue( new FileInputStream("")));
//	    Calendar lastModified = Calendar.getInstance();
//	    //lastModified.setTimeInMillis(file.lastModified());
//	    resNode.setProperty("jcr:lastModified", lastModified);
//	    //Uti
//	    return new ByteArrayOutputStream();
//	}
	
//	@Override
//	public InputStream getInputStream(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
//	{
//		Node jcrContent = node.getNode("jcr:content");
//		String fileName = node.getName();
//		return jcrContent.getProperty("jcr:data").getBinary().getStream();
//	}
	
	@Override
	public void writeXML(VariableContainer variableContainer, CElement element, ResourceParameter... resourceParameters) throws Exception
	{
	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    XPath.dumpNode(element, outputStream);
	    session.importXML(getResourceURI().getResourceURIString().replaceFirst("^repo:", ""), new ByteArrayInputStream(outputStream.toByteArray()), 0);
	    dump(session.getRootNode());
	    session.save();
	    String[] langs = session.getWorkspace().getQueryManager().getSupportedQueryLanguages();
	    String[] prefixes = session.getNamespacePrefixes();
	    
	    Query query = session.getWorkspace().getQueryManager().createQuery("SELECT * FROM [nt:unstructured] where NAME([nt:unstructured]) = 'server:log' order by message", "JCR-SQL2");
	    QueryResult result = query.execute();


	    // Iterate over the nodes in the results ...

	    NodeIterator nodeIter = result.getNodes();
	    System.out.println("=============================");
	    while ( nodeIter.hasNext() ) {

	        Node _node = nodeIter.nextNode();
	        System.out.println("===>"+_node.getName()+" type:"+_node.getPrimaryNodeType().getName());
	        dump(_node);

	    }
	    System.out.println("=============================");
	    session.logout();
	}
	
	
	@Override
	public boolean performAction(VariableContainer variableContainer,Action action,ResourceParameter... resourceParameters) throws Exception
	{
	    super.addResourceParameters(variableContainer, resourceParameters);
	    
        
        boolean success = false;

        if (action == Action.CREATE)
        {
           System.out.println(node.isNew());
           //node.setPrimaryType("nt:folder");
           
           
        }
        else if (action == Action.DELETE)
        {
            if (node != null)
            {
                node.remove();
           
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

 
}
