package com.example.sql_client.game_pkg.activities_pkg;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sql_client.MainActivity;
import com.example.sql_client.game_pkg.Player;
import com.example.sql_client.pop_up.ActivityInterface;
import com.example.sql_client.pop_up.PopUpHandler;
import com.example.sql_client.tcp_socket_pkg.activites_pkg.UserSettingsActivity;
import com.example.sql_client.xml_parser_pkg.activities_pkg.Leaderboard;
import com.example.sql_client.R;
import com.example.sql_client.game_pkg.GameView;
import com.example.sql_client.tcp_socket_pkg.ClientSocket;

public class GameMenu extends ActivityInterface {
    private GameView gameView;
    private Button startGame, gotoLeaderboard, gotoMainMenu;
    private TextView name = null, score = null;
    private ImageView userConfiguration;

    @Override
    public void ActivityPopUpHandling(int PopUpType) {}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_menu);

        ClientSocket.setActivityInterface(this);

        gameView = findViewById(R.id.view);
        gotoLeaderboard = findViewById(R.id.button2);
        gotoMainMenu = findViewById(R.id.button3);
        startGame = findViewById(R.id.button);
        name = findViewById(R.id.menuPlayerName);
        score = findViewById(R.id.menuPlayerPtsAndLevel);
        userConfiguration = findViewById(R.id.UserSettings);

        gameView.createMaze(false);
        {
            String playerName = "/";
            if (!Player.getOffline()) {
                playerName = Player.getNickname();
            }
            name.setText("Name: " + playerName);

            final String playerScore = "Score: " + String.valueOf(Player.getHighestLevel())
                    + " (" + String.valueOf(Player.getHighestPoints()) + ')';
            score.setText(playerScore);
        }

        gotoMainMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GameMenu.this, MainActivity.class);
                startActivity(intent);
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
                if(true == Player.getOffline() ){
                    PopUpHandler.PopUp(ActivityInterface.current_context, -1,
                            "Error Message...", "You are offline!", null );
                    return;
                }
                if(ClientSocket.TransmitFile("GET_LDB\n" ) ) {
                    Intent intent = new Intent(GameMenu.this, Leaderboard.class);
                    startActivity(intent);
                }
            }
        });

        userConfiguration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GameMenu.this, UserSettingsActivity.class);
                startActivity(intent);
            }
        });

    }
}
