package com.delcyon.capo.resourcemanager.types;

import java.util.List;

public interface Versionable
{
	public enum ResourceParameters
	{
		VERSION_UUID
	}
	
	public boolean isVersioned() throws Exception;
	public void checkin() throws Exception;
	public void checkout() throws Exception;
	public void restore(String versionUUID) throws Exception;
	public void remove(String versionUUID) throws Exception;
	/**
	 * 
	 * @param versionUUID
	 * @return String representation of a resourceURI for this version.
	 * @throws Exception
	 */
	public String getVersion(String versionUUID)  throws Exception; 
	public List<ContentMetaData> getVersionHistory() throws Exception;	
}
