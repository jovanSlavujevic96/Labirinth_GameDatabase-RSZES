package com.example.sql_client.xml_parser_pkg;

import android.content.Context;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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

    public boolean DoParsing(Context context, String fileName)
    {
        if(null == context || null == fileName)
        {
            return false;
        }
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = context.openFileInput(fileName);
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }
        InputStream inputStream = new BufferedInputStream(fileInputStream);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        Document document = null;
        try{
            db = factory.newDocumentBuilder();
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            document = db.parse(inputStream);
        }catch (Exception e){
            e.printStackTrace();
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

        try {
            inputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }
}
