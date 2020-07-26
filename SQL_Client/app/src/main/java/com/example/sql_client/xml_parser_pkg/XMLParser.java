package com.example.sql_client.xml_parser_pkg;

import com.example.sql_client.pop_up.ActivityInterface;
import com.example.sql_client.pop_up.PopUpHandler;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
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

    public boolean DoParsing(ActivityInterface activityInterface, String fileName) {
        if (null == activityInterface || null == fileName) {
            return false;
        }
        InputStream inputStream = null;
        Document document = null;
        try {
            FileInputStream fileInputStream = activityInterface.getApplicationContext().openFileInput(fileName);
            inputStream = new BufferedInputStream(fileInputStream);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = factory.newDocumentBuilder();
            document = db.parse(inputStream);
        }catch (SAXParseException e){
            PopUpHandler.PopUp(activityInterface, -1, "Exception happened...", e.toString(), null);
            return false;
        }catch (Exception e){
            PopUpHandler.PopUp(activityInterface, -1, "Exception happened...", e.toString(), null);
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

        try {
            inputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }
}
