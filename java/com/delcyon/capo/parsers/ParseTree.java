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

	public enum ParseOrderPreference
	{
		LEFT,
		RIGHT,
		MAX_LENGTH
	}
	
	public enum TermType
	{
		RULE,
		SYMBOL,
		DELIMITER,
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
	
	public void setNamespaceURI(String namespaceURI)
    {
        this.namespaceURI = namespaceURI;
    }
	
	public String getNamespaceURI()
    {
        return namespaceURI;
    }
	
	public void setPrefix(String prefix)
    {
        this.prefix = prefix;
    }
	
	public String getPrefix()
    {
        return prefix;
    }
	
	public boolean isIncludeLiterals()
    {
        return includeLiterals;
    }
	
	public void setIncludeLiterals(boolean includeLiterals)
    {
        this.includeLiterals = includeLiterals;
    }
	
	public boolean isAllowPartialMatch()
    {
        return allowPartialMatch;
    }
	
	public void setAllowPartialMatch(boolean allowPartialMatch)
    {
        this.allowPartialMatch = allowPartialMatch;
    }
	
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

	public Document parse(Tokenizer tokenizer) throws Exception
	{		
	    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
	    documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();		
		Document parseDocument = documentBuilder.newDocument();		
		parse(tokenizer, parseDocument);		
		return parseDocument;
		
	}
	
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

	public ParseRule getRuleNode(String term)
	{
		return parseRuleHashMap.get(term);
	}

	
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
	
	public ParseOrderPreference getParseOrderPreference()
	{
		return parseOrderPreference;
	}
	
	public void setParseOrderPreference(ParseOrderPreference parseOrderPreference)
	{
		this.parseOrderPreference = parseOrderPreference;
	}

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
