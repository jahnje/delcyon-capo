package com.delcyon.capo.webapp.widgets;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import com.delcyon.capo.webapp.servlets.CapoWebApplication;

import eu.webtoolkit.jwt.AlignmentFlag;
import eu.webtoolkit.jwt.Utils;
import eu.webtoolkit.jwt.WAnchor;
import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WCompositeWidget;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WDialog;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WLength.Unit;
import eu.webtoolkit.jwt.WLineEdit;
import eu.webtoolkit.jwt.WLink;
import eu.webtoolkit.jwt.WLink.Type;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WTable;
import eu.webtoolkit.jwt.WText;

/**
 * This implements the main jcr search functionality. It will result in a dialog of matches, 
 * and links that will change the internal path accordingly. The dialog closes whenever the internal path changes 
 * @author jeremiah
 *
 */
public class WCapoSearchControl extends WCompositeWidget
{
    private WDialog searchResultsDialog;
    private WLineEdit searchFieldTextEdit;
    private WPushButton searchButton;
    private WContainerWidget implementationWidget = new WContainerWidget();
    
    public WCapoSearchControl()
    {
        
        setImplementation(implementationWidget);
        setInline(true);
        searchFieldTextEdit = new WLineEdit();
        implementationWidget.addWidget(searchFieldTextEdit);
        searchButton = new WPushButton("Search");
        implementationWidget.addWidget(searchButton);
        searchButton.clicked().addListener(this,this::search);
        searchFieldTextEdit.enterPressed().addListener(this, this::search);
        //try to close the dialog whenever the internal path changes
        WApplication.getInstance().internalPathChanged().addListener(this, () -> getSearchResultsDialog().hide());
    }
    
    
    /**
     * This needs a whole lot of work, and is mostly here as a place to mess around with jcr searching at the moment
     */
    private void search()
    {
        Session jcrSession = ((CapoWebApplication)CapoWebApplication.getInstance()).getJcrSession();
        try
        {
          //element(*, nt:unstructured)[jcr:contains(., 'foo')]    
            
          //Query query = jcrSession.getWorkspace().getQueryManager().createQuery("SELECT * FROM [nt:unstructured] where NAME([nt:unstructured]) = 'server:log' order by message", "JCR-SQL2");
            String[] langs = jcrSession.getWorkspace().getQueryManager().getSupportedQueryLanguages();
            for (String lang : langs)
            {
                System.out.println(lang+"--"+searchFieldTextEdit.getText());
            }
          //Query query = jcrSession.getWorkspace().getQueryManager().createQuery("//element(*, nt:unstructured)[jcr:contains(@content, '"+searchFieldTextEdit.getText()+"')/(@content)]", Query.XPATH);
          Query query = jcrSession.getWorkspace().getQueryManager().createQuery("SELECT * FROM [nt:unstructured] as n WHERE CONTAINS(n.*, '"+searchFieldTextEdit.getText()+"')", Query.JCR_SQL2);
          QueryResult result = query.execute();
          for (String lang : result.getColumnNames())
          {
              System.out.println(lang);
          }
    
          // Iterate over the nodes in the results ...
          int excerptWidth = 50;
          RowIterator rows = result.getRows();
          System.out.println("=============================");

         
          WTable table = new WTable(getSearchResultsDialog().getContents());
          //table.toggleStyleClass("table-hover", true);
          table.toggleStyleClass("table-condensed", true);
          table.toggleStyleClass("table-striped", true);
          table.toggleStyleClass("table-full", true);
          table.setHeaderCount(1);
          table.getElementAt(0, 0).addWidget(new WText("Path"));                  
          table.getElementAt(0, 1).addWidget(new WText("Excerpt"));
          table.getElementAt(0, 2).addWidget(new WText("Score"));
          table.getElementAt(0, 2).setContentAlignment(AlignmentFlag.AlignRight);

          int rowNumber = 0;
          while ( rows.hasNext() ) {
              
              rowNumber++;
              
              Row row = rows.nextRow();
              Node _node = row.getNode();
              String excerpt = _node.getProperty("content").getString().toLowerCase();
              String searchField = searchFieldTextEdit.getText();
              int excerptLocation = excerpt.indexOf(searchField.toLowerCase());
              int startExcerptLocation = excerptLocation -excerptWidth;
              if(startExcerptLocation < 0 )
              {
                  startExcerptLocation = 0;
              }
              if(excerpt.substring(startExcerptLocation, excerptLocation).indexOf('\n') >= 0)
              {
                  startExcerptLocation += excerpt.substring(startExcerptLocation, excerptLocation).indexOf('\n')+1;
              }
              int endExcerptLocation = excerptLocation+excerptWidth+searchField.length();
              if(endExcerptLocation >= excerpt.length())
              {
                  endExcerptLocation = excerpt.length()-1;
              }
              if(excerpt.substring(excerptLocation+searchField.length(), endExcerptLocation).indexOf('\n') >= 0)
              {
                  int crDistance = excerpt.substring(excerptLocation+searchField.length(), endExcerptLocation).indexOf('\n');
                  endExcerptLocation -= (excerptWidth - crDistance);
              }
              excerpt = _node.getProperty("content").getString().substring(startExcerptLocation, endExcerptLocation);
              
              
              System.out.println("===>"+_node.getPath()+" type:"+_node.getPrimaryNodeType().getName()+" score="+row.getScore()+" exrp = '"+excerpt+"'");
              //dump(_node);new WLink(Type.InternalPath, "/legend")
              table.getElementAt(rowNumber, 0).addWidget(new WAnchor(new WLink(Type.InternalPath, _node.getPath()),_node.getPath(),CapoWebApplication.getInstance().getRoot()));
              table.getElementAt(rowNumber,1).addWidget(new WText(Utils.htmlEncode(excerpt)));
              table.getElementAt(rowNumber,1).setAttributeValue("width", "80%");
              table.getElementAt(rowNumber,1).setContentAlignment(AlignmentFlag.AlignCenter);
              table.getElementAt(rowNumber, 2).addWidget(new WText(row.getScore()+""));                      
              table.getElementAt(rowNumber,2).setContentAlignment(AlignmentFlag.AlignRight);                      
          }
          
          searchResultsDialog.show();
          System.out.println("=============================");
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private WDialog getSearchResultsDialog()
    {
        if(searchResultsDialog == null)
        {
            searchResultsDialog = new WDialog("Search Results");
            searchResultsDialog.setWidth(new WLength(80d, Unit.Percentage));
            searchResultsDialog.setClosable(true);
            searchResultsDialog.rejectWhenEscapePressed(true);
        }
        return searchResultsDialog;
    }
   
}
