package org.example.services;

import org.example.utils.DatabaseConnection;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.ShownBorders;
import org.nocrala.tools.texttablefmt.Table;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class OrderService {
    private final List<Map<String, Object>> cart;

    public OrderService() {
        this.cart = new ArrayList<>();
    }

    // Fetch all menu items from the database
    public List<Map<String, Object>> getMenuItems() {
        List<Map<String, Object>> menuItems = new ArrayList<>();
        String sql = "SELECT * FROM menuitemsadmin ORDER BY category, name";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Map<String, Object> item = new HashMap<>();
                item.put("item_id", rs.getInt("item_id"));
                item.put("name", rs.getString("name"));
                item.put("description", rs.getString("description"));
                item.put("category", rs.getString("category"));
                item.put("size", rs.getString("size"));
                item.put("sell_price", rs.getDouble("sell_price"));
                item.put("discount", rs.getDouble("discount"));
                menuItems.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return menuItems;
    }

    // Display all menu items in a table format
    public void displayMenuItems(List<Map<String, Object>> items) {
        if (items.isEmpty()) {
            System.out.println("No items available.");
            return;
        }

        Table table = new Table(7, BorderStyle.UNICODE_BOX_WIDE, ShownBorders.ALL);
        table.addCell("ID", new CellStyle(CellStyle.HorizontalAlign.CENTER));
        table.addCell("Name", new CellStyle(CellStyle.HorizontalAlign.CENTER));
        table.addCell("Description", new CellStyle(CellStyle.HorizontalAlign.CENTER));
        table.addCell("Category", new CellStyle(CellStyle.HorizontalAlign.CENTER));
        table.addCell("Size", new CellStyle(CellStyle.HorizontalAlign.CENTER));
        table.addCell("Sell Price", new CellStyle(CellStyle.HorizontalAlign.CENTER));
        table.addCell("Discount", new CellStyle(CellStyle.HorizontalAlign.CENTER));

        for (Map<String, Object> item : items) {
            table.addCell(String.valueOf(item.get("item_id")), new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell((String) item.get("name"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell((String) item.get("description"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell((String) item.get("category"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell((String) item.get("size"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell(String.format("$%.2f", item.get("sell_price")), new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell(String.format("$%.2f", item.get("discount")), new CellStyle(CellStyle.HorizontalAlign.CENTER));
        }

        System.out.println(table.render());
    }

    // Add an item to the cart
    public void addItemToCart(Map<String, Object> item, int quantity) {
        Map<String, Object> cartItem = new HashMap<>(item);
        cartItem.put("quantity", quantity);
        cart.add(cartItem);
        System.out.println("✅ Item added to cart successfully!");
    }

    // View the contents of the cart
    public void viewCart() {
        if (cart.isEmpty()) {
            System.out.println("❌ Your cart is empty.");
            return;
        }

        System.out.println("\n--- Your Cart ---");
        double grandTotal = 0;

        Table table = new Table(6, BorderStyle.UNICODE_BOX_WIDE, ShownBorders.ALL);
        table.addCell("ID", new CellStyle(CellStyle.HorizontalAlign.CENTER));
        table.addCell("Name", new CellStyle(CellStyle.HorizontalAlign.CENTER));
        table.addCell("Quantity", new CellStyle(CellStyle.HorizontalAlign.CENTER));
        table.addCell("Sell Price", new CellStyle(CellStyle.HorizontalAlign.CENTER));
        table.addCell("Discount", new CellStyle(CellStyle.HorizontalAlign.CENTER));
        table.addCell("Total Price", new CellStyle(CellStyle.HorizontalAlign.CENTER));

        for (Map<String, Object> cartItem : cart) {
            int itemId = (int) cartItem.get("item_id");
            String name = (String) cartItem.get("name");
            int quantity = (int) cartItem.get("quantity");
            double sellPrice = (double) cartItem.get("sell_price");
            double discount = (double) cartItem.get("discount");
            double unitPrice = sellPrice - discount;
            double totalPrice = unitPrice * quantity;
            grandTotal += totalPrice;

            table.addCell(String.valueOf(itemId), new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell(name, new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell(String.valueOf(quantity), new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell(String.format("$%.2f", sellPrice), new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell(String.format("$%.2f", discount), new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell(String.format("$%.2f", totalPrice), new CellStyle(CellStyle.HorizontalAlign.CENTER));
        }

        System.out.println(table.render());
        System.out.printf("Grand Total: $%.2f%n", grandTotal);
    }

    // Update the quantity of an item in the cart
    public boolean updateCartItemQuantity(int itemId, int newQuantity) {
        for (Map<String, Object> item : cart) {
            if ((int) item.get("item_id") == itemId) {
                item.put("quantity", newQuantity);
                System.out.println("✅ Cart item updated successfully!");
                return true;
            }
        }
        System.out.println("❌ Item ID not found in the cart.");
        return false;
    }

    // Remove an item from the cart
    public boolean removeItemFromCart(int itemId) {
        Iterator<Map<String, Object>> iterator = cart.iterator();
        while (iterator.hasNext()) {
            Map<String, Object> item = iterator.next();
            if ((int) item.get("item_id") == itemId) {
                iterator.remove();
                System.out.println("✅ Item removed from cart successfully!");
                return true;
            }
        }
        System.out.println("❌ Item ID not found in the cart.");
        return false;
    }

    // Clear the cart
    public void clearCart() {
        cart.clear();
        System.out.println("✅ Cart cleared successfully!");
    }

    // Place an order and save it to the database
    public int placeOrder(int paymentMethod) {
        if (cart.isEmpty()) {
            System.out.println("❌ Your cart is empty. Please add items before placing an order.");
            return -1;
        }

        double totalAmount = calculateTotalAmount();
        String paymentMethodString = "QR Code";
        LocalDateTime currentDateTime = LocalDateTime.now();
        Timestamp orderTimestamp = Timestamp.valueOf(currentDateTime);

        String orderQuery = "INSERT INTO orders (payment_method, total_amount, order_date) VALUES (?, ?, ?)";
        String orderItemQuery = "INSERT INTO order_items (order_id, item_id, name, description, size, quantity, base_price, sell_price, discount, total_price) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            try (PreparedStatement orderStmt = conn.prepareStatement(orderQuery, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement orderItemStmt = conn.prepareStatement(orderItemQuery)) {

                // Insert order
                orderStmt.setString(1, paymentMethodString);
                orderStmt.setDouble(2, totalAmount);
                orderStmt.setTimestamp(3, orderTimestamp);
                orderStmt.executeUpdate();

                // Get generated order ID
                ResultSet generatedKeys = orderStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int orderId = generatedKeys.getInt(1);

                    // Insert order items
                    for (Map<String, Object> item : cart) {
                        int itemId = (int) item.get("item_id");
                        int quantity = (int) item.get("quantity");
                        double sellPrice = (double) item.get("sell_price");
                        double discount = (double) item.get("discount");
                        // Assuming base price is the same as sell price before discount
                        double totalPrice = (sellPrice - discount) * quantity;

                        orderItemStmt.setInt(1, orderId);
                        orderItemStmt.setInt(2, itemId);
                        orderItemStmt.setString(3, (String) item.get("name"));
                        orderItemStmt.setString(4, (String) item.get("description"));
                        orderItemStmt.setString(5, (String) item.get("size"));
                        orderItemStmt.setInt(6, quantity);
                        orderItemStmt.setDouble(7, sellPrice);
                        orderItemStmt.setDouble(8, sellPrice);
                        orderItemStmt.setDouble(9, discount);
                        orderItemStmt.setDouble(10, totalPrice);
                        orderItemStmt.addBatch();
                    }
                    orderItemStmt.executeBatch();

                    conn.commit(); // Commit transaction
                    System.out.println("✅ Order placed successfully! Order ID: " + orderId);
                    return orderId;
                } else {
                    throw new SQLException("Failed to retrieve order ID.");
                }
            } catch (SQLException e) {
                conn.rollback(); // Rollback transaction on error
                System.out.println("❌ Database error: " + e.getMessage());
                e.printStackTrace();
                return -1;
            }
        } catch (SQLException e) {
            System.out.println("❌ Database error: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    // Calculate the total amount of the cart
    private double calculateTotalAmount() {
        double totalAmount = 0;
        for (Map<String, Object> item : cart) {
            double sellPrice = item.get("sell_price") != null ? (double) item.get("sell_price") : 0;
            double discount = item.get("discount") != null ? (double) item.get("discount") : 0;
            int quantity = item.get("quantity") != null ? (int) item.get("quantity") : 0;
            totalAmount += (sellPrice - discount) * quantity;
        }
        return totalAmount;
    }

    // Process payment using the chosen payment method
    public boolean processPayment(int orderId, int paymentMethod) {
        try {
            // Only QR Code payment is allowed
            System.out.println("Payment confirmation pending...");
            Thread.sleep(2000); // Simulate a delay for payment confirmation
            System.out.println("✅ Payment confirmed via QR Code.");
            System.out.println("✅ Payment processed successfully!");
            return true;
        } catch (Exception e) {
            System.out.println("❌ Payment failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Generate a receipt for the order
    public void generateReceipt(int orderId, int paymentMethod) {
        String query = "SELECT order_id, total_amount, order_date FROM orders WHERE order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("\n--- Receipt ---");
                    System.out.println("Order ID: " + rs.getInt("order_id"));
                    System.out.println("Total Amount: $" + rs.getDouble("total_amount"));
                    System.out.println("Order Date: " + rs.getTimestamp("order_date"));
                    System.out.println("Payment Method: " + (paymentMethod == 1 ? "Credit Card" : paymentMethod == 2 ? "PayPal" : "Cash"));
                } else {
                    System.out.println("❌ Order ID not found.");
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Check if the cart is empty
    public boolean isCartEmpty() {
        return cart.isEmpty();
    }

    public List<String> getCategories() {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT DISTINCT category FROM menuitemsadmin ORDER BY category";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                categories.add(rs.getString("category"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return categories;
    }

    public void displayItemsByCategory(String selectedCategory) {
        List<Map<String, Object>> items = getMenuItems();
        List<Map<String, Object>> itemsByCategory = new ArrayList<>();

        for (Map<String, Object> item : items) {
            if (item.get("category").equals(selectedCategory)) {
                itemsByCategory.add(item);
            }
        }
        displayMenuItems(itemsByCategory);
    }

    public Map<String, Object> getItemById(int itemId) {
        List<Map<String, Object>> items = getMenuItems();
        for (Map<String, Object> item : items) {
            if ((int) item.get("item_id") == itemId) {
                return item;
            }
        }
        return null;
    }
}