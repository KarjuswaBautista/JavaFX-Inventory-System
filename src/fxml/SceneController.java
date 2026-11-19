package fxml;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public class SceneController implements Initializable {

    private Stage stage;
    private Scene scene;
    private Map<String, Parent> fxmlCache = new HashMap<>();
    
    private long lastSceneChangeTime = 0; 
    private static final long DEBOUNCE_DELAY_MS = 300; 

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    private void switchScene(ActionEvent event, String fxmlFile, boolean maximize) throws IOException {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSceneChangeTime < DEBOUNCE_DELAY_MS) {
            return; 
        }

        lastSceneChangeTime = currentTime;

        Parent root = fxmlCache.get(fxmlFile);
        if (root == null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            root = loader.load();
            fxmlCache.put(fxmlFile, root);          
        }
        
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);

        stage.setScene(scene);
        if (maximize) {
            stage.setMaximized(true);
        }
        stage.centerOnScreen();
        stage.show();
    }

    public void toInventory(ActionEvent event) throws IOException {
        switchScene(event, "Inventory.fxml", true);
    }

    public void toMainMenu(ActionEvent event) throws IOException {
        switchScene(event, "MainMenu.fxml", true);
    }

    public void toOrders(ActionEvent event) throws IOException {
        switchScene(event, "Orders.fxml", true);
    }

    public void toLogin(ActionEvent event) throws IOException {       
        switchScene(event, "LoginPage.fxml", false);
    }

    public void toSignup(ActionEvent event) throws IOException {
        switchScene(event, "SignupPage.fxml", false);
    }
    
    public void toCompletedOrders(ActionEvent event) throws IOException {
        switchScene(event, "CompletedOrders.fxml", false);
    }

    public void logout(ActionEvent event) throws IOException {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setHeaderText("LOGGING OUT");
        alert.setContentText("Click OK to Confirm");

        if (alert.showAndWait().get() == ButtonType.OK) {
            switchScene(event, "LoginPage.fxml", false);
        }
    }
}
