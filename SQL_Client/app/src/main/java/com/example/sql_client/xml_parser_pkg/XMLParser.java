package com.example.sql_client.xml_parser_pkg;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class XMLParser
{
    static private List<String> listNames = null, listPoints = null, listLevels = null;

    public final List<String> getListNames()
    {
        return listNames;
    }
    public final List<String> getListPoints()
    {
        return listPoints;
    }
    public final List<String> getListLevels()
    {
        return listLevels;
    }

    public XMLParser()
    {
        if(null == listNames) {
            listNames = new ArrayList<>();
        }
        if(null == listPoints) {
            listPoints = new ArrayList<>();
        }
        if(null == listLevels) {
            listLevels = new ArrayList<>();
        }
    }

    public boolean DoParsing(Document document)
    {
        if(null == document)
        {
            return false;
        }
        document.getDocumentElement().normalize();
        NodeList nList = document.getElementsByTagName("player");

        listNames.clear();
        listPoints.clear();
        listLevels.clear();

        for (int temp = 0; temp < nList.getLength(); temp++)
        {
            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE)
            {
                Element eElement = (Element) nNode;
                listPoints.add(eElement.getAttribute("points") );
                listNames.add(eElement.getAttribute("name") );
                listLevels.add(eElement.getAttribute("level") );
            }
        }
        return true;
    }
}
