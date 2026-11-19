package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DBUtils {
    public static final String URL = "jdbc:mysql://localhost:3306/infoman_ezpc_inventory_system";
    public static final String USER = "root";
    public static final String PASSWORD = "040902";

    public void insertProduct(ProductTable product) {
        Connection connection = null;
        PreparedStatement checkProduct = null;
        PreparedStatement updateQuantity = null;
        PreparedStatement insertProduct = null;
        ResultSet resultSet = null;

        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);

            String checkQuery = "SELECT quantity FROM products WHERE product_name = ? AND brand = ? AND price = ?";
            checkProduct = connection.prepareStatement(checkQuery);
            checkProduct.setString(1, product.getName());
            checkProduct.setString(2, product.getBrand());
            checkProduct.setFloat(3, product.getPrice());
            resultSet = checkProduct.executeQuery();

            if (resultSet.next()) {
                int existingQuantity = resultSet.getInt("quantity");
                int newQuantity = existingQuantity + product.getQuantity();

                String updateQuery = "UPDATE products SET quantity = ? WHERE product_name = ? AND brand = ? AND price = ?";
                updateQuantity = connection.prepareStatement(updateQuery);
                updateQuantity.setInt(1, newQuantity);
                updateQuantity.setString(2, product.getName());
                updateQuantity.setString(3, product.getBrand());
                updateQuantity.setFloat(4, product.getPrice());

                updateQuantity.executeUpdate();
                System.out.println("Product quantity updated successfully!");

            } else {
                String insertQuery = "INSERT INTO products (product_name, category, brand, price, quantity) VALUES (?, ?, ?, ?, ?)";
                insertProduct = connection.prepareStatement(insertQuery);
                insertProduct.setString(1, product.getName());
                insertProduct.setString(2, product.getCategory());
                insertProduct.setString(3, product.getBrand());
                insertProduct.setFloat(4, product.getPrice());
                insertProduct.setInt(5, product.getQuantity());

                insertProduct.executeUpdate();
                System.out.println("New product inserted successfully!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (checkProduct != null) checkProduct.close();
                if (updateQuantity != null) updateQuantity.close();
                if (insertProduct != null) insertProduct.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    public List<String> getProductNames() {
        List<String> productNames = new ArrayList<>();
        String query = "SELECT product_name FROM products";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                productNames.add(rs.getString("product_name"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return productNames;
    }

    
    public ProductTable getProductDetails(String productName) {
        ProductTable product = null;
        String query = "SELECT * FROM products WHERE product_name = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, productName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                product = new ProductTable(
                    rs.getString("product_name"),
                    rs.getString("category"),
                    rs.getString("brand"),
                    rs.getFloat("price"),
                    rs.getInt("quantity")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return product;
    }
    
    public List<String> getProductsByCategory(String category) {
        List<String> productNames = new ArrayList<>();
        String query = "SELECT product_name FROM products WHERE category = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, category);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                productNames.add(rs.getString("product_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return productNames;
    }
    
    

    public void addQuantityToProduct(String productName, String productBrand, float productPrice, int additionalQuantity) {
        String query = "UPDATE products SET quantity = quantity + ? WHERE product_name = ? AND brand = ? AND price = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setInt(1, additionalQuantity);
            stmt.setString(2, productName);
            stmt.setString(3, productBrand);
            stmt.setFloat(4, productPrice);

            int updatedRows = stmt.executeUpdate();

            if (updatedRows > 0) {
                System.out.println("Quantity updated successfully for: " + productName + " (" + productBrand + ", ₱" + productPrice + ")");
            } else {
                System.out.println("Product not found: " + productName + " (" + productBrand + ", ₱" + productPrice + ")");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    
    
    
    public List<ProductTable> getSimilarProducts() {
        List<ProductTable> products = new ArrayList<>();
        String query = "SELECT product_name, category, brand, price, quantity FROM products ORDER BY product_name, brand, price";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                products.add(new ProductTable(
                    rs.getString("product_name"),
                    rs.getString("category"),
                    rs.getString("brand"),
                    rs.getFloat("price"),
                    rs.getInt("quantity")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }
    
    public ProductTable getProductByBrandAndPrice(String brand, float price) {
        ProductTable product = null;
        String query = "SELECT * FROM products WHERE brand = ? AND price = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, brand);
            stmt.setFloat(2, price);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                product = new ProductTable(
                    rs.getString("product_name"),
                    rs.getString("category"),
                    rs.getString("brand"),
                    rs.getFloat("price"),
                    rs.getInt("quantity")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return product;
    }

    public List<ProductTable> getProductsWithVariants() {
        List<ProductTable> products = new ArrayList<>();
        String query = "SELECT product_name, category, brand, price, quantity FROM products " +
                       "ORDER BY product_name, brand, price";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                products.add(new ProductTable(
                    rs.getString("product_name"),
                    rs.getString("category"),
                    rs.getString("brand"),
                    rs.getFloat("price"),
                    rs.getInt("quantity")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }
    
    public boolean hasVariants(String productName) {
        String query = "SELECT COUNT(DISTINCT brand, price) AS variant_count FROM products WHERE product_name = ?";
        
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, productName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("variant_count") > 1;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int insertCustomer(CustomersTable customer) {
        int customerId = -1;
        String query = "INSERT INTO customers (customer_name, contact_number, address, email) VALUES (?, ?, ?, ?)";
        
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getNumber());
            stmt.setString(3, customer.getAddress());
            stmt.setString(4, customer.getEmail());

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    customerId = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customerId;
    }

    
    public CustomersTable getCustomerById(int customerId) {
        CustomersTable customer = null;
        String query = "SELECT * FROM customers WHERE customer_id = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                customer = new CustomersTable(
                    rs.getString("customer_name"),
                    rs.getString("contact_number"),
                    rs.getString("address"),
                    rs.getString("email")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customer;
    }
    
    public List<ProductTable> getAvailableProducts() {
        List<ProductTable> products = new ArrayList<>();
        String query = "SELECT * FROM products WHERE quantity > 0"; // Fetch only products in stock

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                products.add(new ProductTable(
                    rs.getString("product_name"),
                    rs.getString("category"),
                    rs.getString("brand"),
                    rs.getFloat("price"),
                    rs.getInt("quantity") 
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }
    

    public int getCustomerIdByName(String customerName) {
        String query = "SELECT customer_id FROM customers WHERE customer_name = ?";
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, customerName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("customer_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int getProductIdByNameAndBrand(String productName, String brand) {
        String query = "SELECT product_id FROM products WHERE product_name = ? AND brand = ?";
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, productName);
            stmt.setString(2, brand);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("product_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    public int getTotalQuantityByProductName(String productName) {
        String query = "SELECT SUM(quantity) AS total_quantity FROM products WHERE product_name = ?";
        int totalQuantity = 0;

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, productName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                totalQuantity = rs.getInt("total_quantity");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return totalQuantity;
    }

    public void resetDatabase() {
        String[] tables = {"orders", "products", "customers"};
        
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement statement = connection.createStatement()) {

            for (String table : tables) {
                
                String deleteQuery = "DELETE FROM " + table;
                statement.executeUpdate(deleteQuery);
                
                
                String resetAutoIncrementQuery = "ALTER TABLE " + table + " AUTO_INCREMENT = 1";
                statement.executeUpdate(resetAutoIncrementQuery);
            }
            
            System.out.println("All records deleted and auto-increment reset for products, customers, and orders tables.");
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void deleteAllUsers() {
        String query = "DELETE FROM users";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = connection.prepareStatement(query)) {

            int rowsDeleted = stmt.executeUpdate();
            System.out.println("Deleted " + rowsDeleted + " users from the database.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    
    public List<OrdersTable> getOrderDetails() {
        List<OrdersTable> orderDetailsList = new ArrayList<>();
        String query = "SELECT c.customer_name, p.product_name, o.quantity, o.order_date " +
                       "FROM orders o " +
                       "JOIN customers c ON o.customer_id = c.customer_id " +
                       "JOIN products p ON o.product_id = p.product_id";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                OrdersTable orderDetails = new OrdersTable(
                    rs.getString("customer_name"),
                    rs.getString("product_name"),
                    rs.getInt("quantity"),
                    rs.getDate("order_date")
                );
                orderDetailsList.add(orderDetails);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orderDetailsList;
    }

    
}