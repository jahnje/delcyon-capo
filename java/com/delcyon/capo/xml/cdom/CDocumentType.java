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

import org.w3c.dom.DocumentType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author jeremiah
 */
public class CDocumentType extends CNode implements DocumentType
{

    private String name;
    private CNamedNodeMap entities;
    private CNamedNodeMap notations;    
    private String publicID;
    private String systemID;
    private String internalSubset;

    public CDocumentType(CDocument ownerDocument, String name)
    {

        setNodeName(name);
        setOwnerDocument(ownerDocument);
        entities = new CNamedNodeMap();
        notations = new CNamedNodeMap();        

    }

    public CDocumentType(CDocument ownerDocument, String qualifiedName, String publicID, String systemID)
    {
        this(ownerDocument, qualifiedName);
        this.publicID = publicID;
        this.systemID = systemID;

    } 

    /*
     * (non-Javadoc)
     * @see org.w3c.dom.Node#getNodeType()
     */
    @Override
    public short getNodeType()
    {
        return Node.DOCUMENT_TYPE_NODE;
    }

    /*
     * (non-Javadoc)
     * @see org.w3c.dom.DocumentType#getName()
     */
    @Override
    public String getName()
    {
        return this.name;
    }

    /*
     * (non-Javadoc)
     * @see org.w3c.dom.DocumentType#getEntities()
     */
    @Override
    public NamedNodeMap getEntities()
    {
        return entities;
    }

    /*
     * (non-Javadoc)
     * @see org.w3c.dom.DocumentType#getNotations()
     */
    @Override
    public NamedNodeMap getNotations()
    {
        return notations;
    }

    /*
     * (non-Javadoc)
     * @see org.w3c.dom.DocumentType#getPublicId()
     */
    @Override
    public String getPublicId()
    {
       return this.publicID;
    }

    /*
     * (non-Javadoc)
     * @see org.w3c.dom.DocumentType#getSystemId()
     */
    @Override
    public String getSystemId()
    {
        return this.systemID;
    }

    /*
     * (non-Javadoc)
     * @see org.w3c.dom.DocumentType#getInternalSubset()
     */
    @Override
    public String getInternalSubset()
    {
        return this.internalSubset;
    }

}
