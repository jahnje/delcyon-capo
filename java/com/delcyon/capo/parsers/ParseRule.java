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

import java.util.Arrays;
import java.util.Vector;

import org.w3c.dom.NodeList;

import com.delcyon.capo.parsers.ParseToken.TokenType;
import com.delcyon.capo.parsers.ParseTree.TermType;
import com.delcyon.capo.xml.cdom.CElement;
import com.delcyon.capo.xml.cdom.CNamedNodeMap;
import com.delcyon.capo.xml.cdom.CNode;

/**
 * @author jeremiah
 *
 */
public class ParseRule
{

	private String name;
	private String[][] expressions;
	private ParseTree parseTree;

	/**
	 * 
	 * @param name
	 * @param expressions each expression must be a list of terms w/o any alterations. ie each expression is a separate choice
	 */
	public ParseRule(String name, String[]... expressions)
	{
		this.name = name;
		this.expressions = expressions;		
	}

	public void setParseTree(ParseTree parseTree)
	{
		this.parseTree = parseTree;
	}

	public String getName()
	{
		return name;
	}

	public boolean parse(CElement peerParseNode, ParseTape parseTape) throws Exception
	{
		Vector<MatchItem> matchItemVector = new Vector<MatchItem>();
		boolean foundExpressionMatch = false;
		int initialTapePosition = parseTape.getPosition();
		expressions:
		for (int currentExpression = 0; currentExpression < expressions.length; currentExpression++)
		{
			if(foundExpressionMatch == true)
			{
				matchItemVector.add(new MatchItem((CElement) peerParseNode.cloneNode(true),parseTape.getPosition()));
			}
			//check to see if we need to try something else, if we're out of tape, and we have a match, then we don't
			if(parseTape.hasMore() == false && foundExpressionMatch)
			{
				break;
			}
			
			((CNode)peerParseNode).removeChildrenAll();
			//TODO clear attributes
			foundExpressionMatch = true;
			
			//backup. set list pointer to parse entry position
			parseTape.setPosition(initialTapePosition);
			
			String[] expression = expressions[currentExpression];			
			for (int currentTerm = 0; currentTerm < expression.length; currentTerm++)
			{
				String term = expression[currentTerm];
				boolean useQuantification = false;
				boolean inQuantificationLoop = false;
				int quantifier = 0;
				int minimumQuantity = 0;
				int maximumQuantity = Integer.MAX_VALUE;
				
				if(term.endsWith("+"))
				{
				    useQuantification = true;
				    inQuantificationLoop = true;
				    term = term.substring(0, term.length()-1);
				    minimumQuantity = 1;
				}
				else if(term.endsWith("?"))
                {
                    useQuantification = true;
                    inQuantificationLoop = true;
                    term = term.substring(0, term.length()-1);
                    maximumQuantity = 1;
                }
				else if(term.endsWith("*"))
				{
				    useQuantification = true;
                    inQuantificationLoop = true;
                    term = term.substring(0, term.length()-1);
                    
				}
				else if(term.matches(".+\\{\\d+\\}"))
                {
				    String originalTerm = term;
                    useQuantification = true;
                    inQuantificationLoop = true;
                    term = originalTerm.replaceFirst("(.+)\\{\\d+\\}", "$1");
                    maximumQuantity = Integer.parseInt(originalTerm.replaceFirst(".+\\{(\\d+)\\}", "$1"));
                    minimumQuantity = maximumQuantity;
                }
				else if(term.matches(".+\\{\\d*,\\d+\\}"))
                {
                    String originalTerm = term;
                    useQuantification = true;
                    inQuantificationLoop = true;
                    term = originalTerm.replaceFirst("(.+)\\{\\d*,\\d+\\}", "$1");
                    maximumQuantity = Integer.parseInt(originalTerm.replaceFirst(".+\\{\\d*,(\\d+)\\}", "$1"));
                    String minString = originalTerm.replaceFirst(".+\\{(\\d*),\\d+\\}", "$1");
                    if(minString.isEmpty() == false)
                    {
                        minimumQuantity = Integer.parseInt(minString);
                    }
                }
				do
				{
				    
				    if(parseTape.next() == null && currentTerm < expression.length-1)
				    {					
				        parseTape.pushBack();
				        foundExpressionMatch = false;
				        if(inQuantificationLoop == false)
				        {
				            continue expressions;
				        }
				    }

				    ParseToken token = parseTape.getCurrent();
				    if(token == null )
				    {
				        token = new ParseToken("EOL", TokenType.EOL);
				    }
				
				//figure out what to do with the current term
				
					
					switch (parseTree.getTermType(term))
					{
						case RULE:
							//drill down into new rule
							parseTape.pushBack();
							CElement parseNode = new CElement(parseTree.getRuleNode(term).getName());
							peerParseNode.appendChild(parseNode);
							if (parseTree.getRuleNode(term).parse(parseNode, parseTape) == false)
							{
								peerParseNode.removeChild(parseNode);
								foundExpressionMatch = false;
								if(inQuantificationLoop == false)
		                        {
		                            continue expressions;
		                        }
							}							
							break;
						case LITERAL:
							if(parseTree.getLiteralValue(term).equals(token.getValue()))
							{							    
								CElement cElement = new CElement("LITERAL");
								cElement.setAttribute(parseTree.getLiteralType(token.getValue()), token.getValue());
								peerParseNode.appendChild(cElement);
							}
							else
							{
								parseTape.pushBack();
								foundExpressionMatch = false;
								if(inQuantificationLoop == false)
		                        {
		                            continue expressions;
		                        }
							}
							break;
						case SYMBOL:
						    String value = parseTape.getCurrent().getValue();
							TermType termType = parseTree.getTermType(value);
							//check to see if this is an escaped Literal, if so, it's a symbol
							if(termType == TermType.LITERAL && parseTree.getLiteralValue(value).length() != value.length())
                            {
                                termType = TermType.SYMBOL;                                
                            }
							if(termType == TermType.DELIMITER && parseTape.getCurrent().getTokenType() == TokenType.TERM)
							{
							    termType = TermType.SYMBOL;
							}
							//delimiters should never be treated as symbols
							if(termType == TermType.DELIMITER || termType == TermType.LITERAL)
							{
							    System.err.println(parseTape.getCurrent()+"<=="+termType);
								parseTape.pushBack();
								foundExpressionMatch = false;
								if(inQuantificationLoop == false)
		                        {
		                            continue expressions;
		                        }
							}
							//overlap with RULE names should be ignored as something we're parsing can't refer to a rule name
							//overlap with Literals should be ignored as a literal can be a SYMBOL_NAME							
							else
							{
								peerParseNode.setAttribute(term, value);
							}
							break;
						case DELIMITER:
							
							if(parseTree.getTermType(token.getValue()) == TermType.DELIMITER && token.getTokenType() != TokenType.TERM)
							{
								//comsume it, and do nothing
							}
							else
							{
							    System.err.println(token+"<=="+parseTree.getTermType(token.getValue()));
								parseTape.pushBack();
								foundExpressionMatch = false;
								if(inQuantificationLoop == false)
		                        {
		                            continue expressions;
		                        }
							}
							break;
						default:
							System.err.println("unknown term:"+term);
							foundExpressionMatch = false;
							break;
					}
					
					if(useQuantification == true && inQuantificationLoop == true)
					{
					    if(foundExpressionMatch)
					    {
					        quantifier++;
					        if(quantifier > maximumQuantity)
					        {
					            foundExpressionMatch = false;
                                continue expressions;
					        }
					    }
					    else
					    {
					        inQuantificationLoop = false;
					        if(quantifier >= minimumQuantity && quantifier <= maximumQuantity)
					        {
					            foundExpressionMatch = true;
					        }
					        else
					        {
					            foundExpressionMatch = false; //redundant
					            continue expressions;
					        }
					    }
					}					
				}
				while(useQuantification == true && inQuantificationLoop == true);
			}
		}
		
		if(foundExpressionMatch == true)
		{
			matchItemVector.add(new MatchItem((CElement) peerParseNode.cloneNode(true),parseTape.getPosition()));
		}
		
		peerParseNode.removeChildrenAll();
		
		if(matchItemVector.size() > 0)
		{
			
			MatchItem matchItem = null;
			switch(parseTree.getParseOrderPreference())
			{				
				case LEFT:
					matchItem = matchItemVector.firstElement();
					break;
				case RIGHT:
					matchItem = matchItemVector.lastElement();
					break;
				case MAX_LENGTH:
					int matchItemPos = -1;
					for (int index = 0 ; index < matchItemVector.size(); index++)
					{
						if(matchItemVector.get(index).endTapePosition > matchItemPos)
						{
							matchItem = matchItemVector.get(index);
							matchItemPos = matchItem.endTapePosition;
						}
					}
					break;
			}
			parseTape.setPosition(matchItem.endTapePosition);
			NodeList childrenNodeList = matchItem.parseNode.getChildNodes();
			for(int index = 0; index < childrenNodeList.getLength();)
			{			    
				peerParseNode.appendChild(childrenNodeList.item(index));
				
			}
			peerParseNode.setAttributes((CNamedNodeMap) matchItem.parseNode.getAttributes());
			//XPath.dumpNode(peerParseNode, System.out);
			return true;
		}
		return false;
	}

	public String[][] getExpressions()
	{
		return expressions;
	}
	
	@Override
	public String toString()
	{
	   return getName()+""+Arrays.toString(expressions);
	}
	
	private class MatchItem
	{
		
		CElement parseNode = null;
		int endTapePosition = -1;

		public MatchItem(CElement parseNode, int position)
		{
			this.parseNode = parseNode;
			this.endTapePosition = position;
		}
	}
	
}
