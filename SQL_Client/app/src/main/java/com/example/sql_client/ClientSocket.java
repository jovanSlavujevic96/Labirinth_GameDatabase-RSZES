package com.example.sql_client;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import static android.content.Context.MODE_PRIVATE;

public class ClientSocket
{
    static private Thread Thread1 = null;
    static private Socket Socket = null;
    static private PrintWriter printWriter = null;
    static private BufferedReader bufferedReader = null;
    static private volatile String rcvMsg = null, sndMsg = null;
    final private String SRV_IP = "192.168.0.200";
    final private int SRV_PORT = 8080;
    static private volatile boolean connected = false, file_received = false;
    public volatile boolean Exception_happened = false;
    final private String LDB_Command = "GET_LDB\n", FILE_NAME = "test.txt";

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
            FileOutputStream fos = null;
            try
            {
                fos = context.openFileOutput(FILE_NAME, MODE_PRIVATE);
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
                ClientSocket.this.Exception_happened = true;
                return;
            }

            ClientSocket.this.rcvMsg = null;
            new Thread(new StringTransmission() ).start();
            while(null == ClientSocket.this.rcvMsg );

            ClientSocket.this.rcvMsg.substring(0, ClientSocket.this.rcvMsg.length() - 1);
            int LineNumbers = Integer.parseInt(ClientSocket.this.rcvMsg);

            printWriter.write("OK\n");
            printWriter.flush();

            while (LineNumbers != 0) {
                try {
                    ClientSocket.this.rcvMsg = bufferedReader.readLine();
                    ClientSocket.this.rcvMsg += '\n';
                    fos.write(ClientSocket.this.rcvMsg.getBytes() );
                    LineNumbers--;
                } catch (IOException e) {
                    e.printStackTrace();
                    ClientSocket.this.connected = false;
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
