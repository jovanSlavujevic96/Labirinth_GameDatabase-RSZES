package com.example.sql_client.pop_up;

import android.view.Gravity;
import android.widget.Toast;

public class ToastHandler {

    static private class ToastIt implements Runnable
    {
        private String text;
        private ActivityInterface activity;
        private int duration;
        public ToastIt(final ActivityInterface Activity, final String Text, final int Duration)
        {
            activity = Activity;
            text = Text;
            duration = Duration;
        }
        @Override
        public void run()
        {
            Toast toast = Toast.makeText(activity.getApplicationContext(), text, duration);
            toast.setGravity(Gravity.CENTER, 0,0);
            toast.show();
        }
    }

    static public void Notify(final ActivityInterface activity, final String Text, final boolean shortDuration)
    {
        if(activity == null)
        {
            return;
        }
        int duration = Toast.LENGTH_SHORT;
        if(!shortDuration)
        {
            duration = Toast.LENGTH_LONG;
        }
        activity.runOnUiThread(new ToastIt(activity, Text, duration )) ;
    }
}
