package com.example.sql_client.xml_parser_pkg.activities_pkg;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.example.sql_client.R;
import com.example.sql_client.xml_parser_pkg.XMLParser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;import java.io.InputStreamReader;

public class Leaderboard extends AppCompatActivity
{
    private static final String FILE_NAME = "leaderboard.xml";
    private static XMLParser xmlParser = null;
    EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        mEditText = findViewById(R.id.edit_text);
        if(null == xmlParser)
        {
            xmlParser = new XMLParser(FILE_NAME);
        }
    }

    public void load (View v) {
        FileInputStream fis = null;
        try{
            fis = openFileInput(FILE_NAME);
            if(xmlParser.DoParsing(fis) )
            {
                StringBuilder sb = new StringBuilder();
                for(int i=0; i<xmlParser.getListNames().size(); i++)
                {
                    sb.append(String.valueOf(i+1)+". ").append(xmlParser.getListNames().get(i) ).
                            append(' ').append(xmlParser.getListLevels().get(i) ).
                            append(' ').append(xmlParser.getListLevels().get(i) ).append('\n');
                }
                mEditText.setText(sb.toString());
            }
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            if (fis != null){
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }
}