package com.example.sql_client.tcp_socket_pkg;

import android.content.Context;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;


import static android.content.Context.MODE_PRIVATE;

public class ClientSocket
{
    static private Thread Thread1 = null;
    static private Socket Socket = null;
    static private PrintWriter printWriter = null;
    static private BufferedReader bufferedReader = null;
    static private volatile String rcvMsg = null, sndMsg = null;
    static final private String SRV_IP = "192.168.0.200";
    static final private int SRV_PORT = 8080;
    static private volatile boolean connected = false, file_received = false;
    public volatile boolean Exception_happened = false;
    static final private String LDB_Command = "GET_LDB\n", FILE_NAME = "leaderboard.xml";

    public ClientSocket()
    {
        if(null == Thread1)
        {
            Thread1 = new Thread(new Connector() );
            Thread1.start();
        }
        Exception_happened = false;
    }

    public String TransmitString(String msgData)
    {
        if(false == connected || this.sndMsg != null )
        {
            Thread1 = new Thread(new Connector() );
            Thread1.start();
            return null;
        }
        this.sndMsg = msgData;
        new Thread(new StringTransmission() ).start();
        while(null == this.rcvMsg);
        final String tmp = this.rcvMsg;
        this.rcvMsg = null;
        this.sndMsg = null;
        return tmp;
    }

    public boolean TransmitFile(String msgData, Context context)
    {
        if(false == connected || this.sndMsg != null || !msgData.contentEquals(LDB_Command) )
        {
            Thread1 = new Thread(new Connector() );
            Thread1.start();
            return false;
        }
        this.file_received = false;
        this.sndMsg = msgData;
        new Thread(new FileTransmission(context) ).start();
        while(false == this.file_received && false == this.Exception_happened);
        this.sndMsg = null;
        this.rcvMsg = null;
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
                    sndMsg = null;
                    rcvMsg = null;
                    break;
                }
                catch (IOException e)
                {
                    connected = false;
//                    Exception_happened = true;
                    e.printStackTrace();
                }
            }
        }
    }

    private class StringTransmission implements Runnable
    {
        @Override
        public void run()
        {
            ClientSocket.this.rcvMsg = null;
            printWriter.write(ClientSocket.this.sndMsg);
            printWriter.flush();

            ClientSocket.this.sndMsg = null;
            while(null == ClientSocket.this.rcvMsg) {
                try {
                    ClientSocket.this.rcvMsg = bufferedReader.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                    ClientSocket.this.connected = false;
                    //Exception_happened = true;
                }
            }
        }
    }

    private class FileTransmission implements Runnable
    {
        private Context context = null;
        public FileTransmission(Context context)
        {
            this.context = context;
        }

        @Override
        public void run()
        {
            ClientSocket.this.rcvMsg = null;
            new Thread(new StringTransmission() ).start();
            while(null == ClientSocket.this.rcvMsg );

            List<String> list = new ArrayList<>();

            if(ClientSocket.this.rcvMsg.contentEquals("UP_TO_DATE") )
            {
                ClientSocket.this.file_received = true;
                return;
            }
            list.add(ClientSocket.this.rcvMsg + '\n');

            while (!ClientSocket.this.rcvMsg.contains("</leaderboard>"))
            {
                try {
                    ClientSocket.this.rcvMsg = bufferedReader.readLine();
                    list.add(ClientSocket.this.rcvMsg + '\n');
                } catch (IOException e) {
                    e.printStackTrace();
                    ClientSocket.this.connected = false;
                    ClientSocket.this.Exception_happened = true;
                    return;
                }
            }
            FileOutputStream fos = null;
            try {
                fos = context.openFileOutput(FILE_NAME, MODE_PRIVATE);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                ClientSocket.this.Exception_happened = true;
                return;
            }

            for (int i = 0; i < list.size(); i++)
            {
                try {
                    fos.write(list.get(i).getBytes() );
                } catch (IOException e) {
                    e.printStackTrace();
                    ClientSocket.this.Exception_happened = true;
                    return;
                }
            }

            if (fos != null)
            {
                try
                {
                    fos.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    ClientSocket.this.Exception_happened = true;
                    return;
                }
            }
            ClientSocket.this.file_received = true;
        }
    }


}
