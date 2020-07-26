package com.example.sql_client.tcp_socket_pkg.activites_pkg;

import androidx.appcompat.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.sql_client.MainActivity;
import com.example.sql_client.game_pkg.activities_pkg.GameActivity;
import com.example.sql_client.pop_up.ActivityInterface;
import com.example.sql_client.R;
import com.example.sql_client.game_pkg.Player;
import com.example.sql_client.game_pkg.activities_pkg.GameMenu;
import com.example.sql_client.pop_up.PopUpHandler;
import com.example.sql_client.tcp_socket_pkg.ClientSocket;

public class SignInActivity extends ActivityInterface
{
    private Button signinButton;
    private EditText email_nicknameEditText, passwordEditText;
    private TextView textView;

    static private DialogInterface.OnClickListener Listener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        ClientSocket.setActivityInterface(this);

        if(null == Listener) {
            Listener =  new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            };
        }

        signinButton = findViewById(R.id.signin);
        email_nicknameEditText = findViewById(R.id.nickname_email);
        passwordEditText = findViewById(R.id.password);
        textView = findViewById(R.id.not_registered);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        } );

        signinButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                
                if(!ClientSocket.isConnected() )
                {
                    PopUpHandler.PopUp(ActivityInterface.current_context, -1, "Error Message...", "You can't sign in.\nYou are not connected to the server.\nWe are trying to reconnect.", Listener);
                    return;
                }

                String msg = "SIGN_IN\n" + email_nicknameEditText.getText().toString() + '\n'
                        + passwordEditText.getText().toString();
                msg = ClientSocket.TransmitString(msg);

                if(msg.contentEquals("OK") )
                {
                    Player.setOffline(false);

                    msg = "GET_INFO\n" + email_nicknameEditText.getText().toString();
                    msg = ClientSocket.TransmitString(msg);

                    String[] infos = msg.split(";");
                    Player.setEmail(infos[0]);
                    Player.setNickname(infos[1]);
                    Player.setRecord(Integer.parseInt(infos[3]), Integer.parseInt(infos[2]) );

                    Intent intent = new Intent(SignInActivity.this, GameMenu.class);
                    startActivity(intent);
                    return;
                }

                String Text = msg;
                if(msg.contentEquals("ERR_BAD_NAME")) {
                    Text = "This username doesn't exist!";
                }else if(msg.contentEquals("ERR_BAD_PASS")) {
                    Text = "Wrong password!";
                }else if(msg.contentEquals("ERR_BAD_MAIL")) {
                    Text = "This email doesn't exist!";
                }
                PopUpHandler.PopUp(ActivityInterface.current_context, -1,
                        "Error Message...", Text, null);

            }
        } );
    }
}