package fxml;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import database.DBUtils;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class LoginPageController extends SceneController {


    @FXML
    private CheckBox chkbxShowPass;
    
    @FXML
    private PasswordField tfPassword;
    
    @FXML
    private TextField tfVisiblePass;

    @FXML
    private TextField tfUsername;

    @FXML
    private Label title;

    private SceneController sceneController = new SceneController(); 

    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        tfVisiblePass.setVisible(false);
        tfVisiblePass.setManaged(true); 
    }


    
    @FXML
    void loginUser(ActionEvent event) {
        String username = tfUsername.getText().trim();
        String password = tfPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter both username and password.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DBUtils.URL, DBUtils.USER, DBUtils.PASSWORD)) {
            if (isValidUser(conn, username, password)) {
                
                sceneController.toMainMenu(event);

            } else {
                showAlert("Error", "Invalid username or password.");
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            showAlert("Error", "Database error: " + e.getMessage());
        }
    }

    private boolean isValidUser(Connection conn, String username, String password) throws SQLException {
        String querySQL = "SELECT * FROM Users WHERE user_name = ? AND password = ?";
        try (PreparedStatement stmt = conn.prepareStatement(querySQL)) {
            stmt.setString(1, username);
            stmt.setString(2, password); // Consider using hashed password comparison
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // Returns true if user exists
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @FXML
    void togglePasswordVisibility(ActionEvent event) {
        if (chkbxShowPass.isSelected()) {
            
            tfVisiblePass.setText(tfPassword.getText());
            tfVisiblePass.setVisible(true);
            tfPassword.setVisible(false);
        } else {
           
            tfPassword.setText(tfVisiblePass.getText());
            tfPassword.setVisible(true);
            tfVisiblePass.setVisible(false);
        }
    }

}
