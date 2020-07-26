package com.example.sql_client.tcp_socket_pkg;

import com.example.sql_client.game_pkg.Player;
import com.example.sql_client.pop_up.ActivityInterface;
import com.example.sql_client.pop_up.PopUpHandler;
import com.example.sql_client.pop_up.ToastHandler;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class ClientSocket
{
    static private volatile Socket Socket = null;
    static private BufferedReader bufferedReader = null;
    static private PrintWriter printWriter = null;

    static private volatile String rcvMsg = null, sndMsg = null;
    static private Thread ConnectorTH = null, ReceiverHandlerTH = null;

    static final private String SRV_IP = "192.168.0.200",
            LDB_Command = "GET_LDB\n",
            FILE_NAME = "leaderboard.xml";
    static final private int SRV_PORT = 8080;
    static private volatile boolean connected = false, bufferBoolean = true, file_received = false, file_transmitting = false, Exception_happened = false, up_to_date = false;

    static private ActivityInterface activityInterface = null;
    static private volatile List<String> XmlLines = null;

    static public void setActivityInterface(ActivityInterface activityInterfaceToSet)
    {
        activityInterface = activityInterfaceToSet;
        if(null == ConnectorTH) {
            ConnectorTH = new Thread(new Connector());
            ConnectorTH.start();

            XmlLines = new ArrayList<>();
        }
    }

    static public void ConnectWithServer()
    {
        ConnectorTH = new Thread(new Connector());
        ConnectorTH.start();
        Player.resetPlayersInfO();
    }

    static public void DisconnectWithServer()
    {
        try{
            Socket.shutdownInput();
            Socket.close();
            connected = false;
        }catch (SocketException e) {
            PopUpHandler.PopUp(activityInterface, -1, "Warning...", "Client disconnected with server", null);
        }catch (Exception e){
            PopUpHandler.PopUp(activityInterface, -1, "Exception happened...", e.toString(), null);
        }
    }

    static public String TransmitString(String msgData)
    {
        if(false == connected )
        {
            PopUpHandler.PopUp(activityInterface, -1, "Error...", "You are not connected!", null);
            return null;
        }

        ClientSocket.bufferBoolean = false;
        sndMsg = msgData;
        rcvMsg = null;
        new Thread(new Transmitter(false) ).start();
        while(false == ClientSocket.bufferBoolean);
        return rcvMsg;
    }

    static public boolean TransmitFile(String msgData)
    {
        if(false == connected  || !msgData.contentEquals(LDB_Command) )
        {
            PopUpHandler.PopUp(activityInterface, -1, "Error...", "You are not connected!", null);
            return false;
        }

        file_received = false;
        file_transmitting = true;
        sndMsg = msgData;
        new Thread(new Transmitter(true) ).start();
        while(false == file_received && false == ClientSocket.Exception_happened);
        if(ClientSocket.Exception_happened)
        {
            PopUpHandler.PopUp(activityInterface, -1, "Error Message Received...", ClientSocket.rcvMsg, null);
            rcvMsg = null;
            return false;
        }
        return true;
    }

    static private class Connector implements Runnable
    {
        @Override
        public void run()
        {
            int i=10;
            while(i-- > 0)
            {
                try{
                    Socket = new Socket(SRV_IP, SRV_PORT);
                    printWriter = new PrintWriter(Socket.getOutputStream() );
                    bufferedReader = new BufferedReader(new InputStreamReader(Socket.getInputStream() ));
                    connected = true;
                    sndMsg = null;
                    rcvMsg = null;
                    ToastHandler.Notify(ClientSocket.activityInterface, "Connected to Server", true);
                    ReceiverHandlerTH = new Thread(new ClientSocket.Receiver() );
                    ReceiverHandlerTH.start();
                    connected = true;
                    break;
                } catch (IOException e) {
                    //PopUpHandler.PopUp(ClientSocket.activityInterface, -1,"Exception happened...", e.toString(), null);
                    connected = false;
                    break;
                }
            }
            if(!connected || 0 == i) {
                ToastHandler.Notify(ClientSocket.activityInterface, "There is no connection with Server", false);
            }
        }
    }

    static private class Receiver implements Runnable
    {
        @Override
        public void run() {
            boolean list_cleared = false;
            while(true)
            {
                try{
                    ClientSocket.rcvMsg = bufferedReader.readLine();
                    ClientSocket.bufferBoolean = true;
                    if(null == ClientSocket.rcvMsg)
                    {
                        break;
                    }
                    if(file_transmitting)
                    {
                        if(!list_cleared){
                            XmlLines.clear();
                            list_cleared = true;
                        }
                        XmlLines.add(ClientSocket.rcvMsg + '\n');
                        if(ClientSocket.rcvMsg.contains("</leaderboard>") ){
                            list_cleared = false;
                            file_transmitting = false;
                        }
                    }
                }catch(IOException e){
                    //PopUpHandler.PopUp(ClientSocket.activityInterface, -1, "Exception happened...", e.toString(), null);
                    //catching those exception on the other side
                    break;
                }
            }
            ToastHandler.Notify(ClientSocket.activityInterface, "Disconnected to Server", false);
            ClientSocket.connected = false;
            Player.resetPlayersInfO();
        }

    }

    static private class Transmitter implements Runnable
    {
        private boolean sendFile;
        public Transmitter(boolean sendFile)
        {
            this.sendFile = sendFile;
        }

        @Override
        public void run()
        {
            ClientSocket.bufferBoolean = false;
            printWriter.write(ClientSocket.sndMsg);
            printWriter.flush();
            if(!sendFile)
            {
                return;
            }
            while(false == ClientSocket.bufferBoolean );
            if(ClientSocket.rcvMsg.contentEquals("UP_TO_DATE") )
            {
                ClientSocket.up_to_date = true;
                ClientSocket.file_received = true;
                return;
            }else if(ClientSocket.rcvMsg.contains("ERR")){
                ClientSocket.Exception_happened = true;
                ClientSocket.up_to_date = false;
                return;
            }
            ClientSocket.up_to_date = false;
            while(true == file_transmitting );

            try {
                FileOutputStream fos =  activityInterface.getApplicationContext().openFileOutput(FILE_NAME, MODE_PRIVATE);
                for (int i = 0; i < XmlLines.size(); i++)
                {
                    fos.write(XmlLines.get(i).getBytes() );
                }
                fos.close();
            } catch (Exception e) {
                PopUpHandler.PopUp(ClientSocket.activityInterface, -1, "Exception happened...", e.toString(), null);
            }
            ClientSocket.file_received = true;
        }
    }

    static public String getFileName()
    {
        return FILE_NAME;
    }

    static public boolean isConnected()
    {
        return connected;
    }

    static public boolean isUpToDate()
    {
        return up_to_date;
    }

}
