package com.delcyon.capo.webapp.widgets;

import eu.webtoolkit.jwt.Signal2;
import eu.webtoolkit.jwt.WColor;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WModelIndex;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WTreeView;

/**
 * This is a very basic extension of the standard WTreeview so that we can pack all of our standard display elements in one place
 * but also and more importantly is so that the tree can automatically resize, and be layout aware. 
 * @author jeremiah
 *
 */
public class CapoWTreeView extends WTreeView
{
    private Signal2<WModelIndex, WMouseEvent> rightClicked_ = new Signal2<WModelIndex, WMouseEvent>(); 
    
    public CapoWTreeView()
    {
        //add local listener for right click event
        mouseWentUp().addListener(this, new Signal2.Listener<WModelIndex, WMouseEvent>() {

            @Override
            public void trigger(WModelIndex arg1, WMouseEvent arg2) {
                //make sure this is the right click event
                if (arg2.getButton().getValue() == 4)
                {
                    rightClicked_.trigger(arg1, arg2);
                }
            }});
    }
    
    /**
     * expose right click event, isRightClickAware() must be set to true.
     * @return
     */
    public Signal2<WModelIndex, WMouseEvent> rightClicked() {
        return this.rightClicked_;
    }
    
    private boolean isRightClickAware;

    /**
     * Tells the TreeView to capture the right click of a mouse and pass the right click event.
     * To support right-click, we need to disable the built-in browser
     * context menu.
     * 
     * Note that disabling the context menu and catching the right-click
     * does not work reliably on all browsers.
     * @param isRightClickAware
     * @return
     */
    public void setRightClickAware(boolean isRightClickAware)
    {
        /**
         * To support right-click, we need to disable the built-in browser
         * context menu.
         * 
         * Note that disabling the context menu and catching the right-click
         * does not work reliably on all browsers.
         */
        if(isRightClickAware == true)
        {
            setAttributeValue("oncontextmenu","event.cancelBubble = true; event.returnValue = false; return false;");
        }
        else
        {
            setAttributeValue("oncontextmenu",null);
        }
        this.isRightClickAware = isRightClickAware;
    }
    
    public boolean isRightClickAware()
    {
        return isRightClickAware;
    }
    
    /**
     * this is overridden in order to expose this method publicly
     */
	@Override
	public void setLayoutSizeAware(boolean aware)
	{		
		super.setLayoutSizeAware(aware);
	}
	
	/**
	 * This has to be overridden for it to work at all
	 */
	@Override
	protected void layoutSizeChanged(int width, int height)
	{				
		setColumnWidth(0, new WLength(width-24));
		setColumnBorder(new WColor(255,0,0));
	}
	
	
	
	
}
