package fxml;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import database.DBUtils;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class SignupPageController extends SceneController {
	
	@FXML
    private CheckBox chkbxShowPass;

    @FXML
    private PasswordField tfPassword;
    
    @FXML
    private TextField tfVisiblePass;


    @FXML
    private TextField tfUsername;
    
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        tfVisiblePass.setVisible(false);
        tfVisiblePass.setManaged(true); 
    }


    @FXML
    void registerUser(ActionEvent event) {
        String username = tfUsername.getText().trim();
        String password = tfPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter a username and password.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DBUtils.URL, DBUtils.USER, DBUtils.PASSWORD)) {
            if (isUsernameTaken(conn, username)) {
                showAlert("Error", "Username is already taken. Choose another.");
                return;
            }

            String insertSQL = "INSERT INTO Users (user_name, password) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertSQL)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {                	
                    showAlert("Success", "User registered successfully.");  
                    tfUsername.clear();
                    tfPassword.clear();
                } else {
                    showAlert("Error", "Registration failed. Please try again.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Database error: " + e.getMessage());
        }
    }

    private boolean isUsernameTaken(Connection conn, String username) throws SQLException {
        String checkSQL = "SELECT user_name FROM Users WHERE user_name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(checkSQL)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // If there's a result, the username is taken
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
    
    public void deleteAllUsers() {
    	
    	DBUtils dbUtils = new DBUtils();
    	Alert confirmationAlert = new Alert(AlertType.CONFIRMATION);		   
		confirmationAlert.setTitle("Delete All Users");
	    confirmationAlert.setHeaderText("Are you sure you want to Delete All Users?");
	    confirmationAlert.setContentText("Click OK to confirm.");
	    
	    if (confirmationAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
    	dbUtils.deleteAllUsers();
    	
    	Alert successAlert = new Alert(AlertType.INFORMATION);
        successAlert.setTitle("Users Deleted");
        successAlert.setHeaderText(null);
        successAlert.setContentText("The users has been successfully deleted.");
        successAlert.showAndWait();
    	
    	}
	    
	    else {
	    	return;
	    }
    	
    }
    
}
