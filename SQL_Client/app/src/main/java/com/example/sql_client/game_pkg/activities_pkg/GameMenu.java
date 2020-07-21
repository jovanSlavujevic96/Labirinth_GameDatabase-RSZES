package com.example.sql_client.game_pkg.activities_pkg;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.sql_client.game_pkg.Player;
import com.example.sql_client.xml_parser_pkg.activities_pkg.Leaderboard;
import com.example.sql_client.R;
import com.example.sql_client.game_pkg.GameView;
import com.example.sql_client.tcp_socket_pkg.ClientSocket;

public class GameMenu extends AppCompatActivity {
    private GameView gameView;
    private Button startGame, gotoLeaderboard;
    private static ClientSocket clientSocket = null;
    static private Player player = null;
    private AlertDialog.Builder dlgAlert = null;

    private TextView name = null, score = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_menu);
        gameView = findViewById(R.id.view);
        gameView.createMaze(false);
        gotoLeaderboard = (Button) findViewById(R.id.button2);
        startGame = (Button) findViewById(R.id.button);

        if(null == player) {
            player = new Player();
        }

        if(clientSocket == null && !player.getOffline() )
        {
            clientSocket = new ClientSocket();
        }

        name = findViewById(R.id.menuPlayerName);
        score = findViewById(R.id.menuPlayerPtsAndLevel);

        String playerName = "/";
        if(!player.getOffline() )
        {
            playerName = player.getNickname();
        }
        name.setText("Name: " + playerName);
        final String playerScore = "Score: " + String.valueOf(player.getHighestLevel() ) + " (" + String.valueOf(player.getHighestPoints() )+ ')';
        score.setText(playerScore);
        name.setTextColor(Color.RED); //set red color for text view
        score.setTextColor(Color.RED);

        dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setTitle("Error Message...");
        dlgAlert.setPositiveButton("OK", null);
        dlgAlert.setCancelable(true);
        dlgAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        startGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(GameMenu.this, GameActivity.class);
                startActivity(intent);
            }
        });

        gotoLeaderboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(true == player.getOffline() )
                {
                    dlgAlert.setMessage("You are offline!");
                    dlgAlert.create().show();
                    return;
                }
                if(clientSocket.TransmitFile("GET_LDB\n", getApplicationContext() ) /*|| clientSocket.Exception_happened*/)
                {
                    Intent intent = new Intent(GameMenu.this, Leaderboard.class);
                    startActivity(intent);
                }
            }
        });
    }
}
