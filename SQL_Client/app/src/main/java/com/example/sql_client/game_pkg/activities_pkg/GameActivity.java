package com.example.sql_client.game_pkg.activities_pkg;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.sql_client.R;
import com.example.sql_client.game_pkg.GameView;

public class GameActivity extends AppCompatActivity {

    GameView gameView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        gameView = findViewById(R.id.view);
        gameView.createMaze(true);
    }
}