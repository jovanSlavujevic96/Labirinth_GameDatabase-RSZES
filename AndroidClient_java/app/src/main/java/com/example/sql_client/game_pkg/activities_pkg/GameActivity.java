package com.example.sql_client.game_pkg.activities_pkg;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.sql_client.pop_up.ActivityInterface;
import com.example.sql_client.R;
import com.example.sql_client.game_pkg.GameView;
import com.example.sql_client.game_pkg.Player;
import com.example.sql_client.pop_up.PopUpHandler;
import com.example.sql_client.tcp_socket_pkg.ClientSocket;

public class GameActivity extends ActivityInterface
{
    private GameView gameView = null;
    private ImageButton exitButton = null;
    private EditText currentLvlAndPts = null;
    private Chronometer chronometer  = null;

    static private DialogInterface.OnClickListener Listener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        ClientSocket.setActivityInterface(this);

        if(null == Listener) {
            Listener =  new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    GameActivity.this.UpdateScoreSQL();
                    Intent intent = new Intent(GameActivity.this, GameMenu.class);
                    startActivity(intent);
                }
            };
        }

        gameView = findViewById(R.id.view);
        exitButton = findViewById(R.id.exitImageButton);
        chronometer = findViewById(R.id.chronometer);
        currentLvlAndPts = findViewById(R.id.lvlPts);

        gameView.setGameActivity(this);
        gameView.createMaze(true);

        chronometer.setBase(SystemClock.elapsedRealtime() );
        chronometer.start();

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                final String text ="LEVEL: " + String.valueOf(Player.getTmpLevel()) + "\nPOINTS: " + String.valueOf(Player.getTmpPoints());
                PopUpHandler.PopUp(ActivityInterface.current_context, 0, "Game finished...", text, Listener);
            }
        });
    }

    @Override
    public void ActivityPopUpHandling(int PopUpType)
    {
        if(0 == PopUpType)
        {
            chronometer.stop();
        }
    }

    public DialogInterface.OnClickListener getListener()
    {
        return Listener;
    }

    public void UpdateScoreSQL()
    {
        if(!Player.getScoreChanged() ){
            return;
        }
        String msg = "UPDATE_SCORE\n" + Player.getEmail() + '\n' +
                String.valueOf(Player.getHighestPoints()) + '\n' +
                String.valueOf(Player.getHighestLevel()) + '\n';
        msg = ClientSocket.TransmitString(msg);
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

    public void streamResult(String result /*level and points*/)
    {
        if(null == this.currentLvlAndPts)
        {
            return;
        }
        this.currentLvlAndPts.setText(result);
    }
}