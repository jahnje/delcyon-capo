package com.delcyon.capo.webapp.widgets;

import eu.webtoolkit.jwt.WColor;
import eu.webtoolkit.jwt.WLength;
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
	
}
