package fxml;

import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

import database.DBUtils;
import database.ProductTable;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import database.CustomersTable;

public class OrdersController extends SceneController {
   
	String currItem;
	private static final String[] CATEGORIES = {"Motherboard", "CPU", "RAM", "Graphics Card", "Power Supply", "Case", "Peripheral"};
    private final DBUtils dbUtils = new DBUtils();
	
    @FXML
    private Button addQty, compOrd;  
    @FXML
    private ListView<String> lvInventory;
    @FXML
    private ChoiceBox<String> ordSort;
    @FXML
    private Label ordctgrylbl, ordbrndlbl, ordqtylbl, ordnamelbl, ordcustnamelbl, ovcustlbl, ovbrandlbl, ovprodlbl, ovqtylbl;
    @FXML
    private TextField ordfldcadd, ordfldcmail, ordfldcname, ordfldcnum, ordfldqty;

   @Override
   public void initialize(URL location, ResourceBundle resources) {
	   
	   ordSort.getItems().add("All");
	   ordSort.getItems().addAll(CATEGORIES);
	   ordSort.setOnAction(event -> handleSortSelection());

       lvInventory.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
           if (newValue != null) {
               currItem = newValue;
               displayProductDetails(currItem);
           }
       });

       loadProductList(null);
	   
   }

    
   private void handleSortSelection() {
       String selectedCategory = ordSort.getValue();
       loadProductList("All".equals(selectedCategory) ? null : selectedCategory);
   }
   
   @FXML
   public void loadProductList(String category) {
       List<ProductTable> products = dbUtils.getAvailableProducts(); 
       lvInventory.getItems().clear();

       String lastProductName = "";
       boolean isGrouped = false;

       for (ProductTable product : products) {
           if (category != null && !category.equals(product.getCategory())) {
               continue;
           }

           boolean hasMultipleVariants = dbUtils.hasVariants(product.getName());

           if (!product.getName().equals(lastProductName)) {
               isGrouped = hasMultipleVariants;
               if (isGrouped) {
                   lvInventory.getItems().add("▶ " + product.getName());
               }
               lastProductName = product.getName();
           }

           if (isGrouped) {
               lvInventory.getItems().add("   " + product.getBrand() + " - ₱" + product.getPrice());
           } else {
               lvInventory.getItems().add(product.getName());
           }
       }
   }

   
   private void displayProductDetails(String selectedItem) {
       if (selectedItem == null || selectedItem.startsWith("▶")) {
    	   DBUtils db = new DBUtils();
    	   int totalquantity = db.getTotalQuantityByProductName(selectedItem.replace("▶", "").trim());
    	   ProductTable product = dbUtils.getProductDetails(selectedItem.replace("▶", "").trim());
    	   ordnamelbl.setText(selectedItem.replace("▶", "").trim());
    	   ordctgrylbl.setText(product.getCategory());
    	   ordbrndlbl.setText("Various");
    	   ordqtylbl.setText(String.valueOf(totalquantity));
           return; 
       }

       if (!selectedItem.contains(" - ₱")) {         
           ProductTable product = dbUtils.getProductDetails(selectedItem);
           if (product != null) {
        	   ordnamelbl.setText(product.getName());
        	   ordctgrylbl.setText(product.getCategory());
        	   ordbrndlbl.setText(product.getBrand());
        	   ordqtylbl.setText(String.valueOf(product.getQuantity()));
               
           }
       } else {          
           String[] details = selectedItem.trim().split(" - \\₱");
           if (details.length < 2) return;

           String brand = details[0];
           float price = Float.parseFloat(details[1]);

           ProductTable product = dbUtils.getProductByBrandAndPrice(brand, price);
           if (product != null) {
        	   ordnamelbl.setText(product.getName());
        	   ordctgrylbl.setText(product.getCategory());
        	   ordbrndlbl.setText(product.getBrand());
        	   ordqtylbl.setText(String.valueOf(product.getQuantity()));              
           }
       }
   }
   

   public void addCustomer() {
	    String name = ordfldcname.getText().trim();
	    String number = ordfldcnum.getText().trim();
	    String address = ordfldcadd.getText().trim();
	    String email = ordfldcmail.getText().trim();
	    
	    Alert confirmationAlert = new Alert(AlertType.CONFIRMATION);
	    confirmationAlert.setTitle("Add Customer");
	    confirmationAlert.setHeaderText("Are you sure you want to Add Customer?");
	    confirmationAlert.setContentText("Click OK to confirm.");

	    if (confirmationAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
	    
	    if (name.isEmpty()) {
	    	Alert alert = new Alert(AlertType.WARNING);
        	alert.setTitle("Invalid!");
            alert.setHeaderText("Missing Fields");
            alert.setContentText("Please enter a name.");
            alert.showAndWait();	        
	        return;
	    }

	    if (number.isEmpty() && address.isEmpty() && email.isEmpty()) {
	    	Alert alert = new Alert(AlertType.WARNING);
        	alert.setTitle("Invalid!");
            alert.setHeaderText("Missing Fields");
            alert.setContentText("At least one contact field is required.");
            alert.showAndWait();	        	        
	        return;
	    }

	    CustomersTable customer = new CustomersTable(name, number, address, email);
	    int customerId = dbUtils.insertCustomer(customer); // Get customer_id

	    if (customerId > 0) {
	        CustomersTable newCustomer = dbUtils.getCustomerById(customerId);
	        if (newCustomer != null) {
	            ordcustnamelbl.setText(newCustomer.getName()); // Display customer name
	        }
	    } else {
	    	Alert alert = new Alert(AlertType.ERROR);
	    	alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to insert customer!");
            alert.showAndWait();
	    }
	    } else {
	    	return;
	    }
	}

   @FXML
   public void displayOrderDetails() {
       String selectedProduct = lvInventory.getSelectionModel().getSelectedItem();
       if (selectedProduct == null || selectedProduct.startsWith("▶")) {
    	   Alert alert = new Alert(AlertType.WARNING);
       	   alert.setTitle("Invalid!");
           alert.setHeaderText("Invalid selection.");
           alert.setContentText("No valid product selected.");
           alert.showAndWait();
           return;
       }

       String customerName = ordcustnamelbl.getText().trim(); 
       String quantityText = ordfldqty.getText().trim();

       if (customerName.isEmpty()) {
    	   Alert alert = new Alert(AlertType.WARNING);
       	   alert.setTitle("Invalid!");
           alert.setHeaderText("Insufficient details.");
           alert.setContentText("No customer selected.");
           alert.showAndWait();          
           return;
       }

       if (quantityText.isEmpty() || !quantityText.matches("\\d+")) {
    	   Alert alert = new Alert(AlertType.WARNING);
       	   alert.setTitle("Invalid!");
           alert.setHeaderText("Invalid quantity.");
           alert.setContentText("Please enter numeric values.");
           alert.showAndWait();
           return;
       }

       int quantity = Integer.parseInt(quantityText);
            
           ovcustlbl.setText(customerName);
           ovbrandlbl.setText(ordbrndlbl.getText());
           ovprodlbl.setText(ordnamelbl.getText());
           ovqtylbl.setText(String.valueOf(quantity));        
           clearOrdLabel();
       
   }

   
   @FXML
   public void insertOrderToDatabase() {
       String customerName = ovcustlbl.getText().trim();
       String brand = ovbrandlbl.getText().trim();
       String productName = ovprodlbl.getText().trim();
       String quantityText = ovqtylbl.getText().trim();
       
       Alert confirmationAlert = new Alert(AlertType.CONFIRMATION);
	   
	   confirmationAlert.setTitle("Update Stock");
       confirmationAlert.setHeaderText("Are you sure you want to Update Stock?");
       confirmationAlert.setContentText("Click OK to confirm.");

       if (confirmationAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
       
       if (customerName.isEmpty() || brand.isEmpty() || productName.isEmpty() || quantityText.isEmpty()) {	   
    	   Alert alert = new Alert(AlertType.WARNING);
       	   alert.setTitle("Invalid!");
           alert.setHeaderText("Missing details.");
           alert.setContentText("Products not added due to missing details.");
           alert.showAndWait();          
           return;
       }

       int quantity;
       try {
           quantity = Integer.parseInt(quantityText);
       } catch (NumberFormatException e) {
    	   Alert alert = new Alert(AlertType.WARNING);
       	   alert.setTitle("Invalid!");
           alert.setHeaderText(null);
           alert.setContentText("Invalid quantity. Please enter a valid number.");
           alert.showAndWait();         
           return;
       }

       int customerId = dbUtils.getCustomerIdByName(customerName);
       int productId = dbUtils.getProductIdByNameAndBrand(productName, brand);
       LocalDate orderDate = LocalDate.now(); // Get current date

       if (customerId == -1 || productId == -1) {
    	   Alert alert = new Alert(AlertType.WARNING);
       	   alert.setTitle("Invalid!");
           alert.setHeaderText(null);
           alert.setContentText("Invalid customer or product.");
           alert.showAndWait(); 
           return;
       }
       
       

       String query = "INSERT INTO orders (customer_id, customer_name, product_id, product_name, quantity, order_date) VALUES (?, ?, ?, ?, ?, ?)";

       try (Connection connection = DriverManager.getConnection(DBUtils.URL, DBUtils.USER, DBUtils.PASSWORD);
            PreparedStatement stmt = connection.prepareStatement(query)) {
    	   
    	   if (!updateProductQuantity(productId, quantity, connection)) {
    		    return; // Stop if quantity update fails
    		}

           stmt.setInt(1, customerId);
           stmt.setString(2, customerName);
           stmt.setInt(3, productId);
           stmt.setString(4, productName);
           stmt.setInt(5, quantity);
           stmt.setDate(6, Date.valueOf(orderDate)); // Convert LocalDate to SQL Date

           int rowsInserted = stmt.executeUpdate();
           if (rowsInserted > 0) {
               System.out.println("Order inserted successfully!");
               updateProductQuantity(productId, quantity, connection);

               // Refresh UI after updating the database
               loadProductList(null);
               displayProductDetails(productName);
               
               clearOverview();
               clearOrdLabel();
               
               Alert successAlert = new Alert(AlertType.INFORMATION);
               successAlert.setTitle("Stock Updated!");
               successAlert.setHeaderText(null);
               successAlert.setContentText("The inventory has been successfully updated.");
               successAlert.showAndWait();
               
           }
       } catch (SQLException e) {
           e.printStackTrace();
       }
       
       } else {
    	   
    	   return;
    	   
       }
       
   }
   
   
public void clearOverview() {
	
	ovcustlbl.setText("");
    ovbrandlbl.setText("");
    ovprodlbl.setText("");
    ovqtylbl.setText("");
	
}

public void clearOrdLabel() {
	
	ordctgrylbl.setText("");
	ordbrndlbl.setText("");
	ordqtylbl.setText("");
	ordnamelbl.setText("");
	ordcustnamelbl.setText("");
	
}
   

private boolean updateProductQuantity(int productId, int orderedQuantity, Connection connection) {
    String checkQuery = "SELECT quantity FROM products WHERE product_id = ?";
    String updateQuery = "UPDATE products SET quantity = ? WHERE product_id = ?";

    try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
        checkStmt.setInt(1, productId);
        ResultSet rs = checkStmt.executeQuery();

        if (rs.next()) {
            int currentQuantity = rs.getInt("quantity");

            
            if (currentQuantity == 0) {
                showAlert(AlertType.ERROR, "Out of Stock", "This product is out of stock.");
                return false;
            }

           
            if (orderedQuantity > currentQuantity) {
                showAlert(AlertType.ERROR, "Insufficient Stock", "Only " + currentQuantity + " units available.");
                return false;
            }

            int newQuantity = currentQuantity - orderedQuantity;

            try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                updateStmt.setInt(1, newQuantity);
                updateStmt.setInt(2, productId);
                updateStmt.executeUpdate();
                System.out.println("Product quantity updated to " + newQuantity);
                return true;
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return false;
}

private void showAlert(AlertType type, String title, String message) {
    Alert alert = new Alert(type);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
}
    
}