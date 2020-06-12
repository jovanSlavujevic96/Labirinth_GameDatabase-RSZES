package com.example.sql_client;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SignInActivity extends AppCompatActivity
{
    private Button signinButton;
    private EditText email_nicknameEditText, passwordEditText;
    private TextView textView;
    private static ClientSocket clientSocket = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        if(clientSocket == null) {
            clientSocket = new ClientSocket();
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

        signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name_mail = email_nicknameEditText.getText().toString(),
                        password = passwordEditText.getText().toString();

                final String msg = "SIGN_IN\n" + name_mail + '\n' + password;
                if( true == clientSocket.SendMessage(msg) )
                {

                }
            }
        } );


    }
}