
package com.delcyon.capo.parsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;

/**
 * @author jeremiah
 *
 */
public class Tokenizer
{
    
    /**
     * These values are used to tokenize the token stream as it is read. 
     */
    public enum CharacterType
    {
      //There is a maximum of 8 possibilities, or the bitmask math won't work
        
        /** used to indicate a char that will force delimination, but will still be included as the next token **/ 
        TOKEN,
        /** set's as a white space char **/
        WHITESPACE,
        /** used to indicate a plain text char, ie. part of a token, not a complete token unto itself. **/
        ALPHA,
        /** used to indicate a quote char **/
        QUOTE,
        /** used to indicate a comment char **/
        COMMENT,        
        /** used to indicate the start of an escape sequence for an non ALPHA chars.**/
        ESCAPE
        ;
        
        /**
         * bit mask of the ControlToken
         */
        public byte mask = 0;
        
        private CharacterType()
        {
            mask = (byte) Math.floor(Math.pow(2, ordinal()-1));            
        }
        
    }
    
    public enum TokenType
    {
        NOTHING(-4),
        TOKEN(-3),
        OTHER(-2),
        EOL('\n'),
        EOF(-1);
        
        public int value;

        private TokenType(int intValue)
        {
            this.value = intValue;
        }
    }
    
    private class InternalTokenType
    {
        public TokenType tokenType = null;
        public int tokenValue = 0;
        
        public InternalTokenType(TokenType tokenType)
        {
            this.tokenType = tokenType;
            this.tokenValue = tokenType.value;
        }

        public TokenType setTokenType(TokenType tokenType, int tokenValue)
        {
            this.tokenType = tokenType;
            this.tokenValue = tokenValue;
            return tokenType;
        }
        
        public TokenType setTokenValue(int tokenValue)
        {
            this.tokenValue = tokenValue;
            for (TokenType tokenType : TokenType.values())
            {
                if(tokenType.value == tokenValue)
                {
                    this.tokenType = tokenType;
                    return tokenType;
                }
            }
            tokenType = TokenType.OTHER;
            return tokenType;
        }
    }
    
    private static final int NEED_CHAR = Integer.MAX_VALUE;
    private static final int SKIP_LF = Integer.MAX_VALUE - 1;
    private byte characterTypes[] = new byte[256];
    
    private Reader reader = null;

    private char buffer[] = new char[20];   
    private int peekChar = NEED_CHAR;
    private String value = null;
    private boolean pushedBack = false;
    private int lineNumber = 1;
    private InternalTokenType internalTokenTypeHolder = new InternalTokenType(TokenType.NOTHING);
    
    
    private boolean forceLower;
    private boolean isEOLSignificant = false;
    private boolean enableSlashSlashComments = false;
    private boolean enableSlashStarComments = false;

    public Tokenizer() 
    {
        setCharRangeType('a', 'z', CharacterType.ALPHA);
        setCharRangeType('A', 'Z', CharacterType.ALPHA);
        setCharRangeType(128 + 32, 255, CharacterType.ALPHA);
        setCharRangeType(0, ' ', CharacterType.WHITESPACE);
        setCharType('/', CharacterType.COMMENT);        
        setCharType('"', CharacterType.QUOTE);        
        setCharType('\'', CharacterType.QUOTE);
    }
    
    public Tokenizer(InputStream inputStream)
    {
        this(new BufferedReader(new InputStreamReader(inputStream)));
    }
    
    public Tokenizer(Reader reader) 
    {
        this();
        if (reader == null) {
            throw new NullPointerException();
        }
        this.reader = reader;
    }

    public void setInputStream(InputStream inputStream)
    {
        this.reader = new BufferedReader(new InputStreamReader(inputStream));
        //reset everything
        peekChar = NEED_CHAR;
        value = null;
        pushedBack = false;
        lineNumber = 1;
        internalTokenTypeHolder = new InternalTokenType(TokenType.NOTHING);
    }
    
    /**
     * Set's every char to the TOKEN control type
     */
    public void resetSyntax() 
    {
        Arrays.fill(characterTypes, CharacterType.TOKEN.mask);
    }

    public void setCharType(int character, CharacterType characterType)
    {
        characterTypes[character] |= characterType.mask;
    }
   
    public void setCharRangeType(int lowChar, int highChar, CharacterType characterType)
    {
        if (lowChar < 0)
        {
            lowChar = 0;
        }
        if (highChar >= characterTypes.length)
        {
            highChar = characterTypes.length - 1;
        }
        while (lowChar <= highChar)
        {
            characterTypes[lowChar++] |= characterType.mask;
        }
    }
    
    public void eolIsSignificant(boolean flag)
    {
        isEOLSignificant = flag;
    }

    
    public void slashStarComments(boolean flag)
    {
        enableSlashStarComments = flag;
    }

   
    public void slashSlashComments(boolean flag)
    {
        enableSlashSlashComments = flag;
    }

    
    public void lowerCaseMode(boolean fl)
    {
        forceLower = fl;
    }

    
    public TokenType getTokenType()
    {
        return internalTokenTypeHolder.tokenType;
    }
    

    public String getValue()
    {
        return value;
    }
        
    public int getLineNumber()
    {
        return lineNumber;
    }
    
    public void pushBack()
    {
        if (internalTokenTypeHolder.tokenType != TokenType.NOTHING)
        {
            pushedBack = true;
        }
    }
    
    public TokenType nextToken() throws IOException 
    {
        if (pushedBack)
        {
            pushedBack = false;
            return internalTokenTypeHolder.tokenType;
        }
        
        value = null;

        int currentChar = peekChar;
        if (currentChar < 0)
        {
            currentChar = NEED_CHAR;
        }
        if (currentChar == SKIP_LF) 
        {
            currentChar = reader.read();
            if (currentChar < 0)
            {
                return internalTokenTypeHolder.setTokenType(TokenType.EOF, currentChar);
            }
            if (currentChar == TokenType.EOL.value)
            {
                currentChar = NEED_CHAR;
            }
        }
        if (currentChar == NEED_CHAR)
        {
            currentChar = reader.read();
            if (currentChar < 0)
            {
                return internalTokenTypeHolder.setTokenType(TokenType.EOF, currentChar);
            }
        }
                
        internalTokenTypeHolder.setTokenValue(currentChar);

        /* Set peekc so that the next invocation of nextToken will read
         * another character unless peekc is reset in this invocation
         */
        peekChar = NEED_CHAR;

        int charType = currentChar < 256 ? characterTypes[currentChar] : CharacterType.ALPHA.mask;
        
        
        while ((charType & CharacterType.WHITESPACE.mask) != 0)
        {
            if (currentChar == '\r') 
            {
                lineNumber++;
                if (isEOLSignificant) 
                {
                    peekChar = SKIP_LF;
                    return internalTokenTypeHolder.setTokenType(TokenType.EOL, currentChar);
                }
                currentChar = reader.read();
                if (currentChar == TokenType.EOL.value)
                {
                    currentChar = reader.read();
                }
            } 
            else
            {
                if (currentChar == TokenType.EOL.value) 
                {
                    lineNumber++;
                    if (isEOLSignificant) 
                    {
                        return internalTokenTypeHolder.setTokenType(TokenType.EOL, currentChar);
                    }
                }
                currentChar = reader.read();
            }
            if (currentChar < 0)
            {
                return internalTokenTypeHolder.setTokenType(TokenType.EOL, currentChar);
            }
            charType = currentChar < 256 ? characterTypes[currentChar] : CharacterType.ALPHA.mask;
        }

        

        if ((charType & CharacterType.ALPHA.mask) != 0)
        {
            int i = 0;
            do 
            {
                if (i >= buffer.length)
                {
                    buffer = Arrays.copyOf(buffer, buffer.length * 2);
                }
                buffer[i++] = (char) currentChar;
                currentChar = reader.read();
                charType = currentChar < 0 ? CharacterType.WHITESPACE.mask : currentChar < 256 ? characterTypes[currentChar] : CharacterType.ALPHA.mask;
            } 
            while ((charType & (CharacterType.ALPHA.mask)) != 0);
            
            peekChar = currentChar;
            value = String.copyValueOf(buffer, 0, i);
            if (forceLower)
            {
                value = value.toLowerCase();
            }
            return internalTokenTypeHolder.setTokenType(TokenType.TOKEN, currentChar);
        }

        if ((charType & CharacterType.QUOTE.mask) != 0)
        {
            internalTokenTypeHolder.setTokenValue(currentChar);
            int bufferIndex = 0;
            /* Invariants (because \Octal needs a lookahead):
             *   (i)  c contains char value
             *   (ii) d contains the lookahead
             */
            int nextChar = reader.read();
            while (nextChar >= 0 && nextChar != internalTokenTypeHolder.tokenValue && nextChar != '\n' && nextChar != '\r') 
            {
                if (nextChar == '\\') 
                {
                    currentChar = reader.read();
                    int first = currentChar;   /* To allow \377, but not \477 */
                    if (currentChar >= '0' && currentChar <= '7') 
                    {
                        currentChar = currentChar - '0';
                        int nextNextChar = reader.read();
                        if ('0' <= nextNextChar && nextNextChar <= '7') 
                        {
                            currentChar = (currentChar << 3) + (nextNextChar - '0');
                            nextNextChar = reader.read();
                            if ('0' <= nextNextChar && nextNextChar <= '7' && first <= '3') 
                            {
                                currentChar = (currentChar << 3) + (nextNextChar - '0');
                                nextChar = reader.read();
                            }
                            else
                            {
                                nextChar = nextNextChar;
                            }
                        } 
                        else
                        {
                          nextChar = nextNextChar;
                        }
                    } 
                    else
                    {
                        switch (currentChar) 
                        {
                            case 'a':
                                currentChar = 0x7;
                                break;
                            case 'b':
                                currentChar = '\b';
                                break;
                            case 'f':
                                currentChar = 0xC;
                                break;
                            case 'n':
                                currentChar = '\n';
                                break;
                            case 'r':
                                currentChar = '\r';
                                break;
                            case 't':
                                currentChar = '\t';
                                break;
                            case 'v':
                                currentChar = 0xB;
                                break;
                        }
                        nextChar = reader.read();
                    }
                } 
                else
                {
                    currentChar = nextChar;
                    nextChar = reader.read();
                }
                if (bufferIndex >= buffer.length)
                {
                    buffer = Arrays.copyOf(buffer, buffer.length * 2);
                }
                buffer[bufferIndex++] = (char)currentChar;
            }

            /* If we broke out of the loop because we found a matching quote
             * character then arrange to read a new character next time
             * around; otherwise, save the character.
             */
            peekChar = (nextChar == internalTokenTypeHolder.tokenValue) ? NEED_CHAR : nextChar;

            value = String.copyValueOf(buffer, 0, bufferIndex);
            return internalTokenTypeHolder.tokenType;
        }

        if (currentChar == '/' && (enableSlashSlashComments || enableSlashStarComments)) {
            currentChar = reader.read();
            if (currentChar == '*' && enableSlashStarComments)
            {
                int prevc = 0;
                while ((currentChar = reader.read()) != '/' || prevc != '*') 
                {
                    if (currentChar == '\r') 
                    {
                        lineNumber++;
                        currentChar = reader.read();
                        if (currentChar == TokenType.EOL.value) 
                        {
                            currentChar = reader.read();
                        }
                    } else {
                        if (currentChar == TokenType.EOL.value) 
                        {
                            lineNumber++;
                            currentChar = reader.read();
                        }
                    }
                    if (currentChar < 0)
                    {
                        return internalTokenTypeHolder.setTokenType(TokenType.EOF, currentChar);
                    }
                    prevc = currentChar;
                }
                return nextToken();
            } 
            else if (currentChar == '/' && enableSlashSlashComments) 
            {
                while ((currentChar = reader.read()) != TokenType.EOL.value && currentChar != '\r' && currentChar >= 0);
                peekChar = currentChar;
                return nextToken();
            }
            else
            {
                /* Now see if it is still a single line comment */
                if ((characterTypes['/'] & CharacterType.COMMENT.mask) != 0) 
                {
                    while ((currentChar = reader.read()) != TokenType.EOL.value && currentChar != '\r' && currentChar >= 0);
                    peekChar = currentChar;
                    return nextToken();
                }
                else
                {
                    peekChar = currentChar;
                    return internalTokenTypeHolder.setTokenValue('/'); //XXX
                }
            }
        }

        //if were a comment, then read until EOL or EOF
        if ((charType & CharacterType.COMMENT.mask) != 0)
        {
            while ((currentChar = reader.read()) != TokenType.EOL.value && currentChar != '\r' && currentChar >= 0);
            peekChar = currentChar;
            return nextToken();
        }

        return internalTokenTypeHolder.setTokenValue(currentChar); //XXX
    }
    
    public String toString()
    {
        String ret;
        switch (internalTokenTypeHolder.tokenType) 
        {
            case EOF:
                ret = "EOF";
                break;
            case EOL:
                ret = "EOL";
                break;
            case TOKEN:
                ret = value;
                break;          
            case NOTHING:
                ret = "NOTHING";
                break;
            default: 
            {
                /*
                 * ttype is the first character of either a quoted string or
                 * is an ordinary character. ttype can definitely not be less
                 * than 0, since those are reserved values used in the previous
                 * case statements
                 */
                if (internalTokenTypeHolder.tokenValue < 256 && ((characterTypes[internalTokenTypeHolder.tokenValue] & CharacterType.QUOTE.mask) != 0))
                {
                    ret = value;
                    break;
                }

                char s[] = new char[3];
                s[0] = s[2] = '\'';
                s[1] = (char) internalTokenTypeHolder.tokenValue;
                ret = new String(s);
                break;
            }
        }
        return "Token[" + ret + "], line " + lineNumber;
    }
}
