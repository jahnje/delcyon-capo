package com.delcyon.capo.xml.cdom;

import java.util.HashMap;
import java.util.Vector;

public class SimpleCNodeDefinition extends CElement implements CNodeDefinition 
{

    
    
    private HashMap<String, String> enumMap;
    private String type;
    private String pattern;
    private Integer maxLength;
    private String schemaVersion;
    private String entityType;
    private String description;
    private String title;
    private String version;
    private HashMap<String, String> requiredMap;
    private Boolean additionalProperties;

    public SimpleCNodeDefinition(String localName)
    {
        super(localName);
    }

    @Override
    public boolean isValid(CNode node, Vector<CValidationException> exceptionVector) throws Exception
    {
        // TODO Auto-generated method stub
        return false;
    }

    
    @Override
    public SimpleCNodeDefinition addChild(String localName)
    {
        SimpleCNodeDefinition child = new SimpleCNodeDefinition(localName);
        appendChild(child);
        return child;
    }

    public void setEnumerationMap(HashMap<String, String> enumMap)
    {
        this.enumMap = enumMap;
    }
    
    public void setRequiredMap(HashMap<String, String> requiredMap)
    {
        this.requiredMap = requiredMap; 
    }
    
    @Override
    public String toString()
    {
        
        return getLocalName()+" e="+enumMap+" r="+requiredMap+" p="+pattern+" t="+type;
    }

    
    public String getType()
	{
		return type;
	}
    
    public void setType(String type)
    {
        this.type = type;
    }

    public String getPattern()
	{
		return pattern;
	}
    
    public void setPattern(String pattern)
    {
        this.pattern = pattern;
    }

    public Integer getMaxLength()
	{
		return maxLength;
	}
    
    public void setMaxLength(int maxLength)
    {
        this.maxLength = maxLength;
    }

    public String getSchemaVersion()
	{
		return schemaVersion;
	}
    
    public void setSchemaVersion(String schemaVersion)
    {
        this.schemaVersion = schemaVersion;
        
    }

    public String getEntityType()
	{
		return entityType;
	}
    
    public void setEntityType(String entityType)
    {
        this.entityType = entityType;
        
    }

    public String getDescription()
	{
		return description;
	}
    
    public void setDescription(String description)
    {
       this.description = description;
        
    }

    public String getTitle()
	{
		return title;
	}
    
    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getVersion()
	{
		return version;
	}
    
    public void setVersion(String version)
    {
        this.version = version;
    }

    public Boolean getAdditionalProperties()
	{
		return additionalProperties;
	}
    
    public void setAdditionalProperties(boolean additionalProperties)
    {
        this.additionalProperties = additionalProperties;
        
    }

    
    
    public boolean hasEnumerationMap()
    {
        return enumMap != null;
    }

    public HashMap<String,String> getEnumerationMap()
    {
        return enumMap;
        
    }

    public HashMap<String, String> getRequiredMap()
	{
		return requiredMap;
	}
}
