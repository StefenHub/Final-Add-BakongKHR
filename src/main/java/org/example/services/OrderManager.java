package org.example.services;

import org.example.utils.ConsoleFormatter;
import org.example.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OrderManager {

    public static void viewAllCustomerOrders() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            int orderId = 1; // Example order_id, replace with actual value
            if (isOrderIdValid(conn, orderId)) {
                displayOrders(conn, orderId);
            } else {
                ConsoleFormatter.printErrorMessage("❌ Invalid order ID: " + orderId);
            }
        } catch (SQLException e) {
            ConsoleFormatter.printErrorMessage("❌ Error retrieving customer orders: " + e.getMessage());
        }
    }

    private static boolean isOrderIdValid(Connection conn, int orderId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM orders WHERE order_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    private static void displayOrders(Connection conn, int orderId) throws SQLException {
        String sql = "SELECT oi.order_item_id, o.order_id, oi.description, oi.quantity, oi.sell_price " +
                "FROM order_items oi " +
                "JOIN orders o ON oi.order_id = o.order_id " +
                "WHERE o.order_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int orderItemId = rs.getInt("order_item_id");
                String description = rs.getString("description");
                int quantity = rs.getInt("quantity");
                double price = rs.getDouble("sell_price");

                System.out.printf("Order Item ID: %d, Order ID: %d, Description: %s, Quantity: %d, Price: %.2f%n",
                        orderItemId, orderId, description, quantity, price);
            }
        }
    }

    // test displayOrders method
    public static void main(String[] args) {
        viewAllCustomerOrders();
    }
}