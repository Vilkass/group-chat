package server.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import server.model.Server;
import java.net.URL;
import java.util.ResourceBundle;

public class ServerUIController implements Initializable {

    // FXML components
    @FXML private TextArea serverLogs;
    @FXML private ListView<String> clientsList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Server server = new Server(serverLogs, clientsList);

    }

}
