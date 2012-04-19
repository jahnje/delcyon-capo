package com.delcyon.capo.xml.dom;

import java.util.ArrayList;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ResourceNodeList extends ArrayList<Node> implements NodeList
{

    @Override
    public Node item(int index)
    {
        return get(index);
    }

    @Override
    public int getLength()
    {
        return size();
    }

}
