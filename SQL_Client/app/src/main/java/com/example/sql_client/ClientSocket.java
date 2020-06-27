package com.example.sql_client;

import android.os.Environment;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
    static private volatile boolean connected = false, file_received = false;

    public ClientSocket()
    {
        if(Thread1 == null) {
            Thread1 = new Thread(new Connector() );
            Thread1.start();
        }
    }

    public String TransmitString(String msgData)
    {
        if(false == connected || this.sndMsg != null || msgData.contentEquals("GET_RESULT") )
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
        return tmp;
    }

    public boolean TransmitFile(String msgData)
    {
        if(false == connected || this.sndMsg != null || msgData.contentEquals("GET_RESULT") )
        {
            Thread1 = new Thread(new Connector() );
            Thread1.start();
            return false;
        }
        this.file_received = false;
        this.sndMsg = msgData;
        new Thread(new FileTransmission() ).start();
        return this.file_received;
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

    private class StringTransmission implements Runnable
    {
        @Override
        public void run()
        {
            ClientSocket.this.rcvMsg = null;
            ClientSocket.this.printWriter.write(ClientSocket.this.sndMsg);
            ClientSocket.this.printWriter.flush();
            ClientSocket.this.sndMsg = null;
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

    private class FileTransmission implements Runnable
    {
        @Override
        public void run() {
            ClientSocket.this.printWriter.write(ClientSocket.this.sndMsg);
            ClientSocket.this.printWriter.flush();
            ClientSocket.this.sndMsg = null;
            File file = new File(Environment.getExternalStorageDirectory(),"test.txt");
            while(true)
            {
                try {
                    byte[] bytes = new byte[1024];
                    InputStream is = ClientSocket.this.Socket.getInputStream();
                    FileOutputStream fos = new FileOutputStream(file);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    int bytesRead = is.read(bytes, 0, bytes.length);
                    bos.write(bytes, 0, bytesRead);
                    bos.close();
                    ClientSocket.this.file_received = true;
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                    ClientSocket.this.connected = false;
                }
            }
        }
    }
}
