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
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.datastream.stream_attribute_filter.MD5FilterInputStream;
import com.delcyon.capo.resourcemanager.ResourceParameter;
import com.delcyon.capo.resourcemanager.ResourceParameterBuilder;
import com.delcyon.capo.resourcemanager.ResourceURI;

/**
 * @author jeremiah
 *
 */
public class FileResourceContentMetaData extends AbstractContentMetaData
{
	public enum FileAttributes
	{
        absolutePath, canonicalPath, symlink
	    
	}
    
	@SuppressWarnings("unused")
	private FileResourceContentMetaData() //serialization only
	{
		
	}
	
	public FileResourceContentMetaData(String uri, ResourceParameter... resourceParameters) throws Exception
	{
		init(uri,0,resourceParameters);
	}
	
	public FileResourceContentMetaData(String uri, int currentDepth, ResourceParameter... resourceParameters) throws Exception
	{
		init(uri,currentDepth,resourceParameters);
	}
	
	@SuppressWarnings("rawtypes")
    @Override
	public Enum[] getAdditionalSupportedAttributes()
	{
		return new Enum[]{Attributes.exists,Attributes.executable,Attributes.readable,Attributes.writeable,Attributes.container,Attributes.lastModified,Attributes.MD5,FileAttributes.absolutePath,FileAttributes.canonicalPath,FileAttributes.symlink};
	}

	
	
	public void refresh(String uri) throws Exception
	{
		init(uri,0);
	}
	
	private void init(String uri,int currentDepth, ResourceParameter... resourceParameters) throws Exception
	{
		if (getBoolean(Parameters.USE_RELATIVE_PATHS,false,resourceParameters))
		{
			if (getString(Attributes.path,null,resourceParameters) == null)
			{
				ResourceParameterBuilder resourceParameterBuilder = new ResourceParameterBuilder();
				resourceParameterBuilder.addAll(resourceParameters);
				resourceParameterBuilder.addParameter(Attributes.path, uri.toString());
				resourceParameters = resourceParameterBuilder.getParameters();
			}
			else
			{
				String uriString = uri.toString();
				uriString = uriString.replaceFirst(getString(Attributes.path,null,resourceParameters),"");
				setResourceURI(new ResourceURI(ResourceURI.removeURN(uriString)));
			}
		}
		else
		{
			setResourceURI(new ResourceURI(uri));
		}
		
		File file = new File(new URI(uri));
		if(getResourceURI() == null)
		{
			setResourceURI(new ResourceURI(file.toURI().toString()));
		}
		
		HashMap<String, String> attributeMap = getAttributeMap();
		
		attributeMap.put(Attributes.exists.toString(), file.exists()+"");

		attributeMap.put(Attributes.executable.toString(), file.canExecute()+"");

		attributeMap.put(Attributes.readable.toString(), file.canRead()+"");

		attributeMap.put(Attributes.writeable.toString(), file.canWrite()+"");

		attributeMap.put(Attributes.container.toString(), file.isDirectory()+"");

		attributeMap.put(Attributes.lastModified.toString(), file.lastModified()+"");
		
		attributeMap.put(Attributes.lastModified.toString(), file.lastModified()+"");
		
		attributeMap.put(FileAttributes.absolutePath.toString(), file.getAbsolutePath()+"");
		
		attributeMap.put(FileAttributes.canonicalPath.toString(), file.getCanonicalPath()+"");
		
		boolean isSymlink = isSymlink(file);
		
		attributeMap.put(FileAttributes.symlink.toString(), isSymlink+"");
		
		if (file.exists() == true && file.canRead() == true && file.isDirectory() == false)
		{
		    FileInputStream fileInputStream = new FileInputStream(file);
			readInputStream(fileInputStream,false);
			fileInputStream.close();
		}
		else if (file.isDirectory() == true && getIntValue(ContentMetaData.Parameters.DEPTH,1,resourceParameters) > currentDepth)
		{	
			BigInteger contentMD5 = new BigInteger(new byte[]{0});
			String[] fileList = file.list();
			
			//check for permissions, cause if we can't read, well get a null list back
			if(fileList == null && file.canRead() == false)
			{
			    CapoApplication.logger.log(Level.WARNING, "Can't read directory contents of "+file);
			    fileList = new String[]{};
			}
			
			if(isSymlink)
			{
			    CapoApplication.logger.log(Level.WARNING, "Symlink skipping directory contents of "+file);
                fileList = new String[]{};
			}
			
			Arrays.sort(fileList);
			for (String childURI : fileList)
			{
				File childFile = new File(file,childURI);				
				String tempChildURI = childFile.toURI().toString();
				if (tempChildURI.endsWith(File.separator))
	            {
				    tempChildURI = tempChildURI.substring(0, tempChildURI.length()-File.separator.length());
	            }
				FileResourceContentMetaData contentMetaData = new FileResourceContentMetaData(tempChildURI, currentDepth+1,resourceParameters);
				if (contentMetaData.getMD5() != null)
				{
					contentMD5 = contentMD5.add(new BigInteger(contentMetaData.getMD5(), 16));
				}
				addContainedResource(contentMetaData);
			}
			attributeMap.put(MD5FilterInputStream.ATTRIBUTE_NAME, contentMD5.abs().toString(16));
		}		
		setInitialized(true);
		
	}
	

	@Override
	public Boolean exists()
	{
		return Boolean.parseBoolean(getAttributeMap().get(Attributes.exists.toString()));
	}

	@Override
	public Long getLastModified()
	{		
		return Long.parseLong(getAttributeMap().get(Attributes.lastModified.toString()));
	}

	@Override
	public Boolean isContainer()
	{
		return Boolean.parseBoolean(getAttributeMap().get(Attributes.container.toString()));
	}

	@Override
	public Boolean isReadable()
	{
		return Boolean.parseBoolean(getAttributeMap().get(Attributes.readable.toString()));
	}

	@Override
	public Boolean isWriteable()
	{
		return Boolean.parseBoolean(getAttributeMap().get(Attributes.writeable.toString()));
	}

	


	private boolean isSymlink(File file) throws IOException 
	{
	    
	    File canonicalFile = null;
	    
	    if (file.getParent() == null)
	    {
	        canonicalFile = file;
	    } 
	    else
	    {
	        File canonicalParentDirectory = file.getParentFile().getCanonicalFile();
	        canonicalFile = new File(canonicalParentDirectory, file.getName());
	    }

	    if (canonicalFile.getCanonicalFile().equals(canonicalFile.getAbsoluteFile())) 
	    {
	        return false;
	    }
	    else 
	    {
	        return true;
	    }
	}


}
