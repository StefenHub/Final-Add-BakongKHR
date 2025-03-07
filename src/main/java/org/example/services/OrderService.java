
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
                item.put("base_price", rs.getDouble("base_price"));
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
            System.out.println("\tNo items available.");
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
        if (item == null) {
            System.out.println("\t❌ Error: Attempted to add a null item to the cart.");
            return;
        }

        if (!item.containsKey("base_price")) {
            System.out.println("\t⚠️ Warning: base_price is missing for item: " + item);
        }

        Map<String, Object> cartItem = new HashMap<>(item);
        cartItem.put("quantity", quantity);
        cart.add(cartItem);

        //System.out.println("\t✅ Item added to cart: " + cartItem);
    }


    // View the contents of the cart
    public void viewCart() {
        if (cart.isEmpty()) {
            System.out.println("\t❌ Your cart is empty.");
            return;
        }

        System.out.println("\t\n--- Your Cart ---");
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
        System.out.printf("\tGrand Total: $%.2f%n", grandTotal);
    }

    // Update the quantity of an item in the cart
    public boolean updateCartItemQuantity(int itemId, int newQuantity) {
        for (Map<String, Object> item : cart) {
            if ((int) item.get("item_id") == itemId) {
                item.put("quantity", newQuantity);
                System.out.println("\t✅ Cart item updated successfully!");
                return true;
            }
        }
        System.out.println("\t❌ Item ID not found in the cart.");
        return false;
    }

    // Remove an item from the cart
    public boolean removeItemFromCart(int itemId) {
        Iterator<Map<String, Object>> iterator = cart.iterator();
        while (iterator.hasNext()) {
            Map<String, Object> item = iterator.next();
            if ((int) item.get("item_id") == itemId) {
                iterator.remove();
                System.out.println("\t✅ Item removed from cart successfully!");
                return true;
            }
        }
        System.out.println("\t❌ Item ID not found in the cart.");
        return false;
    }

    // Clear the cart
    public void clearCart() {
        cart.clear();
        System.out.println("\t✅ Cart cleared successfully!");
    }

    // Place an order and save it to the database
    public int placeOrder(int paymentMethod) {
        if (cart.isEmpty()) {
            System.out.println("\t❌ Your cart is empty. Please add items before placing an order.");
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
                try (ResultSet generatedKeys = orderStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int orderId = generatedKeys.getInt(1);

                        // Instead of iterating over getMenuItems(), iterate over cart
                        for (Map<String, Object> item : cart) {
//                            System.out.println("\tItem data: " + item);

                            if (item.get("base_price") == null || item.get("sell_price") == null || item.get("discount") == null) {
                                System.out.println("\t❌ Missing price data for item: " + item);
                            }

                            int itemId = ((Number) item.get("item_id")).intValue();
                            int quantity = item.get("quantity") != null ? ((Number) item.get("quantity")).intValue() : 1; // Default to 1 if null
                            double basePrice = item.get("base_price") != null ? ((Number) item.get("base_price")).doubleValue() : 0.0;
                            double sellPrice = item.get("sell_price") != null ? ((Number) item.get("sell_price")).doubleValue() : 0.0;
                            double discount = item.get("discount") != null ? ((Number) item.get("discount")).doubleValue() : 0.0;
                            double totalPrice = (sellPrice - discount) * quantity;

                            orderItemStmt.setInt(1, orderId);
                            orderItemStmt.setInt(2, itemId);
                            orderItemStmt.setString(3, (String) item.get("name"));
                            orderItemStmt.setString(4, (String) item.get("description"));
                            orderItemStmt.setString(5, (String) item.get("size"));
                            orderItemStmt.setInt(6, quantity);
                            orderItemStmt.setDouble(7, basePrice);
                            orderItemStmt.setDouble(8, sellPrice);
                            orderItemStmt.setDouble(9, discount);
                            orderItemStmt.setDouble(10, totalPrice);
                            orderItemStmt.addBatch();
                        }
                        orderItemStmt.executeBatch();


                        conn.commit(); // Commit transaction
                        System.out.println("\t✅ Order placed successfully! Order ID: " + orderId);
                        return orderId;
                    } else {
                        throw new SQLException("\tFailed to retrieve order ID.");
                    }
                }
            } catch (SQLException e) {
                conn.rollback(); // Rollback transaction on error
                System.err.println("\t❌ Database error occurred: " + e.getMessage());
                e.printStackTrace();
                return -2;
            }
        } catch (SQLException e) {
            System.err.println("\t❌ Database error occurred: " + e.getMessage());
            e.printStackTrace();
            return -2;
        } catch (Exception e) {
            System.err.println("\t❌ Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return -3;
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
            System.out.println("\tPayment confirmation pending...");
            Thread.sleep(2000);
            System.out.println("\t✅ Payment confirmed via QR Code.");
            System.out.println("\t✅ Payment processed successfully!");
            clearCart();
            return true;
        } catch (Exception e) {
            System.out.println("\t❌ Payment failed: " + e.getMessage());
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
                    System.out.println("\n\t--- Receipt ---");
                    System.out.println("\tOrder ID: " + rs.getInt("order_id"));
                    System.out.println("\tTotal Amount: $" + rs.getDouble("total_amount"));
                    System.out.println("\tOrder Date: " + rs.getTimestamp("order_date"));
                    System.out.println("\tPayment Method: " + (paymentMethod == 1 ? "Credit Card" : paymentMethod == 2 ? "PayPal" : "Cash"));
                } else {
                    System.out.println("\t❌ Order ID not found.");
                }
            }
        } catch (SQLException e) {
            System.out.println("\t❌ Database error: " + e.getMessage());
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
