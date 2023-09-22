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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

import com.delcyon.capo.util.CloneControl;
import com.delcyon.capo.util.CloneControl.Clone;
import com.delcyon.capo.xml.XPath;

/**
 * @author jeremiah
 *
 */
public class CDocument extends CNode implements Document, NodeValidationUtilitesFI
{
	public static final String XML_SCHEMA_NS = "http://www.w3.org/2001/XMLSchema";
	public static final String XML_NAMESPACE_NS = "http://www.w3.org/2000/xmlns/";
    private static int documentIDCounter = 0;
	private static int incrementDocumentID()
	{
		documentIDCounter++;
		return documentIDCounter;
	}
    protected CElement documentElement = null;
    private String documentURI = null;
    private DocumentType doctype;
    private String defaultNamespace;
    
    @CloneControl(filter=Clone.exclude)
    private long documentID = incrementDocumentID();
	private boolean silenceEvents = false;
    private VariableProcessor variableProcessor;
	private boolean onlyAllowValidNodeNames = false;
    
	private HashMap<String, CDocument> namespaceSchemaMap = new HashMap<String, CDocument>();
	
    public CDocument()
    {
        setOwnerDocument(this);
    }
    
    public CDocument(DocumentType doctype)
    {
        setOwnerDocument(this);
        this.doctype = doctype;
    }

    @Override
    public Node removeChild(Node oldChild) throws DOMException
    {
    	if(oldChild.equals(documentElement))
    	{
    		documentElement = null;
    	}
    	return super.removeChild(oldChild);
    }
    
    @Override
    public Node appendChild(Node newChild) throws DOMException
    {
        if(newChild instanceof CElement && documentElement == null)
        {
            this.documentElement = (CElement) newChild;
            removeNodeTypeChildrenAll(newChild.getNodeType());
            super.appendChild(newChild);            
            ((CNode) newChild).setParent(this);
            return newChild;
        }
        else if(newChild instanceof CComment || newChild instanceof CProcessingInstruction)
        {
            return super.appendChild(newChild);            
        }
        else
        {
            throw new UnsupportedOperationException();
        }
    }
    
    @Override
    public String getNodeName()
    {
        return "#document";
    }

    @Override
    public short getNodeType()
    {
        return Node.DOCUMENT_NODE;
    }
    
    /* (non-Javadoc)
     * @see org.w3c.dom.Document#getDoctype()
     */
    @Override
    public DocumentType getDoctype()
    {
        return doctype;
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Document#getImplementation()
     */
    @Override
    public DOMImplementation getImplementation()
    {
        return new CDOMImplementation();
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Document#getDocumentElement()
     */
    @Override
    public Element getDocumentElement()
    {
        return this.documentElement;
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Document#createElement(java.lang.String)
     */
    @Override
    public Element createElement(String tagName) throws DOMException
    {
        CElement cElement = new CElement(tagName);
        cElement.setOwnerDocument(this);
        return cElement;
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Document#createDocumentFragment()
     */
    @Override
    public DocumentFragment createDocumentFragment()
    {
        Thread.dumpStack();
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Document#createTextNode(java.lang.String)
     */
    @Override
    public Text createTextNode(String data)
    {
        CText text = new CText();
        text.setData(data);
        text.setOwnerDocument(this);
        return text;
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Document#createComment(java.lang.String)
     */
    @Override
    public Comment createComment(String data)
    {        
        CComment comment = new CComment();
        comment.setData(data);
        comment.setOwnerDocument(this);
        return comment;
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Document#createCDATASection(java.lang.String)
     */
    @Override
    public CDATASection createCDATASection(String data) throws DOMException
    {
        Thread.dumpStack();
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Document#createProcessingInstruction(java.lang.String, java.lang.String)
     */
    @Override
    public ProcessingInstruction createProcessingInstruction(String target, String data) throws DOMException
    {
        Thread.dumpStack();
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Document#createAttribute(java.lang.String)
     */
    @Override
    public Attr createAttribute(String name) throws DOMException
    {
        Thread.dumpStack();
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Document#createEntityReference(java.lang.String)
     */
    @Override
    public EntityReference createEntityReference(String name) throws DOMException
    {
        Thread.dumpStack();
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Document#getElementsByTagName(java.lang.String)
     */
    @Override
    public NodeList getElementsByTagName(String tagname)
    {
        Thread.dumpStack();
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Document#importNode(org.w3c.dom.Node, boolean)
     */
    //XXX incomplete implementation
    @Override
    public Node importNode(Node importedNode, boolean deep) throws DOMException
    {
        Node clonedNode = null;
        if(importedNode instanceof CNode == false)
        {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try
            {
                XPath.dumpNode(importedNode, byteArrayOutputStream);
                InputSource inputSource = new InputSource(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
                CDocumentBuilder documentBuilder = new CDocumentBuilder();
                importedNode = documentBuilder.parse(inputSource).getDocumentElement();                
            }
            catch (Exception e)
            {
                throw new DOMException(DOMException.NOT_SUPPORTED_ERR, e.getMessage());
            } 
            
        }
        else
        {
            clonedNode = importedNode.cloneNode(true);
        }
        ((CNode) clonedNode).detach();
        adoptNode(clonedNode);
        return clonedNode;
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Document#adoptNode(org.w3c.dom.Node)
     */
  //XXX incomplete implementation
    @Override
    public Node adoptNode(Node source) throws DOMException
    {
        ((CNode) source).detach();

        
        final CDocument ownerDocument = this;
        boolean originalSilenceValue = ownerDocument.isSilenceEvents();
        ownerDocument.setSilenceEvents(true);
        NodeProcessor nodeProcessor = new NodeProcessor()
        {
            
            @Override
            public void process(Node parentNode, Node node) throws Exception
            {
                if(node instanceof CNode)
                {                    
                    ((CNode) node).setOwnerDocument(ownerDocument);
                }
                
            }
        };
        try
        {
            walkTree(null,source, nodeProcessor, false);
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        finally
        {
        	ownerDocument.setSilenceEvents(originalSilenceValue);
        }
        
        
       
        return source;
    }
    
    /* (non-Javadoc)
     * @see org.w3c.dom.Document#createElementNS(java.lang.String, java.lang.String)
     */
    @Override
    public Element createElementNS(String namespaceURI, String qualifiedName) throws DOMException
    {
       CElement element = new CElement(qualifiedName);
       element.setNamespaceURI(namespaceURI);
       element.setOwnerDocument(this);
       return element;
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Document#createAttributeNS(java.lang.String, java.lang.String)
     */
    @Override
    public Attr createAttributeNS(String namespaceURI, String qualifiedName) throws DOMException
    {
        Thread.dumpStack();
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Document#getElementsByTagNameNS(java.lang.String, java.lang.String)
     */
    @Override
    public NodeList getElementsByTagNameNS(String namespaceURI, String localName)
    {
        Thread.dumpStack();
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Document#getElementById(java.lang.String)
     */
    @Override
    public Element getElementById(String elementId)
    {
        Thread.dumpStack();
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Document#getInputEncoding()
     */
    @Override
    public String getInputEncoding()
    {
        // TODO Auto-generated method stub
        Thread.dumpStack();
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Document#getXmlEncoding()
     */
    @Override
    public String getXmlEncoding()
    {
        // TODO Auto-generated method stub
        Thread.dumpStack();
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Document#getXmlStandalone()
     */
    @Override
    public boolean getXmlStandalone()
    {
        // TODO Auto-generated method stub
        Thread.dumpStack();
        return false;
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Document#setXmlStandalone(boolean)
     */
    @Override
    public void setXmlStandalone(boolean xmlStandalone) throws DOMException
    {
        // TODO Auto-generated method stub
        Thread.dumpStack();

    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Document#getXmlVersion()
     */
    @Override
    public String getXmlVersion()
    {
        // TODO Auto-generated method stub
        Thread.dumpStack();
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Document#setXmlVersion(java.lang.String)
     */
    @Override
    public void setXmlVersion(String xmlVersion) throws DOMException
    {
        Thread.dumpStack();
        throw new UnsupportedOperationException();

    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Document#getStrictErrorChecking()
     */
    @Override
    public boolean getStrictErrorChecking()
    {
        Thread.dumpStack();
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Document#setStrictErrorChecking(boolean)
     */
    @Override
    public void setStrictErrorChecking(boolean strictErrorChecking)
    {
        // TODO Auto-generated method stub
        Thread.dumpStack();
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Document#getDocumentURI()
     */
    @Override
    public String getDocumentURI()
    {
       return this.documentURI;
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Document#setDocumentURI(java.lang.String)
     */
    @Override
    public void setDocumentURI(String documentURI)
    {
        this.documentURI = documentURI;
    }

   

    /* (non-Javadoc)
     * @see org.w3c.dom.Document#getDomConfig()
     */
    @Override
    public DOMConfiguration getDomConfig()
    {
        // TODO Auto-generated method stub
        Thread.dumpStack();
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Document#normalizeDocument()
     */
    @Override
    public void normalizeDocument()
    {
        this.normalize(); 
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Document#renameNode(org.w3c.dom.Node, java.lang.String, java.lang.String)
     */
    @Override
    public Node renameNode(Node n, String namespaceURI, String qualifiedName) throws DOMException
    {
        // TODO Auto-generated method stub
        Thread.dumpStack();
        throw new UnsupportedOperationException();
    }

    public String getDefaultNamespace()
    {
        return this.defaultNamespace;
    }
    
    public void setDefaultNamespace(String defaultNamespace)
    {
        this.defaultNamespace = defaultNamespace;
    }
    
    @Override
    public String toString()
    {
    	// TODO Auto-generated method stub
    	return documentID+" "+super.toString();
    }

	public boolean isSilenceEvents()
	{
		return silenceEvents ;
	}
    
	public void setSilenceEvents(boolean silenceEvents)
	{
		this.silenceEvents = silenceEvents;
	}
	
	public void setVariableProcessor(VariableProcessor variableProcessor)
	{
	    this.variableProcessor = variableProcessor;
	}
	
	public VariableProcessor getVariableProcessor()
	{
	    return this.variableProcessor;
	}

	/**
	 * This is a simple method to return a simple document structure based on a list of element names.
	 * This will return a CDocument as the root node. 
	 * @param path, a '/' separated XML path of element names.
	 * @return 
	 */
    public static CDocument buildPath(String path)
    {
        return (CDocument) buildPath(null,path);
    }

    /**
     * This is a simple method to return a simple document structure based on a list of element names.
     * This will return a CDocument as the root node.
     * @param contextNode, the node with which to build the path onto, If null, will return a CDocument as the root of the path. 
     * @param path, a '/' separated XML path of element names.
     * @return 
     */
    public static CNode buildPath(CNode contextNode, String path)
    {
        if(contextNode == null)
        {
            contextNode = new CDocument();
        }
        String[] splitPath = path.split("/");
        CNode currentNode = contextNode;
        for (String pathItem : splitPath)
        {
            CElement pathItemElement = new CElement(pathItem);
            currentNode.appendChild(pathItemElement);
            currentNode = pathItemElement;
        }
        return contextNode;
    }

    
    /**
     * 
     * @return whether or not node names are checked for validity when being created. 
     * This allows us to loosen up our node name restrictions if we want. 
     * Spaces etc. can be allowed.
     */
	public boolean onlyAllowValidNodeNames()
	{
		return this.onlyAllowValidNodeNames ;
	}

	public void setOnlyAllowValidNodeNames(boolean onlyAllowValidNodeNames)
	{
		this.onlyAllowValidNodeNames = onlyAllowValidNodeNames;
	}

	/**
	 *  set the schema document to validate against for a namespace declaration in this document
	 * @param namespaceURI
	 * @param schemaDocument
	 */
    public void setSchemaForNamespace(String namespaceURI, CDocument schemaDocument)
    {        
        if(schemaDocument.getDocumentElement().getNamespaceURI().equals(XML_SCHEMA_NS) == true)
        {
            namespaceSchemaMap.put(namespaceURI, schemaDocument);
        }
        else
        {
            throw new UnknownError("Unknown Schema URI"); 
        }
        
    }
	
    public boolean isNodeValid(CNode node, boolean deep, boolean requireNamespace, Vector<CValidationException> exceptionVector) throws Exception
    {
        int initialExceptionCount = 0;
        if(exceptionVector != null)
        {
            initialExceptionCount = exceptionVector.size();
        }
        
        
        if(requireNamespace == true)
        {
            if(node.getNamespaceURI() == null)
            {
                nodeInvalid("Missing Namespace", node, exceptionVector);
            }
            
            if (namespaceSchemaMap.containsKey(node.getNamespaceURI()) == false && node.getNamespaceURI().equals(XML_SCHEMA_NS) == false && node.getNamespaceURI().equals(XML_NAMESPACE_NS) == false)
            {
                nodeInvalid("Unknown Namespace ["+node.getNamespaceURI()+"]", node, exceptionVector);
            }            
        }
        
        //find namespace
        CDocument schemaDocument = namespaceSchemaMap.get(node.getNamespaceURI());
        
        if(schemaDocument != null)
        {
            //find node declaration
            CElement schemaDeclElement = null;
            switch (node.getNodeType())
            {
                case Node.ATTRIBUTE_NODE:
                    schemaDeclElement = (CElement) XPath.selectNSNode(schemaDocument, "//xs:attribute[@name = '"+node.getLocalName()+"']","xs="+XML_SCHEMA_NS);                    
                    break;
                case Node.ELEMENT_NODE:
                    schemaDeclElement = (CElement) XPath.selectNSNode(schemaDocument, "//xs:element[@name = '"+node.getLocalName()+"']","xs="+XML_SCHEMA_NS);                    
                    break;
                default:
                    break;
            }
            System.out.println(schemaDeclElement);
            if(schemaDeclElement != null)
            {
                node.setNodeDefinition(new CXSDNodeDefinition(schemaDeclElement));
                node.isValid(deep,exceptionVector);
            }
            else
            {
                nodeInvalid("unknown node", node, exceptionVector);
            }
        }
        
        
        if(deep == true)
        {
            //iterate over children (node,deep,requireNamespace,exceptionVector);
        }
        
        return exceptionVector == null ? true : (exceptionVector.size() == initialExceptionCount);
    }

    public HashMap<String, CDocument> getNamespaceSchemaMap()
    {
        return namespaceSchemaMap;
    }
}
