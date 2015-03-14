package com.delcyon.capo.webapp.widgets;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import eu.webtoolkit.jwt.WNavigationBar;
import eu.webtoolkit.jwt.WPopupMenu;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WWidget;

/**
 * Uses a simple xml layout of menu->menu->menu(name= path= perm= class= method=) to make nested menu that alter the systems internal path. 
 * Paths that start with a / are considered root paths.
 * The method attribute will run a matching method from the WApplication instance that returns a WWidget.
 * The class attribute will instaniate a class that extends WWidget.
 * setLayoutItem is the method that causes all of the automatic stuff to happen. It should have a widget in that location before setting it, as that widget will be put back in place whenever some other widget can't be found or an error happens.
 * This class should always be loaded as soon as possible if it's going to be managing your internal paths, so that anything else that happens to be listening to path changes will be see the system AFTER this has swapped out widgets.     
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
        for(int index = 0 ; index < menuList.getLength(); index++)
        {
            if(menuList.item(index) instanceof Element)
            {
                Element menu = (Element) menuList.item(index);
                WPushButton menuButton = new WPushButton(menu.getAttribute("name"));
                enableMenuSignal.addListener(menuButton, ()-> menuButton.enable());
                disableMenuSignal.addListener(menuButton, ()-> menuButton.disable());
                if(menuRootElement.hasAttribute("styleClass"))
                {
                    menuButton.setStyleClass(menuRootElement.getAttribute("styleClass"));
                }
                
                //process path attributes
                WPopupMenu popupMenu = new WPopupMenu();
                if(menu.hasAttribute("path"))
                {
                    popupMenu.setInternalPathEnabled(menu.getAttribute("path"));    
                }
                else
                {
                    popupMenu.setInternalPathEnabled(menu.getAttribute("name").toLowerCase());
                }                
                menuButton.setMenu(popupMenu);
                
                //process submenus
                buildSubMenuItems(popupMenu,menu);
                
                //add button to nav menu
                addWidget(menuButton);                
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
     * initialize any menu permissions that are found while processing the xml
     * @param subMenuItem
     * @param menuElement
     */
    private void initPermissions(WMenuItem subMenuItem, Element menuElement)
    {
        if(menuElement.hasAttribute("perm"))
        {
            String perm = menuElement.getAttribute("perm");
            subMenuItem.setDisabled(true);
            List<String> pathList =  permissionPathHashMap.get(perm);
            if(pathList == null)
            {
                pathList = new ArrayList<>();
                permissionPathHashMap.put(perm, pathList);
            }
            pathList.add(getPath(subMenuItem));
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
    	if (this.layoutItem == null)
    	{
    		return;
    	}
        String iternalPath = WApplication.getInstance().getInternalPath();
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
        this.layoutItem  = gridLayout.getItemAt(index);
        
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
            pathInstanceMap.put(path, widget);
        }
        
        return widget;
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
     * interface to be used by anything that gets instantiated automatically and wants to process permission changes
     * @author jeremiah
     *
     */
    public interface PermissionListener
    {
        public void permissionChanged(String permission, Boolean bool);
    }

   
    
}
