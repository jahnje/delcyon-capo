package com.delcyon.capo.webapp.widgets;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import eu.webtoolkit.jwt.WMenu;
import eu.webtoolkit.jwt.WMenuItem;
import eu.webtoolkit.jwt.WNavigationBar;
import eu.webtoolkit.jwt.WPopupMenu;
import eu.webtoolkit.jwt.WPushButton;

/**
 * Uses a simple xml layout of menu->menu->menu(name= path=) to make nested menu that alter the systems internal path 
 * @author jeremiah
 *
 */
public class WXmlNavigationBar extends WNavigationBar
{
    public WXmlNavigationBar(Element menuRootElement)
    {
        super();
        NodeList menuList = menuRootElement.getChildNodes();
        for(int index = 0 ; index < menuList.getLength(); index++)
        {
            if(menuList.item(index) instanceof Element)
            {
                Element menu = (Element) menuList.item(index);
                WPushButton menuButton = new WPushButton(menu.getAttribute("name"));
                
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
                    popupMenu.setInternalPathEnabled();
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
                        subMenuItem.setPathComponent(menuElement.getAttribute("path"));    
                    }
                }
                else //otherwise we're a submenu with our own menu items and we need to be created differently
                {
                    WPopupMenu subMenu = new WPopupMenu();
                    parentMenu.addMenu(menuElement.getAttribute("name"),subMenu);
                    if(menuElement.hasAttribute("path"))
                    {
                        subMenu.setInternalPathEnabled(menuElement.getAttribute("path"));    
                    }
                    else
                    {
                        subMenu.setInternalPathEnabled();
                    }
                    
                    //recursively call our children since we have some
                    buildSubMenuItems(subMenu, menuElement);  
                }                                
            }
        }
    }
}
