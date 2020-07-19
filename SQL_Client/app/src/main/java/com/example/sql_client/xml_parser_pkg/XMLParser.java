package com.example.sql_client.xml_parser_pkg;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class XMLParser
{
    private final String FILE_NAME;
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

    public XMLParser(String fileName)
    {
        this.FILE_NAME = fileName;
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

    public boolean DoParsing(FileInputStream inputFile)
    {
        try
        {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("player");

            listNames.clear();
            listPoints.clear();
            listLevels.clear();

            for (int temp = 0; temp < nList.getLength(); temp++)
            {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element eElement = (Element) nNode;
                    listPoints.add(eElement.getAttribute("points"));
                    listNames.add(eElement.getElementsByTagName("name").item(0).getTextContent());
                    listLevels.add(eElement.getElementsByTagName("level").item(0).getTextContent());
                }
            }
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}