package com.example.sql_client.xml_parser_pkg.activities_pkg;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.example.sql_client.R;
import com.example.sql_client.tcp_socket_pkg.ClientSocket;
import com.example.sql_client.xml_parser_pkg.XMLParser;


public class Leaderboard extends AppCompatActivity
{
    private static final String FILE_NAME = "leaderboard.xml";
    static private  XMLParser xmlParser = null;
    private static ClientSocket socket = null;
    EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        mEditText = findViewById(R.id.edit_text);

        if(null == socket && null == xmlParser)
        {
            xmlParser = new XMLParser();
            socket = new ClientSocket();
        }
    }

    public void load (View v) {
        if(xmlParser.DoParsing(this, socket.getFileName() ) )
        {
            StringBuilder sb = new StringBuilder();
            for(int i=0; i<xmlParser.getListNames().size(); i++)
            {
                sb.append(String.valueOf(i+1)+". ").append(xmlParser.getListNames().get(i) ).
                        append(' ').append(xmlParser.getListLevels().get(i) ).
                        append(' ').append(xmlParser.getListPoints().get(i) ).append('\n');
            }
            mEditText.setText(sb.toString());
        }
    }
}