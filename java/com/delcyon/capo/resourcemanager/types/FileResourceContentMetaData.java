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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.Arrays;
import java.util.logging.Level;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.datastream.stream_attribute_filter.MD5FilterInputStream;
import com.delcyon.capo.datastream.stream_attribute_filter.MD5FilterOutputStream;
import com.delcyon.capo.datastream.stream_attribute_filter.SizeFilterInputStream;
import com.delcyon.capo.resourcemanager.ResourceParameter;
import com.delcyon.capo.resourcemanager.ResourceParameterBuilder;
import com.delcyon.capo.resourcemanager.ResourceURI;
import com.delcyon.capo.util.CloneControl;
import com.delcyon.capo.util.CloneControl.Clone;

/**
 * @author jeremiah
 *
 */
@CloneControl(filter=Clone.exclude, modifiers=Modifier.TRANSIENT)
public class FileResourceContentMetaData extends AbstractContentMetaData
{
    
    private String uri = null; 
    private int currentDepth = 0; 
    private ResourceParameter[] resourceParameters = new ResourceParameter[0];
    private transient File file;
    
    
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
//	    System.err.println("new "+uri);
//        Thread.dumpStack();
	    this.uri = uri;
	    this.resourceParameters = resourceParameters;	    
		init(uri,0,resourceParameters);
	}
	
	public FileResourceContentMetaData(String uri, int currentDepth, ResourceParameter... resourceParameters) throws Exception
	{
//	    System.err.println("new "+uri);
//        Thread.dumpStack();
	    this.uri = uri;
        this.resourceParameters = resourceParameters;
		init(uri,currentDepth,resourceParameters);
	}
	
	@SuppressWarnings("rawtypes")
    @Override
	public Enum[] getAdditionalSupportedAttributes()
	{
		return new Enum[]{Attributes.exists,Attributes.executable,Attributes.readable,Attributes.writeable,Attributes.container,Attributes.lastModified,Attributes.MD5,FileAttributes.absolutePath,FileAttributes.canonicalPath,FileAttributes.symlink};
	}

	
	
	public void refresh(ResourceParameter... resourceParameters) throws Exception
	{	    
	    clearAttributes();
	    setInitialized(false);
	    if (resourceParameters != null)
	    {
	        init(uri,currentDepth,resourceParameters);    
	    }
	    else
	    {
	        init(uri,currentDepth,this.resourceParameters);
	    }
	}
	
	//just initialize anything about ourselves, BUT NOTHING ABOUT OUR CHILDREN
	private void init(String uri,int currentDepth, ResourceParameter... resourceParameters) throws Exception
	{
	    if(uri.contains("libfile:"))
	    {
	        System.out.println("oops");
	    }
		if (getBoolean(Parameters.USE_RELATIVE_PATHS,false,resourceParameters))
		{
			if (getString(Attributes.path,null,resourceParameters) == null)
			{
				ResourceParameterBuilder resourceParameterBuilder = new ResourceParameterBuilder();
				resourceParameterBuilder.addAll(resourceParameters);
				resourceParameterBuilder.addParameter(Attributes.path, uri.toString());
				this.resourceParameters = resourceParameterBuilder.getParameters();
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
		
		file = new File(new URI(uri));
		if(getResourceURI() == null)
		{
			setResourceURI(new ResourceURI(file.toURI().toString()));
		}
		
	}
	
	@Override
	public void init() throws RuntimeException
	{
	    if(isInitialized() == false)
	    {
	        try
	        {
	            load();
	        }
	        catch (Exception e)
	        {
	        	e.printStackTrace();
	            throw new RuntimeException(e);
	        }
	    }

	}

	protected void load() throws Exception
    {
	    if(file == null)
	    {
	        refresh();
	    }
	    setValue(Attributes.exists, file.exists());

        setValue(Attributes.executable, file.canExecute());

        setValue(Attributes.readable, file.canRead());

        setValue(Attributes.writeable, file.canWrite());

        setValue(Attributes.container, file.isDirectory());

        setValue(Attributes.lastModified, file.lastModified());
        
        setValue(SizeFilterInputStream.ATTRIBUTE_NAME, file.length());
        
        setValue(FileAttributes.absolutePath, file.getAbsolutePath());
        
        setValue(FileAttributes.canonicalPath, file.getCanonicalPath());
        
        boolean isSymlink = isSymlink(file);
        
        setValue(FileAttributes.symlink, isSymlink+"");
    
		if (file.exists() == true && file.canRead() == true && file.isDirectory() == false)
		{		    
		    FileInputStream fileInputStream = new FileInputStream(file);
			readInputStream(fileInputStream,false);
			fileInputStream.close();
		}
		else if (file.isDirectory() == true && getIntValue(ContentMetaData.Parameters.DEPTH,1,resourceParameters) > currentDepth)
		{				
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
			
			
			MD5FilterOutputStream md5FilterOutputStream = new MD5FilterOutputStream(new ByteArrayOutputStream());
			if(file != null && file.exists())
            {
                md5FilterOutputStream.write(file.getName());
                md5FilterOutputStream.write(file.length()+"");
                md5FilterOutputStream.write(file.lastModified()+"");
            }
			else
			{
			    md5FilterOutputStream.write("");
			}
			Arrays.sort(fileList);
			//childContentMetaDataLinkedList.clear();
			int currentPreviousChildIndex = 0;
			for (int currentChildURIIndex =0; currentChildURIIndex <  fileList.length; currentChildURIIndex++)
			{
				String childURI = fileList[currentChildURIIndex];
				File childFile = new File(file,childURI);				
				String tempChildURI = childFile.toURI().toString();
				if (tempChildURI.endsWith(File.separator))
	            {
				    tempChildURI = tempChildURI.substring(0, tempChildURI.length()-File.separator.length());
	            }
				FileResourceContentMetaData contentMetaData = new FileResourceContentMetaData(tempChildURI, currentDepth+1,resourceParameters);
				if(contentMetaData.file != null && contentMetaData.file.exists())
				{
				    md5FilterOutputStream.write(contentMetaData.file.getName());
				    md5FilterOutputStream.write(contentMetaData.file.length()+"");
				    md5FilterOutputStream.write(contentMetaData.file.lastModified()+"");
				}
				boolean addedChild = false;
				for( ; currentPreviousChildIndex < childContentMetaDataLinkedList.size(); currentPreviousChildIndex++)
				{	
				    File currentPreviousChildFile = ((FileResourceContentMetaData)childContentMetaDataLinkedList.get(currentPreviousChildIndex)).file;
				    int compare = currentPreviousChildFile.getName().compareTo(childFile.getName()); 
                    if(compare > 0) //previous after newChild
                    {
                        if((currentPreviousChildIndex +1) == childContentMetaDataLinkedList.size())
                        {
                            addContainedResource(contentMetaData);
                            addedChild = true;
                        }
                        else
                        {
                            childContentMetaDataLinkedList.add(currentPreviousChildIndex, contentMetaData);
                            addedChild = true;
                        }
                        //currentPreviousChildIndex--;
                        break;
                    }
                    else if (compare == 0) //previous before newChild
                    {
                        addedChild = true;
                        if(currentPreviousChildFile.lastModified() != childFile.lastModified())
                        {
                        	System.out.println("XXXXXXXXXXXXXXXX");
                        }
                        else if(currentPreviousChildFile.length() != childFile.length())
                        {
                        	System.out.println("XXXXXXXXXXXXXXXX");
                        }
                        break;
                    }                    
                }
				if(addedChild == false)
				{
				    addContainedResource(contentMetaData);
				}
				
				while(childFile.getName().equals(((FileResourceContentMetaData)childContentMetaDataLinkedList.get(currentChildURIIndex)).file.getName()) == false)
				{
					childContentMetaDataLinkedList.remove(currentChildURIIndex);
				}
			}
			
			//This may not be needed as it should effectively does again, what the removale of any non matches against the entire list  
//			for(int currentNewFileIndex =0 ; currentNewFileIndex < fileList.length; currentNewFileIndex++)
//			{
//			    while(new File(file,fileList[currentNewFileIndex]).getName().equals(((FileResourceContentMetaData)childContentMetaDataLinkedList.get(currentNewFileIndex)).file.getName()) == false)
//			    {
//			        childContentMetaDataLinkedList.remove(currentNewFileIndex);
//			    }
//			        
//			}
			
			//this truncates any files that were not in the new list, but are sorted to the end of the list
			while(childContentMetaDataLinkedList.size() != fileList.length)
            {               
                childContentMetaDataLinkedList.removeLast();
            }
			
			System.out.println("MD5 "+file.getCanonicalPath()+" "+md5FilterOutputStream.getMD5());
			setValue(MD5FilterInputStream.ATTRIBUTE_NAME, md5FilterOutputStream.getMD5());
			md5FilterOutputStream.close();
			
		}		
		
		setInitialized(true);
	}
	
	

	@Override
	public Boolean exists()
	{
		return Boolean.parseBoolean(getValue(Attributes.exists));
	}

	@Override
	public Long getLastModified()
	{		
		return Long.parseLong(getValue(Attributes.lastModified));
	}

	@Override
	public Boolean isContainer()
	{
		return Boolean.parseBoolean(getValue(Attributes.container));
	}

	@Override
	public Boolean isReadable()
	{
		return Boolean.parseBoolean(getValue(Attributes.readable));
	}

	@Override
	public Boolean isWriteable()
	{
		return Boolean.parseBoolean(getValue(Attributes.writeable));
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
