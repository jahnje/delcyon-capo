package com.delcyon.capo.tests.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.Configuration;
import com.delcyon.capo.controller.Group;
import com.delcyon.capo.controller.elements.SyncElement;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.Action;
import com.delcyon.capo.resourcemanager.ResourceManager;
import com.delcyon.capo.resourcemanager.types.FileResourceType;

public class Util
{
    
    
    
    public static void startMinimalCapoApplication() throws Exception
    {        
        new TestCapoApplication();
        CapoApplication.setConfiguration(new Configuration(new String[]{}));        
        CapoApplication.setDataManager(new ResourceManager());
    }
    
    
    public static void copyTree(String src,String dest,boolean recursive,boolean prune) throws Exception
    {   
        startMinimalCapoApplication();
        SyncElement syncControlElement = new SyncElement();
        Document document = CapoApplication.getDocumentBuilder().newDocument();
        Element syncElement = document.createElement("sync");
        syncElement.setAttribute(SyncElement.Attributes.src.toString(), src);
        syncElement.setAttribute(SyncElement.Attributes.dest.toString(), dest);
        syncElement.setAttribute(SyncElement.Attributes.recursive.toString(), recursive+"");
        syncElement.setAttribute(SyncElement.Attributes.prune.toString(), prune+"");
        Group group = new Group("test", null, null, null);
        syncControlElement.init(syncElement, null, group, null);        
        ResourceDescriptor sourceResourceDescriptor = new FileResourceType().getResourceDescriptor(src);
        ResourceDescriptor destinationResourceDescriptor = new FileResourceType().getResourceDescriptor(dest);
        syncControlElement.syncTree(sourceResourceDescriptor, destinationResourceDescriptor);
    }
    
    public static void deleteTree(String dest) throws Exception
    {
        startMinimalCapoApplication();
        ResourceDescriptor destinationResourceDescriptor = new FileResourceType().getResourceDescriptor(dest);
        destinationResourceDescriptor.getContentMetaData(null);
        destinationResourceDescriptor.performAction(null, Action.DELETE);
    }
    
    
    
}
