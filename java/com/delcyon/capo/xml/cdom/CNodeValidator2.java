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
     
        ModelStackItem rootItem = null;
        Vector<ModelStackItem> possibilitVector = new Vector<>();
 
        CElement localRootNodeDefinitionElement = nodeDefinition.getNodeDefinitionTypeElement(NodeDefinitionType.complexType);
        
        LinkedList<CElement> childElementList =  node.nodeList.stream().filter(node->node.getNodeType() == Node.ELEMENT_NODE).map(node->(CElement)node).collect(Collectors.toCollection(LinkedList::new));

        //iterator stack for groups as well as some sort of depth multiplier
        //push def stream iterator onto stack
        Stack<GroupStackItem> groupStreamStack = new Stack<>();
        groupStreamStack.push(new GroupStackItem(localRootNodeDefinitionElement.getDepth(), localRootNodeDefinitionElement));

        //def related info
        Stack<ModelStackItem> modelStack = new Stack<>();
        
        int longestMatch = -1;
        
        //multiple node children will use the same def predicate for matching, so keep this up here
        OccurancePredicate predicate = null; 

        boolean getOneExtraDef = true;


        int currentElementIndex = 0;
        int definitionIndex = 0;
        
        
        int skipDefsUntilDepth = -1;
        
        CElement currentChildElement = null;
        
        //walk child element list
        child_node_loop:
        while(true)
        {            
            if((currentElementIndex < childElementList.size() && groupStreamStack.peek().contentModelStreamIterator.hasNext()) == false)
            {
                if(getOneExtraDef)
                {
                    System.out.println("+++++++++++++++++++LOOKING FOR POSSIBILITES+++++++++++++++++++++");
                    getOneExtraDef = false;     
                    currentChildElement = null;
                }
                else
                {
                    break; // finish up    
                }                
            }
            else
            {
                currentChildElement = childElementList.get(currentElementIndex);
                System.out.println(XPath.getXPath(currentChildElement));    
            }
            
            
            
            //Walk definition tree
            definition_loop:
                while(true)            
                {
                    if(groupStreamStack.peek().contentModelStreamIterator.hasNext() == false)
                    {                        
                        if(currentChildElement != null)
                        {
                            break;
                        }
                    }
                    
                    //============================GROUP POP==============================
                    
                    //pop a group reference if we've reached the end of it.
                    while(groupStreamStack.peek().contentModelStreamIterator.hasNext() == false && groupStreamStack.size() > 1)
                    {
                        //System.out.println("\t\t\tPOPPED GROUP");
                        groupStreamStack.pop();
                    }

                    
                    //pop our model stack until we reach something that isn't finished 
                    while(modelStack.isEmpty() == false && modelStack.peek().getProcessingState() == StackItemProcessingState.FINISHED)
                    {
                        boolean rollback = false;
                        if(modelStack.peek().getState() == StackItemState.FAILURE)
                        {
                            rollback = true;
                        }
                        
                        System.out.println("\t\t\tPOPPED DEF "+modelStack.peek());
                        ModelStackItem poppedItem = modelStack.pop();
                        if(modelStack.isEmpty())
                        {
                            if(currentChildElement != null)
                            {
                                //we processed all of the defs 
                                continue definition_loop;
                            }
                            else
                            {
                                //we processed everything nodes and defs
                                //cleanup any outstanding defs
                                while(groupStreamStack.peek().contentModelStreamIterator.hasNext())
                                {
                                    groupStreamStack.peek().contentModelStreamIterator.next();
                                }
                                continue child_node_loop;
                            }
                        }
                        else
                        {
                            modelStack.peek().increment(poppedItem.getState());
                            System.out.println("\t\t\tGOT DEF "+modelStack.peek());
                            if(rollback == true)
                            {
                                System.out.println("\t\t\tRE-SETTING CHILD IDX = "+modelStack.peek().childIdx);
                                if(currentChildElement == null && modelStack.peek().childIdx < currentElementIndex)
                                {
                                    System.out.println("==============STOP LOOKING FOR POSSIBILITIES=========");
                                    continue child_node_loop;
                                }  
                               // if(modelStack.peek().childIdx < currentElementIndex)
                                if(currentChildElement != null){
                                    System.out.println("==============ROLLBACK=========");
                                    currentElementIndex = modelStack.peek().childIdx;
                                    if(modelStack.peek().getProcessingState() != StackItemProcessingState.FINISHED &&modelStack.peek().getState() != StackItemState.FAILURE )
                                    {
                                        continue child_node_loop;
                                    }
                                }
                               // currentElementIndex = modelStack.peek().childIdx;
                                
                                
                            }

                            if(modelStack.peek().getProcessingState() == StackItemProcessingState.FINISHED)
                            {                                       
                                skipDefsUntilDepth = modelStack.peek().vdepth;
                                System.out.println("\t\t\tSKIPPING UNTIL "+skipDefsUntilDepth);
                            }
                        }
                        
                    }
                    
                    
                  
                  //==============================================DEF PUSH======================================
                  //get a new definition if we don't have anything on the stack, or if our current item is finished 
                    if(modelStack.isEmpty())
                    {
                        if(groupStreamStack.peek().contentModelStreamIterator.hasNext() == false)
                        {
                            continue child_node_loop;
                        }
                        defElement = groupStreamStack.peek().contentModelStreamIterator.next();
                        int defElementVirtualDepth = defElement.getDepth()+groupStreamStack.peek().depth;
                        definitionIndex++;
                        modelStack.push(new ModelStackItem(defElementVirtualDepth, currentElementIndex, defElement,definitionIndex));
                        if(rootItem == null)
                        {
                            rootItem = modelStack.peek();
                        }
                    }                    
                    //if something is finished then we need the next def, this is the only place where particles can cause a def advance
                    else if(modelStack.peek().getProcessingState() == StackItemProcessingState.FINISHED)
                    {
                        defElement = groupStreamStack.peek().contentModelStreamIterator.next();
                        int defElementVirtualDepth = defElement.getDepth()+groupStreamStack.peek().depth;
                        definitionIndex++;
                        modelStack.push(new ModelStackItem(defElementVirtualDepth, currentElementIndex, defElement,definitionIndex));                        
                    }
                  //if this isn't a particle, and it's not finished, then we need to read the next def
                    else if(modelStack.peek().isTestable() == false) 
                    {
                        defElement = groupStreamStack.peek().contentModelStreamIterator.next();
                        int defElementVirtualDepth = defElement.getDepth()+groupStreamStack.peek().depth;
                        definitionIndex++;
                        modelStack.push(new ModelStackItem(defElementVirtualDepth, currentElementIndex, defElement,definitionIndex));
                    }
                  //if we're skipping, then we need to read the next def
                    else if(skipDefsUntilDepth >= 0) 
                    {
                        defElement = groupStreamStack.peek().contentModelStreamIterator.next();
                        int defElementVirtualDepth = defElement.getDepth()+groupStreamStack.peek().depth;
                        definitionIndex++;
                        modelStack.push(new ModelStackItem(defElementVirtualDepth, currentElementIndex, defElement,definitionIndex));
                    }
                    
                    //virtual depth of the def node (references make this different than actual document depth)
                    int defElementVirtualDepth = modelStack.peek().vdepth;

                    //debugging
                    String depthString =  "";for(int d = 0;d<modelStack.peek().vdepth;d++){depthString +="\t";};
                    System.out.println("[D.idx="+definitionIndex+" D.vdpth="+modelStack.peek().vdepth+"]"+depthString+modelStack.peek().defNode);
                    
                    
                  //============================================SKIP DEFs==========================
                    
                    //keep reading the tape until we get to the level we need.
                      //make sure that we don't put anything new on our stack until we get there. 
                      if(skipDefsUntilDepth >= 0)
                      {
                          
                          if(modelStack.isEmpty() == false && modelStack.peek().vdepth > skipDefsUntilDepth )
                          {
                              
                              System.out.println("\t\t\tSKIPPING [D.idx="+definitionIndex+" D.vdpth="+modelStack.peek().vdepth+"]"+modelStack.peek().defNode);
                              modelStack.pop();
                              continue definition_loop;
                          }
                          else
                          {
                              //reset skip
                              System.out.println("\t\t\tSTOPPED SKIPPING DEFS");
                              if(currentChildElement != null)
                              {
                                  possibilitVector.clear();
                              }
                              skipDefsUntilDepth = -1;                            
                          }
                      }
                    
                    
                    //==============================================GROUP PUSH======================================


                    if(defElement.getLocalName().equals("group"))
                    {   
                        if(defElement.hasAttribute("ref")) //don't push the stack if this group isn't a reference, such as the next iteration where we've jump to our referenced group
                        {
                            //find model
                            CElement groupElement = (CElement) XPath.selectNSNode(defElement.getOwnerDocument(), "//xs:group[@name = '"+defElement.getAttribute("ref")+"']","xs="+CDocument.XML_SCHEMA_NS);
                            groupStreamStack.push(new GroupStackItem(defElementVirtualDepth, groupElement));
                            System.out.println("\t\t\tPUSHED GROUP: "+(groupStreamStack.peek().depth));
                        }
                        continue;
                    }

                    
                    
                    
                    //========================================PARTICLE TEST======================================
                    if(modelStack.peek().isTestable() == false)
                    {
                        continue definition_loop;
                    }
                    else if(currentChildElement != null) //this will be null when we're doing some last loops looking for possibilities
                    {
                        if (modelStack.peek().test(currentChildElement) == true)
                        {
                            if(currentElementIndex > longestMatch)
                            {
                                longestMatch = currentElementIndex;
                            }
                            possibilitVector.clear(); //obviously if we got a match, then we're not currently looking for possibilities, even if the last test was a failure
                            currentElementIndex++;
                            continue child_node_loop;
                        }
                        else
                        {
                            possibilitVector.add(modelStack.peek());
                            continue definition_loop;
                        }
                    }
                    else //the only way we should hit this is if currentChildElement == null on possibility loop
                    {
                        modelStack.peek().increment(StackItemState.FAILURE);
                        System.out.println(modelStack.peek());
                        possibilitVector.add(modelStack.peek());
                        continue definition_loop;
                    }
                }
        }
        
        //we finished processing, figure out why
        
        if(longestMatch+1 < childElementList.size())
        {        	
        	
        	
        	    invalidNode = childElementList.get(longestMatch+1);
        	
        	System.out.println("Processed all defs, but still have children: "+XPath.getXPath(invalidNode));
        	valid = false;
        }
        else if (groupStreamStack.peek().contentModelStreamIterator.hasNext())
        {
        	System.out.println("Processed all children, but still have defs");
        	int defElementVirtualDepth = defElement.getDepth()+groupStreamStack.peek().depth;
        	System.out.println("Current Def = "+defElement+" current depth = "+defElementVirtualDepth);
        	defElement = groupStreamStack.peek().contentModelStreamIterator.next();
        	defElementVirtualDepth = defElement.getDepth()+groupStreamStack.peek().depth;
        	System.out.println("Next Def = "+defElement+" next depth = "+defElementVirtualDepth);
        	System.out.println("Current model validity = "+modelStack.peek());
        	//if we've run out of children, set the testChild to null, and run the defs out until the end.
        	//keeping track of the last valid node, take the next element test as next possible def, 
        	//??? or the first element of each next choice if it has a parent that's not under our last
        	//figure out if we have a next sequence, or a series of choices, present first particle of each choice
        	while(modelStack.isEmpty() == false)
        	{
        	    ModelStackItem modelStackItem = modelStack.pop();
        	    System.out.println("popping mode stack: "+modelStackItem);
        	}
        	System.out.println();
        	for (ModelStackItem modelStackItem : possibilitVector)
            {
        	    System.out.println("possibility: "+modelStackItem);
            }
        	valid = false;
        }
        else
        {
            System.out.println(rootItem);    
        	valid = (rootItem.getState() == StackItemState.PASS);
        	if(valid == false)
        	{
        	    System.out.println();
        	    for (ModelStackItem modelStackItem : possibilitVector)
                {
                    System.out.println("possibility: "+modelStackItem);
                }
        	}
        }
        
        return valid;
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
    public enum StackItemState
    {
        PASS,
        FAILURE,
        UNKNOWN
    }
    
    @ToStringControl(control=Control.exclude,modifiers=Modifier.FINAL)
    public enum StackItemProcessingState
    {
        CONTINUE,
        FINISHED
    }
    
    @ToStringControl(control=Control.exclude,modifiers=Modifier.FINAL+Modifier.STATIC)
    private class ModelStackItem
    {
        
        
        private int idx;
        private int vdepth;
        //@ToStringControl(control=Control.exclude)
        private CElement defNode;
        private int childIdx;

        private int size = 0;
        
        private int minimumPasses = 0; //minimum number of required passes before we are considered satisfied 
        
        private int allowableFailures = 0; //if we exceed this then we need to set failure
        private int limit = 0; //maximum number of passes before we stop processing 
        
        private int passes = 0; //number of passes we've received
        private int failures = 0; //number of failures we've received
        
        private boolean invert = false; //this allow us to handle NAND,NOR, and NXOR
        
        private boolean testable = false;
        
       
        
        
        /**
         * 
         * @param levelDepth
         * @param currentChildNodeIndex
         * @param defNode
         * @param initialSatisfaction if initial saisfaction changes, then we need to pop back up the tree until we get to a satisfied != current test state
         * also need to pop model stack on negative depth change   
         */
        public ModelStackItem(int levelDepth, int currentChildNodeIndex, CElement defNode,int definitionIndex)
        {
            this.vdepth = levelDepth;
            this.childIdx = currentChildNodeIndex;
            this.idx = definitionIndex;
            this.defNode = defNode;

            if(defNode.getLocalName().equals("sequence"))
            {
                 
                size = defNode.getChildNodes(Node.ELEMENT_NODE).size();;
                
                allowableFailures = 0;
                limit = size;
                minimumPasses = size;                
            }            
            else if(defNode.getLocalName().equals("choice"))
            {
                
                size = defNode.getChildNodes(Node.ELEMENT_NODE).size();
                
                allowableFailures = size;
                limit = 1;
                minimumPasses = 1;                
            }
            else if(defNode.getLocalName().equals("element")) 
            {
                
                
                OccurancePredicate occurancePredicate = new OccurancePredicate(defNode, null);
                allowableFailures = 0;
                limit = occurancePredicate.max;
                size = occurancePredicate.max;
                minimumPasses = occurancePredicate.min;
                testable = true;
            }
            else if(defNode.getLocalName().equals("any")) //not using this yet.
            {                
                size = defNode.getChildNodes(Node.ELEMENT_NODE).size();
                OccurancePredicate occurancePredicate = new OccurancePredicate(defNode, null);
                allowableFailures = 0;
                limit = occurancePredicate.max;
                size = occurancePredicate.max;
                minimumPasses = occurancePredicate.min;
                testable = true;
            }
            else //this should handle any unknown elements that might show up in the stack like xs:complexType  
            {                 
                size = defNode.getChildNodes(Node.ELEMENT_NODE).size();;                
                allowableFailures = 0;
                limit = size;
                minimumPasses = size;                
            }
            
        }
        
        @ToStringControl(control=Control.include)
        public StackItemState getState()
        {
            if (passes >= minimumPasses && failures <= allowableFailures)
            {
                if(invert)//invert results to handle logical NOT
                {
                    return StackItemState.FAILURE;
                }
                return StackItemState.PASS;
            }
            else 
            {
                if(invert) //invert results to handle logical NOT
                {
                    return StackItemState.PASS;
                }
                return StackItemState.FAILURE;
            }
        }
        
        public boolean test(CElement currentChildElement)
        {
           if(currentChildElement.getLocalName().equals(defNode.getAttribute("name")) || defNode.getLocalName().equals("any"))
           {
               System.out.println("\t\t\tMATCH: D.idx="+idx+" D.vdpth="+vdepth);
               increment(StackItemState.PASS);
               return true;
           }
           else
           {
               System.out.println("\t\t\tNO MATCH: D.idx="+idx+" D.vdpth="+vdepth);
               increment(StackItemState.FAILURE);
               return false;
           }
        }

        @ToStringControl(control=Control.include)
        public StackItemProcessingState getProcessingState()
        {
            if(passes + failures >= size) //if we've processed everything, then finish
            {
                return StackItemProcessingState.FINISHED;
            }
            else if (failures > allowableFailures) //if we have too many failures, then finish
            {
                return StackItemProcessingState.FINISHED;
            }
            else if (passes >= limit) //if we have the max number of passes, then stop using this, xor is a good example, we can only have one pass, probably the only time limit doesn't equal size
            {
                return StackItemProcessingState.FINISHED;
            }
            else
            {
                return StackItemProcessingState.CONTINUE;
            }
        }
        
        public boolean isTestable()
        {
            return testable;
        }
        
        public void incrementSatisfaction()
        {            
            passes++;
        }
        
        public void incrementFailure()
        {            
            failures++;
        }
        
        public void increment(StackItemState state)
        {
            switch (state)
            {
                case FAILURE:
                    incrementFailure();
                    break;
                case PASS:
                    incrementSatisfaction();
                    break;
                default:
                    break;
            }
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
