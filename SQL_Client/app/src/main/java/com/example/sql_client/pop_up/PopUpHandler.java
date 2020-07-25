package com.example.sql_client.pop_up;

import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;


public class PopUpHandler {
    static private AlertDialog.Builder dlgAlert = null;

    static public void PopUp(final ActivityInterface activity, final int PopUpType,
                             final String Title, final String Text, final DialogInterface.OnClickListener listener)
    {
        if(null == activity)
        {
            return;
        }
        dlgAlert = new AlertDialog.Builder(activity);

        dlgAlert.setPositiveButton("OK", listener );
        dlgAlert.setCancelable(true);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run(){
                activity.ActivityPopUpHandling(PopUpType);
                dlgAlert.setTitle(Title);
                dlgAlert.setMessage(Text);
                dlgAlert.create().show();
            }
        });
    }

}
