package com.simplecoding.messagingapp.client;

import android.widget.TextView;
import com.simplecoding.messagingapp.config.ServerConfig;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingDeque;

public class Client {

    // Network Communication
    private Socket socket = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;
    private final LinkedBlockingDeque<String> MESSAGES;
    private LinkedBlockingDeque<String> CLIENT_MESSAGES = new LinkedBlockingDeque<>();
    private String username;
    private Thread readFromServerThread;
    private Thread addMessagesToClient;
    private boolean loop = true;

    public Client(String username, TextView chat){
        this.username = username;
        this.MESSAGES = new LinkedBlockingDeque<>();
        try{
            socket = new Socket(ServerConfig.ADDRESS, ServerConfig.PORT);

            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            out = new DataOutputStream(socket.getOutputStream());

            out.writeUTF(this.username);

            // Server welcome message
            String welcomeMessage = in.readUTF();
            chat.append(welcomeMessage);

            addMessagesToClient = new Thread(){

                public void run(){
                    String message = "";
                    while(loop){
                        try{
                            message = MESSAGES.take();
                            String finalMessage = message;
                            chat.append(finalMessage);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            };
            addMessagesToClient.start();


            ReadMessagesFromServer server = new ReadMessagesFromServer(in);
            readFromServerThread = new Thread(server);
            readFromServerThread.start();


            Thread sendMessagesToServer = new Thread(new Runnable() {
                @Override
                public void run() {
                    String message;
                    while (loop){
                        try{
                            message = CLIENT_MESSAGES.take();
                            out.writeUTF(message);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            });
            sendMessagesToServer.start();

            // Checks if there's need to close connection
            Thread disconnectListener = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true){
                        if(!loop){
                            disconnect();
                            break;
                        }
                    }
                }
            });
            disconnectListener.start();


        }catch (Exception e){
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }


    }

    private void disconnect() {
                try {
                    out.writeUTF("/disconnect");
                    loop = false;
                    readFromServerThread.stop();
                    in.close();
                    out.close();
                    socket.close();
                }catch (Exception e) {
                    e.printStackTrace();
                }
    }

    private class ReadMessagesFromServer implements Runnable{

        private DataInputStream in = null;

        public ReadMessagesFromServer(DataInputStream in){
            this.in = in;
        }

        @Override
        public void run() {
            try {
                String line;
                while (loop){
                    try {
                        line = in.readUTF();
                        MESSAGES.put(line);
                    }catch (Exception e2){
                        //e2.printStackTrace();
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public LinkedBlockingDeque<String> getCLIENT_MESSAGES() {
        return CLIENT_MESSAGES;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

}
