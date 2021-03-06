
package com.delcyon.capo.webapp.servlets;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.EnumSet;
import java.util.logging.Level;

import javax.jcr.Session;

import org.w3c.dom.Document;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.Action;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.State;
import com.delcyon.capo.server.CapoServer;
import com.delcyon.capo.server.jackrabbit.CapoJcrServer;
import com.delcyon.capo.webapp.widgets.WCapoResourceExplorer;
import com.delcyon.capo.webapp.widgets.WCapoSearchControl;
import com.delcyon.capo.webapp.widgets.WConsoleWidget;
import com.delcyon.capo.webapp.widgets.WXmlNavigationBar;
import com.delcyon.capo.xml.dom.ResourceDocument;

import eu.webtoolkit.jwt.AlignmentFlag;
import eu.webtoolkit.jwt.PositionScheme;
import eu.webtoolkit.jwt.Signal1.Listener;
import eu.webtoolkit.jwt.StandardButton;
import eu.webtoolkit.jwt.TextFormat;
import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WBootstrapTheme;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WGridLayout;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WLink;
import eu.webtoolkit.jwt.WLink.Type;
import eu.webtoolkit.jwt.WMessageBox;

public class CapoWebApplication extends WApplication {
	
	
	    
    private WContainerWidget rootContainerWidget;
	private WGridLayout rootLayout;
	private WCapoResourceExplorer capoResourceExplorer;	
	private ResourceDocument document;
    private Session jcrSession;
    private WCapoSearchControl capoSearchControl;
    private WXmlNavigationBar navigation;
    private WConsoleWidget consoleWidget;
    private Listener<String> consoleListener;
    
	public CapoWebApplication(WEnvironment env, boolean embedded) {
        super(env);
        WBootstrapTheme bootstrapTheme = new WBootstrapTheme();
        setTheme(bootstrapTheme);
        useStyleSheet(new WLink("/wr/css/local.css"));
        //setCssTheme("polished");
        setTitle("Capo");
        require("/wr/ace/ace.js");
        WApplication.getInstance().enableUpdates(true);
        try
        {
            jcrSession = CapoJcrServer.createSession();
            createUI();
        }
        catch (Exception exception)
        {        
            CapoWebApplication.exception(Level.SEVERE, "error creating UI", exception);
        }
    }

	public Session getJcrSession()
    {
        return this.jcrSession;
    }
	
    private void createUI() throws Exception
    {
        rootContainerWidget = getRoot();
        rootContainerWidget.setPadding(new WLength(0));
        rootContainerWidget.setLayout(getRootLayout());
       
        //WApplication.getInstance().internalPathChanged().addListener(this, this::processInternalPathChanged);
        
        
        getRootLayout().addWidget(getNavigationContainer(),0,0);              
        getRootLayout().addWidget(getCapoResourceExplorer(),1,0);
        getNavigationContainer().setLayoutItem(getRootLayout().getItemAt(1));
        //getRootLayout().addWidget(getConsoleWidget(),2,0);

        //FileResourceType fileResourceType = new FileResourceType();
        //ResourceDescriptor resourceDescriptor = fileResourceType.getResourceDescriptor("file:/");
        ResourceDescriptor resourceDescriptor = CapoApplication.getDataManager().getResourceDescriptor(null, "repo:"+WApplication.getInstance().getInternalPath());
        if(resourceDescriptor.getResourceMetaData(null).exists() == false)
        {
            resourceDescriptor.performAction(null, Action.CREATE);
            resourceDescriptor.reset(State.OPEN);
        }
        //ResourceDescriptor resourceDescriptor2 = CapoApplication.getDataManager().getResourceDirectory("CLIENTS_DIR");
        //ResourceDocumentBuilder documentBuilder = new ResourceDocumentBuilder();
        //document = (ResourceDocument) documentBuilder.buildDocument(resourceDescriptor2);

        getCapoResourceExplorer().setRootResourceDescriptor(resourceDescriptor);
    }
   

	private WCapoResourceExplorer getCapoResourceExplorer() {
    	if (capoResourceExplorer == null)
    	{
    		capoResourceExplorer = new WCapoResourceExplorer();
    	}
    	return capoResourceExplorer;
	}

	private WConsoleWidget getConsoleWidget()
	{
	    if(consoleWidget == null)
	    {
	        consoleWidget = new WConsoleWidget();	        
	        consoleListener = (input) -> {
	        	if(input.startsWith("["))
	        	{
	        		input = input.replaceFirst("(\\[.+\\]) ([a-zA-Z0-9]+) ([a-zA-Z0-9\\.]+) - (.*)", "<span class='console-msg-tsrc'>$1</span> <span class='console-msg-level'>$2</span> <span class='console-msg-jsrc'>$3</span> - $4");
	        		getConsoleWidget().append(input,TextFormat.XHTMLText);
	        	}
	        	else
	        	{
	        	    getConsoleWidget().append(input,TextFormat.PlainText);
	        	}
            };
            CapoServer.errConsole.output().addListener(this, consoleListener);
            CapoServer.outConsole.output().addListener(this, consoleListener);
	    }
	    
	    return consoleWidget;
	}
	
	

	private WGridLayout getRootLayout() {
    	if (rootLayout == null)
    	{
    		rootLayout = new WGridLayout();
    		rootLayout.setContentsMargins(0,0,0,0);
    		getRootLayout().setRowStretch(1, 1);
    	}
    	return rootLayout;
	}

    
	private WXmlNavigationBar getNavigationContainer() throws Exception
	{
	    if(navigation == null)
	    {
	        //create and make nav container and nav bar widget
	        Document menuDocument  = CapoApplication.getDocumentBuilder().parse(CapoWebApplication.class.getClassLoader().getResource("main_navigation_menu.xml").openStream());
	        navigation = new WXmlNavigationBar(menuDocument.getDocumentElement());
	        navigation.addWidget(getSearchControl(),AlignmentFlag.AlignRight);
	        navigation.setResponsive(false);
	        navigation.setTitle("Capo",new WLink(Type.InternalPath,"/"));
	        navigation.setPopup(true);
	        navigation.setHeight(new WLength(5));
	        navigation.setMargin(0);
	        navigation.setOffsets(0);
	        navigation.setPositionScheme(PositionScheme.Relative);
	        navigation.setAttributeValue("style", "background-color: black;");

	    }

	    return navigation;

    }
    
    private WCapoSearchControl getSearchControl()
    {
        if(capoSearchControl == null)
        {
            capoSearchControl = new WCapoSearchControl();
        }
        return capoSearchControl;
    }
   
    
    public static void exception(Level level,String message, Exception exception)
    {
    	    	
    	CapoApplication.logger.log(level, message, exception);
    	ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    	PrintStream printStream = new PrintStream(byteArrayOutputStream);
    	exception.printStackTrace(printStream);
    	WMessageBox.show(message,"<pre class='error_message'>"+new String(byteArrayOutputStream.toByteArray())+"</pre>", EnumSet.of(StandardButton.Ok));
        
    }
    
    /* (non-Javadoc)
     * @see eu.webtoolkit.jwt.WApplication#destroy()
     */
    @Override
    public void destroy()
    {  
        getConsoleWidget().destroy();
        CapoServer.errConsole.output().removeListener(consoleListener);
        CapoServer.outConsole.output().removeListener(consoleListener);
        super.destroy();
    }
    
}
