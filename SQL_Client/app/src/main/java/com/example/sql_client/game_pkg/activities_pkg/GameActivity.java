package com.example.sql_client.game_pkg.activities_pkg;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;

import com.example.sql_client.R;
import com.example.sql_client.game_pkg.GameView;
import com.example.sql_client.game_pkg.Player;
import com.example.sql_client.tcp_socket_pkg.ClientSocket;

public class GameActivity extends AppCompatActivity
{
    static private GameView gameView = null;
    static private Chronometer chronometer  = null;
    static private ImageButton exitButton = null;
    static private Player player = null;
    private static AlertDialog.Builder dlgAlert = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        if(null == gameView)
        {
            gameView = findViewById(R.id.view);
            exitButton = findViewById(R.id.exitImageButton);
        }
        chronometer = findViewById(R.id.chronometer);

        if(null == dlgAlert && null == player)
        {
            player = new Player();

            dlgAlert = new AlertDialog.Builder(this);
            dlgAlert.setTitle("Error Message...");
            dlgAlert.setPositiveButton("OK", null);
            dlgAlert.setCancelable(true);
            dlgAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
        }
        gameView.setGameActivity(this);

        player.setChronometer(chronometer);
        new Thread(new FinishGame() ).start();

        gameView.createMaze(true);

        chronometer.setBase(SystemClock.elapsedRealtime() );
        chronometer.start();

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(GameActivity.this, GameMenu.class);
                startActivity(intent);
            }
        });
    }

    private class FinishGame implements Runnable
    {
        @Override
        public void run() {
            while (true) {
                int val = player.getElapsedTime();
                if (val == 4 * 60) {
                    break;
                }
                val = player.getTmpLevel();
                if (val == 5) {
                    break;
                }
            }
            //chronometer.stop();
//            String tmp = "";
//            if (player.getTmpLevel() == 5) {
//                dlgAlert.setTitle("Game finished...");
//            } else {
//                dlgAlert.setTitle("Time elapsed...");
//                tmp = "Good luck next time!\n";
//            }
//            dlgAlert.setMessage(tmp + "LEVEL: " + String.valueOf(player.getTmpLevel()) + "\nPOINTS: " + String.valueOf(player.getTmpPoints()));
//            dlgAlert.create().show();

            //Intent intent = new Intent(GameActivity.this, GameMenu.class);
            //startActivity(intent);
        }
    }
}