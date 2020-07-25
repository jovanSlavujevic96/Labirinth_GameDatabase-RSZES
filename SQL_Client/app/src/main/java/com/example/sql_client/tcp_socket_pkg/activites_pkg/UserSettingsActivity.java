package com.example.sql_client.tcp_socket_pkg.activites_pkg;

import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.sql_client.MainActivity;
import com.example.sql_client.R;
import com.example.sql_client.game_pkg.Player;
import com.example.sql_client.game_pkg.activities_pkg.GameMenu;
import com.example.sql_client.pop_up.ActivityInterface;
import com.example.sql_client.pop_up.PopUpHandler;
import com.example.sql_client.pop_up.ToastHandler;
import com.example.sql_client.tcp_socket_pkg.ClientSocket;

import java.util.ArrayList;
import java.util.List;

public class UserSettingsActivity extends ActivityInterface {

    private CheckBox changeBoxName,changeBoxEmail,changeBoxPass;
    private TextView currentName, currentEmail;
    private Button doChanges;
    private ImageButton gotoGamenMenu;

    static private List<EditText> editTextList = null;
    static private DialogInterface.OnClickListener Listener = null;

    @Override
    public void ActivityPopUpHandling(int PopUpType) { }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);

        if(null == editTextList)
        {
            editTextList = new ArrayList<>(5);
            Listener =  new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ClientSocket.ConnectWithServer();
                    Intent intent = new Intent(UserSettingsActivity.this, UserSettingsActivity.class);
                    startActivity(intent);
                }
            };
        }

        changeBoxName = findViewById(R.id.changeNameCheckbox);
        changeBoxEmail = findViewById(R.id.changeEmailCheckbox);
        changeBoxPass = findViewById(R.id.changePassCheckbox);

        currentName = findViewById(R.id.currentNameSpot);
        currentEmail = findViewById(R.id.currentMailSpot);

        currentName.setText(Player.getNickname() );
        currentEmail.setText(Player.getEmail() );

        editTextList.add(0, (EditText) findViewById(R.id.editNewName) );
        editTextList.add(1, (EditText) findViewById(R.id.editNewEmail) );
        editTextList.add(2, (EditText) findViewById(R.id.oldPassword) );
        editTextList.add(3, (EditText) findViewById(R.id.NewPassword) );
        editTextList.add(4, (EditText) findViewById(R.id.NewPasswordAgain) );

        doChanges = findViewById(R.id.buttonChange);
        gotoGamenMenu = findViewById(R.id.backToGameMenu);

        doChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if((changeBoxName.isChecked() && (changeBoxEmail.isChecked() || changeBoxPass.isChecked()) ) ||
                    (changeBoxEmail.isChecked() && (changeBoxName.isChecked() ||changeBoxPass.isChecked()) ) )
                {
                    ToastHandler.Notify(ActivityInterface.current_context, "You can't check more than one check-box!", true);
                    return;
                } if(!changeBoxName.isChecked() && !changeBoxEmail.isChecked() && !changeBoxPass.isChecked() ) {
                    ToastHandler.Notify(ActivityInterface.current_context, "You must check one check-box!", true);
                    return;
                }
                String text = null;
                if(changeBoxName.isChecked() ) {
                    if (currentName.getText().toString().contentEquals(editTextList.get(0).getText().toString())) {
                        text = "You can't enter same name!";
                    } else if (editTextList.get(0).getText().toString().isEmpty() || editTextList.get(2).getText().toString().isEmpty() ) {
                        text = "You can't let new nickname or old password field empty!";
                    } if(text != null) {
                        ToastHandler.Notify(ActivityInterface.current_context, text, true);
                        return;
                    }

                    String msg = "CHANGE_NAME\n" + Player.getEmail() + '\n' + editTextList.get(2).getText().toString() + '\n' + editTextList.get(0).getText().toString();
                    msg = ClientSocket.TransmitString(msg);
                    if (msg.equals("OK")){
                        ToastHandler.Notify(ActivityInterface.current_context, "Successfully changed name!", true);
                        Player.setNickname(editTextList.get(0).getText().toString());
                        Intent intent = new Intent(UserSettingsActivity.this, GameMenu.class);
                        startActivity(intent);
                        return;
                    }
                    text = msg;
                    if(msg.equals("ERR_SRV")){
                        text = "SQL server error";
                    }else if(msg.equals("ERR_USED_NAME")){
                        text = "Selected name is already in use";
                    }else if(msg.equals("ERR_BAD_PASS") ){
                        text = "Bad password";
                    }
                } else if(changeBoxEmail.isChecked() ) {
                    if (currentEmail.getText().toString().contentEquals(editTextList.get(1).getText().toString())) {
                        text = "You can't enter same email!";
                    } else if (editTextList.get(1).getText().toString().isEmpty() || editTextList.get(2).getText().toString().isEmpty() ) {
                        text = "You can't let new email or old password field empty!";
                    } if(text != null) {
                        ToastHandler.Notify(ActivityInterface.current_context, text, true);
                        return;
                    }

                    String msg = "CHANGE_EMAIL\n" + Player.getEmail() + '\n' + editTextList.get(2).getText().toString() + '\n' + editTextList.get(1).getText().toString();
                    msg = ClientSocket.TransmitString(msg);
                    if (msg.equals("OK")){
                        ToastHandler.Notify(ActivityInterface.current_context, "Successfully changed email!", true);
                        Player.setNickname(editTextList.get(0).getText().toString());
                        Intent intent = new Intent(UserSettingsActivity.this, GameMenu.class);
                        startActivity(intent);
                        return;
                    }
                    text = msg;
                    if(msg.equals("ERR_SRV")){
                        text = "SQL server error";
                    }else if(msg.equals("ERR_USED_NAME")){
                        text = "Selected email is already in use";
                    }else if(msg.equals("ERR_BAD_PASS") ){
                        text = "Bad password";
                    }
                } else if(changeBoxPass.isChecked() ) {
                    if (editTextList.get(2).getText().toString().contentEquals(editTextList.get(3).getText().toString()) ) {
                        text = "You can't enter same password!";
                    } else if (editTextList.get(2).getText().toString().isEmpty() ||
                            editTextList.get(3).getText().toString().isEmpty() || editTextList.get(4).getText().toString().isEmpty() )
                    {
                        text = "You can't let password fields empty!";
                    } else if(false == editTextList.get(3).getText().toString().contentEquals(editTextList.get(4).getText().toString()) ){
                        text = "New password must be same in both fields!";
                    }if(text != null) {
                        ToastHandler.Notify(ActivityInterface.current_context, text, true);
                        return;
                    }

                    String msg = "CHANGE_PASS\n" + Player.getEmail() + '\n' + editTextList.get(2).getText().toString() + '\n' + editTextList.get(3).getText().toString();
                    msg = ClientSocket.TransmitString(msg);
                    if (msg.equals("OK")){
                        ToastHandler.Notify(ActivityInterface.current_context, "Successfully changed password!", true);
                        Player.setNickname(editTextList.get(0).getText().toString());
                        Intent intent = new Intent(UserSettingsActivity.this, GameMenu.class);
                        startActivity(intent);
                        return;
                    }
                    text = msg;
                    if(msg.equals("ERR_SRV")){
                        text = "SQL server error";
                    }else if(msg.equals("ERR_BAD_PASS") ){
                        text = "Bad password";
                    }
                }
                PopUpHandler.PopUp(ActivityInterface.current_context,-1, "Error..." , text, Listener);
            }
        } );

        gotoGamenMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(UserSettingsActivity.this, GameMenu.class);
                startActivity(intent);
            }
        } );
    }
}