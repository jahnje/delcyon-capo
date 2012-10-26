/**
Copyright (c) 2012 Delcyon, Inc.
This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.delcyon.capo.xml.cdom;

import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;

/**
 * @author jeremiah
 *
 */
public class CDOMImplementation implements DOMImplementation
{

    public boolean hasFeature(String feature, String version)
    {
        return (feature.equalsIgnoreCase("XML") || feature.equalsIgnoreCase("Core")) && (version == null || version.length() == 0 || version.equals("3.0") || version.equals("2.0") || version.equals("1.0"));
    }

    @Override
    public DocumentType createDocumentType(String qualifiedName, String publicId, String systemId) throws DOMException
    {
       return new CDocumentType(null, qualifiedName, publicId, systemId);
    }

    @Override
    public Document createDocument(String namespaceURI, String qualifiedName, DocumentType doctype) throws DOMException
    {
        CDocument cDocument = new CDocument(doctype);
        CElement element = new CElement(qualifiedName);
        element.setNamespaceURI(namespaceURI);
        cDocument.appendChild(element);
        return cDocument;
    }

    @Override
    public Object getFeature(String feature, String version)
    {
        Thread.dumpStack();
        throw new UnsupportedOperationException();
    }

}
