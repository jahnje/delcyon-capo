package com.delcyon.capo.xml.cdom;

import java.util.function.Predicate;

public class OccurancePredicate
{
    
    public enum TestErrorResult
    {            
        FULL,
        NO_MATCH,
        MATCH
    }
    
    
    public int count;
    public Predicate<CElement> predicateChain;
    public int min = 1;
    public int max = 1;
    public CElement definition = null;

    @SuppressWarnings("unused")
    private OccurancePredicate(){}
    
    public OccurancePredicate(CElement def, Predicate<CElement> predicateChain)
    {
        if(def.hasAttribute("minOccurs"))
        {
            this.min = Integer.parseInt(def.getAttribute("minOccurs"));
        }
        if(def.hasAttribute("maxOccurs"))
        {
            if(def.getAttribute("maxOccurs").equals("unbounded"))
            {
                this.max = Integer.MAX_VALUE;
            }
            else
            {
                this.max = Integer.parseInt(def.getAttribute("maxOccurs"));
            }
        }            
        this.predicateChain = predicateChain;
        this.definition = def;
    }
    
    public TestErrorResult increment(CElement node)
    {
        if(node == null)
        {
            return TestErrorResult.NO_MATCH;
        }
        if(predicateChain.test(node))
        {
            if(count+1 <= max )
            {
                count++;
                return TestErrorResult.MATCH;
            }
            else
            {
                return TestErrorResult.FULL;
            }
        }
        return TestErrorResult.NO_MATCH;
    }
    
    public boolean isSatisfied()
    {
        return count >= min && count <= max;
    }
    
    public boolean isFull()
    {
        return count == max;
    }
    
    public CElement getDefinition()
    {
        return definition;
    }
}