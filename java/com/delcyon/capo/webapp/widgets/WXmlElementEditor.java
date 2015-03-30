package com.delcyon.capo.webapp.widgets;

import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.delcyon.capo.webapp.models.DomItemModel;
import com.delcyon.capo.webapp.models.DomItemModel.DomUse;

import eu.webtoolkit.jwt.AlignmentFlag;
import eu.webtoolkit.jwt.SelectionMode;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WLength.Unit;
import eu.webtoolkit.jwt.WLineEdit;
import eu.webtoolkit.jwt.WTabWidget;
import eu.webtoolkit.jwt.WTableView;

public class WXmlElementEditor extends WTabWidget
{

	
	private WTableView attributeTableView;
	private Element element;
	private WContainerWidget detailsContainerWidget;
	private WLineEdit commentEditor;

	public WXmlElementEditor()
	{
		this.addTab(getDetailsContainerWidget(), "Details");
	}
	
	public void setElement(Element element)
	{
		this.element = element;
		getAttributeTableView().setModel(new DomItemModel(this.element, DomUse.ATTRIBUTES));
		Node node = getComment();
		if(node != null && node instanceof Comment)
		{
			getCommentEditor().setText(node.getTextContent());
		}
		else
		{
			getCommentEditor().setText("");
		}
	}
	
	private Comment getComment()
	{
		Comment comment = null;
		Node node = element.getPreviousSibling();
		while(node != null)
		{
			 if(node instanceof Element)
			 {
				 break;
			 }
			 else if (node instanceof Comment)
			 {
				 comment = (Comment) node;
				 break;
			 }
			 else
			 {
				 node = node.getPreviousSibling();
			 }
		}
		return comment;
	}
	
	private WTableView getAttributeTableView()
    {
        if (attributeTableView == null)
        {
            attributeTableView = new WTableView();
            attributeTableView.addStyleClass("bg-transparent");
            attributeTableView.setItemDelegateForColumn(0, new WCSSItemDelegate("font-weight: bold;"));
            attributeTableView.setSortingEnabled(true);
            attributeTableView.setSelectable(true);
            attributeTableView.setAlternatingRowColors(true);
            attributeTableView.setColumnResizeEnabled(true);
            attributeTableView.setColumnAlignment(0, AlignmentFlag.AlignRight);
            attributeTableView.setColumnWidth(1, new WLength(500));
            attributeTableView.setSelectionMode(SelectionMode.SingleSelection);
        }
        return attributeTableView;
    }
	
	private WContainerWidget getDetailsContainerWidget()
    {
        if (detailsContainerWidget == null)
        {
            detailsContainerWidget = new WContainerWidget();
            detailsContainerWidget.addWidget(getCommentEditor());
            detailsContainerWidget.addWidget(getAttributeTableView());
            
        }
        return detailsContainerWidget;
    }

	private WLineEdit getCommentEditor()
	{
		if(commentEditor == null)
		{
			commentEditor = new WLineEdit();
			commentEditor.setWidth(new WLength(95,Unit.Percentage));
			commentEditor.setMargin(5);
		}
		return commentEditor;
	}
}
