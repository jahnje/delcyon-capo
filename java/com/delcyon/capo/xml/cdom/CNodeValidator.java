package com.delcyon.capo.xml.cdom;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;
import java.util.Vector;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.w3c.dom.Node;

import com.delcyon.capo.xml.XPath;
import com.delcyon.capo.xml.cdom.CNodeDefinition.NodeDefinitionType;

public class CNodeValidator implements NodeValidationUtilitesFI
{
    public enum NodeValidationResult
    {
        VALID,
        INVALID_CONTENT,
        MISSING_CONTENT
    }
    
    private boolean debug = true;
    
    private Vector<Object> path = new Vector<>();
    private CElement currentContentDefNode = null;
    //private CNode currentChildNode = null;
    private CNode node = null;
    private CNodeDefinition nodeDefinition = null;
    private CElement nodeDefinitionTypeElement = null;
    private Boolean valid = null;
    
    private CElement bestMatch;
    private CNode invalidNode;
    private CNode nextPossibleNode;
    private NodeValidationResult nodeValidationResult = null;
    
    public CNodeValidator(CNode node, CNodeDefinition definition)
    {
        this.node = node;
        this.nodeDefinition = definition;
    }

    public CElement getBestMatch()
    {
        return bestMatch;
    }
    
    public CNode getInvalidNode()
    {
        return invalidNode;
    }
    
    public CNode getNextPossibleNode()
    {
        return nextPossibleNode;
    }
    
    public NodeValidationResult getNodeValidationResult() throws Exception
    {
        if(valid == null)
        {
            validate();
        }
        if(valid == true)
        {
            return NodeValidationResult.VALID;
        }
        else if (getInvalidNode() != null)
        {
            return NodeValidationResult.INVALID_CONTENT;
        }
        else
        {
            return NodeValidationResult.MISSING_CONTENT;
        }
    }
    
    public void validate() throws Exception
    {
        switch(nodeDefinition.getNodeDefinitionType())
        {
            case simpleType:
                valid = true;
            case complexType:                
                validateComplexType();    
        }        
    }

    public boolean isValid() throws Exception
    {
        if(valid == null)
        {
            validate();
        }
        return valid; 
    }
    

    /*
     * walk through 
     */
    private boolean validateComplexType() throws Exception
    {
        
        int deepestChildMatch = -1;
        int defNodeOfDeepestChildMatch = -1;
        
        //boolean keepNextNoMatch = false;
        CNode nextNoMatch = null;
        CNode nextNoMatchDef = null;
        CNode lastMatch = null;
        
        CElement m = nodeDefinition.getNodeDefinitionTypeElement(NodeDefinitionType.complexType);
        LinkedList<CElement> elementList =  node.nodeList.stream().filter(node->node.getNodeType() == Node.ELEMENT_NODE).map(node->(CElement)node).collect(Collectors.toCollection(LinkedList::new));//.findFirst().orElse(null);
        
//        System.out.println("M="+m);
        //System.out.println("="+a);
        if(elementList.size() == 0)
        {
            return false;
        }
        
        //iterator stack for groups as well as some sort of depth multiplier
        Stack<Iterator<CElement>> groupIteratorStack = new Stack<>();
        Stack<Integer> depthStack = new Stack<Integer>();

        //push def stream iterator onto stack
        Stream<CElement> contentModelStream = m.stream().filter(node->node.getNodeType() == Node.ELEMENT_NODE).map(node->(CElement)node);
        groupIteratorStack.push(((Iterable<CElement>)contentModelStream::iterator).iterator());
        contentModelStream = null; //get rid of content moden stream outside of stack to prevent coding errors
        depthStack.push(m.getDepth()); 
        
        int definitionPathIndex = 0; //debuging only at the moment
        //int childNodeIndex = 0;
        
        //choice related info
        Stack<ChoiceStackItem> choiceStack = new Stack<>();
        int returnDepth = -1;
        
        Stack<ChoiceStackItem> sequenceStack = new Stack<>();
        
        //multiple node children will use the same def predicate for matching, so keep this up here
        OccurancePredicate predicate = null; 
        boolean keepCurrentDef = false;
        int childNodeIndex = 0;
        boolean getOneExtraDef = true;
        
        for(; childNodeIndex < elementList.size()|| getOneExtraDef; childNodeIndex++)
        //for(CElement testChild : (Iterable<CElement>) node.nodeList.stream().filter(node->node.getNodeType() == Node.ELEMENT_NODE).map(node->(CElement)node)::iterator)
        {
            CElement testChild = null;
            if(childNodeIndex == elementList.size() && getOneExtraDef)
            {                
                getOneExtraDef = false;
                childNodeIndex--;
                if(groupIteratorStack.peek().hasNext() == false)
                {
                    //keepCurrentDef = true;
                    //currentContentDefNode = null;
                }
            }
            else
            {
                //childNodeIndex++;
                //filter out no element nodes
                if(elementList.get(childNodeIndex).getNodeType() != Node.ELEMENT_NODE)
                {
                    continue;
                }

                testChild = (CElement) elementList.get(childNodeIndex);
                //keep next possible match 
                if(deepestChildMatch < childNodeIndex)
                {                
                    nextNoMatch = testChild;                
                }
            }
            
            
            
            //==============START CONTENT DEF LOOP===============
            search_loop:
            for(;groupIteratorStack.peek().hasNext() || keepCurrentDef  ;definitionPathIndex++)            
            {
                Integer depthOnLoopEntry = depthStack.peek();
                if(keepCurrentDef == false)
                {
                    currentContentDefNode = groupIteratorStack.peek().next();
                    predicate = null; //we have a new def, so clear out the current occurrence predicate
                    //if we've reached the end of our group start poping things off the group stack until we reach something valid or the end of the stack
                    while(groupIteratorStack.peek().hasNext() == false && groupIteratorStack.size() > 1)
                    {
                        if(debug)
                        {
                            System.out.println("popped group");
                        }
                        groupIteratorStack.pop();
                        depthStack.pop();                        
                    }
                }
                else
                {
                    keepCurrentDef = false;
                }
                
                if(debug)
                {
                    System.out.println("node = "+childNodeIndex+" TESTING: "+(testChild != null ? XPath.getXPath(testChild) : testChild));
                    System.out.println("pos="+definitionPathIndex+" depth="+(currentContentDefNode.getDepth()+depthOnLoopEntry) +" choiceChildLevel= "+(choiceStack.isEmpty() ? "" : choiceStack.peek().levelDepth)+" "+currentContentDefNode);
                }
                //check to see if we've just popped up to our current choice level
                if(choiceStack.isEmpty() == false && (currentContentDefNode.getDepth()+depthOnLoopEntry) == choiceStack.peek().levelDepth+1)
                {                                                            
                    //check to see if we're currently satisfied
                    if (returnDepth < 0) 
                    {                        
                        //check to see if not just the first child in a choice. if we're satisfied, we should have moved passed that point
                        if(childNodeIndex != choiceStack.peek().currentChildNodeIndex)
                        {                         
                           //if so, popup a choice level since we have nothing left to choose
                            //System.out.println("POPUP AT END OF SATISFIED CHOICE");
                            returnDepth = choiceStack.peek().levelDepth;
                            nextNoMatchDef = null;
                        }                              
                    }
                
                }
                
                //figure out if we popped out of a satisfied sequence
                if(sequenceStack.isEmpty() == false && (currentContentDefNode.getDepth()+depthOnLoopEntry) <= sequenceStack.peek().levelDepth)
                {//XXX possible error in subsequences!
                    if(debug)
                    {
                        System.out.println("popping out of seq stack: size = "+sequenceStack.size()+" sat? "+sequenceStack.peek().satified);
                    }
                    sequenceStack.pop();
                }
                
                
                //check to see if walked above the current depth of our choices by reaching the end.
                if(choiceStack.isEmpty() == false && (currentContentDefNode.getDepth()+depthOnLoopEntry) <= choiceStack.peek().levelDepth)
                {
                    //System.out.println("went so high, that we're above our last choice");
                    if(debug)
                    {
                        System.out.println("popping choice stack: size = "+choiceStack.size()+" sat? "+choiceStack.peek().satified);
                    }
                    childNodeIndex = choiceStack.pop().currentChildNodeIndex;                    
                }
                
                //going backup stack
                if(returnDepth >= 0)
                {
                    //System.out.println("going back up tree to level "+(returnDepth+1));
                    if((returnDepth+1) < (currentContentDefNode.getDepth()+depthOnLoopEntry)) //XXX might should be peek at depthStack here
                    {
                        continue; //go to next item in path
                    }
                    else
                    {
                        //System.out.println("returned to level "+(currentContentDefNode.getDepth()+depthStack.peek()));
                        returnDepth = -1; //done going backup, so reset flag
                        if(choiceStack.isEmpty() == false)
                        {
                            
                            if(testChild != null)
                            {
                                childNodeIndex = choiceStack.peek().currentChildNodeIndex;
                                testChild = (CElement) elementList.get(childNodeIndex);//XXX possibly want to reset anything global at this level
                            }
                        }
                    }
                }
                
                
                //======================START PUSHES=================
                
                if(currentContentDefNode.getLocalName().equals("group"))
                {   
                    if(currentContentDefNode.hasAttribute("ref")) //don't push the stack if this group isn't a reference, such as the next iteration where we've jump to our referenced group
                    {
                        //find model
                        CElement groupElement = (CElement) XPath.selectNSNode(currentContentDefNode.getOwnerDocument(), "//xs:group[@name = '"+currentContentDefNode.getAttribute("ref")+"']","xs="+CDocument.XML_SCHEMA_NS);
                        contentModelStream = groupElement.stream().filter(node->node.getNodeType() == Node.ELEMENT_NODE).map(node->(CElement)node);
                        //push new stream onto the stack
                        groupIteratorStack.push(((Iterable<CElement>)contentModelStream::iterator).iterator());
                        contentModelStream = null; //get rid of stream ref outside of stack to prevent code errors 
                        //push new depth multiplier onto the stack
                        depthStack.push((currentContentDefNode.getDepth()+depthStack.peek()));
                        if(debug)
                        {
                            System.out.println("pushed group: "+(currentContentDefNode.getDepth()+depthStack.peek()));
                        }
                    }
                    continue;
                }
                
                
                //if we're a sequence, we just need to move on to the next element
                if(currentContentDefNode.getLocalName().equals("sequence") && testChild != null)
                {                    
                    sequenceStack.push(new ChoiceStackItem(currentContentDefNode.getDepth()+depthStack.peek(),childNodeIndex));
                    if(debug)
                    {
                        System.out.println("pushed sequence: "+(currentContentDefNode.getDepth()+depthStack.peek()));
                    }
                    continue;
                }
                
                if(currentContentDefNode.getLocalName().equals("choice") && testChild != null)
                {
                    choiceStack.push(new ChoiceStackItem(currentContentDefNode.getDepth()+depthStack.peek(),childNodeIndex));
                    //System.out.println("pushed "+(currentContentDefNode.getDepth()+depthStack.peek()));
                    continue;
                }
                
                //=============================START TESTS=======================
                
                if(currentContentDefNode.getLocalName().equals("element") || currentContentDefNode.getLocalName().equals("any"))
                {
                    
                    if(deepestChildMatch < childNodeIndex)
                    {
                        nextNoMatchDef = currentContentDefNode;
                    }
                    
                  //do a node match test here
                    //Occurrence predicates need to be saved until satisfied or moved beyond 
                    if (predicate == null)
                    {
                        if(currentContentDefNode.getLocalName().equals("element"))
                        {    
                            predicate = new OccurancePredicate(currentContentDefNode,buildElementPredicateChain(null, currentContentDefNode));
                        }
                        else if(currentContentDefNode.getLocalName().equals("any"))
                        {
                            predicate = new OccurancePredicate(currentContentDefNode,buildAnyElementPredicateChain(null,testChild,testChild.namespaceURI, currentContentDefNode));
                        }
                    }
                   
                    switch (predicate.increment(testChild))
                    {
                        case FULL: //This should probably never happen, as the match below should always cause the next def to be processed
                            //System.err.println("FULL = "+definitionPathIndex+" "+testChild+" "+currentContentDefNode);
                            continue search_loop;
                        case NO_MATCH:
                            if(debug)
                            {
                                System.out.println("NO_MATCH = "+definitionPathIndex+" "+testChild+":"+childNodeIndex+" "+currentContentDefNode+" "+(currentContentDefNode.getDepth()+depthStack.peek()));
                            }
                            if(predicate.isSatisfied()) 
                            { //if we're satisfied, we should check the next def
                                if(debug)
                                {
                                    System.out.println("IGNORE no match, but staisfied");
                                }
                                continue search_loop;
                            }
                            else
                            {
                                //System.out.println("unstaisfied");                               
                                if(testChild == null) //this just means we walked one past to see if our sequence was satisfied
                                {                                 
                                    if(sequenceStack.isEmpty() == false)
                                    {
                                        sequenceStack.peek().satified = false;
                                        if(deepestChildMatch == childNodeIndex)
                                        {
                                            nextNoMatchDef = currentContentDefNode;
                                            nextNoMatch = null;
                                        }
                                    }
                                    break search_loop;
                                }
                            }
                            if(choiceStack.isEmpty() == false)
                            {
                                //System.out.println("no match, but in a choice");
                                //XXX along with returning to the proper depth, we also need to roll back the currently tested node to the stream position at which we started processing the choice.
                                returnDepth = choiceStack.peek().levelDepth;                                
                                continue search_loop;
                            }
                            //Wow, we are invalid!                            
                            break search_loop;
                        default:
                            if(debug)
                            {
                                System.out.println("MATCH = "+definitionPathIndex+" "+XPath.getXPath(testChild)+":"+childNodeIndex+" <===> "+XPath.getXPath(currentContentDefNode));
                            }
                            if(childNodeIndex > deepestChildMatch)
                            {
                                deepestChildMatch = childNodeIndex;
                                defNodeOfDeepestChildMatch = definitionPathIndex;                                
                                lastMatch = currentContentDefNode;
                            }                            
                            //if our predicate isn't full, then we need to repeat the same def again
                            if(predicate.isFull() == false)
                            {
                                keepCurrentDef = true; 
                            }
                            else
                            {
                                definitionPathIndex++;
                            }
                            break search_loop;
                    }    
                }
            }
        }
        
//        if(sequenceStack.isEmpty() == false)
//        {
//           // System.out.println("seq satisfied = "+sequenceStack.peek().satified);
//        }
//        System.out.println("Satisfied: "+(predicate != null ? predicate.isSatisfied() : ""));
        boolean sequenceStaisfied = sequenceStack.isEmpty();
        while(sequenceStack.isEmpty() == false)
        {
            sequenceStaisfied = sequenceStack.pop().satified;
            if(sequenceStaisfied == false)
            {
                break;
            }
        } 
        if(deepestChildMatch < elementList.size()-1 || sequenceStaisfied == false)
        {
                       
            //System.err.println("INVALID NODE: "+XPath.getXPath(testChild));
            System.out.println("Best match was for "+XPath.getXPath(elementList.get(deepestChildMatch)));
            bestMatch = elementList.get(deepestChildMatch);
            System.out.println("real invalid node was "+nextNoMatch +(nextNoMatch != null ? XPath.getXPath(nextNoMatch)+"" : ""));
            invalidNode = nextNoMatch;
            System.out.println("next Possible node is "+nextNoMatchDef +(nextNoMatchDef != null ? XPath.getXPath(nextNoMatchDef)+"" : ""));
            nextPossibleNode = nextNoMatchDef;
            valid = false;
            return false;
            
        }
        else
        {
//            System.out.println("OK!");
            valid = true;
        }
//        for (Node _childNode : node.nodeList)
//        {
//            //XXX fast-forward to current node
//            currentChildNode = (CNode) _childNode;
//            
//            if(_childNode.getNodeType() == Node.ELEMENT_NODE)
//            {
//                CElement childElement = (CElement) _childNode;
//                
//                //walk child definition elements until we run off the end or we match.
//                //consume a def node by putting it/it's rules on the stack.
//                //rules can consit of occurence and choice attempt
//                
//                
//                for (CElement  childDefinitionElement : (Iterable<CElement>)nodeDefinition.getNodeDefinitionTypeElement(NodeDefinitionType.complexType).nodeList
//                        .stream()
//                        .filter(node->node.getNodeType() == Node.ELEMENT_NODE)
//                        .map(node->(CElement)node)
//                        ::iterator)
//                {
//                    System.out.println(childDefinitionElement);
//                    switch (childDefinitionElement.getLocalName())
//                    {
//                        case "annotation":                            
//                            break;
//                        case "simpleContent":
//                            nodeInvalid("unspported definition ["+childDefinitionElement.getLocalName()+"]", node, exceptionVector);                    
//                            break;
//                        case "complexContent":
//                            nodeInvalid("unspported definition ["+childDefinitionElement.getLocalName()+"]", node, exceptionVector);
//                            break;
//                        case "group":
//                            nodeInvalid("unspported definition ["+childDefinitionElement.getLocalName()+"]", node, exceptionVector);
//                            break;
//                        case "all":
//                            //validateNodeAgainstAll(node,(CNode) childDefNode, exceptionVector);                    
//                            break;
//                        case "choice":
//                            nodeInvalid("unspported definition ["+childDefinitionElement.getLocalName()+"]", node, exceptionVector);
//                            break;
//                        case "sequence":                    
//                            //validateNodeAgainstSequence(node,(CNode) childDefNode, exceptionVector);                            
//                            break;
//                        case "attribute":
//                           // validateNodeAgainstAttribute(node,(CNode) childDefNode, exceptionVector);                     
//                            break;
//                        case "attributeGroup":
//                            nodeInvalid("unspported definition ["+childDefinitionElement.getLocalName()+"]", node, exceptionVector);
//                            break;
//                        case "anyAttribute":
//                            nodeInvalid("unspported definition ["+childDefinitionElement.getLocalName()+"]", node, exceptionVector);
//                            break;
//                        default:
//                            break;
//                    }
//                    
//                } 
//                
//            }
//        }
//        
//        
//        
//        //TODO verify/find elements and attributes NOT mentioned in schema
//        
//        for (Node attributeNode : node.attributeList)
//        {
//            if(attributeNode.getPrefix() == null && attributeNode.getLocalName().equals("xmlns"))
//            {
//                continue;
//            }
//            if(((CNode) attributeNode).getNodeDefinition() == null)
//            {
//                nodeInvalid("Attribute not allowed", (CNode) attributeNode, exceptionVector);
//            }
//        }
        return true;
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
                    if(CNodeDefinition.getDefinitionForNode(_node) == null)
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
    
    private class ChoiceStackItem
    {
        private int levelDepth;
        private int currentChildNodeIndex;
        private boolean satified = true;

        public ChoiceStackItem(int levelDepth, int currentChildNodeIndex)
        {
            this.levelDepth = levelDepth;
            this.currentChildNodeIndex = currentChildNodeIndex;
        }
    }
}
