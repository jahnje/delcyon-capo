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

import java.io.FileInputStream;

import org.junit.Assert;
import org.junit.Test;

import com.delcyon.capo.parsers.Tokenizer.CharacterType;
import com.delcyon.capo.parsers.Tokenizer.TokenType;

/**
 * @author jeremiah
 *
 */
public class TokenizerTest
{

   
    @Test
    public void test() throws Exception
    {
        Tokenizer tokenizer = new Tokenizer(new FileInputStream("test-data/parser_test_data/tokenizer_test_data.txt"));
        tokenizer.setEOLSignificant(true);
        tokenizer.setCharType('"', CharacterType.QUOTE);
        tokenizer.setCharType('\'', CharacterType.ALPHA);
        tokenizer.setCharType(';', CharacterType.COMMENT);
        //tokenizer.resetSyntax();
//        tokenizer.setCharRangeType(33, 126,CharacterType.ALPHA);
//        tokenizer.eolIsSignificant(true);        
//        tokenizer.setCharType('"', CharacterType.QUOTE);
//        tokenizer.setCharType('\t', CharacterType.WHITESPACE);
//        tokenizer.setCharType(' ', CharacterType.WHITESPACE);
        tokenizer.setCharType('\\', CharacterType.ESCAPE);
        int tokenCount = 0;
        while(tokenizer.nextToken() != Tokenizer.TokenType.EOF)
        {
            tokenCount++;            
            if(tokenizer.getValue() == null)
            {
                Assert.assertEquals(TokenType.EOL, tokenizer.getTokenType());
            }
            if(tokenizer.getTokenType() == TokenType.EOL)
            {
                Assert.assertNull(tokenizer.getValue());
            }
            if(tokenizer.getTokenType() == TokenType.TOKEN)
            {
                Assert.assertNotNull(tokenizer.getValue());
            }
        }
        Assert.assertEquals(48, tokenCount);
    }

}
