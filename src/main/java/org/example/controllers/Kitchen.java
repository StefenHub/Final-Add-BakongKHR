package org.example.controllers;

import org.example.services.OrderService;
import org.example.utils.DatabaseConnection;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.ShownBorders;
import org.nocrala.tools.texttablefmt.Table;

import java.sql.*;
import java.util.Scanner;

public class Kitchen {
    private final Scanner scanner;

    public Kitchen(Scanner scanner) {
        this.scanner = scanner;
    }

    // Start the kitchen interaction menu
    public void start() {
        while (true) {
            System.out.println("\n--- Kitchen Menu ---");
            System.out.println("1. View All Orders");
            System.out.println("2. View Pending Orders");
            System.out.println("3. Update Order Status");
            System.out.println("4. Exit");

            int choice = validateIntegerInput(scanner, "Enter your choice: ", 1, 4);
            switch (choice) {
                case 1:
                    viewAllOrders();
                    break;
                case 2:
                    viewPendingOrders();
                    break;
                case 3:
                    updateOrderStatus();
                    break;
                case 4:
                    System.out.println("Exiting kitchen menu. Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // View all orders
    private void viewAllOrders() {
        String query = "SELECT order_id, name, quantity, size, description, order_date, order_status FROM order_items ORDER BY order_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            Table table = new Table(7, BorderStyle.UNICODE_BOX_DOUBLE_BORDER, ShownBorders.ALL);
            table.addCell("Order ID", new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell("Name", new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell("Quantity", new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell("Size", new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell("Description", new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell("Order Date", new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell("Order Status", new CellStyle(CellStyle.HorizontalAlign.CENTER));

            while (rs.next()) {
                int orderId = rs.getInt("order_id");
                String name = rs.getString("name");
                int quantity = rs.getInt("quantity");
                String size = rs.getString("size");
                String description = rs.getString("description");
                Timestamp orderDate = rs.getTimestamp("order_date");
                table.addCell(String.valueOf(orderId), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(name, new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(String.valueOf(quantity), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(size, new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(description, new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(orderDate.toString(), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(rs.getString("order_status"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
            }

            System.out.println("\n--- All Orders ---");
            System.out.println(table.render());

        } catch (SQLException e) {
            System.out.println("❌ Error retrieving orders: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // View pending orders
    private void viewPendingOrders() {
        String query = "SELECT order_id, name, size , quantity, description, order_date, order_status FROM order_items WHERE order_status = 'pending' ORDER BY order_date ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            Table table = new Table(7, BorderStyle.UNICODE_BOX_DOUBLE_BORDER, ShownBorders.ALL);
            table.addCell("Order ID", new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell("Name", new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell("Size", new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell("Quantity", new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell("Description", new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell("Order Date", new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell("Order Status", new CellStyle(CellStyle.HorizontalAlign.CENTER));

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                int orderId = rs.getInt("order_id");
                String name = rs.getString("name");
                String size = rs.getString("size");
                int quantity = rs.getInt("quantity");
                String description = rs.getString("description");
                Timestamp orderDate = rs.getTimestamp("order_date");
                table.addCell(String.valueOf(orderId), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(name, new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(size, new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(String.valueOf(quantity), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(description, new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(orderDate.toString(), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(rs.getString("order_status"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
            }

            if (!hasData) {
                System.out.println("No pending orders available.");
                return;
            }

            System.out.println("\n--- Pending Orders ---");
            System.out.println(table.render());

        } catch (SQLException e) {
            System.out.println("❌ Error retrieving pending orders: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Update order status
    private void updateOrderStatus() {
        System.out.print("Enter the Order ID to update status: ");
        int orderId = validateIntegerInput(scanner, "Enter the Order ID: ", 1, Integer.MAX_VALUE);

        System.out.println("Select the new status:");
        System.out.println("1. Preparing");
        System.out.println("2. Ready");
        System.out.println("3. Completed");

        int statusChoice = validateIntegerInput(scanner, "Enter your choice: ", 1, 3);
        String newStatus = "";
        switch (statusChoice) {
            case 1:
                newStatus = "Preparing";
                break;
            case 2:
                newStatus = "Ready";
                break;
            case 3:
                newStatus = "Completed";
                break;
            default:
                System.out.println("Invalid choice. Status not updated.");
                return;
        }

        String query = "UPDATE orders SET order_status = ? WHERE order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, newStatus);
            pstmt.setInt(2, orderId);
            int rowsUpdated = pstmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("✅ Order status updated successfully!");
            } else {
                System.out.println("❌ Order ID not found. Status not updated.");
            }

        } catch (SQLException e) {
            System.out.println("❌ Error updating order status: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Helper method to validate integer input within a range
    private int validateIntegerInput(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("[b]")) {
                return -1; // Return -1 to indicate the user wants to go back
            }

            try {
                int value = Integer.parseInt(input);
                if (value < min || value > max) {
                    System.out.println("Input out of range. Please enter a number between " + min + " and " + max + ".");
                } else {
                    return value;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid numeric value.");
            }
        }
    }
}