package com.delcyon.capo.controller.elements;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.Group;
import com.delcyon.capo.tests.util.external.Util;

public class SyncElementTest
{

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        Util.deleteTree("capo");
        com.delcyon.capo.tests.util.Util.startMinimalCapoApplication();
    }

    @Before
    public void setUp() throws Exception
    {
    }

    @Test
    public void testProcessServerSideElement() throws Exception
    {
        
        SyncElement syncControlElement = new SyncElement();
        Document document = CapoApplication.getDocumentBuilder().newDocument();
        Element syncElement = document.createElement("sync");
        syncElement.setAttribute(SyncElement.Attributes.src.toString(), "test-data/capo");
        syncElement.setAttribute(SyncElement.Attributes.dest.toString(), "capo");
        syncElement.setAttribute(SyncElement.Attributes.recursive.toString(), "true");
        Group group = new Group("test", null, null, null);
        syncControlElement.init(syncElement, null, group, null);
        syncControlElement.processServerSideElement();
        
    }

}
