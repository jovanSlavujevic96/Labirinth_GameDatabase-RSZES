package com.example.sql_client;

import androidx.appcompat.app.AppCompatActivity;

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
                String email, nickname, password;
                email = emailEditText.getText().toString();
                nickname = usernameEditText.getText().toString();
                password = passwordEditText.getText().toString();

                String data = "SIGN_IN jotzilla96@gmail.com vujsla 1706996";
                //socketConnection.send_text(data);

                //Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                //startActivity(intent);
            }
        } );
    }
}