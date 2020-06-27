package com.example.sql_client;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class GameMenu extends AppCompatActivity {
    private GameView gameView;
    private Button startGame, gotoLeaderboard;
    private static ClientSocket clientSocket = null;

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

                if(true == clientSocket.TransmitFile("GET_LDB") )
                {
                    Intent intent = new Intent(GameMenu.this, SignInActivity.class);
                    startActivity(intent);
                }
            }
        });
    }
}
