package com.delcyon.capo.resourcemanager.types;

import java.util.List;
import java.util.Vector;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import com.delcyon.capo.datastream.stream_attribute_filter.MD5FilterInputStream;
import com.delcyon.capo.datastream.stream_attribute_filter.SizeFilterInputStream;
import com.delcyon.capo.resourcemanager.ContentFormatType;
import com.delcyon.capo.resourcemanager.ResourceParameter;
import com.delcyon.capo.resourcemanager.ResourceURI;
import com.delcyon.capo.resourcemanager.types.FileResourceContentMetaData.FileAttributes;
import com.delcyon.capo.server.jackrabbit.CapoJcrServer;

public class JcrContentMetaData implements ContentMetaData
{

    public static final String CAPO_METADATA_PREFIX = "";
    
    private ResourceURI resourceURI = null;
    private ResourceParameter[] resourceParameters = new ResourceParameter[0];
    
    private JcrContentMetaData()
    {
        //Serialization
    }

//    @Override
//    public void preClone(Object parentClonedObject, Object clonedObject) throws Exception {} //don't need to do anything
//    
//    @Override
//    public void postClone(Object parentClonedObject, Object clonedObject) throws Exception
//    {
//        ((JcrContentMetaData)clonedObject).node = this.node;        
//    }
    
    public JcrContentMetaData(ResourceURI resourceURI,ResourceParameter...resourceParameters)
    {
        this.resourceParameters = resourceParameters;
        this.resourceURI = resourceURI;
    }
    
//    private JcrContentMetaData(Node node) throws ValueFormatException, PathNotFoundException, RepositoryException
//    {
//       // System.out.println(node.getName()+"==>"+node.getPath());
//        //this.node = node;
//        if(node.hasProperty(CAPO_METADATA_PREFIX+"resourceURI"))
//        {
//            this.resourceURI = new ResourceURI(node.getProperty(CAPO_METADATA_PREFIX+"resourceURI").getString());
//        }
//        else
//        {
//            this.resourceURI = new ResourceURI("repo:"+node.getPath());
//        }
//    }
    
//    protected void setNode(Node node)
//    {
//    	this.node = node;    	
//    }
    
    @Override
    public boolean isDynamic()
    {
        
        return false;
    }

    @Override
    public boolean areDynamicAttributeLoaded()
    {
        return true;
    }

    @Override
    public void refresh(ResourceParameter... resourceParameters) throws Exception
    {
        

    }

    @Override
    public boolean isInitialized()
    {
        
        return true;
    }

    @Override
    public void setInitialized(boolean isInitialized)
    {
        

    }

    @Override
    public ContentFormatType getContentFormatType()
    {
        try
        {
         
            return ContentFormatType.valueOf(getValue(ContentFormatType.ATTRIBUTE_NAME));
        }
        catch (Exception exception)
        {
            //exception.printStackTrace();
            return ContentFormatType.NO_CONTENT;
        }
    }

    @Override
    public void setContentFormatType(ContentFormatType contentFormatType)
    {
        
        setValue(ContentFormatType.ATTRIBUTE_NAME, contentFormatType.toString());
    }

    @Override
    public Boolean exists()
    {
    	if(getNode() == null)
    	{
    		return false;
    	}
    	else
    	{
    		return true;
    	}
    }

    @Override
    public Boolean isReadable()
    {
        return Boolean.parseBoolean(getValue("readable"));
    }

    @Override
    public Long getLength()
    {
        if(getValue(SizeFilterInputStream.ATTRIBUTE_NAME) != null)
        {
            return Long.parseLong(getValue(SizeFilterInputStream.ATTRIBUTE_NAME));
        }
        else
        {
            return 0L;
        }
    }

    @Override
    public Boolean isWriteable()
    {
        return Boolean.parseBoolean(getValue("writeable"));
    }

    @Override
    public String getMD5()
    {
        return getValue(MD5FilterInputStream.ATTRIBUTE_NAME);
    }

    @Override
    public Boolean isContainer()
    {
        if(getNode() == null)
        {
            return true;
        }
        
        //check for root node
        try
        {
            if(getNode().getDepth() == 0)
            {
                return true;
            }                        
        }
        catch (RepositoryException e)
        {            
            e.printStackTrace();
        }
        
        return Boolean.parseBoolean(getValue("container"));
    }

    @Override
    public void clearAttributes()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public Long getLastModified()
    {
        return Long.parseLong(getValue("lastmodified"));
    }

    @Override
    public void setValue(String name, String value)
    {
        try
        {
            getNode().setProperty(name, value);
        }
        catch (RepositoryException e)
        {         
            e.printStackTrace();
        }

    }

    @Override
    public String getValue(String name)
    {
        try
        {
            if(getNode() == null)
            {
                return null;
            }
            if(getNode().hasProperty(CAPO_METADATA_PREFIX+name) == false)
            {
                return null;
            }
            Property property = getNode().getProperty(CAPO_METADATA_PREFIX+name);
            if(property.isMultiple() == false)
            {
                return property.getString().substring(CAPO_METADATA_PREFIX.length());
            }
            else
            {
            	Value[] values = property.getValues();
            	StringBuilder builder = new StringBuilder("{");
            	for (Value value : values)
				{
					builder.append(value.getString()+",");
				}
            	builder.setCharAt(builder.length()-1, '}');
                return builder.toString();
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
            return null;
        }
    }

    @Override
    public String getValue(Enum name)
    {
       return getValue(name.toString());
    }

    @Override
    public boolean isSupported(String attributeName)
    {        
        return true;
    }

    @Override
    public boolean hasAttribute(String attributeName)
    {
       return (getValue(attributeName) != null);
    }

    @Override
    public List<String> getSupportedAttributes()
    {
        List<String> properties = new Vector<String>();        
        try
        {
            if(getNode() == null)
            {
                //default to file attributes
                for (Enum attributeEnum : new Enum[]{Attributes.exists,Attributes.executable,Attributes.readable,Attributes.writeable,Attributes.container,Attributes.lastModified,Attributes.MD5,FileAttributes.absolutePath,FileAttributes.canonicalPath,FileAttributes.symlink,FileAttributes.regular})
                {
                    properties.add(attributeEnum.toString());
                }
                return properties;
            }
            PropertyIterator propertyIterator = getNode().getProperties();
            //JcrResourceDescriptor.dump(getNode());
            while(propertyIterator.hasNext())
            {
                Property property = propertyIterator.nextProperty();
                properties.add(property.getName());
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return properties;
    }

    @Override
    public List<ContentMetaData> getContainedResources()
    {
        List<ContentMetaData> childNodeList = new Vector<ContentMetaData>();

        if(getNode() == null)
        {
            return childNodeList;
        }
        
        NodeIterator nodeIterator;
        try
        {
            nodeIterator = getNode().getNodes();
        
        while(nodeIterator.hasNext())
        {
            Node childNode = nodeIterator.nextNode();            
            childNodeList.add(new JcrContentMetaData(new ResourceURI("repo:"+childNode.getPath())));
        }
        }
        catch (RepositoryException e)
        {
            e.printStackTrace();
        }
        return childNodeList;
    }

   

    @Override
    public void addContainedResource(ContentMetaData contentMetaData)
    {
        //node.addNode()
    }

    @Override
    public ResourceURI getResourceURI()
    {
        return this.resourceURI;
    }

    @Override
    public void init()
    {
        

    }

    protected Node getNode()
    {
        try
        {
            if(CapoJcrServer.getSession().nodeExists(resourceURI.getPath()) == true)
            {
                return CapoJcrServer.getSession().getNode(resourceURI.getPath());
            }
        } catch (Exception exception)
        {
            exception.printStackTrace();
        }
        return null;
        
    }
    @Override
    public ResourceParameter[] getResourceParameters()
    {
        return resourceParameters;
    }
}
