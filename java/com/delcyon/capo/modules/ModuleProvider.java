package com.delcyon.capo.modules;

import java.util.logging.Level;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.Configuration;
import com.delcyon.capo.CapoApplication.Location;
import com.delcyon.capo.Configuration.PREFERENCE;
import com.delcyon.capo.annotations.DirectoyProvider;
import com.delcyon.capo.modules.ModuleRequestProcessor.Attributes;
import com.delcyon.capo.protocol.client.CapoConnection;

@DirectoyProvider(preferenceName="MODULE_DIR",preferences=Configuration.PREFERENCE.class,location=Location.SERVER)
public abstract class ModuleProvider
{

    public static Element getModuleElement(String localName) throws Exception
    {
        //check local library first
        Document moduleDocument = CapoApplication.getDataManager().findDocument(localName, null, PREFERENCE.MODULE_DIR);
        if (moduleDocument != null)
        {
            return moduleDocument.getDocumentElement();
        }
        else if (CapoApplication.isServer() == false)
        {
        	CapoConnection capoConnection = new CapoConnection();
        	ModuleRequest moduleRequest = new ModuleRequest(capoConnection, localName);
        	moduleRequest.init();
        	moduleRequest.send();
        	Element responseElement = moduleRequest.readResponse();
        	
        	if (responseElement != null && responseElement.hasAttribute(Attributes.ERROR.toString()) == false && responseElement.getLocalName().equals("request") == false)
        	{
        		return responseElement;
        			
        	}
        	else
        	{
        	    CapoApplication.logger.log(Level.WARNING, "Error trying to find remote module: "+responseElement.getAttribute(Attributes.ERROR.toString()));
        	}
        }	
        
        return null;
        
    }
}
