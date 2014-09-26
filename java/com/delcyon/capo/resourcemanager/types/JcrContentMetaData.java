package com.delcyon.capo.resourcemanager.types;

import java.util.List;
import java.util.Vector;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

import com.delcyon.capo.datastream.stream_attribute_filter.MD5FilterInputStream;
import com.delcyon.capo.datastream.stream_attribute_filter.SizeFilterInputStream;
import com.delcyon.capo.resourcemanager.ContentFormatType;
import com.delcyon.capo.resourcemanager.ResourceParameter;
import com.delcyon.capo.resourcemanager.ResourceURI;

public class JcrContentMetaData implements ContentMetaData
{

    public static final String CAPO_METADATA_PREFIX = "";
    
    private Node node = null;
    private ResourceURI resourceURI = null;
    
    private JcrContentMetaData()
    {
        //Serialization
    }
    
    public JcrContentMetaData(ResourceURI resourceURI, Node node)
    {
        this.node = node;
        try
        {
            JcrResourceDescriptor.dump(node);
        }
        catch (RepositoryException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        this.resourceURI = resourceURI;
    }
    
    private JcrContentMetaData(Node node) throws ValueFormatException, PathNotFoundException, RepositoryException
    {
        System.out.println(node.getName()+"==>"+node.getPath());
        this.node = node;
        if(node.hasProperty(CAPO_METADATA_PREFIX+"resourceURI"))
        {
            this.resourceURI = new ResourceURI(node.getProperty(CAPO_METADATA_PREFIX+"resourceURI").getString());
        }
        else
        {
            this.resourceURI = new ResourceURI("repo:"+node.getPath());
        }
    }
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
       return Boolean.parseBoolean(getValue("exists"));
    }

    @Override
    public Boolean isReadable()
    {
        return Boolean.parseBoolean(getValue("readable"));
    }

    @Override
    public Long getLength()
    {
        return Long.parseLong(getValue(SizeFilterInputStream.ATTRIBUTE_NAME));
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
        return true;//Boolean.parseBoolean(getValue("container"));
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
            node.setProperty(name, value);
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
            if(node.hasProperty(CAPO_METADATA_PREFIX+name) == false)
            {
                return null;
            }
            Property property = node.getProperty(CAPO_METADATA_PREFIX+name);
            if(property.isMultiple() == false)
            {
                return property.getString().substring(CAPO_METADATA_PREFIX.length());
            }
            else
            {
                return "//TODO-- IS MUTLTIPLE";
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
            PropertyIterator propertyIterator = node.getProperties();
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
        NodeIterator nodeIterator;
        try
        {
            nodeIterator = node.getNodes();
        
        while(nodeIterator.hasNext())
        {
            Node childNode = nodeIterator.nextNode();            
            childNodeList.add(new JcrContentMetaData(childNode));
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

}
