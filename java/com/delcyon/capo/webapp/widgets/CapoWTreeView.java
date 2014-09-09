package com.delcyon.capo.webapp.widgets;

import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.types.ContentMetaData;

import eu.webtoolkit.jwt.WColor;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WModelIndex;
import eu.webtoolkit.jwt.WTreeView;

public class CapoWTreeView extends WTreeView
{
	@Override
	public void setLayoutSizeAware(boolean aware)
	{		
		super.setLayoutSizeAware(aware);
	}
	@Override
	protected void layoutSizeChanged(int width, int height)
	{		
		//resize(width, height);
		//setWidth(new WLength(width));
		setColumnWidth(0, new WLength(width-24));
		setColumnBorder(new WColor(255,0,0));
	}
	
	@Override
	public boolean isSelected(WModelIndex index)
	{
	    return super.isSelected(index);
//	    System.out.println(((ResourceDescriptor) index.getInternalPointer()).getResourceURI()+" check");
//	    if(getSelectionModel().getSelectedIndexes().size() > 0)
//	    {
//	        
//	        WModelIndex selectedModelIndex = getSelectionModel().getSelectedIndexes().first();
//	        System.out.println(((ResourceDescriptor) selectedModelIndex.getInternalPointer()).getResourceURI()+" int");
//	        return ((ResourceDescriptor)selectedModelIndex.getInternalPointer()).getResourceURI().toString().equals(((ResourceDescriptor)index.getInternalPointer()).getResourceURI().toString());
//	    }
//	    else
//	    {
//	        return false;
//	    }
	}
	
	
}
