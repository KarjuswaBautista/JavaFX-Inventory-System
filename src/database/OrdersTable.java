package database;

public class OrdersTable {
    private String customerName;
    private String productName;
    private int quantity;
    private java.sql.Date orderDate;

    public OrdersTable(String customerName, String productName, int quantity, java.sql.Date orderDate) {
        this.customerName = customerName;
        this.productName = productName;
        this.quantity = quantity;
        this.orderDate = orderDate;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public java.sql.Date getOrderDate() {
        return orderDate;
    }
}
