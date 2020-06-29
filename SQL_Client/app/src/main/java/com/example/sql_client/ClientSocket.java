package com.example.sql_client;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Environment;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static androidx.core.app.ActivityCompat.requestPermissions;

public class ClientSocket
{
    static private Thread Thread1 = null;
    static private Socket Socket = null;
    static private PrintWriter printWriter = null;
    static private BufferedReader bufferedReader = null;
    static private volatile String rcvMsg = null, sndMsg = null;
    final private String SRV_IP = "192.168.0.200";
    final private int SRV_PORT = 8080, RequsetCode = 656;
    static private volatile boolean connected = false, file_received = false;
    public volatile boolean Exception_happened = false;
    final private String LDB_Command = "GET_LDB\n";
    static private Activity activity = null;

    public ClientSocket()
    {
        if(Thread1 == null) {
            Thread1 = new Thread(new Connector() );
            Thread1.start();
        }
    }

    public void SetActivity(Activity activity)
    {
        this.activity = activity;
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

    public boolean TransmitFile(String msgData)
    {
        if(false == connected || this.sndMsg != null || !msgData.contentEquals(LDB_Command) )
        {
            Thread1 = new Thread(new Connector() );
            Thread1.start();
            return false;
        }
        this.file_received = false;
        this.sndMsg = msgData;
        new Thread(new FileTransmission() ).start();
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
        @Override
        public void run()
        {
//            File files_folder = new File(Environment.getDownloadCacheDirectory().getAbsolutePath() );
//            File files_child = new File(files_folder, "files_child");
//            files_child.mkdirs();
//            File created_folder = new File()
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                requestPermissions(ClientSocket.this.activity,
                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE}, ClientSocket.this.RequsetCode);
            }

            File root = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "My Folder");
            if (!root.exists() )
            {
                root.mkdir();
            }
            if (!root.exists() )
            {
                Exception_happened = true;
                return;
            }
            File filepath = new File(root.getAbsolutePath() + File.separator + "My Text.txt");
            try {
                FileWriter writer = new FileWriter(filepath);
            }
            catch(FileNotFoundException e)
            {
                Exception_happened = true;
                return;
            }
            catch (IOException e) {

            }

            ClientSocket.this.rcvMsg = null;
            new Thread(new StringTransmission() ).start();
            while(null == ClientSocket.this.rcvMsg );

            printWriter.write("OK\n");
            printWriter.flush();

            ClientSocket.this.rcvMsg.substring(0, ClientSocket.this.rcvMsg.length() - 1);
            int LineNumbers = Integer.parseInt(ClientSocket.this.rcvMsg);
            while (LineNumbers != 0) {
                try {
                    ClientSocket.this.rcvMsg = bufferedReader.readLine();
                    LineNumbers--;
                } catch (IOException e) {
                    e.printStackTrace();
                    ClientSocket.this.connected = false;
                }
            }

            ClientSocket.this.file_received = true;
/*
            try
            {
                File root = new File(Environment.getExternalStorageDirectory(), "My Folder");
                if (!root.exists() )
                {
                    root.mkdir();
                }
                File filepath = new File(root, "myfile.txt");
                FileWriter writer = new FileWriter(filepath);
                writer.append("Look how the turn tables!\n");
                writer.flush();
                writer.close();

                ClientSocket.this.sndMsg = filepath.getPath();

                ClientSocket.this.printWriter.write(ClientSocket.this.sndMsg);
                ClientSocket.this.printWriter.flush();

                ClientSocket.this.sndMsg = null;
                ClientSocket.this.file_received = true;
            }
            catch (IOException e)
            {

            }
*/
//            File file = new File(Environment.getExternalStorageDirectory(),"test.txt");
//            while(true)
//            {
//                try {
//                    byte[] bytes = new byte[1024];
//                    InputStream is = ClientSocket.this.Socket.getInputStream();
//                    FileOutputStream fos = new FileOutputStream(file);
//                    BufferedOutputStream bos = new BufferedOutputStream(fos);
//                    int bytesRead = is.read(bytes, 0, bytes.length);
//                    bos.write(bytes, 0, bytesRead);
//                    bos.close();
//                    ClientSocket.this.file_received = true;
//                    break;
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    ClientSocket.this.connected = false;
//                }
//            }

        }
    }


}
