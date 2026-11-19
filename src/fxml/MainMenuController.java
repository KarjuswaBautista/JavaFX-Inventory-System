package fxml;

import database.DBUtils;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;

public class MainMenuController extends SceneController {
		
	   public void emptyDB() {	
		   
		   Alert confirmationAlert = new Alert(AlertType.CONFIRMATION);
		   
		   confirmationAlert.setTitle("Clear Database");
	       confirmationAlert.setHeaderText("Are you sure you want to Clear the Database?");
	       confirmationAlert.setContentText("Click OK to confirm.");
	       
	       if (confirmationAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {	    	   
	    	   DBUtils db = new DBUtils();
			   db.resetDatabase();	 
			   
			   Alert successAlert = new Alert(AlertType.INFORMATION);
	            successAlert.setTitle("Database Cleared");
	            successAlert.setHeaderText(null);
	            successAlert.setContentText("The database has been successfully cleared.");
	            successAlert.showAndWait();
			   
	       }		   
	       else {
	    	   
	    	   return;	   
	       }
	   }

}
