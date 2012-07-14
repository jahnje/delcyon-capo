/**
Copyright (c) 2012 Delcyon, Inc.
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
package com.delcyon.capo.tasks;

import java.io.OutputStream;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.CapoApplication.ApplicationState;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.server.CapoServer;
import com.delcyon.capo.tasks.TaskManagerThread.Preferences;

/**
 * @author jeremiah
 *
 */
public class TaskManagerDocumentUpdaterThread extends Thread
{
    private ConcurrentLinkedQueue<DocumentUpdate> documentUpdateQueue = new ConcurrentLinkedQueue<DocumentUpdate>();
	private volatile ApplicationState state = ApplicationState.NONE;	
	private Transformer transformer;
	private ReentrantLock lock;
	private boolean runAsService;
    
	public TaskManagerDocumentUpdaterThread(ReentrantLock lock, boolean runAsService) throws Exception
	{
		super("TaskDocumentUpdater"+" - "+CapoApplication.getApplication().getApplicationDirectoryName().toUpperCase());
		this.lock = lock;
		this.runAsService = runAsService;
		Document identityDocument = CapoApplication.getDefaultDocument("identity_transform.xsl");
		TransformerFactory tFactory = TransformerFactory.newInstance();		
		transformer = tFactory.newTransformer(new DOMSource(identityDocument));		
		transformer.setOutputProperty(OutputKeys.INDENT, "no");	
	}
	
	@Override
	public void interrupt()
	{
	    CapoServer.logger.log(Level.INFO, "Interrupting TaskDocumentUpdater Thread");	  
	    synchronized (this)
        {
	    	//only set us to stopping if we're running or something earlier, this can happen when we don't run the client as a service
			if (this.state.order < ApplicationState.STOPPING.order)
			{
				this.state = ApplicationState.STOPPING;
			}
            super.interrupt();
        }
	    
	    
	}

	@Override
	public void run()
	{
		this.state = ApplicationState.RUNNING;
		while(getUpdaterState().order < ApplicationState.STOPPING.order || documentUpdateQueue.isEmpty() == false)
		{
			try
			{
			   
				while (documentUpdateQueue.isEmpty() == false)
				{
				    lock.lock();

				    DocumentUpdate documentUpdate = documentUpdateQueue.poll();
				    if (documentUpdate == null)
				    {
				        continue;
				    }
				    Document taskManagerDocument = documentUpdate.getDocument();
				    ResourceDescriptor taskManagerDocumentFileDescriptor = documentUpdate.getDocumentResourceDescriptor();
				    CapoServer.logger.log(Level.FINE, "updating task file");
				    taskManagerDocument.normalizeDocument();
				    taskManagerDocument.normalize();
				    taskManagerDocumentFileDescriptor.open(null);

				    OutputStream taskDocumentOutputStream = taskManagerDocumentFileDescriptor.getOutputStream(null);

				    transformer.setOutputProperty("method", "xml");
				    transformer.setOutputProperty("indent", "yes");
				    transformer.transform(new DOMSource(taskManagerDocument), new StreamResult(taskDocumentOutputStream));

				    taskDocumentOutputStream.close();
				    taskManagerDocumentFileDescriptor.release(null);

				    lock.unlock();
				}
				
				if (runAsService == true && getUpdaterState().order < ApplicationState.STOPPING.order)
				{
				    try
				    {
				        Thread.sleep(CapoApplication.getConfiguration().getLongValue(Preferences.TASK_INTERVAL)/2l);
				    } 
				    catch (InterruptedException interruptedException)
				    {
				        continue;
				    }					    
				}
				else
				{
					break;
				}
			} catch (Exception e)
			{				
				e.printStackTrace();
			}
		}
		this.state = ApplicationState.STOPPED;
	}
    public void add(ResourceDescriptor resourceDescriptor, Document taskDocument)
    {
        documentUpdateQueue.add(new DocumentUpdate(resourceDescriptor, taskDocument));
        
    }    

	public ApplicationState getUpdaterState()
	{
		return this.state;
	}

	//DONE
	private class DocumentUpdate
	{
	    private Document document;
        private ResourceDescriptor documentResourceDescriptor;
        public DocumentUpdate(ResourceDescriptor documentResourceDescriptor, Document document)
        {
            this.documentResourceDescriptor = documentResourceDescriptor;
            this.document = document;
        }
        
        public Document getDocument()
        {
            return document;
        }
        
        public ResourceDescriptor getDocumentResourceDescriptor()
        {
            return documentResourceDescriptor;
        }
	}
    
}
