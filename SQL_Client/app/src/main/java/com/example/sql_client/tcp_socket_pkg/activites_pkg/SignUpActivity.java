package com.example.sql_client.tcp_socket_pkg.activites_pkg;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.sql_client.MainActivity;
import com.example.sql_client.pop_up.ActivityInterface;
import com.example.sql_client.R;
import com.example.sql_client.game_pkg.Player;
import com.example.sql_client.game_pkg.activities_pkg.GameMenu;
import com.example.sql_client.pop_up.PopUpHandler;
import com.example.sql_client.tcp_socket_pkg.ClientSocket;

public class SignUpActivity extends ActivityInterface
{
    private Button signUpButton;
    private EditText emailEditText, usernameEditText, passwordEditText;
    private TextView textView;

    static private DialogInterface.OnClickListener Listener = null;

    @Override
    public void ActivityPopUpHandling(int PopUpType) { }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        ClientSocket.setActivityInterface(this);
        if(null == Listener) {
            Listener =  new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ClientSocket.ConnectWithServer();
                    Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            };
        }

        textView = findViewById(R.id.already_account);
        signUpButton = findViewById(R.id.signup);
        emailEditText = findViewById(R.id.email);
        usernameEditText = findViewById(R.id.nickname);
        passwordEditText = findViewById(R.id.password);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                startActivity(intent);
            }
        } );

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                if(!ClientSocket.isConnected() )
                {
                    PopUpHandler.PopUp(ActivityInterface.current_context, -1, "Error Message...", "You can't sign in.\nYou are not connected to the server.\nWe are trying to reconnect.", Listener);
                    return;
                }

                String msg = "SIGN_UP\n" + emailEditText.getText().toString() + '\n'
                        + usernameEditText.getText().toString() + '\n'
                        + passwordEditText.getText().toString();
                msg = ClientSocket.TransmitString(msg);

                if(msg.contentEquals("OK") )
                {
                    Player.setOffline(false);
                    Player.setNickname(usernameEditText.getText().toString() );
                    Player.setEmail(emailEditText.getText().toString() );

                    Intent intent = new Intent(SignUpActivity.this, GameMenu.class);
                    startActivity(intent);
                    return;
                }

                String Text = msg;
                if(msg.contentEquals("ERR_USED_NAME")) {
                    Text = "This name has already been used";
                } else if(msg.contentEquals("ERR_USED_MAIL")){
                    Text = "This email address has already been used";
                }
                PopUpHandler.PopUp(ActivityInterface.current_context, -1,
                        "Error Message...", Text, null);
            }
        } );
    }
}