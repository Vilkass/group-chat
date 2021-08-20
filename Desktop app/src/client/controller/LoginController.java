package client.controller;

import client.model.Client;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    // FXML components
    @FXML private TextField usernameField;
    @FXML private Button loginButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        usernameField.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER){
                login(null);
            }
        });
    }

    public void login(ActionEvent event){

        // Check if server is online
        if(!Client.checkConnection()){
            // Show alert
            showAlert("Could not connect to server!");
            return;
        }

        // Open chat window
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/view/ClientUI.fxml"));
        ClientUIController mainWindow;
        if(!usernameField.getText().equals("")){
            // Username provided
            if(usernameField.getText().contains(" ")){
                // Contains whitespaces (show error)
                showAlert("Username can not contain white spaces!");
                return;
            }
            mainWindow = new ClientUIController(usernameField.getText().trim(), false);
        }else{
            // Username not provided (GHOST MODE)
            mainWindow = new ClientUIController(usernameField.getText(), true);
        }
        loader.setController(mainWindow);
        try {
            Parent parent = loader.load();
            Stage stage = (Stage)loginButton.getScene().getWindow();
            Scene scene = new Scene(parent);
            stage.setScene(scene);
            stage.show();
        }catch (Exception e){
            e.printStackTrace();
        }

    }



    private void showAlert(String message){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setTitle("Error!");
        alert.setContentText(message);
        alert.show();
    }

}
