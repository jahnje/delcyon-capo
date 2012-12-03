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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

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

	public GrammerParser()
	{

		symbolHashMap.put(SymbolType.DELIMITER.toString(), new String[] { " ", "\t", "EOL" });
		symbolHashMap.put(SymbolType.LITERAL.toString(), new String[] { "\"(.+)\"", "'(.+)'" });
//		symbolHashMap.put(SymbolType.ASSIGNMENT.toString(), new String[] { "=" });
//		symbolHashMap.put(SymbolType.ALTERNATION.toString(), new String[] { "|" });
		symbolHashMap.put(SymbolType.EOL.toString(), new String[] { "\n" });

		notationHashMap.put("SYMBOL", "SYMBOL_NAME '=' LITERAL_LIST EOL");
		notationHashMap.put("LITERAL_LIST", "LITERAL | LITERAL '|' LITERAL_LIST");
		notationHashMap.put("SYMBOL_LIST", "SYMBOL | SYMBOL SYMBOL_LIST");
		notationHashMap.put("SYMBOLS", "'Symbols:' EOL '{' SYMBOL_LIST '}' EOL");
		notationHashMap.put("RULE", "RULE_NAME ASSIGNMENT EXPRESSION EOL");
		notationHashMap.put("RULE_LIST", "RULE | RULE RULE_LIST");
		notationHashMap.put("GRAMMER", "'Grammar:' EOL '{' RULE_LIST '}' EOL");
		notationHashMap.put("EXPRESSION", "LIST | LIST ALTERATION EXPRESSION");
		notationHashMap.put("TERM", "LITERAL | RULE_NAME");
		notationHashMap.put("LIST", "TERM | TERM LIST");

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
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

		StreamTokenizer streamTokenizer = new StreamTokenizer(bufferedReader);
		streamTokenizer.resetSyntax();
		streamTokenizer.wordChars(33, 126);
		streamTokenizer.eolIsSignificant(true);
		setDelimiters(streamTokenizer, SymbolType.DELIMITER.toString());
		while (streamTokenizer.nextToken() != StreamTokenizer.TT_EOF)
		{
			if (streamTokenizer.ttype == StreamTokenizer.TT_EOL)
			{
				System.out.println("EOL");
			}
			else
			{
				if (symbolTypeHashMap.containsKey(streamTokenizer.sval))
				{
					System.out.println(streamTokenizer.sval + "\t ==>\t " + symbolTypeHashMap.get(streamTokenizer.sval));
				}
				else if (ruleHashMap.containsKey(streamTokenizer.sval))
				{
					System.out.println(streamTokenizer.sval + "\t ==>\t RULE");
				}
				else
				{					
					System.out.println(streamTokenizer.sval + "\t ==>\t LITERAL");
				}
			}
		}

	}
	
	public void loadNotationGrammer(InputStream inputStream)
	{
		// TODO Auto-generated method stub
		
	}
	
	public void loadGrammer(InputStream inputStream) throws Exception
	{
		
		
		
		//clear rule hashmap		
		ruleHashMap.clear();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

		//prepare symbol table with loaded symbols
		StreamTokenizer streamTokenizer = new StreamTokenizer(bufferedReader);
		streamTokenizer.resetSyntax();
		streamTokenizer.wordChars(33, 126);
		streamTokenizer.eolIsSignificant(true);
		streamTokenizer.quoteChar('"');
		streamTokenizer.quoteChar('\'');
		setDelimiters(streamTokenizer, SymbolType.DELIMITER.toString());
		
		
		ParseTree notationParseTree = loadNotationParseTree();
		notationParseTree.setSymbolHashMap(symbolHashMap);
		notationParseTree.parse(streamTokenizer);
		XPath.dumpNode(notationParseTree, System.out);
		
		while (streamTokenizer.nextToken() != StreamTokenizer.TT_EOF)
		{
			if (streamTokenizer.ttype == StreamTokenizer.TT_EOL)
			{
				System.out.println("EOL");
			}
			else
			{
				if (symbolTypeHashMap.containsKey(streamTokenizer.sval))
				{
					System.out.println(streamTokenizer.sval + "\t ==>\t " + symbolTypeHashMap.get(streamTokenizer.sval));
				}
				else if (notationHashMap.containsKey(streamTokenizer.sval))
				{
					System.out.println(streamTokenizer.sval + "\t ==>\t RULE");
				}
				else
				{
					String[] patterns = symbolHashMap.get(SymbolType.LITERAL.toString());
					boolean hasLiteralMatch = false;
					if (patterns != null)
					{
						for (String literalPattern : patterns)
						{
							if(streamTokenizer.sval.matches(literalPattern))
							{
								hasLiteralMatch = true;
								System.out.println(streamTokenizer.sval + "\t ==>\t LITERAL");
								break;
							}
						}
					}
					if(hasLiteralMatch == false)
					{
						System.out.println(streamTokenizer.sval + "\t ==>\t SYMBOL");
					}
				}
			}
		}

	}
	
	private ParseTree loadNotationParseTree()
	{
		ParseTree parseTree = new ParseTree();
		parseTree.setSymbolHashMap(symbolHashMap);
//		ParseRule addressParseRule = new ParseRule("ADDRESS",new String[]{"NAME", "STREET"},new String[]{"NAME", "STREET","TOWNS"});
//		ParseRule nameParseRule = new ParseRule("NAME",new String[]{"FIRST_NAME", "LAST_NAME", "'literal'", "EOL"});
//		ParseRule streetParseRule = new ParseRule("STREET",new String[]{"NUMBER", "STREET_NAME", "DESIGNATION", "EOL"});
//		//TOWN = TOWN_NAME ',' STATE ZIPCODE
//		ParseRule townsParseRule = new ParseRule("TOWNS", new String[]{"TOWN"},new String[]{"TOWN","TOWNS"});
//		ParseRule townParseRule = new ParseRule("TOWN", new String[]{"TOWN_NAME","','","STATE","ZIPCODE", "EOL"});
//		parseTree.addRule(addressParseRule);
//		parseTree.addRule(nameParseRule);
//		parseTree.addRule(streetParseRule);
//		parseTree.addRule(townsParseRule);
//		parseTree.addRule(townParseRule);

		/*		
		RULE_LIST		= RULE | RULE RULE_LIST
		RULE			= RULE_NAME ASSIGNMENT EXPRESSION EOL		
		EXPRESSION		= LIST | LIST ALTERATION EXPRESSION
		TERM			= LITERAL | RULE_NAME
		LIST			= TERM | TERM LIST
		*/
		
//		ParseRule symbolParseRule = new ParseRule("SYMBOL",new String[]{"SYMBOL_NAME","'='", "LITERAL_LIST","EOL"});
//		parseTree.addRule(symbolParseRule);
//		ParseRule lieteralListParseRule = new ParseRule("LITERAL_LIST",new String[]{"LITERAL"},new String[]{"LITERAL","'|'","LITERAL_LIST"});
//		parseTree.addRule(lieteralListParseRule);
		ParseRule ruleListParseRule = new ParseRule("RULE_LIST",new String[]{"RULE"},new String[]{"RULE","RULE_LIST"});
		parseTree.addRule(ruleListParseRule);
		ParseRule ruleParseRule = new ParseRule("RULE",new String[]{"RULE_NAME","'='", "EXPRESSION","EOL"});
		parseTree.addRule(ruleParseRule);
		ParseRule expressionParseRule = new ParseRule("EXPRESSION",new String[]{"LIST"},new String[]{"LIST","'|'", "EXPRESSION"});
		parseTree.addRule(expressionParseRule);
//		ParseRule termParseRule = new ParseRule("TERM",new String[]{"LITERAL"},new String[]{"RULE_NAME"});
//		parseTree.addRule(termParseRule);
		ParseRule listParseRule = new ParseRule("LIST",new String[]{"TERM"},new String[]{"TERM","LIST"});
		parseTree.addRule(listParseRule);
		return parseTree;
	}

	public void parse(InputStream inputStream) throws Exception
	{
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

		StreamTokenizer streamTokenizer = new StreamTokenizer(bufferedReader);
		streamTokenizer.resetSyntax();
		streamTokenizer.wordChars(33, 126);
		streamTokenizer.eolIsSignificant(true);
		setDelimiters(streamTokenizer, SymbolType.DELIMITER.toString());
		while (streamTokenizer.nextToken() != StreamTokenizer.TT_EOF)
		{
			if (streamTokenizer.ttype == StreamTokenizer.TT_EOL)
			{
				System.out.println("EOL");
			}
			else
			{
				if (symbolTypeHashMap.containsKey(streamTokenizer.sval))
				{
					System.out.println(streamTokenizer.sval + "\t ==>\t " + symbolTypeHashMap.get(streamTokenizer.sval));
				}
				else if (ruleHashMap.containsKey(streamTokenizer.sval))
				{
					System.out.println(streamTokenizer.sval + "\t ==>\t RULE");
				}
				else
				{
					System.out.println(streamTokenizer.sval + "\t ==>\t LITERAL");
				}
			}
		}

	}

	private void setDelimiters(StreamTokenizer streamTokenizer, String symbolName)
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
				streamTokenizer.whitespaceChars(string.charAt(0), string.charAt(0));
			}
			else if (string.length() > 1)
			{
				setDelimiters(streamTokenizer, string);
			}
		}
	}

	

	

	
}
