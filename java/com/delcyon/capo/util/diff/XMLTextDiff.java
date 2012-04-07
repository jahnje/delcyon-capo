/**
Copyright (c) 2011 Delcyon, Inc.
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
package com.delcyon.capo.util.diff;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.delcyon.capo.util.XMLSerializer;
import com.delcyon.capo.util.diff.Diff.Side;
import com.delcyon.capo.util.diff.InputStreamTokenizer.TokenList;
import com.delcyon.capo.xml.XMLDiff;
import com.delcyon.capo.xml.XPath;

/**
 * @author jeremiah
 * THIS IS ONLY FOR THE TEXT CONTENT OF ELEMENTS.
 * If you want to compare two XML trees Use XMLDiff
 * Used to format a DiffEntry stream into a difference Element.
 * Used to get one side or another of a difference element. 
 * Used to get one side or another of an element given that element, and it's corresponding difference element.
 */

public class XMLTextDiff
{

	public static final String DIFF_ELEMENT_NAME = XMLDiff.XDIFF_PREFIX+":text";
	public static final String DIFF_ENTRY_ELEMENT_NAME = XMLDiff.XDIFF_PREFIX+":diff";
	public static final String SIDE_ATTRIBUTE_NAME = XMLDiff.XDIFF_PREFIX+":diff";
	
	private transient DocumentBuilder documentBuilder;
	private ArrayList<ArrayList<Integer>> tokenLists = null;
	private TokenList tokenList = null;
	private transient XMLSerializer xmlSerializer;

	public XMLTextDiff() throws Exception
	{
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		this.documentBuilder = documentBuilderFactory.newDocumentBuilder();
		xmlSerializer = new XMLSerializer();
		xmlSerializer.setNamespace(XMLDiff.XDIFF_PREFIX,XMLDiff.XDIFF_NAMESPACE_URI);
	}
	
	/**
	 * Given a difference element that includes the non-differences, it will return the specified side.
	 * @param differenceElement
	 * @param side
	 * @return
	 * @throws Exception
	 */
	public Text getTextForSide(Element differenceElement, Side side) throws Exception
	{
		
		StringBuilder stringBuilder = new StringBuilder();
		
		NodeList nodeList = XPath.selectNSNodes(differenceElement, DIFF_ENTRY_ELEMENT_NAME+"[@"+SIDE_ATTRIBUTE_NAME+" = '"+side+"' or @"+SIDE_ATTRIBUTE_NAME+" = '"+Side.BASE +" = "+ Side.MOD+"']","xdiff="+XMLDiff.XDIFF_NAMESPACE_URI);
		for (int nodeIndex = 0; nodeIndex < nodeList.getLength(); nodeIndex++)
		{
			Element chunkElement = (Element) nodeList.item(nodeIndex);
			stringBuilder.append(chunkElement.getTextContent());
			
		}
		
		return differenceElement.getOwnerDocument().createTextNode(stringBuilder.toString());
	}
	
	
	/**
	 * Given two String will create a difference element for the two Strings.
	 * @param baseText
	 * @param otherText
	 * @return
	 * @throws Exception
	 */
	public Element getDifferenceElement(String baseText, String otherText) throws Exception
	{
		Diff diff = new Diff(baseText, otherText);
		return getDifferenceElement(diff);
	}
	
	/**
	 * Given two textNodes will create a difference element for the textual content of the two nodes.
	 * The text differencing will default to TokenList.NEW_LINE 
	 * @param baseText
	 * @param otherText
	 * @return
	 * @throws Exception
	 */
	public Element getDifferenceElement(Text baseText, Text otherText) throws Exception
	{
		return getDifferenceElement(baseText, otherText, TokenList.NEW_LINE, null);		
	}
	
	/**
	 * Given two textNodes will create a difference element for the textual content of the two nodes.
	 * @param baseText
	 * @param otherText
	 * @param tokenList 
	 * @param tokenLists can be null if tokenList isn't equal to CUSTOM
	 * @return
	 * @throws Exception
	 */
	public Element getDifferenceElement(Text baseText, Text otherText, TokenList tokenList, ArrayList<ArrayList<Integer>> tokenLists) throws Exception
	{
		Diff diff = null;
		if (tokenList == TokenList.CUSTOM)
		{
			diff = new Diff(baseText.getTextContent(), otherText.getTextContent(),tokenLists);
		}
		else
		{
			diff = new Diff(baseText.getTextContent(), otherText.getTextContent(),tokenList);
		}
		
		return getDifferenceElement(diff);
	}
	
	/**
	 * Will get the difference element for a diff object
	 * @param diff
	 * @return
	 * @throws Exception
	 */
	public Element getDifferenceElement(Diff diff) throws Exception
	{
		Document document = documentBuilder.newDocument();
		Element diffRootElement = document.createElementNS(XMLDiff.XDIFF_NAMESPACE_URI,DIFF_ELEMENT_NAME);
		document.appendChild(diffRootElement);
		
		long baseStartPosition = 1l;
		long baseStopPosition = 1l;
		long otherStartPosition = 1l;
		long otherStopPosition = 1l;
	
		
		
		InputStreamTokenizer inputStreamTokenizer = null;
		if (diff.getTokenList() == TokenList.CUSTOM)
		{
			this.tokenList = diff.getTokenList();
			tokenLists = diff.getTokenLists();
			inputStreamTokenizer = new InputStreamTokenizer(diff.getDifferencesAsBytes(), tokenLists);		
			xmlSerializer.export(this, diffRootElement, 0);	 		
		}
		else
		{
			this.tokenList = diff.getTokenList();
			inputStreamTokenizer = new InputStreamTokenizer(diff.getDifferencesAsBytes(), tokenList);
			xmlSerializer.export(this, diffRootElement, 0);
		}
		
		
		
		byte[] buffer = null;
		Side previousSide = null;
		String nodeText = "";
		
		//this deals with the special case of data that doesn't end with a line break of some sort. 
		boolean skipRead = false;
		
		while(true)
		{
			
			if (skipRead == false)
			{
				buffer = inputStreamTokenizer.readBytes();
			}
			else
			{
				skipRead = false;
			}
			
			if (buffer.length == 0)
			{
				addDiffEntryElement(nodeText, document, previousSide,baseStartPosition,baseStopPosition,otherStartPosition,otherStopPosition);
				break;
			}
			else
			{
				DiffEntry diffEntry = DiffEntry.parseLineData(buffer);
				String text = new String(diffEntry.getData());
				
				/*
				 * The end of files can be tricky be cause they don't have to end in a line delimiter. 
				 * So if we get a line that's longer than what we expect, we should save it for the next go around, and use it as a line.
				 * The other option is to append something onto the end of the stream, but this seems like it could have strange consequences 
				 */
				if (text.length() != diffEntry.getExpectedTextLength())
				{
					text = text.substring(0,diffEntry.getExpectedTextLength());
					buffer = Arrays.copyOfRange(diffEntry.getData(),diffEntry.getExpectedTextLength(),diffEntry.getData().length);					
					skipRead = true;
				}
				
				char directionChar = diffEntry.getDirectionChar();
				Side currentSide = null;
				if (directionChar == '+')
				{
					currentSide = Side.MOD;
				}
				else if (directionChar == '-')
				{
					currentSide = Side.BASE;
				}
				
				//we've switched sides, so write the chunk out 
				if (currentSide != previousSide)
				{
					addDiffEntryElement(nodeText, document, previousSide,baseStartPosition,baseStopPosition,otherStartPosition,otherStopPosition);
					
					//store all of the current values for the next chunk 
					baseStartPosition = diffEntry.getBaseStreamPosition();
					baseStopPosition = diffEntry.getBaseStreamPosition();
					otherStartPosition = diffEntry.getOtherStreamPosition();
					otherStopPosition = diffEntry.getOtherStreamPosition();
					nodeText = text;
					
				}
				else
				{
					nodeText = nodeText+text;
					baseStopPosition = diffEntry.getBaseStreamPosition();
					otherStopPosition = diffEntry.getOtherStreamPosition();
				}
				previousSide = currentSide;
			}
			
		}
		return document.getDocumentElement();
	}
	
	/**
	 * Converts a DifferenceEntry into a DifferenceEntry Element
	 * @param nodeText
	 * @param document
	 * @param previousSide
	 * @param baseStartPosition
	 * @param baseStopPosition
	 * @param otherStartPosition
	 * @param otherStopPosition
	 */
	private void addDiffEntryElement(String nodeText, Document document, Side previousSide, long baseStartPosition, long baseStopPosition, long otherStartPosition, long otherStopPosition)
	{
		Element diffElement = document.createElementNS(XMLDiff.XDIFF_NAMESPACE_URI,DIFF_ENTRY_ELEMENT_NAME);
		diffElement.appendChild(document.createTextNode(nodeText));					
		if (previousSide != null)
		{
			diffElement.setAttributeNS(XMLDiff.XDIFF_NAMESPACE_URI,SIDE_ATTRIBUTE_NAME, previousSide.toString());
		}
		else
		{
			diffElement.setAttributeNS(XMLDiff.XDIFF_NAMESPACE_URI,SIDE_ATTRIBUTE_NAME, Side.BASE +" = "+ Side.MOD);
		}
		diffElement.setAttributeNS(XMLDiff.XDIFF_NAMESPACE_URI,XMLDiff.XDIFF_PREFIX+":"+Side.BASE.toString().toLowerCase()+"Start", baseStartPosition+"");
		diffElement.setAttributeNS(XMLDiff.XDIFF_NAMESPACE_URI,XMLDiff.XDIFF_PREFIX+":"+Side.BASE.toString().toLowerCase()+"Stop", baseStopPosition+"");
		diffElement.setAttributeNS(XMLDiff.XDIFF_NAMESPACE_URI,XMLDiff.XDIFF_PREFIX+":"+Side.MOD.toString().toLowerCase()+"Start", otherStartPosition+"");
		diffElement.setAttributeNS(XMLDiff.XDIFF_NAMESPACE_URI,XMLDiff.XDIFF_PREFIX+":"+Side.MOD.toString().toLowerCase()+"Stop", otherStopPosition+"");
		if (nodeText.length() != 0)
		{
			document.getDocumentElement().appendChild(diffElement);
		}
		
	}

	/**
	 * Same at other, only takes Text nodes, and returns a TextNode
	 * @param differenceElement
	 * @param side
	 * @param sideText
	 * @return
	 * @throws Exception
	 */
	public Text getAlternateSideText(Element differenceElement, Side side, Text sideText) throws Exception
	{
		
		return differenceElement.getOwnerDocument().createTextNode(getAlternateSideText(differenceElement,side, sideText.getTextContent()));
	}
	
	/**
	 * 
 	 * @param differenceElement (only has to contain differences, doesn't need similarities) 
	 * @param Side this tell the processor witch side the sideText belongs to.  
	 * @param sideText the text corresponding to the side parameter 
	 * @return a String corresponding to the text that's opposite of the Side parameter
	 * For example if the side and side Text are the original, the the resultant Text will be the modified side 
	 * @throws Exception 
	 */
	public String getAlternateSideText(Element differenceElement, Side side, String sideText) throws Exception
	{
		String inSideAttributePrefix = XMLDiff.XDIFF_PREFIX+":"+Side.BASE.toString().toLowerCase();
		
		//flip the document names around				
		if (side == Side.MOD)
		{		
			inSideAttributePrefix = XMLDiff.XDIFF_PREFIX+":"+Side.MOD.toString().toLowerCase();			
		}
		
		xmlSerializer.marshall(differenceElement, this);
		
		InputStreamTokenizer inputStreamTokenizer = null;
		if (tokenList == TokenList.CUSTOM)
		{
			inputStreamTokenizer = new InputStreamTokenizer(new ByteArrayInputStream(sideText.getBytes()), tokenLists);
		}
		else
		{
			inputStreamTokenizer = new InputStreamTokenizer(new ByteArrayInputStream(sideText.getBytes()), tokenList);
		}
		
		StringBuilder stringBuilder = new StringBuilder();
		long currentLine = 1l;
		//don't care about where things are the same since we have the original document, and we might not have the BOTH stuff in the diff
		NodeList nodeList = XPath.selectNSNodes(differenceElement, "//"+DIFF_ENTRY_ELEMENT_NAME+"[@"+SIDE_ATTRIBUTE_NAME+" = '"+Side.BASE+"' or @"+SIDE_ATTRIBUTE_NAME+" = '"+Side.MOD+"']","xdiff="+XMLDiff.XDIFF_NAMESPACE_URI);
		for (int nodeIndex = 0; nodeIndex < nodeList.getLength(); nodeIndex++)
		{
			Element diffEntryElement = (Element) nodeList.item(nodeIndex);
			
			Side chunkSide = Side.valueOf(diffEntryElement.getAttribute(SIDE_ATTRIBUTE_NAME));
			long inSideStartLine = Long.parseLong(diffEntryElement.getAttribute(inSideAttributePrefix+"Start"));
			while (currentLine < inSideStartLine)
			{
				stringBuilder.append(new String(inputStreamTokenizer.readBytes()));
				currentLine++;
			}
			if (chunkSide == side)
			{
				long inSideStoptLine = Long.parseLong(diffEntryElement.getAttribute(inSideAttributePrefix+"Stop"));
				while (currentLine <= inSideStoptLine)
				{
					//read stuff into the void if it belongs to the sideDocuments side only
					inputStreamTokenizer.readBytes();
					currentLine++;
				}
			}
			else
			{				
				//add this nodes data
				stringBuilder.append(diffEntryElement.getTextContent());				
			}
			
			
		}
		
		//read the rest of the data since there are no chucks left at this point, everything should be equal
		while(true)
		{
			byte[] data = inputStreamTokenizer.readBytes();
			if (data.length == 0)
			{
				break;
			}
			else
			{
				stringBuilder.append(new String(data));
			}
		}
		
		//add the node, and lets get out of here
		return stringBuilder.toString();
	}
}
