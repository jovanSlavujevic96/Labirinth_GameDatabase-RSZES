package com.example.sql_client.xml_parser_pkg.activities_pkg;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.sql_client.pop_up.ActivityInterface;
import com.example.sql_client.R;
import com.example.sql_client.game_pkg.GameView;
import com.example.sql_client.pop_up.PopUpHandler;
import com.example.sql_client.pop_up.ToastHandler;
import com.example.sql_client.tcp_socket_pkg.ClientSocket;
import com.example.sql_client.xml_parser_pkg.XMLParser;

import java.util.ArrayList;
import java.util.List;

public class Leaderboard extends ActivityInterface
{
    private GameView gameView;
    private boolean firstLoad = true;

    static private  XMLParser xmlParser = null;
    static private ClientSocket clientSocket = null;
    static private List<TextView> textViewList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        ClientSocket.setActivityInterface(this);

        gameView = findViewById(R.id.ldbGameView);
        gameView.createMaze(false);

        if(null == textViewList)
        {
            textViewList = new ArrayList<>(3);
            xmlParser = new XMLParser();
            clientSocket = new ClientSocket();
        }

        textViewList.add(0, (TextView) (findViewById(R.id.ldbTV)) ); //name
        textViewList.add(1, (TextView) (findViewById(R.id.ldbTV2)) ); //level
        textViewList.add(2, (TextView) (findViewById(R.id.ldbTV3)) ); //points
    }

    public void load (View v){
        if(!firstLoad) {
            if (!ClientSocket.TransmitFile("GET_LDB\n")) {
                return;
            }
            if(ClientSocket.isUpToDate() ){
                return;
            }
        }

        if(true == xmlParser.DoParsing(this, clientSocket.getFileName() ) )
        {
            List<StringBuilder> sbList = new ArrayList<>(3);
            for(int i=0; i<3; i++) {
                sbList.add(i, new StringBuilder());
            }
            for(int i=0; i<xmlParser.getListNames().size(); i++)
            {
                sbList.get(0).append(String.valueOf(i+1)+". ").append(xmlParser.getListNames().get(i) ).append('\n');
                sbList.get(1).append(xmlParser.getListLevels().get(i) ).append('\n');
                sbList.get(2).append(xmlParser.getListPoints().get(i) ).append('\n');
            }
            for(int i=0; i<3; i++)
            {
                textViewList.get(i).setText(sbList.get(i).toString());
            }
        }
        else{
            return;
        }
        firstLoad = false;
    }
}