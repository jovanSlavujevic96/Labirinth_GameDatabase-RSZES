package com.example.sql_client.game_pkg;

import android.widget.Chronometer;

import com.example.sql_client.game_pkg.activities_pkg.GameActivity;

public class Player
{
    static private int m_HighestLevel, m_HighestPoints ;
    static private Chronometer chronometer = null;
    static public int m_TmpLevel , m_TmpPoints ;
    static private boolean m_offline = true;
    static private String m_Email = null, m_Nickname = null;
    static boolean alreadyInitialised = false;

    public Player()
    {
        if(!alreadyInitialised) {
            m_HighestLevel = 0;
            m_HighestPoints = 0;
            m_TmpLevel = 0;
            m_TmpPoints = 0;
        }
        alreadyInitialised = true;
    }


    public void setEmail(String email)
    {
        this.m_Email = email;
    }

    public String getEmail()
    {
        return this.m_Email;
    }

    public void setNickname(String nickname)
    {
        this.m_Nickname = nickname;
    }

    public String getNickname()
    {
        return this.m_Nickname;
    }

    public void setOffline(boolean offline)
    {
        this.m_offline = offline;
    }

    public boolean getOffline()
    {
        return this.m_offline;
    }

    public void resetTmpScore()
    {
        this.m_TmpLevel = 0;
        this.m_TmpPoints = 0;
    }

    public void setChronometer(Chronometer chronometer)
    {
        this.chronometer = chronometer;
    }

    public void setRecord(int level, int points)
    {
        if(this.m_HighestPoints < points)
        {
            this.m_HighestPoints = points;
            this.m_HighestLevel = level;
        }
    }

    public int getHighestLevel()
    {
        return this.m_HighestLevel;
    }

    public int getHighestPoints()
    {
        return this.m_HighestPoints;
    }

    public int getTmpLevel()
    {
        return this.m_TmpLevel;
    }

    public int getTmpPoints()
    {
        return this.m_TmpPoints;
    }

    public int getElapsedTime()
    {
        return getSecondsFromDurationString( this.chronometer.getText().toString()) ;
    }

    private int getSecondsFromDurationString(String value){

        String [] parts = value.split(":");

        // Wrong format, no value for you.
        if(parts.length < 2 || parts.length > 3)
            return 0;

        int seconds = 0, minutes = 0, hours = 0;

        if(parts.length == 2){
            seconds = Integer.parseInt(parts[1]);
            minutes = Integer.parseInt(parts[0]);
        }
        else if(parts.length == 3){
            seconds = Integer.parseInt(parts[2]);
            minutes = Integer.parseInt(parts[1]);
            hours = Integer.parseInt(parts[0]);
        }

        return seconds + (minutes*60) + (hours*3600);
    }
}
