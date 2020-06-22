package com.example.sql_client;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SignUpActivity extends AppCompatActivity
{
    private Button signupButton;
    private EditText emailEditText, usernameEditText, passwordEditText;
    private TextView textView;
    private static ClientSocket clientSocket = null;
    private static AlertDialog.Builder dlgAlert = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        textView = findViewById(R.id.already_account);
        signupButton = findViewById(R.id.signup);
        emailEditText = findViewById(R.id.email);
        usernameEditText = findViewById(R.id.nickname);
        passwordEditText = findViewById(R.id.password);

        if(null == clientSocket)
        {
            clientSocket = new ClientSocket();
        }

        if(null == dlgAlert)
        {
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

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                startActivity(intent);
            }
        } );

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                final String email = emailEditText.getText().toString(),
                nickname = usernameEditText.getText().toString(),
                password = passwordEditText.getText().toString();

                String msg = "SIGN_UP\n" + email + '\n' + nickname + '\n' + password;
                msg = clientSocket.Communicate(msg);

                if(msg.contentEquals("ERR_USED_NAME"))
                {
                    dlgAlert.setMessage("This name has already been used");
                    dlgAlert.create().show();
                }
                else if(msg.contentEquals("ERR_USED_MAIL"))
                {
                    dlgAlert.setMessage("This email address has already been used");
                    dlgAlert.create().show();
                }
                else if(msg.contentEquals("OK") )
                {
                    Intent intent = new Intent(SignUpActivity.this, GameMenu.class);
                    startActivity(intent);
                }
            }
        } );
    }
}