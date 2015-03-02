package com.delcyon.capo.webapp.models;

import java.util.List;

import com.delcyon.capo.resourcemanager.types.ContentMetaData;

import eu.webtoolkit.jwt.ItemDataRole;
import eu.webtoolkit.jwt.Orientation;
import eu.webtoolkit.jwt.WAbstractItemModel;
import eu.webtoolkit.jwt.WModelIndex;

public class WContentMetaDataItemModel extends WAbstractItemModel
{

	private List<ContentMetaData> contentMetaDataList;
	private String[] columns;

	public WContentMetaDataItemModel(List<ContentMetaData> contentMetaDataList, String...columns)
	{
		this.contentMetaDataList = contentMetaDataList;
		this.columns = columns;
	}
	
	@Override
	public int getColumnCount(WModelIndex parent)
	{
		
		if(columns == null || columns.length == 0)
		{
			if(contentMetaDataList.isEmpty())
			{
				return 0;
			}
			columns = contentMetaDataList.get(0).getSupportedAttributes().toArray(new String[]{}); 			
		}
		return columns.length;
	}

	@Override
	public int getRowCount(WModelIndex parent)
	{		
		return contentMetaDataList.size();
	}

	@Override
	public WModelIndex getParent(WModelIndex index)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getHeaderData(int section, Orientation orientation, int role)
	{
		if(role == ItemDataRole.DisplayRole)
		{
			return getColumnName(section);
		}
		else
		{
			return super.getHeaderData(section, orientation, role);
		}
	}
	
	private String getColumnValue(int column)
	{
		String[] splitColumn = columns[column].split(",");
		return splitColumn[0];
	}
	
	private String getColumnName(int column)
	{
		String[] splitColumn = columns[column].split(",");
		if(splitColumn.length > 1)
		{
			return splitColumn[1];
		}
		else
		{
			return splitColumn[0];
		}
	}
	
	@Override
	public Object getData(WModelIndex index, int role)
	{
		if (role == ItemDataRole.DisplayRole)
	    {
			return contentMetaDataList.get(index.getRow()).getValue(getColumnValue(index.getColumn()));
			
	    }
		return null;
	}

	@Override
	public WModelIndex getIndex(int row, int column, WModelIndex parent)
	{
		if(contentMetaDataList == null || row >= contentMetaDataList.size())
		{
			return null;
		}
		return createIndex(row, column, contentMetaDataList.get(row));
	}

}
