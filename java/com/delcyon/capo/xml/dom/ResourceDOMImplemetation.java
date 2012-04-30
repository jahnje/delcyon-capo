package com.delcyon.capo.xml.dom;

import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;

public class ResourceDOMImplemetation implements DOMImplementation
{

	@Override
	public Document createDocument(String namespaceURI, String qualifiedName, DocumentType doctype) throws DOMException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public DocumentType createDocumentType(String qualifiedName, String publicId, String systemId) throws DOMException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getFeature(String feature, String version)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasFeature(String feature, String version)
	{
		
		return (feature.equalsIgnoreCase("XML") || feature.equalsIgnoreCase("Core")) && (version == null || version.length() == 0 || version.equals("3.0") || version.equals("2.0") || version.equals("1.0"));
	}
}
