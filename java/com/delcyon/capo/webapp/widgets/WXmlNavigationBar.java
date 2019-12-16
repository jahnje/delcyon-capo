package com.delcyon.capo.webapp.widgets;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import eu.webtoolkit.jwt.Signal;
import eu.webtoolkit.jwt.Signal2;
import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WGridLayout;
import eu.webtoolkit.jwt.WLayoutItem;
import eu.webtoolkit.jwt.WLink;
import eu.webtoolkit.jwt.WMenu;
import eu.webtoolkit.jwt.WMenuItem;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WNavigationBar;
import eu.webtoolkit.jwt.WPopupMenu;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WSplitButton;
import eu.webtoolkit.jwt.WWidget;

/**
 * Uses a simple xml layout of menu->menu->menu(name= path= perm= class= method=) to make nested menu that alter the systems internal path. 
 * Paths that start with a / are considered root paths.
 * permmissions can be a comma seperated list of strings.
 * The method attribute will run a matching method from the WApplication instance that returns a WWidget.
 * The class attribute will instaniate a class that extends WWidget.
 * setLayoutItem is the method that causes all of the automatic stuff to happen. It should have a widget in that location before setting it, as that widget will be put back in place whenever some other widget can't be found or an error happens.
 * This class should always be loaded as soon as possible if it's going to be managing your internal paths, so that anything else that happens to be listening to path changes will be see the system AFTER this has swapped out widgets.
 * This call will throw an internalPathInvalid signal if a path does not have enough permissions to be shown.     
 * @author jeremiah
 *
 */
public class WXmlNavigationBar extends WNavigationBar
{
    private Signal enableMenuSignal = new Signal();
    private Signal disableMenuSignal = new Signal();
    private Signal2<String, Boolean> permissionChangedSignal = new Signal2<>();
    @SuppressWarnings("rawtypes")
	private HashMap<String, Class> pathClassMap = new HashMap<>();
    private HashMap<String, WWidget> pathInstanceMap = new HashMap<>();
    private HashMap<String, Method> pathMethodMap = new HashMap<>();
    private WLayoutItem layoutItem = null;
    private WLayoutItem originalLayoutItem;
    private HashMap<String, Boolean> permissionsHashMap = new HashMap<>();
    private HashMap<String, List<String>> permissionPathHashMap = new HashMap<>();
    private HashMap<String, WMenuItem> pathMenuItemHashMap = new HashMap<>();
    private HashMap<String, Element> pathMenuElementHashMap = new HashMap<>();
    private Vector<MenuHolder> menuHolderVector = new Vector<>();
    private boolean ignoreNavBarClick = false;
    private boolean reloadWidgetOnMenuReselection = false;
    private boolean internalPathChanged = false;
    private String[] titleAttributes = null;
    private Element currentMenuElement = null;
    private boolean setPageTitle = false;
    private boolean setNavbarTitle = true;
    /**
     *  instantiate this with the root element of a menu/permission tree
     * @param menuRootElement
     */
    public WXmlNavigationBar(Element menuRootElement)
    {
        super();        
        //do this as soon as possible or all hell can break loose.  items visible checks will return true even though they are about to be swapped out for example.
        WApplication.getInstance().internalPathChanged().addListener(this, this::internalPathChanged);
        NodeList menuList = menuRootElement.getChildNodes();
        if("true".equalsIgnoreCase(menuRootElement.getAttribute("reloadWidgetOnMenuReselection")))
        {
            reloadWidgetOnMenuReselection = true;
        }
        if(menuRootElement.hasAttribute("titleAttributes"))
        {
            titleAttributes = menuRootElement.getAttribute("titleAttributes").split(",");
        }
        if(menuRootElement.hasAttribute("setPageTitle"))
        {
            setPageTitle = "true".equalsIgnoreCase(menuRootElement.getAttribute("setPageTitle"));
        }
        if(menuRootElement.hasAttribute("setNavbarTitle"))
        {
            setNavbarTitle = "true".equalsIgnoreCase(menuRootElement.getAttribute("setNavbarTitle"));
        }
        
        for(int index = 0 ; index < menuList.getLength(); index++)
        {
            if(menuList.item(index) instanceof Element)
            {
                Element menuElement = (Element) menuList.item(index);
                WPushButton _menuButton = null;
                WSplitButton wSplitButton = null;
                if(menuElement.hasAttribute("actionPath"))
                {
                     wSplitButton = new WSplitButton(menuElement.getAttribute("name"));
                    _menuButton = wSplitButton.getDropDownButton();
                    wSplitButton.getActionButton().clicked().addListener(this, ()->{
                        WApplication.getInstance().setInternalPath(menuElement.getAttribute("actionPath"), true);
                    });
                }
                else
                {
                    _menuButton = new WPushButton(menuElement.getAttribute("name"));
                }
                
                WPushButton menuButton = _menuButton;
                
               // WPushButton 
                enableMenuSignal.addListener(menuButton, ()-> menuButton.enable());
                disableMenuSignal.addListener(menuButton, ()-> menuButton.disable());
                if(menuRootElement.hasAttribute("styleClass"))
                {
                    
                    if(wSplitButton != null)
                    {
                        wSplitButton.getActionButton().setStyleClass(menuRootElement.getAttribute("styleClass"));                        
                    }
                    else
                    {
                        menuButton.setStyleClass(menuRootElement.getAttribute("styleClass"));    
                    }
                }
                
                if(menuElement.hasAttribute("styleClass"))
                {
                    
                    if(wSplitButton != null)
                    {
                        wSplitButton.getActionButton().addStyleClass(menuElement.getAttribute("styleClass"),true);
                    }
                    else
                    {
                        menuButton.addStyleClass(menuElement.getAttribute("styleClass"),true);    
                    }
                }
                
                //process path attributes
                WPopupMenu popupMenu = new WPopupMenu();
                if(menuElement.hasAttribute("path"))
                {
                    popupMenu.setInternalPathEnabled(menuElement.getAttribute("path"));    
                }
                else
                {
                    popupMenu.setInternalPathEnabled(menuElement.getAttribute("name").toLowerCase());
                }                
                menuButton.setMenu(popupMenu);
                
                //process submenus
                buildSubMenuItems(popupMenu,menuElement);
                //add button to nav menu
                if(wSplitButton != null)
                {
                    addWidget(wSplitButton);
                }
                else
                {
                    addWidget(menuButton);
                }
                //keep track of root menus so we can do decent mouse event processing
                menuHolderVector.add(new MenuHolder(popupMenu,menuButton));
            }
        }
        
        //walk list of root menus and close them if the click event wasn't heard by one of them
        //this listener must come after we've made all of the menu holders, or the events will fire in the wrong order 
        clicked().addListener(this, (e)->{
            if(ignoreNavBarClick == false)
            {
                menuHolderVector.forEach((menuHolder)->menuHolder.popupMenu.hide());
            }
            ignoreNavBarClick = false;
        });

    }
   
    /**
     * Used for mouse event type filtering, since it doesn't exist in WMouseEvent
     * @author jeremiah
     *
     */
    private enum MouseEventType
    {
        DOWN,
        CLICKED,
        UP,
        OVER
    }
    
    /**
     * used to associated a menu with it's button with it's visual state, and perform event filtering 
     * @author jeremiah
     *
     */
    private class MenuHolder
    {
        
        WPopupMenu popupMenu = null;
        WPushButton menuButton = null;
        boolean isHidden = true;
        public MenuHolder(WPopupMenu popupMenu, WPushButton menuButton)
        {
            this.popupMenu = popupMenu;
            this.menuButton = menuButton;
            //all of these are done here since mouse event doesn't have a source nor an event type
            menuButton.mouseWentDown().addListener(WXmlNavigationBar.this, (event)->processMouseEvent(event, MouseEventType.DOWN, popupMenu, menuButton));
            menuButton.clicked().addListener(WXmlNavigationBar.this, (event)->processMouseEvent(event, MouseEventType.CLICKED, popupMenu, menuButton));
            menuButton.mouseWentOver().addListener(WXmlNavigationBar.this, (event)->processMouseEvent(event, MouseEventType.OVER, popupMenu, menuButton));
        }
        
        
        
        private void processMouseEvent(WMouseEvent mouseEvent,MouseEventType eventType, WPopupMenu menu, WPushButton menuButton)
        {
            
            //use mouse down to track initial hidden state before clicked gets processed (by listeners that are queued up before us), and possible changes state.
            if(eventType == MouseEventType.DOWN)
            {
                isHidden = menu.isHidden();
                return;
            }
            //if we're processing a click, then the navbar shouldn't
            if(eventType == MouseEventType.CLICKED)
            {
                ignoreNavBarClick = true;
            }
            
            //run through all of the root menu items that aren't us, and see if we need to hide any of them, keep track if we do. 
            boolean askedSomeoneToHide = false;
            for (MenuHolder mh : menuHolderVector)
            {
                if(menu != mh.popupMenu)
                {
                    if(mh.popupMenu.isHidden() == false)
                    {
                        mh.popupMenu.hide();
                        //not needed, but left here for reference later
                        //mh.popupMenu.doJavaScript("$.data("+ mh.popupMenu.getJsRef()+").obj.setHidden(true);");                                                        
                        mh.menuButton.toggleStyleClass("active", false,true);
                        askedSomeoneToHide = true;
                    }                            
                }
            }
            
            //if we're not hidden, and we didn't ask anyone else to hide, then we got clicked to close, so do so
            if(isHidden == false && askedSomeoneToHide == false) 
            {                
                menu.hide();
                //not needed, but left here for reference later
                //menuButton.getMenu()("$.data("+ b.getJsRef()+").obj.setHidden(true);");
                //on force hides, toggle the style since it's only done when the js in the browsers hides it
                menuButton.toggleStyleClass("active", false,true);
            }
            
            
            //if we're a mouse over and we've asked someone else to hide, and we're hidden, then show ourselves
            if(eventType == MouseEventType.OVER && askedSomeoneToHide == true && isHidden == true)
            {                
                //use popup, show doesn't know where we should popup, so it won't work.
                menu.popup(menuButton);               
            } 
        }
    }
    
    /**
     * disable all of the menu made by this class
     */
    public void disableMenus()
    {
        disableMenuSignal.trigger();
    }
    
    /**
     * enable all of the menus made by this form
     */
    public void enableMenus()
    {
        enableMenuSignal.trigger();
    }
    
    /**
     * Will process all of the children of a menu element and create their respective menu items or recurse through their children and do the same 
     * @param parentMenu
     * @param parentMenuElement
     */
    private void buildSubMenuItems(WMenu parentMenu, Element parentMenuElement)
    {
        NodeList menuList = parentMenuElement.getChildNodes();
        for(int index = 0 ; index < menuList.getLength(); index++)
        {
            if(menuList.item(index) instanceof Element)
            {
                Element menuElement = (Element) menuList.item(index);
                //if we don't have anychild nodes, then we're just a leaf aka menuitem
                if(menuElement.hasChildNodes() == false)
                {
                    WMenuItem subMenuItem = parentMenu.addItem(menuElement.getAttribute("name"));
                    subMenuItem.triggered().addListener(this, this::menuItemTriggered);
                    subMenuItem.setInternalPathEnabled(true);               
                    if(menuElement.hasAttribute("path"))
                    {
                    	String path = menuElement.getAttribute("path");
                    	if(path.startsWith("/"))
                    	{
                    		subMenuItem.setInternalPathEnabled(false);               
                    		subMenuItem.setLink(new WLink(WLink.Type.InternalPath, path));
                    	}
                    	else
                    	{
                    		subMenuItem.setPathComponent(path);
                    	}
                        
                    }
                    else
                    {
                        subMenuItem.setPathComponent(menuElement.getAttribute("name").toLowerCase()); 
                    }
                    pathMenuItemHashMap.put(getPath(subMenuItem), subMenuItem);
                    pathMenuElementHashMap.put(getPath(subMenuItem),menuElement);
                    loadPathClass(getPath(subMenuItem),menuElement);
                    loadPathMethod(getPath(subMenuItem),menuElement);
                    initPermissions(subMenuItem,menuElement);
                }
                else //otherwise we're a submenu with our own menu items and we need to be created differently
                {
                    WPopupMenu subMenu = new WPopupMenu();
                    
                    parentMenu.addMenu(menuElement.getAttribute("name"),subMenu);
                    //make sure the this submenu is not considered a valid place to go. It should have no internal path, but apparently one get created when you use addMenu 
                    subMenu.getParentItem().setInternalPathEnabled(false);
                    if(menuElement.hasAttribute("path"))
                    {
                    	String path = menuElement.getAttribute("path");
                    	if(path.startsWith("/"))
                    	{
                    		subMenu.setInternalPathEnabled(path);
                    	}
                    	else
                    	{
                    		subMenu.setInternalPathEnabled(parentMenu.getInternalBasePath()+path);
                    	}
                    }
                    else
                    {
                        subMenu.setInternalPathEnabled(parentMenu.getInternalBasePath()+menuElement.getAttribute("name").toLowerCase());
                    }
                    
                    //recursively call our children since we have some
                    buildSubMenuItems(subMenu, menuElement);  
                }                                
            }
        }
    }
    
    /** 
     * Called to see if we need to reload the widget on menu item reselection.
     * It will be a case where the internal path hasn't "changed" and yet our menuitem is being triggered.
     * This only works because the IPC method always get called first, before our own added triggers on the menu item 
     * @param menuItem
     */
    private void menuItemTriggered(WMenuItem menuItem)
    {
        
        if(reloadWidgetOnMenuReselection == true && internalPathChanged == false)
        {            
            internalPathChanged();
        }
        internalPathChanged = false;        
    }
    
    /**
     * forces a widget reload even when the internal path hasn't changed
     * @param reloadWidgetOnMenuReselection
     */
    public void setReloadWidgetOnMenuReselection(boolean reloadWidgetOnMenuReselection)
    {
        this.reloadWidgetOnMenuReselection = reloadWidgetOnMenuReselection;
    }

    public boolean isReloadWidgetOnMenuReselection()
    {
        return reloadWidgetOnMenuReselection;
    }
    
    /**
     * initialize any menu permissions that are found while processing the xml
     * @param subMenuItem
     * @param menuElement
     */
    private void initPermissions(WMenuItem subMenuItem, Element menuElement)
    {
        if(menuElement.hasAttribute("perm"))
        {
            String[] perms = menuElement.getAttribute("perm").split(",");
            subMenuItem.setDisabled(true);
            for (String perm : perms)
            {
                List<String> pathList =  permissionPathHashMap.get(perm);
                if(pathList == null)
                {
                    pathList = new ArrayList<>();
                    permissionPathHashMap.put(perm, pathList);
                }
                pathList.add(getPath(subMenuItem));
            }                        
        }
    }


    /**
     * get the associated internal path for a menu item 
     * @param menuItem
     * @return
     */
    private String getPath(WMenuItem menuItem)
    {
        
        return menuItem.getLink().getInternalPath();
        //commented out until we know this works
//        String path = menuItem.getPathComponent();
//        WMenu parentMenu = menuItem.getParentMenu();
//        while(parentMenu != null)
//        {
//            path = parentMenu.getInternalBasePath()+path;
//            if(parentMenu.getParentItem() != null)
//            {
//            	if (parentMenu.getParentItem().getParentMenu() != null)
//            	{
//            		parentMenu = parentMenu.getParentItem().getParentMenu();
//            	}
//            	else
//            	{
//            		parentMenu = parentMenu.getParentItem().getMenu();
//            	}
//            }
//            else
//            {
//                parentMenu = null;
//            }
//        }
//        return path;
        
    }
    
    /**
     * load a class from the xml so we can instantiate it later. 
     * @param path
     * @param menuElement
     */
    private void loadPathClass(String path, Element menuElement)
    {
        if(menuElement.hasAttribute("class"))
        {
            try
            {
                pathClassMap.put(path, Class.forName(menuElement.getAttribute("class")));
                if(menuElement.hasAttribute("cache") && menuElement.getAttribute("cache").equalsIgnoreCase("false"))
                {
                    pathInstanceMap.put(path, null);
                }
            }
            catch (ClassNotFoundException e)
            {
                Logger.getGlobal().log(Level.SEVERE, "Couldn't load class for path "+path, e);
            }
        }
        
    }

    /**
     * load a WAplication Instance Method from the xml so we can use it to get a widget later. 
     * @param path
     * @param menuElement
     */
    private void loadPathMethod(String path, Element menuElement)
    {
        if(menuElement.hasAttribute("method"))
        {
            try
            {
            	String methodName = menuElement.getAttribute("method");
            	WApplication wApplication = WApplication.getInstance();
            	Method method = wApplication.getClass().getDeclaredMethod(methodName);
                pathMethodMap.put(path, method);
            }
            catch (Exception e)
            {
                Logger.getGlobal().log(Level.SEVERE, "Couldn't find method for path "+path, e);
            }
        }
        
    }
    

    /**
     * set a layout item from a gridLayout here, if you want this class to take care of instantiating anything/everything in the menu tree
     * @param layoutItem
     */
    public void setLayoutItem(WLayoutItem layoutItem)
    {
        if(this.layoutItem == null)
        {            
            this.originalLayoutItem = layoutItem;
        }
        this.layoutItem  = layoutItem;
        
    }
    
    
    
    /**
     * Process internal path changes. This basically will add any widget to the grid layout at the same position every time based on the internal path changes
     */
    private void internalPathChanged()
    {        
        internalPathChanged = true;
    	if (this.layoutItem == null)
    	{
    		return;
    	}
        String iternalPath = WApplication.getInstance().getInternalPath();
        
        //check permissions
        if(hasPermission(getPermissionsForPath(iternalPath)) == false)
        {
            WApplication.getInstance().setInternalPathValid(false);
            return;
        }
        
        WGridLayout gridLayout = (WGridLayout) layoutItem.getParentLayout();        
        
        int index = gridLayout.indexOf(layoutItem);
        int row = index / gridLayout.getColumnCount();
        int column = index % gridLayout.getColumnCount();
        gridLayout.removeItem(layoutItem);
        try
        {
        	if( pathMethodMap.containsKey(iternalPath))
        	{
        		gridLayout.addWidget(getInstanceMethodForPath(iternalPath), row, column);
        	}
            else if( pathClassMap.containsKey(iternalPath))
            {                
                gridLayout.addWidget(getInstanceForPath(iternalPath), row, column);                
            }
            else
            {             
                gridLayout.addItem(originalLayoutItem, row, column);
            }
        }
        catch (Exception e)
        {   
            gridLayout.addItem(originalLayoutItem, row, column);
            e.printStackTrace();            
        }
        if(pathMenuElementHashMap.containsKey(iternalPath))
        {
            this.currentMenuElement  = pathMenuElementHashMap.get(iternalPath);
            if(titleAttributes != null)
            {
                
                String attrName = Arrays.stream(titleAttributes).filter(atr->getCurrentMenuElement().hasAttribute(atr)).findFirst().orElse("name");
                if(setNavbarTitle)
                {
                    setTitle(getCurrentMenuElement().getAttribute(attrName));
                }
                if(setPageTitle)
                {
                    WApplication.getInstance().setTitle(getCurrentMenuElement().getAttribute(attrName));
                }
            }
        }
        
        this.layoutItem  = gridLayout.getItemAt(index);
        this.layoutItem.getWidget().refresh();
        
    }
    
    /**
     * This can be an empty array
     * @param titleAttributes
     */
    public void setTitleAttributes(String...titleAttributes)
    {
        this.titleAttributes = titleAttributes;
    }
    
    public String[] getTitleAttributes()
    {
        return titleAttributes;
    }
   
    /**
     * if true this will cause the page title to be set if there is a title attribute name set
     * the default is false
     * @param setPageTitle
     */
    public void setPageTitle(boolean setPageTitle)
    {
        this.setPageTitle = setPageTitle;
    }
    
    public boolean isSetPageTitle()
    {
        return setPageTitle;
    }
    
    /**
     * if true this will cause the navbar title to be set if there is a title attribute name set
     * the default is true
     * @param setPageTitle
     */
    public void setNavbarTitle(boolean setNavbarTitle)
    {
        this.setNavbarTitle = setNavbarTitle;
    }    
    
    public boolean isSetNavbarTitle()
    {
        return setNavbarTitle;
    }
    
    public Element getCurrentMenuElement()
    {
        return currentMenuElement;
    }
    
    /**
     * This is used to swap out the content widget, without modifying the internal path. Such as in stepping through a process. 
     * @param widget to be swapped in
     * @param refresh whether or not to call refresh on newly placed widget 
     * @return widget that was swapped out
     */
    public WWidget replaceContentWidget(WWidget widget, boolean refresh)
    {
        if (this.layoutItem == null)
        {
            return null;
        }
        WWidget originalWidget = this.layoutItem.getWidget();
        
        String iternalPath = WApplication.getInstance().getInternalPath();
        
        //check permissions
        if(hasPermission(getPermissionsForPath(iternalPath)) == false)
        {
            WApplication.getInstance().setInternalPathValid(false);
            return null;
        }
        
        WGridLayout gridLayout = (WGridLayout) layoutItem.getParentLayout();        
        
        int index = gridLayout.indexOf(layoutItem);
        int row = index / gridLayout.getColumnCount();
        int column = index % gridLayout.getColumnCount();
        gridLayout.removeItem(layoutItem);
        gridLayout.addWidget(widget, row, column);
       
        this.layoutItem  = gridLayout.getItemAt(index);
        this.layoutItem.getWidget().refresh();
        return originalWidget;
    }
    
    /**
     * Call the method in our WApplication that returns the widget we are looking for
     * @param path
     * @return
     * @throws Exception
     */
    private WWidget getInstanceMethodForPath(String path) throws Exception
	{
    	Method method = pathMethodMap.get(path);
    	boolean accessible = method.isAccessible();
    	method.setAccessible(true);    	
    	WWidget widget = (WWidget) method.invoke(WApplication.getInstance());
    	method.setAccessible(accessible);

    	return widget;
	}

	/**
     * gets a cached instance of the widget referred to in the menu path. Will also register it for permission changes if needed 
     * @param path
     * @return
     * @throws Exception
     */
    private WWidget getInstanceForPath(String path) throws Exception
    {
        WWidget widget = pathInstanceMap.get(path);
        if(widget == null)
        {
            widget = (WWidget) pathClassMap.get(path).newInstance();
            if(widget instanceof PermissionListener)
            {                
                permissionChanged().addListener(widget, ((PermissionListener)widget)::permissionChanged);
            }
            //if we already had a null value in here, then we want to keep it that way, as were not supposed to do any caching
            if(pathInstanceMap.containsKey(path) == false) 
            {
                pathInstanceMap.put(path, widget);
            }
        }
        
        return widget;
    }

    
    public static WXmlNavigationBar getNavBar() throws Exception
    {
        WApplication application = WApplication.getInstance();
        if(application != null)
        {
            Field[] fields = application.getClass().getDeclaredFields();
            for (Field field : fields)
            {
                if(field.getType().isAssignableFrom(WXmlNavigationBar.class))
                {
                    field.setAccessible(true);
                    return (WXmlNavigationBar) field.get(application);
                }
            }
            Method[] methods = application.getClass().getMethods();
            for (Method method : methods)
            {
                if(method.getReturnType() == WXmlNavigationBar.class)
                {                    
                    method.setAccessible(true);
                    if(method.getParameterCount() == 0)
                    {
                        return (WXmlNavigationBar) method.invoke(application, (Object[])null);
                    }
                }
            }
        }
        return null;
    }
    

/**
 * This will completely overwrite all existing permissions by setting them to false
 * @param permissionsHashMap
 */
    public void setPermissions(HashMap<String, Boolean> permissionsHashMap)
    {
        //clear all permissions, by setting them all to false
        this.permissionsHashMap.forEach((perm,path)->setPermission(perm, false));        
        if(permissionsHashMap != null)
        {
            permissionsHashMap.forEach(this::setPermission);
        }        
    }
    
    /**
     * sets a permission, and notifies all listeners if it changed 
     * @param perm
     * @param bool
     */
    public void setPermission(String perm, Boolean bool)
    {
        if(bool == null)
        {
            bool = false; //default to false for security
        }
        List<String> paths = permissionPathHashMap.get(perm);
        if(paths != null)
        {
            for (String path : paths)
            {
                WMenuItem menuItem = pathMenuItemHashMap.get(path);
                if(menuItem != null)
                {
                    menuItem.setDisabled(!bool); //reverse the meaning of the boolean since this is to setDisabled not Enabled        
                }
            }
        }
        storePermission(perm, bool);

    }
    
    /**
     * stores value in hashmap, and does actual notification of listeners
     * @param perm
     * @param bool
     */
    private void storePermission(String perm, Boolean bool)
    {
        if(permissionsHashMap.containsKey(perm))
        {
            if(bool.equals(permissionsHashMap.get(perm)) == false)
            {
                permissionsHashMap.put(perm, bool);
                permissionChangedSignal.trigger(perm, bool);
            }
        }
        else
        {
            permissionsHashMap.put(perm, bool);
            permissionChangedSignal.trigger(perm, bool);
        }
    }
    
    /**
     * This will cause a trigger whenever a permission is changed.
     * The First trigger argument is the name of the permission, the second argument is it's value. 
     * @return
     */
    public Signal2<String, Boolean> permissionChanged()
    {
        return permissionChangedSignal;
    }
    
    /**
     * 
     * @param perm
     * @return whether or not this permission is enabled or not
     */
    public boolean hasPermission(String perm)
    {        
        return permissionsHashMap.getOrDefault(perm, false);        
    }
    
    /**
     * check a list of permissions to see if all are met. 
     * @param permissions
     * @return (true on empty list, false if any single permission is not met, true if all permissions are met)
     */
    public boolean hasPermission(List<String> permissions)
    {
        boolean hasPermission = true;
        for (String perm : permissions)
        {
            if(hasPermission(perm) == false)
            {
                hasPermission = false;
                break;
            }
        }
        
        return hasPermission;
    }
    
    /** given a path, this will return a list of required permissions for that path
     * 
     * @param path
     * @return
     */
    private List<String> getPermissionsForPath(String path)
    {
       ArrayList<String> permissionsList = new ArrayList<>();
       permissionPathHashMap.forEach((perm,pathlist)->{
           pathlist.forEach((path_)->{
               if(path_.equals(path))
               {
                   permissionsList.add(perm);
               }
           });
       });
       return permissionsList;
    }
    
    @Override
    protected void finalize() throws Throwable
    {
        destroy();
        super.finalize();
    }
    
    /**
     * Just make sure we clear out an references that might be being used other places
     */
    public void destroy()
    {
        pathInstanceMap.clear();
        pathClassMap.clear();
        permissionsHashMap.clear();
        permissionPathHashMap.clear();
        pathMenuItemHashMap.clear();
    }
    
    /**
     * Clear out any cached values that might want to be different the next time this navmenu is used. For example by a different user or something. 
     */
    public void clearCache()
    {
        pathInstanceMap.clear();
    }
    
    /**
     * interface to be used by anything that gets instantiated automatically and wants to process permission changes
     * @author jeremiah
     *
     */
    public interface PermissionListener
    {
        public void permissionChanged(String permission, Boolean bool);
    }

   
    
}
