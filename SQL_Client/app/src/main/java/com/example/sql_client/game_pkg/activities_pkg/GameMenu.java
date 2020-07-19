package com.example.sql_client.game_pkg.activities_pkg;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.sql_client.xml_parser_pkg.activities_pkg.Leaderboard;
import com.example.sql_client.R;
import com.example.sql_client.game_pkg.GameView;
import com.example.sql_client.tcp_socket_pkg.ClientSocket;

public class GameMenu extends AppCompatActivity {
    private GameView gameView;
    private Button startGame, gotoLeaderboard;
    private static ClientSocket clientSocket = null;
    final int RequsetCode = 656;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_menu);
        gameView = findViewById(R.id.view);
        gameView.createMaze(false);
        gotoLeaderboard = (Button) findViewById(R.id.button2);
        startGame = (Button) findViewById(R.id.button);
        if(clientSocket == null)
        {
            clientSocket = new ClientSocket();
        }
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

                if(clientSocket.TransmitFile("GET_LDB\n", getApplicationContext() ) /*|| clientSocket.Exception_happened*/)
                {
                    Intent intent = new Intent(GameMenu.this, Leaderboard.class);
                    startActivity(intent);
                }
            }
        });
    }
}
