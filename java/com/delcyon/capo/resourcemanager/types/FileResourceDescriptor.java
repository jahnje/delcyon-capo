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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.logging.Level;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.VariableContainer;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceManager;
import com.delcyon.capo.resourcemanager.ResourceParameter;
import com.delcyon.capo.resourcemanager.ResourceType;
import com.delcyon.capo.resourcemanager.types.FileResourceType.Parameters;

/**
 * @author jeremiah
 */
public class FileResourceDescriptor extends AbstractResourceDescriptor implements ResourceDescriptor
{

	private FileResourceContentMetaData contentMetaData = null;
	private FileResourceContentMetaData iterationContentMetaData = null;

	
	
	private FileResourceContentMetaData buildContentMetatData(ResourceParameter...resourceParameters) throws Exception
	{		
		FileResourceContentMetaData contentMetaData = new FileResourceContentMetaData(getResourceURI(),resourceParameters);		
		return contentMetaData;
	}
	
	@Override
	public void setup(ResourceType resourceType, String resourceURI) throws Exception
	{
		
		super.setup(resourceType, resourceURI);
	}
	
	@Override
	public void init(VariableContainer variableContainer,LifeCycle lifeCycle, boolean iterate,ResourceParameter... resourceParameters) throws Exception
	{
		
		super.init(variableContainer,lifeCycle, iterate, resourceParameters);
		//see if we have requested a parent directory
		File parentDir = null;

		
		String parentDirParameter = null;
		
		
		parentDirParameter = getVarValue(variableContainer, Parameters.PARENT_PROVIDED_DIRECTORY);	
		

		if (parentDirParameter != null && parentDirParameter.isEmpty() == false)
		{
		    //this must be converted to a URI before processing or file treats it as a string and append things wierdly. 
			parentDir = new File(new URI(CapoApplication.getDataManager().getResourceDirectory(parentDirParameter).getResourceURI()));			
		}
				
		//this lets us use custom relative URIs for files.
		//file:filename and file:/filename file://filename
		
		if (ResourceManager.isOpaque(getResourceURI()) || ResourceManager.getAuthroity(getResourceURI()) != null || ResourceManager.getScheme(getResourceURI()) == null)
		{
		    String uri = null;
			if (parentDir == null)
			{
			    uri = new File(getResourceURI().replaceFirst("file:(//){0,1}", "")).toURI().toString();
			}
			else
			{
				 uri = new File(parentDir,getResourceURI().replaceFirst("file:(//){0,1}", "")).toURI().toString();
			}
			if (uri.endsWith(File.separator))
			{
			    uri = uri.substring(0, uri.length()-File.separator.length());
			}
			setResourceURI(uri);
		}
		else
		{
		   CapoApplication.logger.log(Level.FINE, "Not rewriting URI: "+getResourceURI());
		}
	}
	
	

	@Override
	public void open(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{
		super.open(variableContainer,resourceParameters);
		
		if (contentMetaData == null)
		{			
			contentMetaData = buildContentMetatData(resourceParameters);			
		}		
	}

	
	@Override
	public ContentMetaData getContentMetaData(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{
		if (getResourceState() != State.OPEN)
		{
			open(variableContainer,resourceParameters);
		}
		//always refresh content meta data if this is a directory 
		if (contentMetaData != null && contentMetaData.isContainer() == true)
		{
		    this.contentMetaData = buildContentMetatData(resourceParameters);
		}
		return this.contentMetaData;
	}
	
	@Override
	public ContentMetaData getIterationMetaData(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{
		if (getResourceState() != State.OPEN && getResourceState() != State.STEPPING)
		{
			open(variableContainer,resourceParameters);
		}
		return iterationContentMetaData;
	}
	

	@Override
	public InputStream getInputStream(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{
		if (getResourceState() != State.OPEN)
		{
			open(variableContainer,resourceParameters);
		}
		
		iterationContentMetaData = new FileResourceContentMetaData(getResourceURI());
		
		return iterationContentMetaData.wrapInputStream(new FileInputStream(new File(new URI(getResourceURI()))));
	}

	@Override
	public OutputStream getOutputStream(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{
		if (getResourceState() != State.OPEN)
		{
			open(variableContainer,resourceParameters);
		}
		iterationContentMetaData = new FileResourceContentMetaData(getResourceURI());
		File outputFile = new File(new URI(getResourceURI()));
		if (outputFile.exists() == false)
		{
		    new File(outputFile.getParent()).mkdirs();
		    outputFile.createNewFile();
		}
		return contentMetaData.wrapOutputStream(new FileOutputStream(outputFile));		
	}
	
	@Override
	public void close(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
	{		
		super.close(variableContainer,resourceParameters);
		if (iterationContentMetaData != null)
		{
			iterationContentMetaData.refresh(getResourceURI());
		}
	}
	
	
	
	public void create() throws Exception
	{
		
		File file = new File(new URI(getResourceURI()));
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
	    URI uri = new URI(getResourceURI());
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
		if (success == true)
		{
			this.contentMetaData = buildContentMetatData(resourceParameters);
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
