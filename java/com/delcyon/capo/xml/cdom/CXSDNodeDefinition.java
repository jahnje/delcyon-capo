package com.delcyon.capo.xml.cdom;

import java.util.List;
import java.util.Vector;
import java.util.function.Predicate;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

import com.delcyon.capo.xml.XPath;
import com.delcyon.capo.xml.cdom.OccurancePredicate.TestErrorResult;

public class CXSDNodeDefinition implements NodeValidationUtilitesFI, CNodeDefinition
{
    private CElement def = null;
    
    private CXSDNodeDefinition(){}    
    
    public CXSDNodeDefinition(CElement definingElement)
    {
        this.def = definingElement;
    }

    public CElement getDefiningElement()
    {
        return def;
    }
    
    public enum NodeDefinitionType
    {
        annotation,
        simpleType,
        complexType,
        unique,
        key,
        keyref
    }
    
    public NodeDefinitionType getNodeDefinitionType()
    {
        for (Node  childNode : def.nodeList)
        {
            if(childNode.getNodeType() == Node.ELEMENT_NODE)
            {
                switch (childNode.getLocalName())
                {
                    case "annotation":                            
                        return NodeDefinitionType.annotation;
                    case "simpleType":
                        return NodeDefinitionType.simpleType;
                    case "complexType":
                        return NodeDefinitionType.complexType;
                    case "unique":
                        return NodeDefinitionType.unique;
                    case "key":
                        return NodeDefinitionType.key;
                    case "keyref":
                        return NodeDefinitionType.keyref;
                    default:
                        return NodeDefinitionType.simpleType;
                }
            }
        } 
        return NodeDefinitionType.simpleType;
    }
    
    public CElement getNodeDefinitionTypeElement(NodeDefinitionType nodeDefinitionType)
    {
        for (Node  childNode : def.nodeList)
        {
            if(childNode.getNodeType() == Node.ELEMENT_NODE)
            {
                if(childNode.getLocalName().equals(nodeDefinitionType.toString()))
                {
                    return (CElement) childNode;
                }                
            }
        } 
        return null;
    }
    
    public List<CNodeDefinition> getPossibleChildren(CNode node,short nodeType,boolean filtered)
    {
        return null;
    }
    
    public boolean isValid(CNode node, Vector<CValidationException> exceptionVector) throws Exception
    {
        
        switch (node.getNodeType())
        {
            case Node.COMMENT_NODE:
            case Node.PROCESSING_INSTRUCTION_NODE:
                return true;            
            case Node.TEXT_NODE:
                return validateTextNode(node,exceptionVector);                
            case Node.ELEMENT_NODE:
            case Node.ATTRIBUTE_NODE:
            default:
                for (Node  childNode : def.nodeList)
                {
                    switch (childNode.getLocalName())
                    {
                        case "annotation":                            
                            break;
                        case "simpleType":
                            validateNodeAgainstSimpleType(node,childNode, exceptionVector);
                            break;
                        case "complexType":
                            validateNodeAgainstComplexType(node,(CNode) childNode, exceptionVector);
                            break;
                        case "unique":
                            nodeInvalid("unspported definition", node, exceptionVector);
                            break;
                        case "key":
                            nodeInvalid("unspported definition", node, exceptionVector);
                            break;
                        case "keyref":
                            nodeInvalid("unspported definition", node, exceptionVector);
                            break;
                        default:
                            break;
                    }                    
                } 
        }
     
        return false;
    }
    
    /**
     * 
     * @return the base data type for this node.
     */
    public String getTypeDefinition()
    {
        return null;
    }
    
    
    
    private boolean validateTextNode(CNode node, Vector<CValidationException> exceptionVector) throws Exception
    {
        
       return true;
    }

    private void validateNodeAgainstComplexType(CNode node, CNode complexNodeDef, Vector<CValidationException> exceptionVector) throws Exception
    {
        // TODO Auto-generated method stub
        System.out.println(node+":"+complexNodeDef);
        
        for (Node  childDefNode : complexNodeDef.nodeList)
        {
            switch (childDefNode.getLocalName())
            {
                case "annotation":                            
                    break;
                case "simpleContent":
                    nodeInvalid("unspported definition ["+childDefNode.getLocalName()+"]", node, exceptionVector);                    
                    break;
                case "complexContent":
                    nodeInvalid("unspported definition ["+childDefNode.getLocalName()+"]", node, exceptionVector);
                    break;
                case "group":
                    nodeInvalid("unspported definition ["+childDefNode.getLocalName()+"]", node, exceptionVector);
                    break;
                case "all":
                    validateNodeAgainstAll(node,(CNode) childDefNode, exceptionVector);                    
                    break;
                case "choice":
                    nodeInvalid("unspported definition ["+childDefNode.getLocalName()+"]", node, exceptionVector);
                    break;
                case "sequence":                    
                    validateNodeAgainstSequence(node,(CNode) childDefNode, exceptionVector);
                    break;
                case "attribute":
                    validateNodeAgainstAttribute(node,(CNode) childDefNode, exceptionVector);                     
                    break;
                case "attributeGroup":
                    nodeInvalid("unspported definition ["+childDefNode.getLocalName()+"]", node, exceptionVector);
                    break;
                case "anyAttribute":
                    nodeInvalid("unspported definition ["+childDefNode.getLocalName()+"]", node, exceptionVector);
                    break;
                default:
                    break;
            }
            
        } 
        
        //TODO verify/find elements and attributes NOT mentioned in schema
        
        for (Node attributeNode : node.attributeList)
        {
            if(attributeNode.getPrefix() == null && attributeNode.getLocalName().equals("xmlns"))
            {
                continue;
            }
            if(((CNode) attributeNode).getNodeDefinition() == null)
            {
                nodeInvalid("Attribute not allowed", (CNode) attributeNode, exceptionVector);
            }
        }
    }

    
    
    
    private void validateNodeAgainstAttribute(CNode node, CNode attributeDefNode, Vector<CValidationException> exceptionVector) throws Exception
    {
        CElement element = (CElement) node;
        CElement elementDef = (CElement) attributeDefNode;
        String prefix = element.getPrefix();
        String namespaceURI = element.namespaceURI;
        String attrName = elementDef.hasAttribute("name") ? elementDef.getAttribute("name") : elementDef.getAttribute("ref"); 
        
        switch (elementDef.getAttribute("use"))
        {
            case "prohibited":
                if(element.hasAttribute(attrName))
                {
                    nodeInvalid("@"+attrName+" not allowed on  ["+node.getLocalName()+"]", node, exceptionVector);
                }
                break;
            case "required":
                if(!element.hasAttribute(attrName))
                {
                    nodeInvalid("@"+attrName+" required on  ["+node.getLocalName()+"]", node, exceptionVector);
                }
                else
                {
                    ((CNode) element.getAttributeNode(attrName)).setNodeDefinition(new CXSDNodeDefinition(elementDef));
                }
                break;                
            case "optional":
            default:
                if(element.hasAttribute(attrName))
                {
                    ((CNode) element.getAttributeNode(attrName)).setNodeDefinition(new CXSDNodeDefinition(elementDef));
                }                
                break;
        }
        
        //todo process simple type 
        
    }

    private void validateNodeAgainstSequence(CNode node, CNode nodeDef, Vector<CValidationException> exceptionVector) throws Exception
    {
        
        CElement element = (CElement) node;
        String prefix = element.getPrefix();
        String namespaceURI = element.namespaceURI;
        
        
        Vector<OccurancePredicate> functionVector = new Vector<>();
        for (Node  childDefNode : nodeDef.nodeList)
        {
            if(childDefNode.getNodeType() != Node.ELEMENT_NODE)
            {
                continue;
            }
            
            CElement def = (CElement)childDefNode;
            Predicate<CElement> chain = null;
            
            switch (childDefNode.getLocalName())
            {
                case "annotation":                            
                    break;
                case "element":
                    chain = buildElementPredicateChain(chain,def);                                                           
                    break;                
                case "group":
                    nodeInvalid("unspported definition ["+childDefNode.getLocalName()+"]", node, exceptionVector);
                    break;                
                case "choice":
                    nodeInvalid("unspported definition ["+childDefNode.getLocalName()+"]", node, exceptionVector);
                    break;
                case "sequence":                    
                    validateNodeAgainstSequence(node,(CNode) childDefNode, exceptionVector);
                    break;
                case "any":
                    validateNodeAgainstAttribute(node,(CNode) childDefNode, exceptionVector);                     
                    break;               
                default:
                    break;
            }
            
            if(chain != null)
            {
                functionVector.add(new OccurancePredicate(def, chain));
            }
            
        } 
        //DOCUMENT ME 
        //Verify each child of this node according to the rules from above 
        for (Node  childNode : node.nodeList)
        {
            //we're only interested in element nodes for the moment, text nodes will also be an option
            if(childNode.getNodeType() != Node.ELEMENT_NODE)
            {
                continue;
            }
            
            //convert the child node to an element
            CElement childElement = (CElement)childNode;
            System.out.println("SEQ testing "+childElement+"===>"+XPath.getXPath(childElement));
            
            //keep track of whether or not we've satisfied our predicate/test vector 
            boolean satisfied  = false;
            TestErrorResult overAllResult = null;
            
            //Iterate over our list of match tests
            for (OccurancePredicate occurancePredicate : functionVector)
            {
                TestErrorResult result = occurancePredicate.increment(childElement); 
                if(result == null)
                {
                    satisfied = true;
                    break;
                }
                else if(result == TestErrorResult.FULL)
                {
                    overAllResult = result;
                }
            }
            
            if(satisfied == false)
            {
                if(overAllResult == TestErrorResult.FULL)
                {
                    nodeInvalid("Limit exceeded ", (CNode) childNode, exceptionVector);
                }
                else
                {
                    nodeInvalid("unknown element ", (CNode) childNode, exceptionVector);
                }
            }
        }
        
        for (OccurancePredicate occurancePredicate : functionVector)
        {
            if(occurancePredicate.isSatisfied() == false)
            {
                nodeInvalid("missing child "+occurancePredicate.getDefinition(), node, exceptionVector);
                break;
            }
        }
        
    }

    
    /**
     * (all) contain all and only exactly zero or one of each element specified in {particles}. The elements can occur in any order. In this case, to reduce implementation complexity, {particles} is restricted to contain local and top-level element declarations only, with {min occurs}=0 or 1, {max occurs}=1.
     * @param node
     * @param childNode
     * @param exceptionVector
     * @throws Exception 
     * @throws DOMException 
     */
    private void validateNodeAgainstAll(CNode node, CNode allNode, Vector<CValidationException> exceptionVector) throws Exception
    {
        CElement element = (CElement) node;
        String prefix = element.getPrefix();
        String namespaceURI = element.namespaceURI;
        
        
        //=============Structure validation only=====================
        //You can see who the kids are, verify their existence, cardinality, and order, but not whether or not they are valid in and of themselves.
        //NodeList nodeList = XPath.selectNSNodes(node, "child::"+prefix+":"+((CElement)childNode).getAttributeNS(XML_SCHEMA_NS, "name"), prefix+"="+namespaceURI);
        //build tests
        Vector<OccurancePredicate> functionVector = new Vector<>();
        for (Node  childDefNode : allNode.nodeList)
        {
            if(childDefNode.getNodeType() != Node.ELEMENT_NODE)
            {
                continue;
            }
            
            CElement def = (CElement)childDefNode;
            Predicate<CElement> chain = null;
            switch (childDefNode.getLocalName())
            {
                case "annotation":                            
                    break;
                case "element": //add element to head of test list
                    chain = buildElementPredicateChain(chain,def);                    
                    break;
                case "any": 
                    chain = buildAnyElementPredicateChain(chain,node,namespaceURI,def);
                    break;
                case "group":
                    
                    nodeInvalid("unspported definition ["+childDefNode.getLocalName()+"]", node, exceptionVector);
                
                break;
            }
            
            if(chain != null)
            {
                functionVector.add(new OccurancePredicate(def, chain));
            }
            
            
        }
        
        for (Node  childNode : node.nodeList)
        {
            if(childNode.getNodeType() != Node.ELEMENT_NODE)
            {
                continue;
            }
            CElement childElement = (CElement)childNode;
            System.out.println("ALL testing "+childElement+"===>"+XPath.getXPath(childElement));
            boolean satisfied  = false;
            TestErrorResult overAllResult = null;
            for (OccurancePredicate occurancePredicate : functionVector)
            {
                TestErrorResult result = occurancePredicate.increment(childElement); 
                if(result == null)
                {
                    satisfied = true;
                    break;
                }
                else if(result == TestErrorResult.FULL)
                {
                    overAllResult = result;
                }
            }
            if(satisfied == false)
            {
                if(overAllResult == TestErrorResult.FULL)
                {
                    nodeInvalid("Limit exceeded ", (CNode) childNode, exceptionVector);
                }
                else
                {
                    nodeInvalid("unknown element ", (CNode) childNode, exceptionVector);
                }
            }
        }
        
        for (OccurancePredicate occurancePredicate : functionVector)
        {
            if(occurancePredicate.isSatisfied() == false)
            {
                nodeInvalid("missing child "+occurancePredicate.getDefinition(), node, exceptionVector);
                break;
            }
        }
        
    }
    
    private Predicate<CElement> buildAnyElementPredicateChain(Predicate<CElement> chain, CNode node,String namespaceURI,CElement def)
    {
      //add any element to end of test list
        //set default chain, must consist of whether or not it's a defined element TODO this is where lax comes in
        /*
         Specifies the validation constraint to be applied on the content that matched the wildcard. Possible values are
            skip - that means no validation
            lax - that means validation will be performed if a schema is available
            strict - that means validation is required
            The default is strict.
         */
        chain = (_node)->{
            String processContents = "strict";
            ((CDocument)node.getOwnerDocument()).getNamespaceSchemaMap();
            if(def.hasAttribute("processContents"))
            {
                processContents = def.getAttribute("processContents");
            }
            switch (processContents)
            {
                case "skip":                                
                    return true;
                case "lax": //make sure that we can find a definition for this element, if we have a schema for it's namespace
                    if (((CDocument)node.getOwnerDocument()).getNamespaceSchemaMap().containsKey(_node.getNamespaceURI()) == false)
                    {
                        return true;
                    }                                
                case "strict": //make sure that we can find a definition for this element                                
                default:
                    if(getDefinitionForNode(_node) == null)
                    {
                        return false;
                    }
            }
            return true;
        };
        
        chain = buildPredicate(chain, (_def)-> _def.hasAttribute("namespace"),
                _node -> {
                    switch (def.getAttribute("namespace"))
                    {
                        case "##any":                                        
                            return true;
                        case "##other":                                        
                            return (!_node.getNamespaceURI().equals(namespaceURI));
                        case "##targetNamespace":                                        
                            return (_node.getNamespaceURI().equals(namespaceURI));
                        case "##local":                                        
                            return (_node.getNamespaceURI().equals(namespaceURI));
                        case "":                                        
                            return (_node.getNamespaceURI().equals(namespaceURI));
                        default:
                            return (_node.getNamespaceURI().equals(namespaceURI));
                    }                                
                }, def);
        chain = buildPredicate(chain, (_def)-> _def.hasAttribute("notNamespace"),
                _node -> {
                    switch (def.getAttribute("notNamespace"))
                    {                                   
                        case "##targetNamespace":                                        
                            return (!_node.getNamespaceURI().equals(namespaceURI));
                        case "##local":                                        
                            return (!_node.getNamespaceURI().equals(namespaceURI));                                       
                        default:
                            return (!_node.getNamespaceURI().equals(namespaceURI));
                    }                                
                }, def);
        if(def.hasAttribute("notQName"))
        {
            //nodeInvalid("unspported definition ["+childDefNode.getLocalName()+"]", node, exceptionVector);
        }                    
       
        return chain;
    }

    private Predicate<CElement> buildElementPredicateChain(Predicate<CElement> chain,CElement def)
    {
        //localname test
        chain = buildPredicate(chain, (_def)-> _def.hasAttribute("name"),_node -> _node.getLocalName().equals(def.getAttribute("name")),  def);
        //refname test
        chain = buildPredicate(chain, (_def)-> _def.hasAttribute("ref"), _node -> _node.getLocalName().equals(def.getAttribute("ref")), def);
        
        return chain;
    }
    
    private <T> Predicate<T> buildPredicate(Predicate<T> chain,Predicate<T> additionTest, Predicate<T> predicateToAdd,  T t)
    {
        if(additionTest.test(t))
        {
            if(chain == null)
            {
                chain = predicateToAdd;
            }
            else
            {
                chain = chain.and(predicateToAdd);
            }
        }
        return chain;
    }
     
    
    private void validateNodeAgainstSimpleType(CNode node, Node simpleNode, Vector<CValidationException> exceptionVector)
    {
        // TODO Auto-generated method stub
        
    }

    //XXX serious caching needed here
    //XXX this also needs to verify that it's the right definition, as an element defined as a NON-top level element can have a different definition as the child of some other complex element
   public static CNodeDefinition getDefinitionForNode(CNode node)
   {
       //===========PROCESS NAMESPACE DECLARATION===============
       if("xmlns".equals(node.getLocalName()))
       {
         System.out.println("name space declaration");
         return new CNodeDefinition(){
             @Override
            public boolean isValid(CNode node, Vector<CValidationException> exceptionVector) throws Exception
            {   
                if(node.getNodeType() == Node.ATTRIBUTE_NODE)
                {
                    if(CDocument.XML_NAMESPACE_NS.equals(node.getNamespaceURI()))
                    {
                        if("xmlns".equals(node.getLocalName()) || "xmlns".equals(node.getPrefix()))
                        {
                            return true;
                        }
                    }
                }
                nodeInvalid("Not a valid xmlns attribute", node, exceptionVector);
                return false;
            }
         };
       }
     //===========END PROCESS NAMESPACE DECLARATION===============
       CNode textNode = null;
       if(node.getNodeType() == Node.TEXT_NODE)
       {
           textNode = node;
           node = (CNode) node.getParentNode();
       }
       CDocument schemaDocument = ((CDocument)node.getOwnerDocument()).getNamespaceSchemaMap().get(node.getNamespaceURI());
       try
       {
           if(schemaDocument != null)
           {
               //find node declaration
               CElement schemaDeclElement = null;
               switch (node.getNodeType())
               {
                   case Node.ATTRIBUTE_NODE:
                       schemaDeclElement = (CElement) XPath.selectNSNode(schemaDocument, "//xs:attribute[@name = '"+node.getLocalName()+"']","xs="+CDocument.XML_SCHEMA_NS);                    
                       break;
                   case Node.ELEMENT_NODE:
                       schemaDeclElement = (CElement) XPath.selectNSNode(schemaDocument, "//xs:element[@name = '"+node.getLocalName()+"']","xs="+CDocument.XML_SCHEMA_NS);                    
                       break;
                   case Node.TEXT_NODE:
                       schemaDeclElement = (CElement) XPath.selectNSNode(schemaDocument, "//xs:element[@name = '"+node.getParentNode().getLocalName()+"']","xs="+CDocument.XML_SCHEMA_NS);
                   default:
                       break;
               }

               if(schemaDeclElement != null)
               {
                   return new CXSDNodeDefinition(schemaDeclElement);              
               }          
           }
       } catch (Exception exception)
       {
           exception.printStackTrace();
       }
       return null;
   }
    
}
