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
package com.delcyon.capo.parsers;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.delcyon.capo.parsers.GrammerParser.SymbolType;
import com.delcyon.capo.parsers.Tokenizer.CharacterType;

/**
 * @author jeremiah
 *
 */
public class ParseTree
{

    /**
     * Used when multiple but different expression matches are found for a rule. 
     * @author jeremiah
     *
     */
	public enum ParseOrderPreference
	{
	    /**
	     * choose the farthest left matching expression
	     */
		LEFT,
		/**
		 * choose the farthest right matching expression
		 */
		RIGHT,
		/**
		 * choose the expression with the longest match, starting from the left. 
		 */
		MAX_LENGTH
	}
	
	public enum TermType
	{
	    /**
	     * indicates this term is a RULE
	     */
		RULE,
		/**
		 * indicates this term is a SYMBOL, which means it has no defined meaning.
		 */
		SYMBOL,
		/**
		 * indicates that this term is used to separate terms, generally EOL is the only one that should really show up this way.
		 */
		DELIMITER,
		/**
		 * indicates that this term is a literal, and should be used for demarcating a token list.
		 */
		LITERAL
	}
	
	private Vector<ParseRule> parseRuleVector = new Vector<ParseRule>();
	private HashMap<String, ParseRule> parseRuleHashMap = new HashMap<String, ParseRule>();
	
	private HashMap<String, String[]> symbolHashMap = new HashMap<String, String[]>();	
	private HashMap<String, SymbolType> symbolTypeHashMap = new HashMap<String, SymbolType>();
	private HashMap<String, String> literalHashMap = new HashMap<String, String>();
	
	private ParseOrderPreference parseOrderPreference = ParseOrderPreference.RIGHT;
	private boolean allowPartialMatch = false;
	private boolean includeLiterals = false;
	private String namespaceURI = null;
	private String prefix = null;
	
	/**
	 * This will set the namespace to be used for any created elements resulting from the parse.
	 * @param prefix prefix of the namespace to use.  
	 * @param namespaceURI namespaceURI to use.
	 */
	public void setNamespace(String prefix, String namespaceURI)
    {
        this.namespaceURI = namespaceURI;
        this.prefix = prefix;
    }
	
	public String getNamespaceURI()
    {
        return namespaceURI;
    }
	
	public String getPrefix()
    {
        return prefix;
    }
	
	public boolean isIncludeLiterals()
    {
        return includeLiterals;
    }
	
	/**
	 * The parse can either consume any literals it encounters, or include them in the result XML as <LITERAL VALUE=""/> elements.
	 * @param includeLiterals
	 */
	public void setIncludeLiterals(boolean includeLiterals)
    {
        this.includeLiterals = includeLiterals;
    }
	
	/**
	 * Controls whether or not we return the best match we've found, even if all of the data has not been matched. 
	 * This is handy if you want to partially parse the first part of a file, but don't care about the remainder.   
	 *  
	 *  defaults to false
	 */
	
	public void setAllowPartialMatch(boolean allowPartialMatch)
    {
        this.allowPartialMatch = allowPartialMatch;
    }
	
	public boolean isAllowPartialMatch()
    {
        return allowPartialMatch;
    }
	
	
	/**
	 * This adds a parse rule to the tree. The order that these rules are added is the order in which they will be processed. 
	 * @param parseRule
	 */
	public void addRule(ParseRule parseRule)
	{
		parseRuleVector.add(parseRule);
		parseRuleHashMap.put(parseRule.getName(), parseRule);
		//find any literals in the expression, and mark them as a literal
		String[][] expressions = parseRule.getExpressions();
		for (String[] expresssion : expressions)
		{
			for (String term : expresssion)
			{
				String[] patterns = symbolHashMap.get(SymbolType.LITERAL.toString());			
				if (patterns != null)
				{
					for (String literalPattern : patterns)
					{
						if(term.matches(literalPattern))
						{
							symbolTypeHashMap.put(term, SymbolType.LITERAL);
							literalHashMap.put(term.replaceAll(literalPattern, "$1"), term);
						}
					}
				}
			}
		}
		parseRule.setParseTree(this);
	}

	/**
	 * Given a tokenizer object, apply an parse rules, and return the resulting XML Document 
	 * @param tokenizer
	 * @return
	 * @throws Exception
	 */
	public Document parse(Tokenizer tokenizer) throws Exception
	{		
	    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
	    documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();		
		Document parseDocument = documentBuilder.newDocument();		
		parse(tokenizer, parseDocument);		
		return parseDocument;
		
	}
	
	/**
	 * Given a tokenizer, and an XML Element or Document, this will append the result of the parse rules to that node. 
	 * @param tokenizer
	 * @param node
	 * @throws Exception
	 */
	public void parse(Tokenizer tokenizer, Node node) throws Exception
    {
	    //walk the list of literals, and find any that have a lenght of 1. then make sure that, that char is treated as a separate token, and not part of a word.
	    Set<Entry<String, String>> entries =  literalHashMap.entrySet();
	    for (Entry<String, String> entry : entries)
        {
            if(entry.getKey().length() == 1)
            {
                tokenizer.setCharType(entry.getKey().charAt(0), CharacterType.TOKEN);
            }
        }
	    
        ParseTape parseTape = new ParseTape(tokenizer);        
        
        Element parseNode = createElement(node, parseRuleVector.firstElement().getName());        
        //appendChild(parseNode);
        if(parseRuleVector.firstElement().parse(parseNode,parseTape))
        {
            if(allowPartialMatch == true || parseTape.hasMore() == false)
            {
                node.appendChild(parseNode);               
            }
        }        
        
    }

	/**
	 * This returns whether or not a string in the name of a parse Rule. 
	 * @param term
	 * @return
	 */
	public boolean isRule(String term)
	{
		return parseRuleHashMap.containsKey(term);
	}

	public void setSymbolHashMap(HashMap<String, String[]> symbolHashMap)
	{
		this.symbolHashMap = symbolHashMap;
		Set<Entry<String, String[]>> symbolEntrySet = symbolHashMap.entrySet();
		for (Entry<String, String[]> entry : symbolEntrySet)
		{
			String[] symbols = entry.getValue();
			for (String symbol : symbols)
			{
				symbolTypeHashMap.put(symbol, SymbolType.valueOf(entry.getKey()));
			}
		}
	}

	/**
	 * This returns a rule for a given name.
	 * @param term
	 * @return
	 */
	public ParseRule getRule(String term)
	{
		return parseRuleHashMap.get(term);
	}

	/**
	 * this returns the TermType for a given string.
	 * @param term
	 * @return
	 */
	public TermType getTermType(String term)
	{
		if (symbolTypeHashMap.containsKey(term) )
		{
			if(symbolTypeHashMap.get(term) == SymbolType.LITERAL)
			{
				return TermType.LITERAL;
			}
			else
			{
				return TermType.DELIMITER;
			}
		}
		else if (parseRuleHashMap.containsKey(term))
		{
			return TermType.RULE;
		}
		else
		{
			String[] patterns = symbolHashMap.get(SymbolType.LITERAL.toString());			
			if (patterns != null)
			{
				for (String literalPattern : patterns)
				{
					if(term.matches(literalPattern))
					{
						return TermType.LITERAL;
					}
				}
			}
			return TermType.SYMBOL;
		}
	}
	
	/**
	 * Given a term that still has it's literal indicators around it, will find a matching pattern and use it to remove them.
	 * For example 'value' will result in value.
	 * @param term
	 * @return
	 */
	public String getLiteralValue(String term)
	{
		String[] patterns = symbolHashMap.get(SymbolType.LITERAL.toString());		
		if (patterns != null)
		{
			for (String literalPattern : patterns)
			{
				if(term.matches(literalPattern))
				{					
					//System.out.println(term + "\t ==>\t LITERAL");
					return term.replaceAll(literalPattern, "$1");
				}
			}
		}
		return term;
	}

	/**
	 * Check to see if a token value is registered as a literal
	 * @param value
	 * @return
	 */
	public boolean isLiteral(String value)
	{
	    return literalHashMap.containsKey(value);
	}
	
	
	/**
	 * Allows you to set the way the parse tree will choose when finding multiple rules that match a particular token list.
	 * @param parseOrderPreference
	 */
	public void setParseOrderPreference(ParseOrderPreference parseOrderPreference)
	{
		this.parseOrderPreference = parseOrderPreference;
	}

	public ParseOrderPreference getParseOrderPreference()
    {
        return parseOrderPreference;
    }
	
	/**
	 * Returns the symbol type of this literal, or LITERAL if there is no match.
	 * @param value
	 * @return
	 */
    public String getLiteralType(String value)
    {
        if(symbolTypeHashMap.containsKey(value))
        {
            return symbolTypeHashMap.get(value).toString();
        }
        else
        {
            return SymbolType.LITERAL.toString();
        }
    }

    public void setSymbolTypeHashMap(HashMap<String, SymbolType> symbolTypeHashMap)
    {
        this.symbolTypeHashMap = symbolTypeHashMap;        
    }

    /**
     * centralized method for creating an element where we take set namespace and prefix into account. 
     * @param someNode
     * @param name
     * @return
     */
    public Element createElement(Node someNode, String name)
    {
        Document ownerDocument = null;
        if(someNode instanceof Document)
        {
            ownerDocument = (Document) someNode;
        }
        else
        {
            ownerDocument = someNode.getOwnerDocument();
        }
        if(namespaceURI != null && prefix != null)
        {
            return ownerDocument.createElementNS(namespaceURI,prefix+":"+name);
        }
        else
        {
            return ownerDocument.createElement(name);
        }
    }
	
	
}
