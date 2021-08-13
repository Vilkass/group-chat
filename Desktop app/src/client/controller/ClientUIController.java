package client.controller;

import client.model.Client;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.text.TextFlow;
import java.net.URL;
import java.util.ResourceBundle;


public class ClientUIController implements Initializable {

    // FXML components
    @FXML private TextFlow chat;
    @FXML private TextField messageField;
    @FXML private Button sendBtn;
    @FXML private Button discBtn;
    @FXML private Label usernameLabel;
    @FXML private Label userLabel;

    // Back-End components
    private String username;
    private boolean ghost_mode;

    public ClientUIController(String username, boolean ghost_mode){
        this.username = username;
        this.ghost_mode = ghost_mode;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if(ghost_mode){
            userLabel.setVisible(false);
            usernameLabel.setVisible(false);
            sendBtn.setVisible(false);
            messageField.setVisible(false);
        }
        Client client = new Client(username, chat, sendBtn, discBtn, messageField);
        usernameLabel.setText(username);
    }

}
