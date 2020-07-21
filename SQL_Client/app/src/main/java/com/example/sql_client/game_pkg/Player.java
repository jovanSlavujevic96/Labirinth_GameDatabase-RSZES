package com.example.sql_client.game_pkg;

import android.widget.Chronometer;

public class Player
{
    static private int m_HighestLevel, m_HighestPoints ;
    static private volatile int m_TmpLevel , m_TmpPoints ;
    static private boolean m_offline = true;
    static private String m_Email = null, m_Nickname = null;
    static boolean alreadyInitialised = false, scoreChanged = false;

    public boolean getScoreChanged()
    {
        if(scoreChanged)
        {
            scoreChanged = false;
            return true;
        }
        return false;
    }

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

    public void setRecord(int level, int points)
    {
        if(this.m_HighestPoints < points)
        {
            this.m_HighestPoints = points;
            this.m_HighestLevel = level;
            scoreChanged = true;
        }
        this.m_TmpLevel = level;
        this.m_TmpPoints = points;
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

}
