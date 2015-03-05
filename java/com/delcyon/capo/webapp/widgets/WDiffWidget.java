package com.delcyon.capo.webapp.widgets;

import java.io.ByteArrayInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.delcyon.capo.parsers.Tokenizer;
import com.delcyon.capo.parsers.Tokenizer.CharacterType;

import eu.webtoolkit.jwt.Utils;
import eu.webtoolkit.jwt.WCompositeWidget;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WTable;
import eu.webtoolkit.jwt.WTableCell;
import eu.webtoolkit.jwt.WText;

/**
 * Will take a formatted diff string and produce a side by side highlighted view of that diff.
 * @author jeremiah
 *
 */
public class WDiffWidget extends WCompositeWidget
{
    private WTable table = new WTable();
    
    /**
     * Supported diff formats
     * @author jeremiah
     *
     */
    public enum DiffFormat
    {
        CAPO
    }
    
    public WDiffWidget()
    {
        WContainerWidget containerWidget = new WContainerWidget();
        containerWidget.addWidget(table);
        setImplementation(containerWidget);
        setStyleClass("diff");
    }
    
    public void setDiff(String differences, DiffFormat format) throws Exception
    {
        table.clear();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(differences.getBytes());
        Tokenizer tokenizer = new Tokenizer(byteArrayInputStream);
        tokenizer.resetSyntax();        
        tokenizer.setCharRangeType(0, 255, CharacterType.ALPHA);        
        tokenizer.setCharType('\n', CharacterType.EOL);
        tokenizer.setCharType('\r', CharacterType.EOL);
        int baseRow = 0;
        int modRow = 0;
        String lastType = "=";
        while(tokenizer.hasMore())
        {
            tokenizer.nextToken();
            String line = tokenizer.getValue();
            if(line == null)
            {
                line ="";
            }
            Matcher matcher = Pattern.compile("([-+=])\\((\\d+),(\\d+)\\)\\[\\d+\\](.*)").matcher(line);
            
            if(matcher.matches() || tokenizer.hasMore() == false)
            {
                
                //System.out.println("data = "+matcher.group(1) +"==>"+line);
                String type = null;
                if(tokenizer.hasMore() == false)
                {
                    type = "="; 
                }
                else
                {
                    type = matcher.group(1);
                }
                
                //check to see if were switching types, and not from an equals, as the system should be in balance at that point
                if(type.equals(lastType) == false && lastType.equals("=") == false)
                {
                    if(type.equals("=")) //check to see if we're moving out of a mod
                    {
                        while(baseRow < modRow) //we just finished an addition, so add all of the blank lines in the base side
                        {
                            styledCell(baseRow, 1,"diff_base_addition").addWidget(new WText(""));
                            styledCell(baseRow, 0,"diff_base_addition_linenumber").addWidget(new WText(""));
                            baseRow++;
                        }
                        
                        while(baseRow > modRow)
                        {
                            styledCell(modRow, 3,"diff_mod_deletion").addWidget(new WText(""));
                            styledCell(modRow, 2,"diff_mod_deletion_linenumber").addWidget(new WText(""));
                            modRow++;
                        }
                    }
                    if(tokenizer.hasMore() == false)
                    {
                        break; //we we're just here to finish flushing out the empty rows
                    }
                }

                lastType = type;
                String text = Utils.htmlEncode(matcher.group(4));
                switch (type)
                {
                    case "+":
                        //This all takes care of hightlighing on an indivdual line if we find ourselves across from an already existing base or mod that we're going to fill out here
                        if(modRow < baseRow) //check too see if we're filling in a mod 
                        {
                            WText wtext = (WText) table.getElementAt(modRow, 1).getWidget(0);
                            int[] spanPos = getLineDifferences(wtext.getText().getValue(),text);
                            if(spanPos[0] != -1 && spanPos[1] != -1 && spanPos[2] != -1) //skip if we're missing something
                            {
                                text = text.substring(0,spanPos[0])+"<span class='diff_mod_text'>"+text.substring(spanPos[0],spanPos[2])+"</span>"+text.substring(spanPos[2]);
                                String otherText = wtext.getText().getValue();
                                otherText = otherText.substring(0,spanPos[0])+"<span class='diff_base_text'>"+otherText.substring(spanPos[0],spanPos[1])+"</span>"+otherText.substring(spanPos[1]);
                                wtext.setText(otherText);
                            }
                        }                        
                        styledCell(modRow, 3,"diff_mod_addtition").addWidget(new WText(text));                                        
                        styledCell(modRow, 2,"diff_mod_addition_linenumber").addWidget(new WText(matcher.group(3)));
                        modRow++;
                        break;
                        
                        
                    case "-":                        
                        //This all takes care of hightlighing on an indivdual line if we find ourselves across from an already existing base or mod that we're going to fill out here
                        if(modRow > baseRow) //check too see if we're filling in a base 
                        {
                            WText wtext = (WText) table.getElementAt(baseRow, 3).getWidget(0);                            
                            
                            int[] spanPos = getLineDifferences(wtext.getText().getValue(),text);
                            if(spanPos[0] != -1 && spanPos[1] != -1 && spanPos[2] != -1) //skip if we're missing something
                            {
                                text = text.substring(0,spanPos[0])+"<span class='diff_base_text'>"+text.substring(spanPos[0],spanPos[2])+"</span>"+text.substring(spanPos[2]);
                                String otherText = wtext.getText().getValue();
                                otherText = otherText.substring(0,spanPos[0])+"<span class='diff_mod_text'>"+otherText.substring(spanPos[0],spanPos[1])+"</span>"+otherText.substring(spanPos[1]);
                                wtext.setText(otherText);
                            }                            
                        }                        
                        styledCell(baseRow, 1,"diff_base_deletion").addWidget(new WText(text));                        
                        styledCell(baseRow, 0,"diff_base_deletion_linenumber").addWidget(new WText(matcher.group(2)));                                        
                        baseRow++;                        
                        break;
                        
                    default: //dealing with an '=' so just do the same for both sides 
                        styledCell(baseRow, 1,"diff_base_same").addWidget(new WText(text));
                        styledCell(modRow, 3,"diff_mod_same").addWidget(new WText(text));
                        styledCell(baseRow, 0,"diff_base_same_linenumber").addWidget(new WText(matcher.group(2)));                
                        styledCell(modRow, 2,"diff_mod_same_linenumber").addWidget(new WText(matcher.group(3)));
                        baseRow++;
                        modRow++;
                        break;
                }
                
            }
            
            
        }
    }

    /**
     * takes two strings and computes the positions of the differences between them
     * @param baseText
     * @param modText
     * @return int[3]  where 0 = startPos, 1 = baseEndPos, 2 = modEndPos
     */
    private int[] getLineDifferences(String baseText, String modText)
    {
        int[] lineDiffs = new int[]{-1,-1,-1};
        
        //first find start position, by finding first place where chars don't match
        for(int index = 0; index < baseText.length() && index < modText.length(); index++ )
        {
            if(lineDiffs[0] == -1 && baseText.charAt(index) != modText.charAt(index))
            {
                lineDiffs[0] = index;
            }
            
            int baseRIndex = baseText.length()-1-index;
            int modRIndex = modText.length()-1-index;
            
            
            if(baseRIndex <= lineDiffs[0] && lineDiffs[1] == -1) //break if we've passsed each other
            {
                lineDiffs[1] = lineDiffs[0];
                lineDiffs[2] = modRIndex;
            }
            if(modRIndex <= lineDiffs[0] && lineDiffs[2] == -1) //break if we've passsed each other
            {
                lineDiffs[2] = lineDiffs[0];
                lineDiffs[1] = baseRIndex;
            }
            
            if(lineDiffs[1] == -1 && baseText.charAt(baseRIndex) != modText.charAt(modRIndex))
            {
                lineDiffs[1] = baseRIndex+1;
                lineDiffs[2] = modRIndex+1;
            }
            
            if(lineDiffs[0] != -1 && lineDiffs[1] != -1 && lineDiffs[2] != -1) //break if we know everything
            {
                break;
            }
        }
        
        
        
        return lineDiffs;
    }

    /**
     * quick method to make our code a little shorter
     * @param row
     * @param column
     * @param styleClass
     * @return
     */
    private WTableCell styledCell(int row, int column,String styleClass)
    {
        WTableCell cell = table.getElementAt(row, column);
        cell.setStyleClass(styleClass);
        return cell;
    }
    
    
}
