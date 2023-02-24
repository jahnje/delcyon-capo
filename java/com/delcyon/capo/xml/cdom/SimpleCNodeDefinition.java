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

    public void setType(String type)
    {
        this.type = type;
    }

    public void setPattern(String pattern)
    {
        this.pattern = pattern;
    }

    public void setMaxLength(int maxLength)
    {
        this.maxLength = maxLength;
    }

    public void setSchemaVersion(String schemaVersion)
    {
        this.schemaVersion = schemaVersion;
        
    }

    public void setEntityType(String entityType)
    {
        this.entityType = entityType;
        
    }

    public void setDescription(String description)
    {
       this.description = description;
        
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public void setAdditionalProperties(boolean additionalProperties)
    {
        this.additionalProperties = additionalProperties;
        
    }

    
}
