package com.delcyon.capo.xml.cdom;

import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;
import java.util.Vector;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.w3c.dom.Node;

import com.delcyon.capo.util.ReflectionUtility;
import com.delcyon.capo.util.ToStringControl;
import com.delcyon.capo.util.ToStringControl.Control;
import com.delcyon.capo.xml.XPath;
import com.delcyon.capo.xml.cdom.CNodeDefinition.NodeDefinitionType;

public class CNodeValidator2 implements NodeValidationUtilitesFI
{
    public enum NodeValidationResult
    {
        VALID,
        INVALID_CONTENT,
        MISSING_CONTENT
    }
    
    private boolean debug = true;
    
    private Vector<Object> path = new Vector<>();
    private CElement defElement = null;
    //private CNode currentChildNode = null;
    private CNode node = null;
    private CNodeDefinition nodeDefinition = null;
    private CElement nodeDefinitionTypeElement = null;
    private Boolean valid = null;
    
    private CElement bestMatch;
    private CNode invalidNode;
    private CNode nextPossibleNode;
    private NodeValidationResult nodeValidationResult = null;

   
    
    public CNodeValidator2(CNode node, CNodeDefinition definition)
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
     
        int definitionPathIndex = 0;

        int deepestChildMatch = -1;
        int defNodeOfDeepestChildMatch = -1;

        //boolean keepNextNoMatch = false;
        CNode nextNoMatch = null;
        CNode nextNoMatchDef = null;
        CNode lastMatch = null;

        CElement localRootNodeDefinitionElement = nodeDefinition.getNodeDefinitionTypeElement(NodeDefinitionType.complexType);
        
        LinkedList<CElement> childElementList =  node.nodeList.stream().filter(node->node.getNodeType() == Node.ELEMENT_NODE).map(node->(CElement)node).collect(Collectors.toCollection(LinkedList::new));//.findFirst().orElse(null);

        //        System.out.println("M="+m);
        //System.out.println("="+a);
        if(childElementList.size() == 0)
        {
            return false;
        }

        //iterator stack for groups as well as some sort of depth multiplier
        //push def stream iterator onto stack
        Stack<GroupStackItem> groupStreamStack = new Stack<>();
        groupStreamStack.push(new GroupStackItem(localRootNodeDefinitionElement.getDepth(), localRootNodeDefinitionElement));


        //choice related info
        Stack<ModelStackItem> modelStack = new Stack<>();
        
        
        //multiple node children will use the same def predicate for matching, so keep this up here
        OccurancePredicate predicate = null; 
        boolean keepCurrentDef = false;
        int childNodeIndex = 0;
        boolean getOneExtraDef = true;


        int currentElementIndex = 0;
        int definitionIndex = 0;
        
        
        int skipDefsUntilDepth = -1;
        
        child_node_loop:
        for(; currentElementIndex < childElementList.size() && groupStreamStack.peek().contentModelStreamIterator.hasNext(); )
        {
            CElement currentChildElement = childElementList.get(currentElementIndex);
            System.out.println(XPath.getXPath(currentChildElement));
            
            //Walk definition tree
            definition_loop:
                for(;groupStreamStack.peek().contentModelStreamIterator.hasNext();)            
                {
                    
                    
                    defElement = groupStreamStack.peek().contentModelStreamIterator.next();
                    definitionIndex++;
                    
                    //virtual depth of the def node (references make this different than actual document depth)
                    int defElementVirtualDepth = defElement.getDepth()+groupStreamStack.peek().depth;

                    String depthString =  "";for(int d = 0;d<defElementVirtualDepth;d++){depthString +="\t";};
                    System.out.println("[D.idx="+definitionIndex+" D.vdpth="+defElementVirtualDepth+"]"+depthString+defElement);
                    
                    
                    //==============POP================
                    while(groupStreamStack.peek().contentModelStreamIterator.hasNext() == false && groupStreamStack.size() > 1)
                    {
                        if(debug)
                        {
                            System.out.println("\t\t\tPOPPED GROUP");
                        }
                        groupStreamStack.pop();
                    }

                    while(modelStack.isEmpty() == false && defElementVirtualDepth <= modelStack.peek().vdepth)
                    {
                        System.out.println("\t\t\tPOPPED DEF "+modelStack.peek());
                        boolean childSatisfaction = modelStack.peek().satified;
                        
                        //only roll back stuff if we aren't satisfied while going back up
                        if(childSatisfaction == false)
                        {
                            currentElementIndex = modelStack.peek().idx;
                        }
                        modelStack.pop();
                        
                        //if we've reached the top, see if we're transitioning and skipping and if so
                        if((defElementVirtualDepth <= modelStack.peek().vdepth) == false)
                        {
                            System.out.println("\t\t\tgot to top! "+modelStack.peek());
                            if(modelStack.peek().satified != childSatisfaction)
                            {
                                modelStack.peek().satified = childSatisfaction;
                                System.out.println("\t\t\tswitched satisfaction "+skipDefsUntilDepth+":"+modelStack.peek().vdepth);
                                skipDefsUntilDepth = modelStack.peek().vdepth;
                            }
                        }
                        else
                        {
                            modelStack.peek().satified = childSatisfaction;
                        }
                        
                    }
                    
                    //==============PUSH===============


                    if(defElement.getLocalName().equals("group"))
                    {   
                        if(defElement.hasAttribute("ref")) //don't push the stack if this group isn't a reference, such as the next iteration where we've jump to our referenced group
                        {
                            //find model
                            CElement groupElement = (CElement) XPath.selectNSNode(defElement.getOwnerDocument(), "//xs:group[@name = '"+defElement.getAttribute("ref")+"']","xs="+CDocument.XML_SCHEMA_NS);
                            groupStreamStack.push(new GroupStackItem(defElementVirtualDepth, groupElement));

                            if(debug)
                            {
                                System.out.println("\t\t\tPUSHED GROUP: "+(groupStreamStack.peek().depth));
                            }
                        }
                        continue;
                    }

                    //keep reading the tape until we get to the level we need.
                    //make sure that we don't put anything new on our stack until we get there. 
                    if(skipDefsUntilDepth >= 0)
                    {
                        
                        if(modelStack.isEmpty() == false && modelStack.peek().vdepth >= skipDefsUntilDepth)
                        {
                            System.out.println("\t\t\tSKIPPING DEF");                                
                            continue child_node_loop;
                        }
                        else
                        {
                            //reset skip
                            System.out.println("\t\t\tSTOPPED SKIPPING DEF");
                            skipDefsUntilDepth = -1;                            
                        }
                    }
                    
                    if(defElement.getLocalName().equals("choice"))
                    {
                        modelStack.push(new ModelStackItem(defElementVirtualDepth, currentElementIndex, defElement,false));
                        continue definition_loop;
                    }
                    
                    if(defElement.getLocalName().equals("sequence"))
                    {
                        modelStack.push(new ModelStackItem(defElementVirtualDepth, currentElementIndex, defElement,true));
                        continue definition_loop;
                    }
                    
                    
                    
                    //========================PARTICLE TEST==================

                    if(defElement.getLocalName().equals("element") || defElement.getLocalName().equals("any"))
                    {
                        if(defElement.getAttribute("name").equals(currentChildElement.nodeName) || defElement.getLocalName().equals("any"))
                        {
                            currentElementIndex++;
                            System.out.println("\t\t\tMATCH: D.idx="+definitionIndex+" D.vdpth="+defElementVirtualDepth);
                            if(modelStack.peek().satified != true) //probably satisfied a choice
                            {
                                //since we've satisfied a choice, we should run the tape until we reach a peer or parent of that choice
                                //who is the current model stack node
                                
                                skipDefsUntilDepth = modelStack.peek().vdepth;
                                System.out.println("\t\t\tSKIP DEFS UNTIL: "+skipDefsUntilDepth);
                            }
                            //else
                            {
                                modelStack.peek().satified = true;
                                continue child_node_loop;
                            }
                        }
                        else
                        {
                            System.out.println("\t\t\tNO MATCH: D.idx="+definitionIndex+" D.vdpth="+defElementVirtualDepth+"");
                            if(modelStack.peek().satified != false) //probably failed a sequence
                            {                                
                                //since we've failed a sequence, we should run the tape until we reach a peer or parent of that sequence
                                //who is the current model stack node
                                
                                skipDefsUntilDepth = modelStack.peek().vdepth;
                                System.out.println("\t\t\tSKIP DEFS UNTIL: "+skipDefsUntilDepth);
                            }
                            //else failed a choice
                            {
                                modelStack.peek().satified = false;
                                continue definition_loop;
                            }
                        }
                    }

                }
        }
        
        System.out.println();
        if(debug)
        {
            valid = modelStack.peek().satified;
            return debug;
        }

        
//        int definitionPathIndex = 0; //debuging only at the moment
//        //int childNodeIndex = 0;
//        
//        //choice related info
//        Stack<ModelStackItem> choiceStack = new Stack<>();
//        int returnDepth = -1;
//        
//        Stack<ModelStackItem> sequenceStack = new Stack<>();
//        
//        //multiple node children will use the same def predicate for matching, so keep this up here
//        OccurancePredicate predicate = null; 
//        boolean keepCurrentDef = false;
//        int childNodeIndex = 0;
//        boolean getOneExtraDef = true;
//        
//        for(; childNodeIndex < childElementList.size()|| getOneExtraDef; childNodeIndex++)
//        //for(CElement testChild : (Iterable<CElement>) node.nodeList.stream().filter(node->node.getNodeType() == Node.ELEMENT_NODE).map(node->(CElement)node)::iterator)
//        {
//            CElement testChild = null;
//            if(childNodeIndex == childElementList.size() && getOneExtraDef)
//            {                
//                getOneExtraDef = false;
//                childNodeIndex--;
//                if(groupIteratorStack.peek().hasNext() == false)
//                {
//                    //keepCurrentDef = true;
//                    //currentContentDefNode = null;
//                }
//            }
//            else
//            {
//                //childNodeIndex++;
//                //filter out no element nodes
//                if(childElementList.get(childNodeIndex).getNodeType() != Node.ELEMENT_NODE)
//                {
//                    continue;
//                }
//
//                testChild = (CElement) childElementList.get(childNodeIndex);
//                //keep next possible match 
//                if(deepestChildMatch < childNodeIndex)
//                {                
//                    nextNoMatch = testChild;                
//                }
//            }
//            
//            
//            
//            //==============START CONTENT DEF LOOP===============
//            search_loop:
//            for(;groupIteratorStack.peek().hasNext() || keepCurrentDef  ;definitionPathIndex++)            
//            {
//                Integer depthOnLoopEntry = depthStack.peek();
//                if(keepCurrentDef == false)
//                {
//                    currentContentDefNode = groupIteratorStack.peek().next();
//                    predicate = null; //we have a new def, so clear out the current occurrence predicate
//                    //if we've reached the end of our group start poping things off the group stack until we reach something valid or the end of the stack
//                    while(groupIteratorStack.peek().hasNext() == false && groupIteratorStack.size() > 1)
//                    {
//                        if(debug)
//                        {
//                            System.out.println("popped group");
//                        }
//                        groupIteratorStack.pop();
//                        depthStack.pop();                        
//                    }
//                }
//                else
//                {
//                    keepCurrentDef = false;
//                }
//                
//                if(debug)
//                {
//                    System.out.println("node = "+childNodeIndex+" TESTING: "+(testChild != null ? XPath.getXPath(testChild) : testChild));
//                    System.out.println("pos="+definitionPathIndex+" depth="+(currentContentDefNode.getDepth()+depthOnLoopEntry) +" choiceChildLevel= "+(choiceStack.isEmpty() ? "" : choiceStack.peek().levelDepth)+" "+currentContentDefNode);
//                }
//                //check to see if we've just popped up to our current choice level
//                if(choiceStack.isEmpty() == false && (currentContentDefNode.getDepth()+depthOnLoopEntry) == choiceStack.peek().levelDepth+1)
//                {                                                            
//                    //check to see if we're currently satisfied
//                    if (returnDepth < 0) 
//                    {                        
//                        //check to see if not just the first child in a choice. if we're satisfied, we should have moved passed that point
//                        if(childNodeIndex != choiceStack.peek().currentChildNodeIndex)
//                        {                         
//                           //if so, popup a choice level since we have nothing left to choose
//                            //System.out.println("POPUP AT END OF SATISFIED CHOICE");
//                            returnDepth = choiceStack.peek().levelDepth;
//                            nextNoMatchDef = null;
//                        }                              
//                    }
//                
//                }
//                
//                //figure out if we popped out of a satisfied sequence
//                if(sequenceStack.isEmpty() == false && (currentContentDefNode.getDepth()+depthOnLoopEntry) <= sequenceStack.peek().levelDepth)
//                {//XXX possible error in subsequences!
//                    if(debug)
//                    {
//                        System.out.println("popping out of seq stack: size = "+sequenceStack.size()+" sat? "+sequenceStack.peek().satified);
//                    }
//                    sequenceStack.pop();
//                }
//                
//                
//                //check to see if walked above the current depth of our choices by reaching the end.
//                if(choiceStack.isEmpty() == false && (currentContentDefNode.getDepth()+depthOnLoopEntry) <= choiceStack.peek().levelDepth)
//                {
//                    //System.out.println("went so high, that we're above our last choice");
//                    if(debug)
//                    {
//                        System.out.println("popping choice stack: size = "+choiceStack.size()+" sat? "+choiceStack.peek().satified);
//                    }
//                    childNodeIndex = choiceStack.pop().currentChildNodeIndex;                    
//                }
//                
//                //going backup stack
//                if(returnDepth >= 0)
//                {
//                    //System.out.println("going back up tree to level "+(returnDepth+1));
//                    if((returnDepth+1) < (currentContentDefNode.getDepth()+depthOnLoopEntry)) //XXX might should be peek at depthStack here
//                    {
//                        continue; //go to next item in path
//                    }
//                    else
//                    {
//                        //System.out.println("returned to level "+(currentContentDefNode.getDepth()+depthStack.peek()));
//                        returnDepth = -1; //done going backup, so reset flag
//                        if(choiceStack.isEmpty() == false)
//                        {
//                            
//                            if(testChild != null)
//                            {
//                                childNodeIndex = choiceStack.peek().currentChildNodeIndex;
//                                testChild = (CElement) childElementList.get(childNodeIndex);//XXX possibly want to reset anything global at this level
//                            }
//                        }
//                    }
//                }
//                
//                
//                //======================START PUSHES=================
//                
//                if(currentContentDefNode.getLocalName().equals("group"))
//                {   
//                    if(currentContentDefNode.hasAttribute("ref")) //don't push the stack if this group isn't a reference, such as the next iteration where we've jump to our referenced group
//                    {
//                        //find model
//                        CElement groupElement = (CElement) XPath.selectNSNode(currentContentDefNode.getOwnerDocument(), "//xs:group[@name = '"+currentContentDefNode.getAttribute("ref")+"']","xs="+CDocument.XML_SCHEMA_NS);
//                        contentModelStream = groupElement.stream().filter(node->node.getNodeType() == Node.ELEMENT_NODE).map(node->(CElement)node);
//                        //push new stream onto the stack
//                        groupIteratorStack.push(((Iterable<CElement>)contentModelStream::iterator).iterator());
//                        contentModelStream = null; //get rid of stream ref outside of stack to prevent code errors 
//                        //push new depth multiplier onto the stack
//                        depthStack.push((currentContentDefNode.getDepth()+depthStack.peek()));
//                        if(debug)
//                        {
//                            System.out.println("pushed group: "+(currentContentDefNode.getDepth()+depthStack.peek()));
//                        }
//                    }
//                    continue;
//                }
//                
//                
//                //if we're a sequence, we just need to move on to the next element
//                if(currentContentDefNode.getLocalName().equals("sequence") && testChild != null)
//                {                    
//                    sequenceStack.push(new ModelStackItem(currentContentDefNode.getDepth()+depthStack.peek(),childNodeIndex,currentContentDefNode));
//                    if(debug)
//                    {
//                        System.out.println("pushed sequence: "+(currentContentDefNode.getDepth()+depthStack.peek()));
//                    }
//                    continue;
//                }
//                
//                if(currentContentDefNode.getLocalName().equals("choice") && testChild != null)
//                {
//                    choiceStack.push(new ModelStackItem(currentContentDefNode.getDepth()+depthStack.peek(),childNodeIndex,currentContentDefNode));
//                    //System.out.println("pushed "+(currentContentDefNode.getDepth()+depthStack.peek()));
//                    continue;
//                }
//                
//                //=============================START TESTS=======================
//                
//                if(currentContentDefNode.getLocalName().equals("element") || currentContentDefNode.getLocalName().equals("any"))
//                {
//                    
//                    if(deepestChildMatch < childNodeIndex)
//                    {
//                        nextNoMatchDef = currentContentDefNode;
//                    }
//                    
//                  //do a node match test here
//                    //Occurrence predicates need to be saved until satisfied or moved beyond 
//                    if (predicate == null)
//                    {
//                        if(currentContentDefNode.getLocalName().equals("element"))
//                        {    
//                            predicate = new OccurancePredicate(currentContentDefNode,buildElementPredicateChain(null, currentContentDefNode));
//                        }
//                        else if(currentContentDefNode.getLocalName().equals("any"))
//                        {
//                            predicate = new OccurancePredicate(currentContentDefNode,buildAnyElementPredicateChain(null,testChild,testChild.namespaceURI, currentContentDefNode));
//                        }
//                    }
//                   
//                    switch (predicate.increment(testChild))
//                    {
//                        case FULL: //This should probably never happen, as the match below should always cause the next def to be processed
//                            //System.err.println("FULL = "+definitionPathIndex+" "+testChild+" "+currentContentDefNode);
//                            continue search_loop;
//                        case NO_MATCH:
//                            if(debug)
//                            {
//                                System.out.println("NO_MATCH = "+definitionPathIndex+" "+testChild+":"+childNodeIndex+" "+currentContentDefNode+" "+(currentContentDefNode.getDepth()+depthStack.peek()));
//                            }
//                            if(predicate.isSatisfied()) 
//                            { //if we're satisfied, we should check the next def
//                                if(debug)
//                                {
//                                    System.out.println("IGNORE no match, but staisfied");
//                                }
//                                continue search_loop;
//                            }
//                            else
//                            {
//                                //System.out.println("unstaisfied");                               
//                                if(testChild == null) //this just means we walked one past to see if our sequence was satisfied
//                                {                                 
//                                    if(sequenceStack.isEmpty() == false)
//                                    {
//                                        sequenceStack.peek().satified = false;
//                                        if(deepestChildMatch == childNodeIndex)
//                                        {
//                                            nextNoMatchDef = currentContentDefNode;
//                                            nextNoMatch = null;
//                                        }initialSatified
//                                    }
//                                    break search_loop;
//                                }
//                            }
//                            if(choiceStack.isEmpty() == false)
//                            {
//                                //System.out.println("no match, but in a choice");
//                                //XXX along with returning to the proper depth, we also need to roll back the currently tested node to the stream position at which we started processing the choice.
//                                returnDepth = choiceStack.peek().levelDepth;                                
//                                continue search_loop;
//                            }
//                            //Wow, we are invalid!                            
//                            break search_loop;
//                        default:
//                            if(debug)
//                            {
//                                System.out.println("MATCH = "+definitionPathIndex+" "+XPath.getXPath(testChild)+":"+childNodeIndex+" <===> "+XPath.getXPath(currentContentDefNode));
//                            }
//                            if(childNodeIndex > deepestChildMatch)
//                            {
//                                deepestChildMatch = childNodeIndex;
//                                defNodeOfDeepestChildMatch = definitionPathIndex;                                
//                                lastMatch = currentContentDefNode;
//                            }                            
//                            //if our predicate isn't full, then we need to repeat the same def again
//                            if(predicate.isFull() == false)
//                            {
//                                keepCurrentDef = true; 
//                            }
//                            else
//                            {
//                                definitionPathIndex++;
//                            }
//                            break search_loop;
//                    }    
//                }
//            }
//        }
//        
////        if(sequenceStack.isEmpty() == false)
////        {
////           // System.out.println("seq satisfied = "+sequenceStack.peek().satified);
////        }
////        System.out.println("Satisfied: "+(predicate != null ? predicate.isSatisfied() : ""));
//        boolean sequenceStaisfied = sequenceStack.isEmpty();
//        while(sequenceStack.isEmpty() == false)
//        {
//            sequenceStaisfied = sequenceStack.pop().satified;
//            if(sequenceStaisfied == false)
//            {
//                break;
//            }
//        } 
//        if(deepestChildMatch < childElementList.size()-1 || sequenceStaisfied == false)
//        {
//                       
//            //System.err.println("INVALID NODE: "+XPath.getXPath(testChild));
//            System.out.println("Best match was for "+XPath.getXPath(childElementList.get(deepestChildMatch)));
//            bestMatch = childElementList.get(deepestChildMatch);
//            System.out.println("real invalid node was "+nextNoMatch +(nextNoMatch != null ? XPath.getXPath(nextNoMatch)+"" : ""));
//            invalidNode = nextNoMatch;
//            System.out.println("next Possible node is "+nextNoMatchDef +(nextNoMatchDef != null ? XPath.getXPath(nextNoMatchDef)+"" : ""));
//            nextPossibleNode = nextNoMatchDef;
//            valid = false;
//            return false;
//            
//        }
//        else
//        {
////            System.out.println("OK!");
//            valid = true;
//        }

        return true;
    }
    
    private class GroupStackItem
    {
        /**
         * The depth at which this stream was pushed onto the stack 
         */
        private int depth = -1; 
        private Iterator<CElement> contentModelStreamIterator = null;
        /**
         * @param depth
         * @param contentModelStream
         */
        private GroupStackItem(int depth, CElement groupElement)
        {            
            this.depth = depth;
            Stream<CElement> contentModelStream = groupElement.stream().filter(node->node.getNodeType() == Node.ELEMENT_NODE).map(node->(CElement)node);
            contentModelStreamIterator = ((Iterable<CElement>)contentModelStream::iterator).iterator();            
        }
        
    }
    
    @ToStringControl(control=Control.exclude,modifiers=Modifier.FINAL)
    private class ModelStackItem
    {
        private int vdepth;
        private int idx;
        private boolean satified = true;
        private boolean initialSatisfaction = true;
        //@ToStringControl(control=Control.exclude)
        private CElement defNode;

        /**
         * 
         * @param levelDepth
         * @param currentChildNodeIndex
         * @param defNode
         * @param initialSatisfaction if initial saisfaction changes, then we need to pop back up the tree until we get to a satisfied != current test state
         * also need to pop model stack on negative depth change   
         */
        public ModelStackItem(int levelDepth, int currentChildNodeIndex, CElement defNode, boolean initialSatisfaction)
        {
            this.vdepth = levelDepth;
            this.idx = currentChildNodeIndex;
            this.defNode = defNode;
            this.satified = initialSatisfaction;
            this.initialSatisfaction = initialSatisfaction;
        }
        
        @Override
        public String toString()
        {
            return ReflectionUtility.processToString(this);
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
    
    
}
