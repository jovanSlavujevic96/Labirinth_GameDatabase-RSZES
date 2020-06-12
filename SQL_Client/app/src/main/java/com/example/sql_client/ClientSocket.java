package com.example.sql_client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientSocket
{
    static private Thread Thread1 = null, Thread2 = null, Thread3 = null;
    static private Socket Socket = null;
    static private PrintWriter printWriter = null;
    static private BufferedReader bufferedReader = null;
    static private volatile String rcvMsg = null, sndMsg = null;
    final private String SRV_IP = "192.168.0.200";
    final private int SRV_PORT = 8080;
    static private volatile boolean connected = false;

    public ClientSocket()
    {
        if(Thread1 == null) {
            Thread1 = new Thread(new Connector() );
            Thread1.start();
        }
    }

    public boolean SendMessage(String msgData)
    {
        if(false == connected || this.sndMsg != null)
        {
            return false;
        }
        this.sndMsg = msgData;
        new Thread(new Talker() ).start();
        return true;
    }

    private class Connector implements Runnable
    {
        @Override
        public void run()
        {
            while(true)
            {
                try
                {
                    Socket = new Socket(SRV_IP, SRV_PORT);
                    printWriter = new PrintWriter(Socket.getOutputStream() );
                    bufferedReader = new BufferedReader(new InputStreamReader(Socket.getInputStream() ));
                    connected = true;
                    break;
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private class Listener implements Runnable
    {
        @Override
        public void run()
        {
            while(true)
            {
                try
                {
                    final String rcv = bufferedReader.readLine();
                    if(rcv != null)
                    {
                        rcvMsg = rcv;
                    }
                    else
                    {
                        connected = false;
                        Thread1 = new Thread(new Connector() );
                        Thread1.start();
                        break;
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }

        }
    }

    private class Talker implements Runnable
    {
        @Override
        public void run()
        {
            if(false == connected)
            {
                return;
            }
            printWriter.write(sndMsg);
            printWriter.flush();
            sndMsg = null;
        }
    }

}
