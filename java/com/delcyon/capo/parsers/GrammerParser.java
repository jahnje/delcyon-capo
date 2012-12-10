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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.delcyon.capo.parsers.Tokenizer.CharacterType;
import com.delcyon.capo.xml.XPath;

/**
 * @author jeremiah
 */
public class GrammerParser
{
	public enum SymbolType
	{
		DELIMITER, LITERAL, ASSIGNMENT, ALTERNATION, DECLARATION, EOL,SYMBOL
	}

	private HashMap<String, String[]> symbolHashMap = new HashMap<String, String[]>();
	private HashMap<String, String> ruleHashMap = new HashMap<String, String>();
	private HashMap<String, SymbolType> symbolTypeHashMap = new HashMap<String, SymbolType>();
	private HashMap<String, String> notationHashMap = new HashMap<String, String>();
    private Vector<ParseRule> notationParseRuleVector;
    private Vector<ParseRule> grammerParseRuleVector;

	public GrammerParser()
	{

		symbolHashMap.put(SymbolType.DELIMITER.toString(), new String[] { " ", "\t", "EOL" });
		symbolHashMap.put(SymbolType.LITERAL.toString(), new String[] { "\"(.+)\"", "'(.+)'" });
		symbolHashMap.put(SymbolType.ASSIGNMENT.toString(), new String[] { "=" });
		symbolHashMap.put(SymbolType.ALTERNATION.toString(), new String[] { "|" });
		symbolHashMap.put(SymbolType.EOL.toString(), new String[] { "\n" });

//		notationHashMap.put("SYMBOL", "SYMBOL_NAME '=' LITERAL_LIST EOL");
//		notationHashMap.put("LITERAL_LIST", "LITERAL | LITERAL '|' LITERAL_LIST");
//		notationHashMap.put("SYMBOL_LIST", "SYMBOL | SYMBOL SYMBOL_LIST");
//		notationHashMap.put("SYMBOLS", "'Symbols:' EOL '{' SYMBOL_LIST '}' EOL");
//		notationHashMap.put("RULE", "RULE_NAME ASSIGNMENT EXPRESSION EOL");
//		notationHashMap.put("RULE_LIST", "RULE | RULE RULE_LIST");
//		notationHashMap.put("GRAMMER", "'Grammar:' EOL '{' RULE_LIST '}' EOL");
//		notationHashMap.put("EXPRESSION", "LIST | LIST ALTERATION EXPRESSION");
//		notationHashMap.put("TERM", "LITERAL | RULE_NAME");
//		notationHashMap.put("LIST", "TERM | TERM LIST");

		Set<Entry<String, String[]>> symbolEntrySet = symbolHashMap.entrySet();
		for (Entry<String, String[]> entry : symbolEntrySet)
		{
			String[] symbols = entry.getValue();
			for (String symbol : symbols)
			{
				symbolTypeHashMap.put(symbol, SymbolType.valueOf(entry.getKey()));
			}
		}

		Set<Entry<String, String>> ruleEntrySet = ruleHashMap.entrySet();
		for (Entry<String, String> entry : ruleEntrySet)
		{
			//System.out.println(Arrays.toString(entry.getValue().split("[ \t]")));
			String[] expressions = entry.getValue().split("[ \t]");
			for (String expression : expressions)
			{
				if (ruleHashMap.containsKey(expression))
				{
					//System.out.println("NON-TERMINAL:\t" + expression);
				}
				else if (symbolTypeHashMap.containsKey(expression))
				{
					//System.out.println(symbolTypeHashMap.get(expression) + ":\t" + expression);
				}
				else
				{
					//System.out.println("TERMINAL:\t" + expression);
				}
			}
		}
		// constructTable
	}

	public void loadSymbols(InputStream inputStream) throws Exception
	{
		

		Tokenizer streamTokenizer = new Tokenizer(inputStream);
		streamTokenizer.resetSyntax();
		streamTokenizer.setCharRangeType(33, 126,CharacterType.ALPHA);
		streamTokenizer.eolIsSignificant(true);
		setDelimiters(streamTokenizer, SymbolType.DELIMITER.toString());
		while (streamTokenizer.nextToken() != Tokenizer.TokenType.EOF)
		{
			if (streamTokenizer.getTokenType() == Tokenizer.TokenType.EOL)
			{
				System.out.println("EOL");
			}
			else
			{
				if (symbolTypeHashMap.containsKey(streamTokenizer.getValue()))
				{
					System.out.println(streamTokenizer.getValue() + "\t ==>\t " + symbolTypeHashMap.get(streamTokenizer.getValue()));
				}
				else if (ruleHashMap.containsKey(streamTokenizer.getValue()))
				{
					System.out.println(streamTokenizer.getValue() + "\t ==>\t RULE");
				}
				else
				{					
					System.out.println(streamTokenizer.getValue() + "\t ==>\t LITERAL");
				}
			}
		}

	}
	
	private Vector<ParseRule> getParseRules(Document grammarParseTree) throws Exception
	{
	    Vector<ParseRule> parseRuleVector = new Vector<ParseRule>();
	    
	    NodeList ruleList = XPath.selectNodes(grammarParseTree, "//RULE");
        for(int ruleIndex = 0; ruleIndex < ruleList.getLength(); ruleIndex++)
        {
            Element ruleElement = (Element) ruleList.item(ruleIndex);
            
            NodeList expressionNodeList =  XPath.selectNodes(ruleElement, "EXPRESSION");
            Vector<Vector<String>> expressionsVector = new Vector<Vector<String>>();
           
            
            for(int expressionIndex = 0; expressionIndex < expressionNodeList.getLength(); expressionIndex++)
            {   
                Vector<String> expressionVector = new Vector<String>();     
                NodeList termNodeList = XPath.selectNodes(expressionNodeList.item(expressionIndex), "TERM");
                
                for(int termIndex = 0; termIndex < termNodeList.getLength(); termIndex++)
                {
                    String value = ((Element) termNodeList.item(termIndex)).getAttribute("VALUE");
                    if(symbolTypeHashMap.get(value) == SymbolType.ALTERNATION)
                    {                   
                        expressionsVector.add(expressionVector);
                        expressionVector = new Vector<String>();
                        //System.err.println(symbolTypeHashMap.get(value)+"<---"+value);
                    }
                    else
                    {
                        //System.err.println(symbolTypeHashMap.get(value)+"<==="+value);
                        expressionVector.add(value);
                    }
                }
                expressionsVector.add(expressionVector);    
            }
            
            
            String[][] expressions = new String[expressionsVector.size()][];
            for(int expressionsIndex = 0 ; expressionsIndex < expressionsVector.size(); expressionsIndex++)
            {
                Vector<String> expressionVectorLocal = expressionsVector.get(expressionsIndex);
                expressions[expressionsIndex] = new String[expressionVectorLocal.size()];
                for(int termIndex = 0; termIndex < expressions[expressionsIndex].length; termIndex++)
                {
                    expressions[expressionsIndex][termIndex] = expressionVectorLocal.get(termIndex);
                }
            }
            ParseRule parseRule = new ParseRule(ruleElement.getAttribute("RULE_NAME"),expressions);
            if(parseRule.getName().equals("ALTERNATION"))
            {
                System.out.println(parseRule.getName()+"==>"+expressionsVector);
            }
            else if(parseRule.getName().equals("ASSIGNMENT"))            
            {
                System.out.println(parseRule.getName()+"==>"+expressionsVector);
            }
            else
            {
                parseRuleVector.add(parseRule);
                System.out.println(parseRule);
            }
        }
        return parseRuleVector;
	}
	
	public void loadNotationGrammer(InputStream inputStream) throws Exception
	{
	    
        
        //clear rule hashmap        
        
        

        //prepare symbol table with loaded symbols
        Tokenizer streamTokenizer = new Tokenizer(inputStream);
        streamTokenizer.resetSyntax();
        streamTokenizer.setCharRangeType(33, 126,CharacterType.ALPHA);
        streamTokenizer.eolIsSignificant(true);        
        streamTokenizer.setCharType('"', CharacterType.QUOTE);
        //streamTokenizer.quoteChar('\'');
        setDelimiters(streamTokenizer, SymbolType.DELIMITER.toString());
        
        
        ParseTree notationParseTree = loadNotationParseTree();
        notationParseTree.setSymbolHashMap(symbolHashMap);
        notationParseTree.setSymbolTypeHashMap(symbolTypeHashMap);
        Document parseDocument = notationParseTree.parse(streamTokenizer);
        XPath.dumpNode(parseDocument, System.out);
        
        notationParseRuleVector = getParseRules(parseDocument);
	}
	
	
	public void loadGrammer(InputStream inputStream) throws Exception
	{

        //prepare symbol table with loaded symbols
        Tokenizer streamTokenizer = new Tokenizer(inputStream);
        streamTokenizer.resetSyntax();
        streamTokenizer.setCharRangeType(33, 126,CharacterType.ALPHA);
        streamTokenizer.eolIsSignificant(true);        
        streamTokenizer.setCharType('"', CharacterType.QUOTE);
        //streamTokenizer.quoteChar('\'');
        setDelimiters(streamTokenizer, SymbolType.DELIMITER.toString());
        
        
        ParseTree grammerParseTree = new ParseTree();
        grammerParseTree.setSymbolHashMap(symbolHashMap);       
        for (ParseRule parseRule : notationParseRuleVector)
        {
            grammerParseTree.addRule(parseRule);
        }
        grammerParseTree.setSymbolTypeHashMap(symbolTypeHashMap);
        Document parseDocument = grammerParseTree.parse(streamTokenizer);
        XPath.dumpNode(parseDocument, System.out);
        
        grammerParseRuleVector = getParseRules(parseDocument);

	}
	
	private ParseTree loadNotationParseTree()
	{
		ParseTree parseTree = new ParseTree();
		parseTree.setSymbolHashMap(symbolHashMap);		
		ParseRule ruleListParseRule = new ParseRule("RULE_LIST",new String[]{"RULE+"});
		parseTree.addRule(ruleListParseRule);
		ParseRule ruleParseRule = new ParseRule("RULE",new String[]{"RULE_NAME","'='", "EXPRESSION","EOL"});
		parseTree.addRule(ruleParseRule);
		ParseRule expressionParseRule = new ParseRule("EXPRESSION",new String[]{"TERM+"},new String[]{"TERM+","'|'", "EXPRESSION"});
		parseTree.addRule(expressionParseRule);
		ParseRule termParseRule = new ParseRule("TERM",new String[]{"VALUE"});
		parseTree.addRule(termParseRule);

		return parseTree;
	}

	public void parse(InputStream inputStream) throws Exception
	{

        //prepare symbol table with loaded symbols
        Tokenizer streamTokenizer = new Tokenizer(inputStream);
        streamTokenizer.resetSyntax();
        streamTokenizer.setCharRangeType(33, 126,CharacterType.ALPHA);
        streamTokenizer.eolIsSignificant(true);        
        streamTokenizer.setCharType('"', CharacterType.QUOTE);
        //streamTokenizer.quoteChar('\'');
        setDelimiters(streamTokenizer, SymbolType.DELIMITER.toString());
        
        
        ParseTree grammerParseTree = new ParseTree();
        grammerParseTree.setAllowPartialMatch(true);
        grammerParseTree.setSymbolHashMap(symbolHashMap);
        
        for (ParseRule parseRule : grammerParseRuleVector)
        {
            grammerParseTree.addRule(parseRule);
        }
        Document parseDocument = grammerParseTree.parse(streamTokenizer);
        grammerParseTree.setSymbolTypeHashMap(symbolTypeHashMap);
        XPath.dumpNode(parseDocument, System.out);
        

	}

	private void setDelimiters(Tokenizer streamTokenizer, String symbolName)
	{
		String[] delimiters = symbolHashMap.get(symbolName);
		if (delimiters == null)
		{
			return;
		}
		for (String string : delimiters)
		{
			if (string.length() == 1)
			{			            
		        streamTokenizer.setCharType(string.charAt(0), CharacterType.WHITESPACE);		        
			}
			else if (string.length() > 1)
			{
				setDelimiters(streamTokenizer, string);
			}
		}
	}

	

	

	
}
