package com.delcyon.capo.webapp.widgets;

import eu.webtoolkit.jwt.AnchorTarget;
import eu.webtoolkit.jwt.WAnchor;
import eu.webtoolkit.jwt.WLink;

/**
 * Extends WAnchor, with an open method to fire an event to open link in new window. Also include an alert message if popups are blocked. Message can be set with setAlertMessage.
 * @author jeremiah
 *
 */
public class WWindowAnchor extends WAnchor
{
    private String alertMessage = "Please enable popups in your browser";
    
    public WWindowAnchor()
    {
        setTarget(AnchorTarget.TargetNewWindow);
    }
    
    public WWindowAnchor(WLink wLink, String text)
    {
        super(wLink, text);
        setTarget(AnchorTarget.TargetNewWindow);
    }

    public void open()
    {        
        doJavaScript("win = window.open('"+getLink().getUrl()+"','_blank'); if(win == null || typeof win === \"undefined\") {alert('"+getAlertMessage()+"');}");
    }
    
    public void setAlertMessage(String alertMessage)
    {
        this.alertMessage = alertMessage;
    }
    
    public String getAlertMessage()
    {
        return alertMessage;
    }
    
}
