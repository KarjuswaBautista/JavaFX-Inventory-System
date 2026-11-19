package fxml;

import database.DBUtils;
import database.OrdersTable;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class CompletedOrdersController extends SceneController {

    @FXML
    private ListView<String> compOrd_contents;

    private DBUtils dbUtils = new DBUtils(); 

   
    @Override
    public void initialize(URL location, ResourceBundle resources) {
    	loadCompletedOrders();
    }

    private void loadCompletedOrders() {
        List<OrdersTable> orderDetailsList = dbUtils.getOrderDetails(); 
        ObservableList<String> orderStrings = FXCollections.observableArrayList();

        for (OrdersTable order : orderDetailsList) {
            String orderInfo = "Customer: " + order.getCustomerName() + 
                               " | Product: " + order.getProductName() + 
                               " | Quantity: " + order.getQuantity() + 
                               " | Date: " + order.getOrderDate();
            orderStrings.add(orderInfo);
        }

        compOrd_contents.setItems(orderStrings); 
    }
}
