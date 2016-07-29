package com.delcyon.capo.webapp.widgets;

import java.util.EnumSet;

import eu.webtoolkit.jwt.AlignmentFlag;
import eu.webtoolkit.jwt.AnchorTarget;
import eu.webtoolkit.jwt.Signal1;
import eu.webtoolkit.jwt.TextFormat;
import eu.webtoolkit.jwt.WAnchor;
import eu.webtoolkit.jwt.WBoxLayout;
import eu.webtoolkit.jwt.WBoxLayout.Direction;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WImage;
import eu.webtoolkit.jwt.WLayout;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WLink;
import eu.webtoolkit.jwt.WLink.Type;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WScrollArea;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.WToolBar;
import eu.webtoolkit.jwt.WWidget;

/**
 * This is a compound class that takes care of creating a bounded and titled container with a possible toolbar, upon which one or more components can be added. 
 * You should NOT change the layout of this class 
 * @author jeremiah
 *
 */
public class WBoundedContainerWidget extends WContainerWidget
{

    
    private WText title = new WText();
    private WLink helpLink = new WLink(Type.Url,"http:");
    private WAnchor helpAnchor = new WAnchor();
    private WBoxLayout internalLayout = new WBoxLayout(Direction.TopToBottom);
    private WContainerWidget titleLayout = new WContainerWidget();
    
    private WBoxLayout layout = new WBoxLayout(Direction.TopToBottom);
    private WToolBar toolBar = new WToolBar();
    private WContainerWidget internalContainer = new WContainerWidget();
    private WLength scrollWidth;
    private WLength scrollHeight;
    
    public WBoundedContainerWidget()
    {
        this(null);
        
    }
    
    public WBoundedContainerWidget(WContainerWidget parent)
    {
        super(parent);
        super.setLayout(layout);
        setInline(false);
        
        internalContainer.setLayout(internalLayout);        
        internalContainer.setInline(false);        
        internalContainer.addStyleClass("bounded_container_ic",false);
        
        layout.setSpacing(0);
        layout.addWidget(toolBar,0);
        layout.addWidget(titleLayout,0);
        helpAnchor.setLink(helpLink);
        helpAnchor.setImage(new WImage(new WLink("help_icon.png")));
        helpAnchor.setHidden(true);
        helpAnchor.setTarget(AnchorTarget.TargetNewWindow);
        helpAnchor.setMargin(8);
        titleLayout.addWidget(title);
        titleLayout.addWidget(helpAnchor);                
        layout.addWidget(internalContainer,1);
        
        super.addStyleClass("bounded_container",false);
        setLayoutSizeAware(true);
        titleLayout.setContentAlignment(AlignmentFlag.AlignCenter);
        title.setInline(true);
        titleLayout.addStyleClass("h2");
        title.setTextFormat(TextFormat.XHTMLText);
        
    }

    
    @Override
    public void setLayout(WLayout layout)
    {
        throw new UnsupportedOperationException("can't set layout on WBoundedContainer");
    }
    
    @Override
    public void addStyleClass(String styleClass, boolean force)
    {        
        internalContainer.addStyleClass(styleClass, force);
    }
    
    
    /**
     * Adds a widget to the layout.
     * <p>
     * Calls {@link #addWidget(WWidget widget, int stretch, EnumSet alignment)
     * addWidget(widget, 0, EnumSet.noneOf(AlignmentFlag.class))}
     */
    public final void addLayoutWidget(WWidget widget) {
        internalLayout.addWidget(widget,0);
    }

    /**
     * Set contents margins (in pixels).
     * <p>
     * The default contents margins are 9 pixels in all directions. 
     * </p>
     * 
     * @see WLayout#setContentsMargins(int left, int top, int right, int bottom)
     */
    public void setContentsMargins(int left, int top, int right, int bottom) 
    {
        internalLayout.setContentsMargins(left, top, right, bottom);
    }
    
    /**
     * Specifies how child widgets must be aligned within the container.    
     */
    public final void setInternalContentAlignment(AlignmentFlag alignmentFlag,AlignmentFlag... alignmentFlags)
    {
        internalContainer.setContentAlignment(alignmentFlag, alignmentFlags);
    }
    
    /**
     * Sets the height of the scroll area
     * @param wLength
     */
    public void setScrollHeight(WLength wLength)
    {
        this.scrollHeight = wLength;
        
        for(int currentChild = 0; currentChild < internalLayout.getCount(); currentChild++)
        {
            WWidget widget = internalLayout.getItemAt(currentChild).getWidget();
            if(widget != null && widget instanceof WScrollArea)
            {
                widget.setHeight(scrollHeight);
                widget.addStyleClass("scroll-post-height");
            }
            else if(widget != null && widget instanceof WContainerWidget)
            {
                widget.setHeight(scrollHeight);
                widget.addStyleClass("scroll-post-height");
            }
                
        }
    }
    
    /**
     * sets the width of the scroll area
     * @param wLength
     */
    public void setScrollWidth(WLength wLength)
    {
        this.scrollWidth = wLength;
        
        for(int currentChild = 0; currentChild < internalLayout.getCount(); currentChild++)
        {
            WWidget widget = internalLayout.getItemAt(currentChild).getWidget();
            if(widget != null && widget instanceof WScrollArea)
            {
                widget.setWidth(scrollWidth);
                widget.addStyleClass("scroll-post-width");
            }
            else if(widget != null && widget instanceof WContainerWidget)
            {
                widget.setWidth(scrollWidth);
                widget.addStyleClass("scroll-post-width");
            }
                
        }
    }
    
    /**
     * Adds a widget to the layout.
     * <p>
     * Calls {@link #addWidget(WWidget widget, int stretch, EnumSet alignment)
     * addWidget(widget, stretch, EnumSet.noneOf(AlignmentFlag.class))}
     */
    public final void addLayoutWidget(WWidget widget, int stretch) {
        if(stretch > 0)
        {
            //in a nutshell, for things to work correctly, we have to automatically add a scroll area or set the overflow on a widget whenever it get added.
            //as well as any sizes that might have been set. 
            if(widget instanceof WContainerWidget)
            {
                ((WContainerWidget) widget).setOverflow(Overflow.OverflowAuto);
                widget.addStyleClass("scrollarea-overflow");
                if(scrollWidth != null)
                {
                    widget.setWidth(scrollWidth);
                    widget.addStyleClass("scroll-width");
                }
                if(scrollHeight != null)
                {
                    widget.setHeight(scrollHeight);
                    widget.addStyleClass("scroll-height");
                }
            }
            else
            {
                WScrollArea scrollArea = new WScrollArea();
                scrollArea.setWidget(widget);
                scrollArea.addStyleClass("scrollarea");
                if(scrollWidth != null)
                {
                    scrollArea.setWidth(scrollWidth);
                    scrollArea.addStyleClass("scroll-width");
                }
                if(scrollHeight != null)
                {
                    scrollArea.setHeight(scrollHeight);
                    scrollArea.addStyleClass("scroll-height");
                }
                widget = scrollArea;
            }
            
        }
        internalLayout.addWidget(widget, stretch);
    }
    
    /**
     * This will set the title bar at the top of this widget
     * @param title
     */
    public void setTitle(String title)
    {
        this.title.setText(title);
    }
   
    /** can be internal or external url that points to help page. Null will hide image **/
    public void setHelpLinkRef(String url)
    {
        helpLink.setUrl(url);        
        if(url == null)
        {
            helpAnchor.setHidden(true);
        }
        else
        {
            helpAnchor.setHidden(false);
        }
    }
    
    /**
     * This will add a button to the toolbar that requires tha associated permission, and will call the associated click listener on clicked()
     * @param buttonName
     * @param permission
     * @param clickListener
     * @throws Exception
     */
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

    public WContainerWidget getInternalContainer()
    {
        return internalContainer;
    }

    
    
}
