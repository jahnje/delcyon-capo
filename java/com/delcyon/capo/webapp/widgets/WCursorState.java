/*
 * Created on Mar 30, 2015
 */
package com.delcyon.capo.webapp.widgets;

import eu.webtoolkit.jwt.WWidget;

/**
 * @author jeremiah
 * Sets the cursor state on a web widget via the setCursorStateOn(widget) method. 
 */
public enum WCursorState
{
    /**The cursor indicates an alias of something is to be created **/
    alias,
    /**The cursor indicates that something can be scrolled in any direction **/
    all_scroll,
    /**Default. The browser sets a cursor **/
    auto,
    /**The cursor indicates that a cell (or set of cells) may be selected **/
    cell,
    /**The cursor indicates that a context-menu is available **/
    context_menu,
    /**The cursor indicates that the column can be resized horizontally **/
    col_resize,
    /**The cursor indicates something is to be copied **/
    copy,
    /**The cursor render as a crosshair **/
    crosshair,
    /**The default cursor **/
    Default,
    /**The cursor indicates that an edge of a box is to be moved right (east) **/
    e_resize,
    /**Indicates a bidirectional resize cursor **/
    ew_resize,
    /**The cursor indicates that something can be grabbed **/
    grab,
    /**The cursor indicates that something can be grabbed **/
    grabbing,
    /**The cursor indicates that help is available **/
    help,
    /**The cursor indicates something is to be moved **/
    move,
    /**The cursor indicates that an edge of a box is to be moved up (north) **/
    n_resize,
    /**The cursor indicates that an edge of a box is to be moved up and right (north/east) **/
    ne_resize,
    /**Indicates a bidirectional resize cursor **/
    nesw_resize,
    /**Indicates a bidirectional resize cursor **/
    ns_resize,
    /**The cursor indicates that an edge of a box is to be moved up and left (north/west) **/
    nw_resize,
    /**Indicates a bidirectional resize cursor **/
    nwse_resize,
    /**The cursor indicates that the dragged item cannot be dropped here **/
    no_drop,
    /**No cursor is rendered for the element **/
    none,
    /**The cursor indicates that the requested action will not be executed **/        
    not_allowed,
    /**The cursor is a pointer and indicates a link **/
    pointer,
    /**The cursor indicates that the program is busy (in progress) **/
    progress,
    /**The cursor indicates that the row can be resized vertically **/
    row_resize,
    /**The cursor indicates that an edge of a box is to be moved down (south) **/
    s_resize,
    /**The cursor indicates that an edge of a box is to be moved down and right (south/east) **/
    se_resize,
    /**The cursor indicates that an edge of a box is to be moved down and left (south/west) **/
    sw_resize,
    /**The cursor indicates text that may be selected  **/
    text,
    /**A comma separated list of URLs to custom cursors. Note: Always specify a generic cursor at the end of the list, in case none of the URL-defined cursors can be used **/
    //URL  Play it »
    /**The cursor indicates vertical-text that may be selected **/
    vertical_text,
    /**The cursor indicates that an edge of a box is to be moved left (west) **/
    w_resize,
    /**The cursor indicates that the program is busy **/
    wait,
    /**The cursor indicates that something can be zoomed in  **/
    zoom_in,
    /**The cursor indicates that something can be zoomed out **/
    zoom_out,
    /**Sets this property to its default value. Read about initial **/
    initial,
    /**Inherits this property from its parent element **/
    inherit;
    
    public void setCursourStateOn(WWidget widget)
    {
        widget.doJavaScript(widget.getJsRef()+".style.cursor='"+toString().replace("_", "-").toLowerCase()+"';");
    }
    
}