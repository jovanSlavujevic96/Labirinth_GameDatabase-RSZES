package com.example.sql_client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientSocket
{
    static private Thread Thread1 = null;
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

    public String Communicate(String msgData)
    {
        if(false == connected || this.sndMsg != null)
        {
            Thread1 = new Thread(new Connector() );
            Thread1.start();
            return null;
        }
        this.sndMsg = msgData;
        new Thread(new Communication() ).start();
        while(null == this.rcvMsg);
        final String tmp = this.rcvMsg;
        this.rcvMsg = null;
        return tmp;
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
                    sndMsg = null;
                    rcvMsg = null;
                    break;
                }
                catch (IOException e)
                {
                    connected = false;
                    e.printStackTrace();
                }
            }
        }
    }

    private class Communication implements Runnable
    {
        @Override
        public void run()
        {
            ClientSocket.this.printWriter.write(ClientSocket.this.sndMsg);
            ClientSocket.this.printWriter.flush();
            ClientSocket.this.sndMsg = null;
            ClientSocket.this.rcvMsg = null;
            while(null == ClientSocket.this.rcvMsg) {
                try {
                    ClientSocket.this.rcvMsg = bufferedReader.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                    ClientSocket.this.connected = false;
                }
            }
        }
    }
}
