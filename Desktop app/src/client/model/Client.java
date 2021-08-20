package client.model;

import config.ServerConfig;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingDeque;

public class Client {

    // Network Communication
    private Socket socket = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;
    private final LinkedBlockingDeque<String> MESSAGES;
    private String username;
    private Thread readFromServerThread;
    private Thread addMessagesToClient;
    private boolean loop = true;

    // Front-End components
    private Button sendBtn;
    private Button discBtn;
    private TextField messageToSend;

    public Client(String username, TextFlow chat, Button sendBtn, Button discBtn, TextField messageToSend){
        this.username = username;
        this.MESSAGES = new LinkedBlockingDeque<>();
        this.sendBtn = sendBtn;
        this.discBtn = discBtn;
        this.messageToSend = messageToSend;
        try{
            // Connect to server socket
            socket = new Socket(ServerConfig.ADDRESS, ServerConfig.PORT);

            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            out = new DataOutputStream(socket.getOutputStream());

            // Send username to server
            out.writeUTF(this.username);

            // Server welcome message
            String welcomeMessage = in.readUTF();
            if(welcomeMessage.equals(ServerConfig.USERNAME_TAKEN)){
                // Username is taken
                in.close();
                out.close();
                socket.close();
                //System.out.println("Username is taken!");
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        returnToLoginWindow();
                        // Show alert !
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setHeaderText(null);
                        alert.setTitle("Error!");
                        alert.setContentText("Username already taken!");
                        alert.show();
                    }
                });
                return;
            }
            chat.getChildren().add(new Text(welcomeMessage));

            // Add messages to chat
            addMessagesToClient = new Thread(){

                public void run(){
                    String message = "";
                    while(loop){
                        try{
                            message = MESSAGES.take();
                            String finalMessage = message;
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    chat.getChildren().add(new Text(finalMessage));
                                }
                            });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            };
            addMessagesToClient.start();

            ReadMessagesFromServer server = new ReadMessagesFromServer();
            readFromServerThread = new Thread(server);
            readFromServerThread.start();

            // Set up UI components and their behavior
            setUpSendBtn();
            setUpDiscBtn();
            setUpEnterKeyListener();

        }catch (Exception e){
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }

    }

    private void setUpEnterKeyListener() {
        messageToSend.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER){
                sendMessageToServer();
            }
        });
    }

    private void setUpDiscBtn() {
        discBtn.setOnAction(e -> {
            try {
                out.writeUTF("/disconnect");
                loop = false;
                readFromServerThread.stop();
                in.close();
                out.close();
                socket.close();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        returnToLoginWindow();
                    }
                });
            }catch (Exception e2) {
                e2.printStackTrace();
            }
        });
    }

    private void returnToLoginWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/view/Login.fxml"));
            Parent parent = loader.load();

            Stage stage = (Stage)discBtn.getScene().getWindow();
            Scene scene = new Scene(parent);
            scene.getStylesheets().getClass().getResource("/client/view/app.css");
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessageToServer(){
        try {
            out.writeUTF(messageToSend.getText());
            messageToSend.setText("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpSendBtn() {
        sendBtn.setOnAction(e -> {
            sendMessageToServer();
        });
    }

    public static boolean checkConnection(){
        try{
            Socket socket = new Socket(ServerConfig.ADDRESS, ServerConfig.PORT);
            if(socket.isConnected()){
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    private class ReadMessagesFromServer implements Runnable{

        private DataInputStream in = null;

        @Override
        public void run() {
            try {
                this.in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                String line;
                while (loop){
                    try {
                        line = in.readUTF();
                        MESSAGES.put(line);
                    }catch (Exception e2){
                        // e2.printStackTrace();
                    }
                }
                this.in.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


}
