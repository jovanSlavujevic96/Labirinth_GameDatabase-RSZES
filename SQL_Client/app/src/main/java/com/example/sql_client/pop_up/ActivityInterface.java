package com.example.sql_client.pop_up;

import androidx.appcompat.app.AppCompatActivity;

abstract public class ActivityInterface extends AppCompatActivity
{
    abstract public void ActivityPopUpHandling(int PopUpType);

    static protected ActivityInterface current_context = null;

    @Override
    protected void onPause(){
        current_context = null;
        super.onPause();
    }

    @Override
    protected void onResume(){
        current_context = this;
        super.onResume();
    }
}
