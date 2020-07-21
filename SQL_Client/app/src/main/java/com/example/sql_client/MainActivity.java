package com.example.sql_client;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.sql_client.game_pkg.Player;
import com.example.sql_client.game_pkg.activities_pkg.GameMenu;
import com.example.sql_client.tcp_socket_pkg.ClientSocket;
import com.example.sql_client.tcp_socket_pkg.activites_pkg.SignInActivity;
import com.example.sql_client.tcp_socket_pkg.activites_pkg.SignUpActivity;

public class MainActivity extends AppCompatActivity
{
    private TextView signIn;
    private Button signUp, playOffline;
    private Player player = null;

    private static ClientSocket clientSocket = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(clientSocket == null)
        {
            clientSocket = new ClientSocket();
        }

        player = new Player();

        signIn = (TextView)findViewById(R.id.sigin);
        signUp = (Button)findViewById(R.id.signup);
        playOffline = (Button)findViewById(R.id.play_offline);

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        } );

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                startActivity(intent);   
            }
        } );

        playOffline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                player.setOffline(true);
                Intent intent = new Intent(MainActivity.this, GameMenu.class);
                startActivity(intent);
            }
        } );

    }
}