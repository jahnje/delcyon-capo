package com.delcyon.capo.webapp.widgets;

import eu.webtoolkit.jwt.JSignal1;
import eu.webtoolkit.jwt.TextFormat;
import eu.webtoolkit.jwt.WText;

/**
 * In the end this should be a complete wrapper for an ace editor. Still needs a toolbar, and cancel buttons.
 * Probably want a way to integrate all of the javascript as well. 
 * @author jeremiah
 *
 */
public class WAceEditor extends WText
{

    private JSignal1<String> save;
    
    public WAceEditor()
    {
        save = new JSignal1<String>(this, "save") { };
    }
    
    public WAceEditor(String string, TextFormat xhtmlunsafetext)
    {
        super(string, xhtmlunsafetext);
        save = new JSignal1<String>(this, "save") { };
    }

    public JSignal1<String> save() { return save; }

    
}
