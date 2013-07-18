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
public class GrammarParser
{
	public enum SymbolType
	{
	    /** used to specify WHITESPACE in the tokenizer **/
		DELIMITER,
		/** used to specify a regex replacement pattern to identify LITERALS and strip them of their indicator chars **/  
		LITERAL, 
		ASSIGNMENT, 
		ALTERNATION, 
		DECLARATION, 
		EOL,
		SYMBOL
	}

	
	private HashMap<SymbolType, String[]> symbolHashMap = new HashMap<SymbolType, String[]>();
	
	//symbol types are only used in the setDelimter code
	private HashMap<String, SymbolType> symbolTypeHashMap = new HashMap<String, SymbolType>();
	
	
    private Vector<ParseRule> notationParseRuleVector; //this is used to parse and understand a grammar.
    private Vector<ParseRule> grammerParseRuleVector; //this set of rules is used to parse input based on a grammar.

    private String prefix;

    private String uri;

	public GrammarParser()
	{

		symbolHashMap.put(SymbolType.DELIMITER, new String[] { " ", "\t", "EOL" });
		symbolHashMap.put(SymbolType.LITERAL, new String[] {  "'(.+)'" });
		//symbolHashMap.put(SymbolType.LITERAL.toString(), new String[] { "\"(.+)\"", "'(.+)'" });
//		symbolHashMap.put(SymbolType.ASSIGNMENT, new String[] { "=" });
//		symbolHashMap.put(SymbolType.ALTERNATION, new String[] { "|" });
//		symbolHashMap.put(SymbolType.EOL, new String[] { "\n" });

		Set<Entry<SymbolType, String[]>> symbolEntrySet = symbolHashMap.entrySet();
		for (Entry<SymbolType, String[]> entry : symbolEntrySet)
		{
			String[] symbols = entry.getValue();
			for (String symbol : symbols)
			{
				symbolTypeHashMap.put(symbol, entry.getKey());
			}
		}
		
	}

	
	private ParseTree loadDefaultNotationParseTree()
    {
        ParseTree parseTree = new ParseTree();
        parseTree.setSymbolHashMap(symbolHashMap);      
        ParseRule ruleListParseRule = new ParseRule("RULE_LIST",new String[]{"RULE+"});
        parseTree.addRule(ruleListParseRule);
        ParseRule ruleParseRule = new ParseRule("RULE",new String[]{"RULE_NAME","'='", "EXPRESSION+","EOL"});
        parseTree.addRule(ruleParseRule);
        ParseRule expressionParseRule = new ParseRule("EXPRESSION",new String[]{"TERM+"},new String[]{"'|'", "TERM+"});
        parseTree.addRule(expressionParseRule);
        ParseRule termParseRule = new ParseRule("TERM",new String[]{"VALUE"});
        parseTree.addRule(termParseRule);

        return parseTree;
    }
	
	public void loadNotationGrammer(InputStream inputStream) throws Exception
	{

        //prepare symbol table with loaded symbols
        Tokenizer streamTokenizer = new Tokenizer(inputStream);
        streamTokenizer.resetSyntax();
        streamTokenizer.setCharRangeType(33, 126,CharacterType.ALPHA);
                
        streamTokenizer.setCharType('"', CharacterType.QUOTE);
        streamTokenizer.setCharType('\\', CharacterType.ESCAPE);
        streamTokenizer.setCharType('\n', CharacterType.EOL);
        streamTokenizer.setCharType('\r', CharacterType.EOL);
        setDelimiters(streamTokenizer, SymbolType.DELIMITER);
        
        
        ParseTree notationParseTree = loadDefaultNotationParseTree();
        notationParseTree.setSymbolHashMap(symbolHashMap);
        notationParseTree.setUseLiteralsAsTokens(false);
        //notationParseTree.setSymbolTypeHashMap(symbolTypeHashMap);
        Document parseDocument = notationParseTree.parse(streamTokenizer);
        //XPath.dumpNode(parseDocument, System.out);
        
        notationParseRuleVector = getParseRules(parseDocument);
	}
	
	
	public void loadGrammer(InputStream inputStream) throws Exception
	{

        //prepare symbol table with loaded symbols
        Tokenizer streamTokenizer = new Tokenizer(inputStream);
        streamTokenizer.resetSyntax();
        streamTokenizer.setCharRangeType(33, 126,CharacterType.ALPHA);                
        streamTokenizer.setCharType('"', CharacterType.QUOTE);
        streamTokenizer.setCharType('\\', CharacterType.ESCAPE);
        streamTokenizer.setCharType('\n', CharacterType.EOL);
        streamTokenizer.setCharType('\r', CharacterType.EOL);
        setDelimiters(streamTokenizer, SymbolType.DELIMITER);
        
        ParseTree grammerParseTree = null;
        
        if(notationParseRuleVector != null)
        {
            grammerParseTree = new ParseTree();                   
            for (ParseRule parseRule : notationParseRuleVector)
            {
                grammerParseTree.addRule(parseRule);
            }
            
        }
        else
        {
            grammerParseTree = loadDefaultNotationParseTree();            
        }
        
        grammerParseTree.setSymbolHashMap(symbolHashMap);
        grammerParseTree.setUseLiteralsAsTokens(false);
        //grammerParseTree.setSymbolTypeHashMap(symbolTypeHashMap);
        Document parseDocument = grammerParseTree.parse(streamTokenizer);
        
        //XPath.dumpNode(parseDocument, System.out);        
        grammerParseRuleVector = getParseRules(parseDocument);

	}
	
	

	public Document parse(InputStream inputStream) throws Exception
	{

        //prepare symbol table with loaded symbols
        Tokenizer streamTokenizer = new Tokenizer(inputStream);
        streamTokenizer.resetSyntax();
        streamTokenizer.setCharRangeType(33, 126,CharacterType.ALPHA);
        streamTokenizer.setCharType('\n', CharacterType.EOL);
        streamTokenizer.setCharType('\r', CharacterType.EOL);
        //streamTokenizer.setCharType('"', CharacterType.QUOTE);
        //streamTokenizer.quoteChar('\'');
        setDelimiters(streamTokenizer, SymbolType.DELIMITER);
        
        
        ParseTree inputParseTree = new ParseTree();        
        inputParseTree.setNamespace(prefix,uri);
        inputParseTree.setAllowPartialMatch(true);
        inputParseTree.setSymbolHashMap(symbolHashMap);
        inputParseTree.setUseLiteralsAsTokens(true);
        for (ParseRule parseRule : grammerParseRuleVector)
        {
            inputParseTree.addRule(parseRule);
        }
        Document parseDocument = inputParseTree.parse(streamTokenizer);
       
       // XPath.dumpNode(parseDocument, System.out);
        
        return parseDocument;

	}

	private void setDelimiters(Tokenizer streamTokenizer, SymbolType symbolName)
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
				setDelimiters(streamTokenizer, SymbolType.valueOf(string));
			}
		}
	}
	
	/**
     * This returns a set of rules that represent a grammar. 
     * @param ruleDocument
     * @return
     * @throws Exception
     */
    private Vector<ParseRule> getParseRules(Document ruleDocument) throws Exception
    {
        Vector<ParseRule> parseRuleVector = new Vector<ParseRule>();
        
        NodeList ruleList = XPath.selectNodes(ruleDocument, "//RULE");
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
                //System.out.println(parseRule);
            }
        }
        return parseRuleVector;
    }


    public void setNamespace(String prefix, String uri)
    {
        this.prefix = prefix;
        this.uri = uri;
    }
}
