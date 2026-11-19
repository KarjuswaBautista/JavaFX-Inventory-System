package fxml;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import database.DBUtils;
import database.ProductTable;

public class InventoryController extends SceneController {

    private static final String[] CATEGORIES = {"Motherboard", "CPU", "RAM", "Graphics Card", "Power Supply", "Case", "Peripheral"};
    private final DBUtils dbUtils = new DBUtils();
    private String currItem;

    @FXML
    private Label inv_brand, inv_ctgry, inv_name, inv_price, inv_qty;
    @FXML
    private TextField inv_fld_brand, inv_fld_name, inv_fld_price, inv_fld_qty, inv_fld_qty2;
    @FXML
    private ListView<String> lv_inv_contents;
    @FXML
    private ChoiceBox<String> inv_cb_ctgry, inv_cb_sort;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        inv_cb_ctgry.getItems().addAll(CATEGORIES);        

        inv_cb_sort.getItems().add("All");
        inv_cb_sort.getItems().addAll(CATEGORIES);
        inv_cb_sort.setOnAction(event -> handleSortSelection());

        lv_inv_contents.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                currItem = newValue;
                displayProductDetails(currItem);
            }
        });

        loadProductList(null); 
    }

    private void handleSortSelection() {
        String selectedCategory = inv_cb_sort.getValue();
        loadProductList("All".equals(selectedCategory) ? null : selectedCategory);
    }


    public void update() {
        String name = inv_fld_name.getText();
        String category = inv_cb_ctgry.getValue();
        String brand = inv_fld_brand.getText();

        if (!isValidInput(name, category, brand)) {      	
        	Alert alert = new Alert(AlertType.WARNING);
        	alert.setTitle("Invalid!");
            alert.setHeaderText("Invalid input.");
            alert.setContentText("Products not added due to insufficient input.");
            alert.showAndWait();
            return;
        }

        try {
            float price = Float.parseFloat(inv_fld_price.getText());
            int quantity = Integer.parseInt(inv_fld_qty.getText());
            
            Alert confirmationAlert = new Alert(AlertType.CONFIRMATION);
 		   
 		    confirmationAlert.setTitle("Update Stock");
 	        confirmationAlert.setHeaderText("Are you sure you want to proceed?");
 	        confirmationAlert.setContentText("Click OK to confirm.");

 	        if (confirmationAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            ProductTable product = new ProductTable(name, category, brand, price, quantity);
            dbUtils.insertProduct(product);

            
            inv_cb_sort.setValue("All");           
            loadProductList(null);

            
            if (lv_inv_contents.getItems().contains(name)) {
                lv_inv_contents.getSelectionModel().select(name);
            }
           
            displayProductDetails(name);
            
            Alert successAlert = new Alert(AlertType.INFORMATION);
            successAlert.setTitle("Stock Updated!");
            successAlert.setHeaderText(null);
            successAlert.setContentText("The inventory has been successfully updated.");
            successAlert.showAndWait();
            
 	        } else { 
 	        	return; 
 	        	}
        } catch (NumberFormatException e) {
        	Alert alert = new Alert(AlertType.WARNING);
        	alert.setTitle("Invalid!");
            alert.setHeaderText("Invalid quantity and price input.");
            alert.setContentText("Please enter numeric values.");
            alert.showAndWait();
        }
    }



    private boolean isValidInput(String name, String category, String brand) {
        return name != null && !name.isEmpty() &&
               category != null && !category.isEmpty() &&
               brand != null && !brand.isEmpty();
    }

    @FXML
    public void loadProductList(String category) {
        List<ProductTable> products = dbUtils.getProductsWithVariants();
        lv_inv_contents.getItems().clear();

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
                    lv_inv_contents.getItems().add("▶ " + product.getName());
                }
                lastProductName = product.getName();
            }

            
            if (isGrouped) {
                lv_inv_contents.getItems().add("   " + product.getBrand() + " - ₱" + product.getPrice());
            } else {
                lv_inv_contents.getItems().add(product.getName()); 
            }
        }
    }

    private void displayProductDetails(String selectedItem) {
    	if (selectedItem == null || selectedItem.startsWith("▶")) {
     	   DBUtils db = new DBUtils();
     	   int totalquantity = db.getTotalQuantityByProductName(selectedItem.replace("▶", "").trim());
     	   ProductTable product = dbUtils.getProductDetails(selectedItem.replace("▶", "").trim());
     	   inv_name.setText(selectedItem.replace("▶", "").trim());
     	   inv_ctgry.setText(product.getCategory());
     	   inv_brand.setText("Various");
     	   inv_price.setText("Various");
     	   inv_qty.setText(String.valueOf(totalquantity));
            return; 
        }

        if (!selectedItem.contains(" - ₱")) {         
            ProductTable product = dbUtils.getProductDetails(selectedItem);
            if (product != null) {
                inv_name.setText(product.getName());
                inv_ctgry.setText(product.getCategory());
                inv_brand.setText(product.getBrand());
                inv_price.setText(String.valueOf(product.getPrice()));
                inv_qty.setText(String.valueOf(product.getQuantity()));
            }
        } else {          
            String[] details = selectedItem.trim().split(" - \\₱");
            if (details.length < 2) return;

            String brand = details[0];
            float price = Float.parseFloat(details[1]);

            ProductTable product = dbUtils.getProductByBrandAndPrice(brand, price);
            if (product != null) {
                inv_name.setText(product.getName());
                inv_ctgry.setText(product.getCategory());
                inv_brand.setText(product.getBrand());
                inv_price.setText(String.valueOf(product.getPrice()));
                inv_qty.setText(String.valueOf(product.getQuantity()));
            }
        }
    }



    public void addStock() {
        String selectedProduct = lv_inv_contents.getSelectionModel().getSelectedItem();
        String selectedSortCategory = inv_cb_sort.getValue(); // Store sort selection
        
        Alert confirmationAlert = new Alert(AlertType.CONFIRMATION);

        confirmationAlert.setTitle("Add Stock");
	    confirmationAlert.setHeaderText("Are you sure you want to Add Stock?");
	    confirmationAlert.setContentText("Click OK to confirm.");
	    
	    if (confirmationAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
        
        if (selectedProduct == null) {
        	Alert alert = new Alert(AlertType.WARNING);
        	alert.setTitle("Invalid!");
            alert.setHeaderText("No Product Selected.");
            alert.setContentText("Please select a product from the list.");
            alert.showAndWait();
            return;
        }

        try {
            int additionalQuantity = Integer.parseInt(inv_fld_qty2.getText());

            if (additionalQuantity <= 0) {
            	Alert alert = new Alert(AlertType.WARNING);
            	alert.setTitle("Invalid!");
                alert.setHeaderText("Invalid input.");
                alert.setContentText("Please enter positive numbers.");
                alert.showAndWait();
                return;
            }

            
            dbUtils.addQuantityToProduct(inv_name.getText(),inv_brand.getText(), Float.parseFloat(inv_price.getText()), additionalQuantity);
        
            loadProductList("All".equals(selectedSortCategory) ? null : selectedSortCategory);

            
            if (selectedProduct != null && lv_inv_contents.getItems().contains(selectedProduct)) {
                lv_inv_contents.getSelectionModel().select(selectedProduct);
            }

            displayProductDetails(selectedProduct);
                                  
            Alert successAlert = new Alert(AlertType.INFORMATION);
            successAlert.setTitle("Successfully Added!");
            successAlert.setHeaderText(null);
            successAlert.setContentText("Quantity successfully added!");
            successAlert.showAndWait();
            
            
        } catch (NumberFormatException e) {
        	Alert alert = new Alert(AlertType.WARNING);
        	alert.setTitle("Invalid!");
            alert.setHeaderText("Invalid quantity input.");
            alert.setContentText("Please enter numeric values.");
            alert.showAndWait();
        }
    } else {
    	
    	return;
    	
    }
	    
    } 

}