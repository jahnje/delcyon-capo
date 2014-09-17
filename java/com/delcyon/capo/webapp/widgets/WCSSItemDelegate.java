package com.delcyon.capo.webapp.widgets;

import java.util.EnumSet;

import eu.webtoolkit.jwt.ViewItemRenderFlag;
import eu.webtoolkit.jwt.WItemDelegate;
import eu.webtoolkit.jwt.WModelIndex;
import eu.webtoolkit.jwt.WWidget;

public class WCSSItemDelegate extends WItemDelegate
{

    private String[] styles = null;
    public WCSSItemDelegate(String... styles)
    {
        this.styles = styles;
    }
    
    @Override
    public WWidget update(WWidget widget, WModelIndex index, EnumSet<ViewItemRenderFlag> flags)
    {
        // TODO Auto-generated method stub
         WWidget _widget = super.update(widget, index, flags);
         StringBuilder styleAttribute = new StringBuilder();
         for (String style : styles)
         {
             if(style.contains(";"))
             {
                 styleAttribute.append(style+" ");                 
             }
             else
             {
                 _widget.addStyleClass(style);
             }
         }
         if(styleAttribute.length() > 0)
         {
             _widget.setAttributeValue("style", styleAttribute.toString());
         }
         return _widget;
    }
    
}
