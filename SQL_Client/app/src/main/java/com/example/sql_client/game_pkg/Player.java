package com.example.sql_client.game_pkg;

public class Player
{
    static private int m_HighestLevel, m_HighestPoints;
    static private volatile int m_TmpLevel , m_TmpPoints ;
    static private boolean m_offline = true;
    static private String m_Email = null, m_Nickname = null;
    static private boolean scoreChanged = false;

    static public boolean getScoreChanged()
    {
        if(scoreChanged)
        {
            scoreChanged = false;
            return true;
        }
        return false;
    }

    static public void setEmail(String email)
    {
        m_Email = email;
    }

    static public String getEmail()
    {
        return m_Email;
    }

    static public void setNickname(String nickname)
    {
        m_Nickname = nickname;
    }

    static public String getNickname()
    {
        return m_Nickname;
    }

    static public void setOffline(boolean offline)
    {
        m_offline = offline;
    }

    static public boolean getOffline()
    {
        return m_offline;
    }

    static public void resetTmpScore()
    {
        m_TmpLevel = 0;
        m_TmpPoints = 0;
    }

    static public void setRecord(int level, int points)
    {
        if(m_HighestPoints < points)
        {
            m_HighestPoints = points;
            m_HighestLevel = level;
            scoreChanged = true;
        }
        m_TmpLevel = level;
        m_TmpPoints = points;
    }


    static public int getHighestLevel()
    {
        return m_HighestLevel;
    }

    static public int getHighestPoints()
    {
        return m_HighestPoints;
    }

    static public int getTmpLevel()
    {
        return m_TmpLevel;
    }

    static public int getTmpPoints()
    {
        return m_TmpPoints;
    }

}
