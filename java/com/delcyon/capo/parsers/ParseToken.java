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

/**
 * @author jeremiah
 *
 */
public class ParseToken
{
    public enum TokenType
    {
        EOF,
        EOL,
        WORD,
        ASSIGNMENT,
        ALTERNATION,
        SYMBOL
    }
    
    private String value = null;
    private TokenType tokenType = null;
    
    public ParseToken(String value, TokenType tokenType)
    {
        this.value = value;
        this.tokenType = tokenType;
    }

    public TokenType getTokenType()
    {
        return tokenType;
    }
    
    public String getValue()
    {
        return value;
    }
    
    @Override
    public String toString()
    {
        return new String("'"+value+"':["+tokenType+"]");
    }
}