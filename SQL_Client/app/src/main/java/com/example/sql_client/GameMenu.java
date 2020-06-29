package com.example.sql_client;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import static androidx.core.app.ActivityCompat.requestPermissions;

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

                if(clientSocket.TransmitFile("GET_LDB\n") /*|| clientSocket.Exception_happened*/)
                {
                    Intent intent = new Intent(GameMenu.this, SignInActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(this.RequsetCode == requestCode && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {

        }
        else
        {
            requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE}, this.RequsetCode);
        }
    }
}
