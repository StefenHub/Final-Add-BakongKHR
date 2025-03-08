package org.example.controllers;

import org.example.utils.DatabaseConnection;
import org.example.utils.InputValidator;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.ShownBorders;
import org.nocrala.tools.texttablefmt.Table;

import java.sql.*;
import java.util.Scanner;

public class ChefController {
    private static final int ITEMS_PER_PAGE = 10; // Number of items per page
    private final Scanner scanner;

    public ChefController(Scanner scanner) {
        this.scanner = scanner;
    }

    // Start the chefController interaction menu
    public void start() {
        while (true) {
            System.out.println("\n--- Kitchen Menu ---");
            System.out.print("""
                         \u001B[34m
                         ╔════════════════════════════════╗
                         ║      ‍🍳 Kitchen Dashboard      ║
                         ╠════════════════════════════════╣
                         ║   \u001B[33m[1]. 📋 View All Orders\u001B[34m      ║
                         ║   \u001B[33m[2]. ⏳ View Pending Orders\u001B[34m  ║
                         ║   \u001B[33m[3]. 🔄 Update Order Status\u001B[34m  ║
                         ║   \u001B[31m[0]. ❌ Exit\u001B[34m                 ║
                         ╚════════════════════════════════╝\u001B[0m
                    """);
            int choice = InputValidator.validateIntegerInput(scanner, "\tEnter your choice: ", 0, 3);
            if (choice == -1) {
                continue; // Invalid input, re-prompt the menu
            }
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
                case 0:
                    System.out.println("Exiting chefController menu...");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // View all orders
    private void viewAllOrders() {
        int currentPage = 1;
        int totalPages = getTotalPages("SELECT COUNT(*) FROM order_items");

        while (true) {
            displayOrders("SELECT order_id, name, quantity, size, description, order_date, order_status FROM order_items ORDER BY order_date DESC LIMIT ? OFFSET ?", currentPage);

            System.out.println("\n\t📄 Page " + currentPage + " of " + totalPages);
            System.out.println("\t[➡️] Next  |  [⬅️] Previous  |  [❌] Exit");

            String choice = scanner.next().toLowerCase();
            if (choice.equals("n")) {
                if (currentPage < totalPages) {
                    currentPage++;
                } else {
                    System.out.println("\t⚠️ You are already on the last page.");
                }
            } else if (choice.equals("p")) {
                if (currentPage > 1) {
                    currentPage--;
                } else {
                    System.out.println("\t⚠️ You are already on the first page.");
                }
            } else if (choice.equals("e")) {
                break;
            } else {
                System.out.println("\t⚠️ Invalid input! Try again.");
            }
        }
    }

    // View pending orders
    private void viewPendingOrders() {
        int currentPage = 1;
        int totalPages = getTotalPages("SELECT COUNT(*) FROM order_items WHERE order_status = 'pending'");

        while (true) {
            displayOrders("SELECT order_id, name, quantity, size, description, order_date, order_status FROM order_items WHERE order_status = 'pending' ORDER BY order_date ASC LIMIT ? OFFSET ?", currentPage);

            System.out.println("\n\t📄 Page " + currentPage + " of " + totalPages);
            System.out.println("\t[➡️] Next  |  [⬅️] Previous  |  [❌] Exit");

            String choice = scanner.next().toLowerCase();
            if (choice.equals("n")) {
                if (currentPage < totalPages) {
                    currentPage++;
                } else {
                    System.out.println("\t⚠️ You are already on the last page.");
                }
            } else if (choice.equals("p")) {
                if (currentPage > 1) {
                    currentPage--;
                } else {
                    System.out.println("\t⚠️ You are already on the first page.");
                }
            } else if (choice.equals("e")) {
                break;
            } else {
                System.out.println("\t⚠️ Invalid input! Try again.");
            }
        }
    }

    // Display orders with pagination
    private void displayOrders(String query, int page) {
        int offset = (page - 1) * ITEMS_PER_PAGE;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, ITEMS_PER_PAGE);
            stmt.setInt(2, offset);
            ResultSet rs = stmt.executeQuery();

            Table table = new Table(7, BorderStyle.UNICODE_ROUND_BOX_WIDE, ShownBorders.ALL);
            table.addCell("Order ID", new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell("Name", new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell("Quantity", new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell("Size", new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell("Description", new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell("Order Date", new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell("Order Status", new CellStyle(CellStyle.HorizontalAlign.CENTER));

            while (rs.next()) {
                table.addCell(String.valueOf(rs.getInt("order_id")), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(rs.getString("name"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(String.valueOf(rs.getInt("quantity")), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(rs.getString("size"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(rs.getString("description"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(rs.getTimestamp("order_date").toString(), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(rs.getString("order_status"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
            }

            System.out.println(table.render());

        } catch (SQLException e) {
            System.out.println("❌ Error retrieving orders: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Get total pages for pagination
    private int getTotalPages(String countQuery) {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countQuery)) {

            if (rs.next()) {
                int totalItems = rs.getInt(1);
                return (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);
            }

        } catch (SQLException e) {
            System.out.println("❌ Error retrieving total pages: " + e.getMessage());
            e.printStackTrace();
        }
        return 1;
    }

    // Update order status
    private void updateOrderStatus() {
        viewAllOrders();
        int orderId = InputValidator.validateIntegerInput(scanner, "Enter the Order ID: ", 1, Integer.MAX_VALUE);
        if (orderId == -1) {
            return; // Invalid input, do not proceed
        }
        System.out.print("""
                        \u001B[34m╔═════════════════════════════════╗
                        ║  \u001B[36m         Status Options   \u001B[34m       ║
                        ╠═════════════════════════════════╣
                        ║   \u001B[33m[1]. 🔄 Preparing\u001B[34m               ║
                        ║   \u001B[33m[2]. ✅ Ready\u001B[34m                   ║
                        ║   \u001B[33m[3]. 🎉 Completed\u001B[34m               ║
                        ╚═════════════════════════════════╝ \u001B[0m
                    """);
        int statusChoice = InputValidator.validateIntegerInput(scanner, "\tEnter your choice: ", 1, 3);
        if (statusChoice == -1) {
            return; // Invalid input, do not proceed
        }
        String newStatus;
        switch (statusChoice) {
            case 1 -> newStatus = "Preparing";
            case 2 -> newStatus = "Ready";
            case 3 -> newStatus = "Completed";
            default -> {
                System.out.println("Invalid choice. Status not updated.");
                return;
            }
        }

        String query = "UPDATE order_items SET order_status = ? WHERE order_id = ?";
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
}