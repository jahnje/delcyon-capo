package com.delcyon.capo.resourcemanager.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.version.Version;
import javax.jcr.version.VersionManager;

import com.delcyon.capo.resourcemanager.ResourceParameter;
import com.delcyon.capo.resourcemanager.ResourceURI;
import com.delcyon.capo.server.jackrabbit.CapoJcrServer;

/**
 * This is basically a compound jcr content meta data class. The Root should always be the Version Node, The child will always be the the frozenNode 
 * @author jeremiah
 *
 */
public class JcrVersionContentMetaData extends JcrContentMetaData
{

	public enum Attributes
	{
		versionName,
		versionTimeStamp,
		versionLabel,
		versionNote,
		isBaseVersion
	}
	
	private JcrContentMetaData frozenNodeContentMetaData = null; 
	
	public JcrVersionContentMetaData(ResourceURI resourceURI,ResourceParameter... resourceParameters) throws Exception
	{
		super(resourceURI, resourceParameters);
		Version version = (Version) getNode();		
		frozenNodeContentMetaData = new JcrContentMetaData(new ResourceURI("repo:"+version.getFrozenNode().getPath()));
	}

	@Override
	public List<String> getSupportedAttributes()
	{
		
		List<String> supportedAtrributes = super.getSupportedAttributes();
		EnumSet.allOf(Attributes.class).forEach(attr -> supportedAtrributes.add(attr.toString()));		
		supportedAtrributes.addAll(frozenNodeContentMetaData.getSupportedAttributes());
		return supportedAtrributes;
	}
	
	@Override
	public String getValue(String name)
	{
		try
		{
			if(Attributes.versionLabel.toString().equals(name))
			{
				return ((Version) getNode()).getContainingHistory().getVersionLabels((Version) getNode()).toString(); //TODO turn this into a string array
			}
			else if(Attributes.versionName.toString().equals(name))
			{
				return ((Version) getNode()).getName();
			}
			else if(Attributes.versionTimeStamp.toString().equals(name))
			{
				
				return ((Version) getNode()).getCreated().getTime().toString();
			}
			else if(Attributes.isBaseVersion.toString().equals(name))
			{
				return ((Version) getNode()).isSame(CapoJcrServer.getSession().getWorkspace().getVersionManager().getBaseVersion(getResourceURI().getPath()))+"";
			}
//			else if(Attributes.versionNote.toString().equals(name))
//			{
//				return ((Version) getNode()).getFrozenNode().getProperty("versionNote").getString();
//			}
			String value = super.getValue(name);
			if(value != null)
			{
				return value;
			}
			return frozenNodeContentMetaData.getValue(name);
				
		} catch (Exception exception)
		{
			exception.printStackTrace();
		}
		return super.getValue(name);
	}
	
	@Override
	protected Node getNode()
	{
		try
		{
			VersionManager versionManager = CapoJcrServer.getSession().getWorkspace().getVersionManager();
			return versionManager.getVersionHistory(getResourceURI().getPath()).getVersion(getResourceURI().getParameterMap().get("version"));
		} catch (Exception exception)
		{
			exception.printStackTrace();
		}
		return null;
	}
}
