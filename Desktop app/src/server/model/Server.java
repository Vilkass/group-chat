package server.model;

import config.ServerConfig;
import javafx.application.Platform;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.LinkedBlockingDeque;

public class Server {

    // Back-End
    private Socket socket = null;
    private ServerSocket serverSocket = null;
    private ArrayList<HandleClient> connectedClients = new ArrayList<>();
    private LinkedBlockingDeque<String> messagesForClients = new LinkedBlockingDeque<>();
    private SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");

    // Front-End
    private ListView<String> clientsList;
    private TextArea serverLogs;

    public Server(TextArea serverLogs, ListView clientList){

        this.clientsList = clientList;
        this.serverLogs = serverLogs;

        // Accept Clients
        Thread acceptClients = new Thread(){
            public void run(){
                try {
                    serverSocket = new ServerSocket(ServerConfig.PORT);
                    serverLogs.appendText("Server started!\n");
                    serverLogs.appendText("Waiting for clients...");

                    while(true){
                        socket = serverSocket.accept();
                        HandleClient client = new HandleClient(socket, socket.getPort());
                        connectedClients.add(client);
                        new Thread(client).start();
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
        acceptClients.start();

        // Sent Messages To Clients
        Thread writeMessages = new Thread(){
            public void run(){
                while(true){
                    try {
                        String message = messagesForClients.take();
                        for(HandleClient client : connectedClients){
                            // client.username != null
                            if(client.socket != null){
                                client.write("\n> " + message);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        writeMessages.start();

    }

    public class HandleClient implements Runnable{

        private DataInputStream in = null;
        private DataOutputStream out = null;
        private String username;
        public Socket socket;
        public int port;

        public HandleClient(Socket socket, int port) {

            this.port = port;
            this.socket = socket;
            
            try{
                this.in = new DataInputStream(socket.getInputStream());
                this.out = new DataOutputStream(socket.getOutputStream());
            }catch (Exception e){
                e.printStackTrace();
            }

        }

        @Override
        public void run() {
            try{
                // Get username
                String username = null;

                while (username == null){
                    try{
                        username = in.readUTF();
                    }catch (Exception e){
                        //e.printStackTrace();
                    }
                    this.username = username;
                }

                if(!username.isEmpty()){
                    // Check if such username is already connected
                    for(String connectedUsers : clientsList.getItems()){
                        if(username.equals(connectedUsers)){
                            out.writeUTF(ServerConfig.USERNAME_TAKEN);
                            connectedClients.remove(this);
                            this.in.close();
                            this.out.close();
                            this.socket.close();
                            return;
                        }
                    }
                    // Allow username
                    addClientToUi(this.username);
                    out.writeUTF("Connection accepted\nWelcome " + this.username);

                }else {
                    // Ghost Mode
                    out.writeUTF("Connection accepted\nNOTE: SINCE YOU HAVEN'T PROVIDED AN USERNAME, YOU'RE IN READ ONLY MODE.");
                }

                // Log connected clients
                if(!username.isBlank()){
                    try {
                        messagesForClients.put("SERVER: " + this.username + " connected!");
                        addServerLog("CLIENT " + this.username + " connected!");
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                String line = "";
                while(!line.equals(ServerConfig.DISCONNECT)){
                    try{
                        line = in.readUTF();
                        if(line.equals(ServerConfig.HELP_COMMAND)){
                            out.writeUTF(ServerConfig.ALL_COMMANDS);
                            addServerLog("CLIENT " + this.username + " > " + ServerConfig.HELP_COMMAND);
                            continue;
                        }
                        if(line.equals(ServerConfig.ALL_USERS)){
                            int count = 0;
                            String tmp = "";
                            for(HandleClient client : connectedClients){
                                if(client.username != null){
                                    count++;
                                    tmp += client.username + ", ";
                                }
                            }
                            String allUsers = "\n =======================\n Total users connected: " + count+"\n [";
                            allUsers += tmp;
                            allUsers = allUsers.substring(0, allUsers.length()-2);
                            allUsers += "]\n =======================";
                            out.writeUTF(allUsers);
                            addServerLog("CLIENT " + this.username + " > " + ServerConfig.ALL_USERS);
                            continue;
                        }
                        if(!this.username.isBlank() && line.charAt(0) != '/'){
                            String formatted_line = this.username + ": " + line;
                            messagesForClients.put(formatted_line);
                            addServerLog(formatted_line);
                        }
                    }catch (EOFException timedOut){
                        addServerLog("CLIENT " + username + " timed out!");
                        messagesForClients.put("SERVER: " + username + " timed out");
                        username = " ";
                        break;
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                if(!username.isBlank()){
                    try{
                        messagesForClients.put("SERVER: " + this.username + " disconnected!");
                        addServerLog("CLIENT " + this.username + " disconnected!");
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                try{
                    Thread.sleep(2000);
                }catch (Exception e){
                    e.printStackTrace();
                }

                removeClientFromUi(this.username);
                connectedClients.remove(this);
                this.in.close();
                this.out.close();
                this.socket.close();

            }catch (Exception e){
                e.printStackTrace();
            }
        }

        private void addServerLog(String message) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Date date = new Date(System.currentTimeMillis());
                    String dateFormatted = formatter.format(date);
                    serverLogs.appendText("\n" + dateFormatted + " " + message);
                }
            });
        }

        private void addClientToUi(String username) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    clientsList.getItems().add(username);
                }
            });
        }

        private void removeClientFromUi(String username) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    clientsList.getItems().remove(username);
                }
            });
        }

        public void write(String message) {
            try{
                this.out.writeUTF(message);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


}

