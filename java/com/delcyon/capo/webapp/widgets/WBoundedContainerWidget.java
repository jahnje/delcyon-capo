package com.delcyon.capo.webapp.widgets;

import java.util.EnumSet;

import eu.webtoolkit.jwt.AlignmentFlag;
import eu.webtoolkit.jwt.Signal1;
import eu.webtoolkit.jwt.WBoxLayout;
import eu.webtoolkit.jwt.WBoxLayout.Direction;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.WToolBar;
import eu.webtoolkit.jwt.WWidget;

public class WBoundedContainerWidget extends WContainerWidget
{

    
    private WText title = new WText();
    private WBoxLayout layout = new WBoxLayout(Direction.TopToBottom);    
    private WToolBar toolBar = new WToolBar();
    
    public WBoundedContainerWidget()
    {
        
        setLayout(layout);
        setInline(false);        
        layout.setSpacing(1);
        layout.addWidget(toolBar, 0);
        layout.addWidget(title,0);
        
        addStyleClass("bounded_container");
        //setLayoutSizeAware(true);
        title.setTextAlignment(AlignmentFlag.AlignCenter);
        title.setInline(false);
        title.addStyleClass("h2");
        
    }
    
    /**
     * Adds a widget to the layout.
     * <p>
     * Calls {@link #addWidget(WWidget widget, int stretch, EnumSet alignment)
     * addWidget(widget, 0, EnumSet.noneOf(AlignmentFlag.class))}
     */
    public final void addLayoutWidget(WWidget widget) {
        layout.addWidget(widget);
    }

    /**
     * Adds a widget to the layout.
     * <p>
     * Calls {@link #addWidget(WWidget widget, int stretch, EnumSet alignment)
     * addWidget(widget, stretch, EnumSet.noneOf(AlignmentFlag.class))}
     */
    public final void addLayoutWidget(WWidget widget, int stretch) {
        layout.addWidget(widget, stretch);
    }
    
    public void setTitle(String title)
    {
        this.title.setText(title);
    }
    
    public void addToolButton(String buttonName, String permission, Signal1.Listener<WMouseEvent> clickListener) throws Exception
    {
        WPushButton pushButton = new WPushButton(buttonName);
        if(permission != null)
        {
            WXmlNavigationBar navigationBar = WXmlNavigationBar.getNavBar();
            
            if(navigationBar != null)
            {
                pushButton.setEnabled(navigationBar.hasPermission(permission));
                
                //listen for permission changes for our button 
                navigationBar.permissionChanged().addListener(this, (perm,bool)->{
                    if(perm.equals(permission))
                    {
                        pushButton.setEnabled(bool);                    
                    }
                });
                
                //check permissions first before passing on event
                pushButton.clicked().addListener(this,(mouseEvent)->{
                    if(navigationBar.hasPermission(permission))
                    {
                        clickListener.trigger(mouseEvent);
                    }
                });
            }
        }
        else
        {
            pushButton.clicked().addListener(this,clickListener);
        }
        toolBar.addButton(pushButton);
    }
    
}
