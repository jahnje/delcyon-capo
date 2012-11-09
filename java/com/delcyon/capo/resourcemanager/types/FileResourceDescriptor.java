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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.logging.Level;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.Configuration.PREFERENCE;
import com.delcyon.capo.controller.VariableContainer;
import com.delcyon.capo.datastream.NullOutputStream;
import com.delcyon.capo.datastream.StreamUtil;
import com.delcyon.capo.resourcemanager.ResourceParameter;
import com.delcyon.capo.resourcemanager.ResourceType;
import com.delcyon.capo.resourcemanager.ResourceURI;
import com.delcyon.capo.resourcemanager.types.ContentMetaData.Attributes;
import com.delcyon.capo.resourcemanager.types.FileResourceType.Parameters;
import com.delcyon.capo.util.XMLSerializer;
import com.delcyon.capo.xml.cdom.CElement;
import com.delcyon.capo.xml.dom.ResourceDeclarationElement;

/**
 * @author jeremiah
 */
public class FileResourceDescriptor extends AbstractResourceDescriptor
{
	
	private FileResourceContentMetaData contentMetaData = null;
	private FileResourceContentMetaData actionMetaData = null;
	private FileResourceContentMetaData outputMetaData = null;
	private InputStream contentInputStream = null;
	private CElement containerElement = null;
	
	@Override
	protected FileResourceContentMetaData buildResourceMetaData(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{		
		FileResourceContentMetaData contentMetaData = new FileResourceContentMetaData(getResourceURI().getBaseURI(),resourceParameters);		
		return contentMetaData;
	}
	
	@Override
	public void setup(ResourceType resourceType, String resourceURI) throws Exception
	{
		
		super.setup(resourceType, resourceURI);
	}
	
	@Override
	public void init(ResourceDeclarationElement declaringResourceElement,VariableContainer variableContainer, LifeCycle lifeCycle,boolean iterate, ResourceParameter... resourceParameters) throws Exception
	{
		
		super.init(declaringResourceElement,variableContainer, lifeCycle, iterate, resourceParameters);
		//see if we have requested a parent directory
		File parentDir = null;

		
		String parentDirParameter = getVarValue(variableContainer, Parameters.PARENT_PROVIDED_DIRECTORY);	
		String rootDirParameter = getVarValue(variableContainer, Parameters.ROOT_DIR);

		if (parentDirParameter != null && parentDirParameter.isEmpty() == false)
		{
		    //this must be converted to a URI before processing or file treats it as a string and append things wierdly. 
			parentDir = new File(new URI(CapoApplication.getDataManager().getResourceDirectory(parentDirParameter).getResourceURI().getBaseURI()));			
		}
		else if(rootDirParameter != null && rootDirParameter.isEmpty() == false)
		{
		    parentDir = new File(rootDirParameter.replaceFirst("file:(//){0,1}", ""));		    
		}
		//Set default dir to capo dir on file requests, if one wasn't specified, and we have everything running
		else if(CapoApplication.getDataManager() != null && CapoApplication.getDataManager().getResourceDirectory(PREFERENCE.CAPO_DIR.toString()) != null)
		{			
			parentDir = new File(new URI(CapoApplication.getDataManager().getResourceDirectory(PREFERENCE.CAPO_DIR.toString()).getResourceURI().getBaseURI()));
		}
				
		//this lets us use custom relative URIs for files.
		// file:filename and file:/filename file://filename
		
		if (getResourceURI().isOpaque() || getResourceURI().getAuthority() != null || getResourceURI().getScheme() == null)
		{
		    String uri = null;
			if (parentDir == null)
			{
			    uri = new File(getResourceURI().getBaseURI().replaceFirst("file:(//){0,1}", "")).toURI().toString();
			}
			else
			{
			    //if the parent dir is already included in the URI, don't use it.
			    //and we're not specifically adding it with the parentDir parameter.
			    //The ROOT_DIR parameter will cascade down to child resource, so it has to be stripped out.
			    uri = new File(parentDir,getResourceURI().getBaseURI().replaceFirst("file:(//){0,1}", "")).toURI().toString();
			    if (uri.toString().startsWith(parentDir.toURI().toString()) && rootDirParameter != null && rootDirParameter.trim().isEmpty() == false)
			    {
			        uri = new File(getResourceURI().getBaseURI().replaceFirst("file:(//){0,1}", "")).toURI().toString();
			    }
			}
			if (uri.endsWith(File.separator))
			{
			    uri = uri.substring(0, uri.length()-File.separator.length());
			}
			setResourceURI(new ResourceURI(uri));
		}
		else
		{
		   CapoApplication.logger.log(Level.FINE, "Not rewriting URI: "+getResourceURI().getBaseURI());
		}
	}
	
	@Override
	public ContentMetaData getContentMetaData(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{
		if (getResourceState() != State.OPEN && getResourceState() != State.STEPPING)
		{
			open(variableContainer,resourceParameters);
		}
		return contentMetaData;
	}
	
	
	@Override
	protected void clearContent() throws Exception
	{
	    contentMetaData = null;
	    if(contentInputStream != null)
	    {
	        contentInputStream.close();
	    }
	    containerElement = null;
	}
	
	@Override
	public boolean next(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{
	    advanceState(State.OPEN, variableContainer, resourceParameters);
	    if(getResourceState() == State.OPEN)
	    {
	        setResourceState(State.STEPPING);
	        if(getResourceMetaData(variableContainer, resourceParameters).isContainer())
	        {
	            contentMetaData = new FileResourceContentMetaData(getResourceURI().getBaseURI());
	            containerElement = (CElement) XMLSerializer.export(CapoApplication.getDocumentBuilder().newDocument(), getResourceMetaData(variableContainer, resourceParameters));
	        }
	        else
	        {
	            contentMetaData = new FileResourceContentMetaData(getResourceURI().getBaseURI());
	            if(contentMetaData.exists())
	            {
	                contentInputStream = trackInputStream(contentMetaData.wrapInputStream(new FileInputStream(new File(new URI(getResourceURI().getBaseURI())))));
	                StreamUtil.readInputStreamIntoOutputStream(contentInputStream, new NullOutputStream());
	                contentInputStream = trackInputStream(contentMetaData.wrapInputStream(new FileInputStream(new File(new URI(getResourceURI().getBaseURI())))));
	            }
	            
	        }
	        return true;
	    }
	    else if (getResourceState() == State.STEPPING)
        {
            setResourceState(State.OPEN);
            return false;
        }
	    return false;
	}
	
	//This is over written only to handle the special case of a directory, everything else actually goes through getInputStream() 
	@Override
	public CElement readXML(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
	{	    
	    if(getResourceMetaData(variableContainer, resourceParameters).isContainer())
	    {
	        advanceState(State.STEPPING, variableContainer, resourceParameters);	        
	        return containerElement;
	    }
	    else
	    {	        
	        return super.readXML(variableContainer, resourceParameters);
	    }
	}
	

	@Override
	public InputStream getInputStream(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{		    
	    advanceState(State.STEPPING, variableContainer, resourceParameters);	    
		return contentInputStream;
	}

	@Override
	public OutputStream getOutputStream(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{
	    advanceState(State.OPEN, variableContainer, resourceParameters);
		outputMetaData = new FileResourceContentMetaData(getResourceURI().getBaseURI());
		File outputFile = new File(new URI(getResourceURI().getBaseURI()));
		if (outputFile.exists() == false)
		{
		    new File(outputFile.getParent()).mkdirs();
		    outputFile.createNewFile();
		}
		
		return trackOutputStream(outputMetaData.wrapOutputStream(new FileOutputStream(outputFile)));	
	}
	
	

    @Override
	public void close(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{		
		super.close(variableContainer,resourceParameters);
		if (contentMetaData != null)
		{
			contentMetaData.refresh(getResourceURI().getBaseURI());
		}
		if(contentInputStream != null)
		{
		    try
		    {
		        contentInputStream.close();
		    }
		    catch (IOException ioException)
		    {
		        CapoApplication.logger.log(Level.WARNING, "Error closing stream", ioException);
		    }
		}
	}
	
	
	
	public void create() throws Exception
	{
		
		File file = new File(new URI(getResourceURI().getBaseURI()));
		if (file.exists() == false)
		{
			String path = file.getCanonicalPath();
			path = path.substring(0, path.lastIndexOf(File.separator+file.getName()));
			File dirs = new File(path);
			if (dirs.exists() == false)
			{
				CapoApplication.logger.log(Level.INFO, "Creating Directory: "+dirs.getCanonicalPath());
				if (dirs.mkdirs() == false)
				{
					throw new Exception("Couldn't create: "+dirs.getCanonicalPath());
				}
			}
			file.createNewFile();
		}
	}

	@Override
	public StreamFormat[] getSupportedStreamFormats(StreamType streamType)
	{
		if (streamType == StreamType.INPUT)
		{
			return new StreamFormat[]{StreamFormat.STREAM};
		}
		else if(streamType == StreamType.OUTPUT)
		{
			return new StreamFormat[]{StreamFormat.STREAM};
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
		return new Action[]{Action.CREATE,Action.DELETE};
	}
	
	@Override
	public boolean performAction(VariableContainer variableContainer,Action action,ResourceParameter... resourceParameters) throws Exception
	{
	    super.addResourceParameters(variableContainer, resourceParameters);
	    URI uri = new URI(getResourceURI().getBaseURI());
	    if (uri.isAbsolute() == false)
	    {
	        CapoApplication.logger.log(Level.WARNING, "URI isn't absolute! "+uri);
	    }
	    File file = new File(uri);
	    boolean success = false;

	    if (action == Action.CREATE)
	    {
	        if (file.exists() == false)
	        {
	            String containerFlag = getVarValue(variableContainer, DefaultParameters.CONTAINER); 
	            if (containerFlag != null && containerFlag.equalsIgnoreCase("true"))
	            {
	                CapoApplication.logger.log(Level.INFO, "Creating Directory: "+file.getCanonicalPath());
	                success = file.mkdirs();
	            }
	            else
	            {
	                String path = file.getCanonicalPath();
	                path = path.substring(0, path.lastIndexOf(File.separator+file.getName()));
	                File dirs = new File(path);
	                if (dirs.exists() == false)
	                {
	                    CapoApplication.logger.log(Level.INFO, "Creating Directory: "+dirs.getCanonicalPath());
	                    dirs.mkdirs();
	                }
	                success = file.createNewFile();
	            }
	        }		
	    }
	    else if (action == Action.DELETE)
        {
            if (file.exists())
            {
                success = delete(file);
            }
            else
            {
                success = true;
            }
        }
	    
	    else if (action == Action.SET_ATTRIBUTE && resourceParameters.length > 0)
        {
	    	Attributes attribute = Attributes.valueOf(resourceParameters[0].getName());
            if (file.exists())
            {
                switch (attribute)
				{
					case lastModified:
						success = file.setLastModified(Long.parseLong(resourceParameters[0].getValue()));
						break;
					case executable:
						success = file.setExecutable(Boolean.parseBoolean(resourceParameters[0].getValue()));
						break;
					case readable:
						success = file.setReadable(Boolean.parseBoolean(resourceParameters[0].getValue()));
						break;
					case writeable:
						success = file.setWritable(Boolean.parseBoolean(resourceParameters[0].getValue()));
						break;					
					default:
						success = false;
						break;
				}
            }
            else
            {
                success = false;
            }
        }
	    
		if (success == true)
		{
			refreshResourceMetaData(variableContainer,resourceParameters);
		}
		return success;
	}
	
	
    private boolean delete(File file) throws Exception
	{
	    
	    if (file.exists())
	    {
	        if(file.isDirectory())
	        {
	            File[] children = file.listFiles();
	            for (File child : children)
                {	                
                    delete(child);
                }
	        }
	        
	        return file.delete();
	    }
	    else
	    {
	        return true;
	    }
	}
}
