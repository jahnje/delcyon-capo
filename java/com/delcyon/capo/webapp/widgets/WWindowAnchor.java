package com.delcyon.capo.webapp.widgets;

import java.util.HashMap;

import eu.webtoolkit.jwt.AnchorTarget;
import eu.webtoolkit.jwt.Icon;
import eu.webtoolkit.jwt.JSignal;
import eu.webtoolkit.jwt.StandardButton;
import eu.webtoolkit.jwt.WAnchor;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WLink;
import eu.webtoolkit.jwt.WMessageBox;
import eu.webtoolkit.jwt.WPushButton;

/**
 * Extends WAnchor, with an open method to fire an event to open link in new window. Also include an alert message if popups are blocked. Message can be set with setAlertMessage.
 * @author jeremiah
 *
 */
public class WWindowAnchor extends WAnchor
{
    
    private JSignal openError = new JSignal(this, "openError") { };
    private String alertMessageTitle = "Please enable popups in your browser";
    private String alertMessage = "You must disable popup blocking for this application to open things in other tabs or windows.";
    private boolean unhideOnError = false;
    private boolean attached = false;
    
    public WWindowAnchor()
    {
        setTarget(AnchorTarget.TargetNewWindow);
        openError.addListener(this, this::openError);
    }
    
    public WWindowAnchor(WLink wLink, String text)
    {
        super(wLink, text);
        setTarget(AnchorTarget.TargetNewWindow);
        openError.addListener(this, this::openError);
    }

    public void open()
    {                
        doJavaScript("win = window.open('"+getLink().getUrl()+"','_blank'); if(win == null || typeof win === \"undefined\") {"+openError.createCall()+";}");
    }
    
    public void openPost(HashMap<String, String> parameters)
    {
    
        //convert parameters to json string for doJavaScript 
        /*
         
         openWindowWithPost("http://www.example.com/index.php", {
            p: "view.map",
            coords: encodeURIComponent(coords)
         });
          
         */
        final StringBuffer jsonArray = new StringBuffer();
        parameters.entrySet().forEach(entry->{
            jsonArray.append((jsonArray.length() != 0 ? ",\n" : "")+ entry.getKey()+": '"+entry.getValue()+"'");           
        });
        jsonArray.append("\n");
        
        if(attached == false)
        {
            attachJS();
            attached = true;
        }
        doJavaScript(getJsRef()+".openWindowWithPost('"+getLink().getUrl()+"', {"+jsonArray+"});");
        
    }
    
    private void attachJS()
    {
               
        setJavaScriptMember("openWindowWithPost","function(url, data) { "
                +"var form = document.createElement(\"form\"); "
                +"form.target = \"_blank\"; "
                +"form.method = \"POST\"; "
                +"form.action = url; "
            +"form.style.display = \"none\"; "
            +" "
            +"for (var key in data) { "
            +"var input = document.createElement(\"input\"); "
            +"input.type = \"hidden\"; "
            +"input.name = key; "
                +"input.value = data[key]; "
                +"form.appendChild(input); "
                +"} "
            +" "
            +"document.body.appendChild(form); "
            +"form.submit(); "
            +"document.body.removeChild(form); "
            +"} "
        );
    }
    public void setAlertMessageTitle(String alertMessage)
    {
        this.alertMessageTitle = alertMessage;
    }
    
    public String getAlertMessageTitle()
    {
        return alertMessageTitle;
    }
    
    public void setAlertMessage(String alertMessage)
    {
        this.alertMessage = alertMessage;
    }
    
    public String getAlertMessage()
    {
        return alertMessage;
    }
    
    public void setUnhideOnError(boolean unhideOnError)
    {
        this.unhideOnError = unhideOnError;
    }
    
    public void openError()
    {   
        if(unhideOnError)
        {
            setHidden(false);
        }
        WMessageBox messageBox = new WMessageBox();
        messageBox.setWindowTitle(getAlertMessageTitle());
        //messageBox.getTextWidget().setTextFormat(TextFormat.XHTMLUnsafeText);
        messageBox.setText(getAlertMessage());
        messageBox.getTextWidget().setPadding(new WLength(10));
        messageBox.getTextWidget().setInline(false);
        
        messageBox.setIcon(Icon.Warning);
        WPushButton button = new WPushButton("Open in New Window");
        button.setLink(getLink());
        button.setLinkTarget(AnchorTarget.TargetNewWindow);
        //button.setDefault(true);
        messageBox.addButton(button, StandardButton.Ok);
        messageBox.setDefaultButton(button);        
        messageBox.buttonClicked().addListener(messageBox, ()->messageBox.hide());
        messageBox.show();
    }
    
}
