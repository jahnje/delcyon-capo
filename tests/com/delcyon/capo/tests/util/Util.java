package com.delcyon.capo.tests.util;

import java.io.File;

import org.junit.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.Configuration;
import com.delcyon.capo.Configuration.PREFERENCE;
import com.delcyon.capo.controller.Group;
import com.delcyon.capo.controller.elements.SyncElement;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceDescriptor.Action;
import com.delcyon.capo.resourcemanager.ResourceManager;
import com.delcyon.capo.resourcemanager.ResourceParameter;
import com.delcyon.capo.resourcemanager.types.ContentMetaData;
import com.delcyon.capo.resourcemanager.types.FileResourceType;
import com.delcyon.capo.xml.XMLDiff;
import com.delcyon.capo.xml.XPath;
import com.delcyon.capo.xml.dom.ResourceDocument;

public class Util
{
    
    public static CapoApplication minmalApplication = null;
    
    public static void startMinimalCapoApplication() throws Exception
    {        
    	if (minmalApplication == null)
    	{
    	    minmalApplication = new TestCapoApplication();
    		
    	}
    	if (CapoApplication.getApplication() == null)
    	{
    	    minmalApplication = new TestCapoApplication();
    	}
    	if (CapoApplication.getConfiguration() == null)
    	{
    	    CapoApplication.setConfiguration(new Configuration(new String[]{"-"+PREFERENCE.DISABLE_CONFIG_AUTOSYNC.toString()}));
    	}
    	
    	if (CapoApplication.getDataManager() == null)
    	{
    	    CapoApplication.setDataManager(new ResourceManager());
    	}
    	
    	if (CapoApplication.getDataManager() == null)
    	{
    	    CapoApplication.setDataManager(new ResourceManager());
    	}
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
        Element resourceParameterElement = document.createElementNS(CapoApplication.RESOURCE_NAMESPACE_URI, "resouce:parameter");
        resourceParameterElement.setAttribute("name", FileResourceType.Parameters.ROOT_DIR.toString());
        resourceParameterElement.setAttribute("value", new File(".").getCanonicalPath());
        syncElement.appendChild(resourceParameterElement);
        Group group = new Group("test", null, null, null);
        syncControlElement.init(syncElement, null, group, null);        
        //ResourceDescriptor sourceResourceDescriptor = new FileResourceType().getResourceDescriptor(src);        
        //ResourceDescriptor destinationResourceDescriptor = new FileResourceType().getResourceDescriptor(dest);        
        //syncControlElement.syncTree(sourceResourceDescriptor, destinationResourceDescriptor);
        syncControlElement.processServerSideElement();
    }
    
    public static void deleteTree(String dest) throws Exception
    {
        startMinimalCapoApplication();
        ResourceDescriptor destinationResourceDescriptor = new FileResourceType().getResourceDescriptor(dest);
        destinationResourceDescriptor.getResourceMetaData(null,new ResourceParameter(FileResourceType.Parameters.ROOT_DIR, new File(".").getCanonicalPath()));
        destinationResourceDescriptor.performAction(null, Action.DELETE);
    }
    
    public static Boolean areSame(String src, String dest) throws Exception
    {
        startMinimalCapoApplication();
        ResourceDescriptor sourceResourceDescriptor = new FileResourceType().getResourceDescriptor(src);
        sourceResourceDescriptor.getResourceMetaData(null,new ResourceParameter(FileResourceType.Parameters.ROOT_DIR, new File(".").getCanonicalPath()));
        ResourceDescriptor destinationResourceDescriptor = new FileResourceType().getResourceDescriptor(dest);
        destinationResourceDescriptor.getResourceMetaData(null,new ResourceParameter(FileResourceType.Parameters.ROOT_DIR, new File(".").getCanonicalPath()));
//        Assert.assertTrue(sourceResourceDescriptor.getContentMetaData(null).exists());
//        Assert.assertTrue(destinationResourceDescriptor.getContentMetaData(null).exists());
        //use resource document to get results from both sides
        ResourceDocument baseDocument = new ResourceDocument(sourceResourceDescriptor);
        //Assert.assertTrue(baseDocument.getDocumentElement().getAttribute("exists").equals("true"));
        //XPath.dumpNode(baseDocument, System.out);
        ResourceDocument modDocument = new ResourceDocument(destinationResourceDescriptor);
        //XPath.dumpNode(modDocument, System.out);
        //Assert.assertTrue(modDocument.getDocumentElement().getAttribute("exists").equals("true"));
        //use xml diff to generate diff between both side
        XMLDiff xmlDiff = new XMLDiff();
        xmlDiff.addIgnoreableAttribute(null,ContentMetaData.Attributes.path.toString());
        xmlDiff.addIgnoreableAttribute(null,ContentMetaData.Attributes.uri.toString());
        xmlDiff.addIgnoreableAttribute(null,ContentMetaData.Attributes.lastModified.toString());
        Document diffDocument = xmlDiff.getDifferences(baseDocument, modDocument);
        
        //verify that root element of xml diff contains mod = base
        
        if (diffDocument.getDocumentElement().getAttributeNS(XMLDiff.XDIFF_NAMESPACE_URI, XMLDiff.XDIFF_ELEMENT_ATTRIBUTE_NAME).equals(XMLDiff.EQUALITY) == false)
        {
            XPath.dumpNode(diffDocument, System.out);
        }
        return XMLDiff.EQUALITY.equals(diffDocument.getDocumentElement().getAttributeNS(XMLDiff.XDIFF_NAMESPACE_URI, XMLDiff.XDIFF_ELEMENT_ATTRIBUTE_NAME));
    }
    
}
