package com.example.sql_client.xml_parser_pkg.activities_pkg;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.sql_client.R;
import com.example.sql_client.tcp_socket_pkg.ClientSocket;
import com.example.sql_client.xml_parser_pkg.XMLParser;

import org.w3c.dom.Document;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


public class Leaderboard extends AppCompatActivity
{
    static private  XMLParser xmlParser = null;
    private static ClientSocket socket = null;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        textView = findViewById(R.id.ldbTV);

        if(null == socket && null == xmlParser)
        {
            xmlParser = new XMLParser();
            socket = new ClientSocket();
        }
    }

    public void load (View v){
        FileInputStream fis = null;
        try {
            fis = openFileInput(socket.getFileName());
        }catch (FileNotFoundException e){
            e.printStackTrace();
            return;
        }
        InputStream is = new BufferedInputStream(fis);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        Document document = null;
        try{
            db = factory.newDocumentBuilder();
            document = db.parse(is);
        }catch (Exception e){
            e.printStackTrace();
        }

        if(xmlParser.DoParsing(document) )
        {
            StringBuilder sb = new StringBuilder();
            for(int i=0; i<xmlParser.getListNames().size(); i++)
            {
                sb.append(String.valueOf(i+1)+". ").append(xmlParser.getListNames().get(i) ).
                        append(' ').append(xmlParser.getListLevels().get(i) ).
                        append(' ').append(xmlParser.getListPoints().get(i) ).append('\n');
            }
            textView.setText(sb.toString());
        }

        try {
            fis.close();
        }catch (Exception e){
            e.printStackTrace();
            return;
        }

    }
}