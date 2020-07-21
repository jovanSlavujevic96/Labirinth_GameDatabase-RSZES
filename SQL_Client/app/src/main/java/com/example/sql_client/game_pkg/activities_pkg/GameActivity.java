package com.example.sql_client.game_pkg.activities_pkg;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.sql_client.R;
import com.example.sql_client.game_pkg.GameView;
import com.example.sql_client.game_pkg.Player;
import com.example.sql_client.tcp_socket_pkg.ClientSocket;

import java.net.Socket;

public class GameActivity extends AppCompatActivity
{
    private GameView gameView = null;
    static private Chronometer chronometer  = null;
    private ImageButton exitButton = null;
    static private Player player = null;
    static private AlertDialog.Builder dlgAlert = null;
    private EditText currentLvlAndPts = null;

    static private ClientSocket socket = null;

    static volatile private int LEVEL, POINTS;
    static volatile private String TITLE;

    protected static GameActivity current_context = null;

    public void UpdateScoreSQL()
    {
        if(null == socket || null == player || !player.getScoreChanged() )
        {
            return;
        }
        final String messageToSrv = "UPDATE_SCORE\n" + player.getEmail() + '\n' +
                String.valueOf(player.getHighestPoints()) + '\n' +
                String.valueOf(player.getHighestLevel()) + '\n';
        final String messageFromSrv = socket.TransmitString(messageToSrv);
    }

    @Override
    protected void onPause(){
        current_context = null;
        super.onPause();
    }

    @Override
    protected void onResume(){
        current_context = this;
        super.onResume();
    }

    public static void createPopUp(String title, int level, int points){
        //show nothing, if no activity has focus

        POINTS = points;
        LEVEL = level;
        TITLE = title;

        if(current_context == null)return;
        current_context.runOnUiThread(new Runnable() {
            @Override
            public void run(){
                if(null == dlgAlert)
                {
                    return;
                }
                chronometer.stop();
                final String text ="LEVEL: " + String.valueOf(LEVEL) + "\nPOINTS: " + String.valueOf(POINTS);
                dlgAlert.setTitle(TITLE);
                dlgAlert.setMessage(text);
                dlgAlert.create().show();
            }
        });
    }

    public void streamResult(String result /*level and points*/)
    {
        if(null == this.currentLvlAndPts)
        {
            return;
        }
        this.currentLvlAndPts.setText(result);
    }

    public int getSecondsFromDurationString(){

        String [] parts = (chronometer.getText().toString() ).split(":");

        // Wrong format, no value for you.
        if(parts.length < 2 || parts.length > 3)
            return 0;

        int seconds = 0, minutes = 0, hours = 0;

        if(parts.length == 2){
            seconds = Integer.parseInt(parts[1]);
            minutes = Integer.parseInt(parts[0]);
        }
        else if(parts.length == 3){
            seconds = Integer.parseInt(parts[2]);
            minutes = Integer.parseInt(parts[1]);
            hours = Integer.parseInt(parts[0]);
        }

        return seconds + (minutes*60) + (hours*3600);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);


        gameView = findViewById(R.id.view);
        exitButton = findViewById(R.id.exitImageButton);

        chronometer = findViewById(R.id.chronometer);
        currentLvlAndPts = findViewById(R.id.lvlPts);

        if(null == player)
        {
            player = new Player();
        }
        dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setPositiveButton("OK", null);
        dlgAlert.setCancelable(true);
        dlgAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                UpdateScoreSQL();
                Intent intent = new Intent(GameActivity.this, GameMenu.class);
                startActivity(intent);
            }
        });

        if(!player.getOffline() && null == socket)
        {
            socket = new ClientSocket();
        }

        gameView.setGameActivity(this);
        gameView.createMaze(true);

        chronometer.setBase(SystemClock.elapsedRealtime() );
        chronometer.start();

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                createPopUp("Game finished...", player.getTmpLevel(), player.getTmpPoints());
            }
        });
    }


}