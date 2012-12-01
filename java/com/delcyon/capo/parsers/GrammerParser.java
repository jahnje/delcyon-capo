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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author jeremiah
 *
 */
public class GrammerParser
{
    public enum SymbolType
    {
        DELIMITER,
        LITERAL,
        ASSIGNMENT,
        ALTERNATION,
        DECLARATION,
        EOL
    }
    private HashMap<String, String[]> symbolHashMap = new HashMap<String, String[]>();
    private HashMap<String, String> ruleHashMap = new HashMap<String, String>();
    private HashMap<String, SymbolType> symbolTypeHashMap = new HashMap<String, SymbolType>();
    
    public GrammerParser()
    {
        
        
        
        symbolHashMap.put(SymbolType.DELIMITER.toString(), new String[]{" ","\t","EOL"});
        symbolHashMap.put(SymbolType.LITERAL.toString(), new String[]{"\"..\"","'..'"});
        symbolHashMap.put(SymbolType.ASSIGNMENT.toString(), new String[]{"="});
        symbolHashMap.put(SymbolType.ALTERNATION.toString(), new String[]{"|"});        
        symbolHashMap.put(SymbolType.EOL.toString(), new String[]{"\n"});
        
        ruleHashMap.put("SYMBOL","SYMBOL_NAME '=' LITERAL_LIST EOL");
        ruleHashMap.put("LITERAL_LIST","LITERAL | LITERAL '|' LITERAL_LIST");
        ruleHashMap.put("SYMBOL_LIST","SYMBOL | SYMBOL SYMBOL_LIST");
        ruleHashMap.put("SYMBOLS","'Symbols:' EOL '{' SYMBOL_LIST '}' EOL");
        ruleHashMap.put("RULE","RULE_NAME ASSIGNMENT EXPRESSION EOL");
        ruleHashMap.put("RULE_LIST","RULE | RULE RULE_LIST");
        ruleHashMap.put("GRAMMER","'Grammar:' EOL '{' RULE_LIST '}' EOL");
        ruleHashMap.put("EXPRESSION","LIST | LIST ALTERATION EXPRESSION");
        ruleHashMap.put("TERM","LITERAL | RULE_NAME");
        ruleHashMap.put("LIST","TERM | TERM LIST");
        
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
            System.out.println(Arrays.toString(entry.getValue().split("[ \t]")));
            String[] expressions = entry.getValue().split("[ \t]");
            for (String expression : expressions)
            {
                if(ruleHashMap.containsKey(expression))
                {
                    System.out.println("NON-TERMINAL:\t"+expression);
                }
                else if(symbolTypeHashMap.containsKey(expression))
                {
                    System.out.println(symbolTypeHashMap.get(expression)+":\t"+expression);
                }
                else 
                {
                    System.out.println("TERMINAL:\t"+expression);
                }
            }
        }
        //constructTable
    }
    
    
    public void parse(InputStream inputStream) throws Exception
    {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        
        StreamTokenizer streamTokenizer = new StreamTokenizer(bufferedReader);
        streamTokenizer.resetSyntax();        
        streamTokenizer.wordChars(33,126);
        streamTokenizer.eolIsSignificant(true);
        setDelimiters(streamTokenizer, SymbolType.DELIMITER.toString());
        while(streamTokenizer.nextToken() != StreamTokenizer.TT_EOF)
        {
            if(streamTokenizer.ttype == StreamTokenizer.TT_EOL)
            {
                System.out.println("EOL");
            }
            else
            {   
                if(symbolTypeHashMap.containsKey(streamTokenizer.sval))
                {
                    System.out.println(streamTokenizer.sval+"\t ==>\t "+symbolTypeHashMap.get(streamTokenizer.sval));
                }
                else if(ruleHashMap.containsKey(streamTokenizer.sval))
                {
                    System.out.println(streamTokenizer.sval+"\t ==>\t RULE");
                }
                else
                {
                    System.out.println(streamTokenizer.sval+"\t ==>\t LITERAL");
                }
            }
        }
        
    }
    
    private void setDelimiters(StreamTokenizer streamTokenizer, String symbolName)
    {
        String[] delimiters = symbolHashMap.get(symbolName);
        if(delimiters == null)
        {
            return;
        }
        for (String string : delimiters)
        {
            if(string.length() == 1)
            {
                streamTokenizer.whitespaceChars(string.charAt(0), string.charAt(0));        
            }
            else if(string.length() > 1)
            {
                setDelimiters(streamTokenizer, string);
            }
        }
    }
}
