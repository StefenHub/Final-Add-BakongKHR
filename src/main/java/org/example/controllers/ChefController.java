package org.example.controllers;

import org.example.utils.ColorFormatter;
import org.example.utils.ConsoleFormatter;
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
    private static final String RESET = "\u001B[0m";
    private static final String BOLD_BLUE = "\033[1;34m"; // Blue title
    private static final String BRIGHT_WHITE = "\033[97m"; // White text for options
    private static final String WHITE_BORDER = "\033[97m"; // White border
    private static final String BLUE = "\u001B[34m"; // Blue for padding

    private static final int consoleWidth = 180; // Console width
    private static final int tableWidth = 100; // Wider table width
    private static final String padding = " ".repeat((consoleWidth - tableWidth) / 2);

    public ChefController(Scanner scanner) {
        this.scanner = scanner;
    }

    // Start the chefController interaction menu
    public void start() {
        while (true) {
            // Create a table with a SINGLE wide column
            Table table = new Table(1, BorderStyle.UNICODE_BOX_DOUBLE_BORDER_WIDE, ShownBorders.ALL);
            table.setColumnWidth(0, 80, 90); // Explicitly setting column width wider

            CellStyle centerStyle = new CellStyle(CellStyle.HorizontalAlign.CENTER);
            CellStyle leftStyle = new CellStyle(CellStyle.HorizontalAlign.LEFT); // Left alignment for numbers

            // Title Row (Centered)
            table.addCell(BOLD_BLUE + "CHEF DASHBOARD" + RESET, centerStyle);

            // Menu Options (Numbers Left-Aligned)
            String[] options = {
                    "1.  View All Orders",
                    "2.  View Pending Orders",
                    "3.  Update Order Status",
                    "0.  Exit"
            };

            for (String option : options) {
                table.addCell(BOLD_BLUE + option.trim() + RESET, leftStyle); // Left-align & reset colors properly
            }

            // Print the Table with WHITE Borders
            String[] tableLines = table.render().split("\n");
            for (String line : tableLines) {
                System.out.println(WHITE_BORDER + padding + line + RESET);
            }

            int choice = InputValidator.validateIntegerInput(scanner, ConsoleFormatter.centerText(ColorFormatter.colorText("Enter your choice: ", ColorFormatter.GREEN + ColorFormatter.BOLD)), 0, 3);
            if (choice == -1) {
                System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("⚠️ Invalid input! Try again.", ColorFormatter.YELLOW + ColorFormatter.BOLD)));
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
                    System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("Exiting chefController menu...", ColorFormatter.GREEN + ColorFormatter.BOLD)));
                    return;
                default:
                    System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("Invalid choice. Please try again.", ColorFormatter.RED + ColorFormatter.BOLD)));
            }
        }
    }

    // View all orders
    private void viewAllOrders() {
        int currentPage = 1;
        int totalPages = getTotalPages("SELECT COUNT(*) FROM order_items");

        while (true) {
            displayOrders("SELECT order_id, name, quantity, size, description, order_date, order_status FROM order_items ORDER BY order_date DESC LIMIT ? OFFSET ?", currentPage);

            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("📄 Page " + currentPage + " of " + totalPages, ColorFormatter.GREEN + ColorFormatter.BOLD)));
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("[➡️] Next  |  [⬅️] Previous  |  [❌] Exit", ColorFormatter.GREEN + ColorFormatter.BOLD)));

            String choice = scanner.next().toLowerCase();
            if (choice.equals("n")) {
                if (currentPage < totalPages) {
                    currentPage++;
                } else {
                    System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("⚠️ You are already on the last page.", ColorFormatter.YELLOW + ColorFormatter.BOLD)));
                }
            } else if (choice.equals("p")) {
                if (currentPage > 1) {
                    currentPage--;
                } else {
                    System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("⚠️ You are already on the first page.", ColorFormatter.YELLOW + ColorFormatter.BOLD)));
                }
            } else if (choice.equals("e")) {
                break;
            } else {
                System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("⚠️ Invalid input! Try again.", ColorFormatter.YELLOW + ColorFormatter.BOLD)));
            }
        }
    }

    // View pending orders
    private void viewPendingOrders() {
        int currentPage = 1;
        int totalPages = getTotalPages("SELECT COUNT(*) FROM order_items WHERE order_status = 'pending'");

        while (true) {
            displayOrders("SELECT order_id, name, quantity, size, description, order_date, order_status FROM order_items WHERE order_status = 'pending' ORDER BY order_date ASC LIMIT ? OFFSET ?", currentPage);

            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("📄 Page " + currentPage + " of " + totalPages, ColorFormatter.GREEN + ColorFormatter.BOLD)));
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("[➡️] Next  |  [⬅️] Previous  |  [❌] Exit", ColorFormatter.GREEN + ColorFormatter.BOLD)));

            String choice = scanner.next().toLowerCase();
            if (choice.equals("n")) {
                if (currentPage < totalPages) {
                    currentPage++;
                } else {
                    System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("⚠️ You are already on the last page.", ColorFormatter.YELLOW + ColorFormatter.BOLD)));
                }
            } else if (choice.equals("p")) {
                if (currentPage > 1) {
                    currentPage--;
                } else {
                    System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("⚠️ You are already on the first page.", ColorFormatter.YELLOW + ColorFormatter.BOLD)));
                }
            } else if (choice.equals("e")) {
                break;
            } else {
                System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("⚠️ Invalid input! Try again.", ColorFormatter.YELLOW + ColorFormatter.BOLD)));
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
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("❌ Error retrieving orders: " + e.getMessage(), ColorFormatter.RED + ColorFormatter.BOLD)));
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
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("❌ Error retrieving total pages: " + e.getMessage(), ColorFormatter.RED + ColorFormatter.BOLD)));
            e.printStackTrace();
        }
        return 1;
    }

    // Update order status
    private void updateOrderStatus() {
        viewAllOrders();
        int orderId = InputValidator.validateIntegerInput(scanner, ConsoleFormatter.centerText(ColorFormatter.colorText("Enter the Order ID: ", ColorFormatter.GREEN + ColorFormatter.BOLD)), 1, Integer.MAX_VALUE);
        if (orderId == -1) {
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("⚠️ Invalid input! Try again.", ColorFormatter.YELLOW + ColorFormatter.BOLD)));
            return; // Invalid input, do not proceed
        }

        // Create a table with a SINGLE wide column
        Table table = new Table(1, BorderStyle.UNICODE_BOX_DOUBLE_BORDER_WIDE, ShownBorders.ALL);
        table.setColumnWidth(0, 80, 90); // Explicitly setting column width wider

        CellStyle centerStyle = new CellStyle(CellStyle.HorizontalAlign.CENTER);
        CellStyle leftStyle = new CellStyle(CellStyle.HorizontalAlign.LEFT); // Left alignment for numbers

        // Title Row (Centered)
        table.addCell(BOLD_BLUE + "Restaurant Ordering System" + RESET, centerStyle);

        // Menu Options (Numbers Left-Aligned)
        String[] options = {
                "1.  Preparing",
                "2.  Ready",
                "3.  Completed"
        };

        for (String option : options) {
            table.addCell(BOLD_BLUE + option.trim() + RESET, leftStyle); // Left-align & reset colors properly
        }

        // Print the Table with WHITE Borders
        String[] tableLines = table.render().split("\n");
        for (String line : tableLines) {
            System.out.println(WHITE_BORDER + padding + line + RESET);
        }

        int statusChoice = InputValidator.validateIntegerInput(scanner, ConsoleFormatter.centerText(ColorFormatter.colorText("Enter your choice: ", ColorFormatter.GREEN + ColorFormatter.BOLD)), 1, 3);
        if (statusChoice == -1) {
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("⚠️ Invalid input! Try again.", ColorFormatter.YELLOW + ColorFormatter.BOLD)));
            return; // Invalid input, do not proceed
        }
        String newStatus;
        switch (statusChoice) {
            case 1 -> newStatus = "Preparing";
            case 2 -> newStatus = "Ready";
            case 3 -> newStatus = "Completed";
            default -> {
                System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("Invalid choice. Status not updated.", ColorFormatter.RED + ColorFormatter.BOLD)));
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
                System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("✅ Order status updated successfully!", ColorFormatter.GREEN + ColorFormatter.BOLD)));
            } else {
                System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("❌ Order ID not found. Status not updated.", ColorFormatter.RED + ColorFormatter.BOLD)));
            }

        } catch (SQLException e) {
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("❌ Error updating order status: " + e.getMessage(), ColorFormatter.RED + ColorFormatter.BOLD)));
            e.printStackTrace();
        }
    }

    // test the chefController method
    public static void main(String[] args) {
        new ChefController(new Scanner(System.in)).start();
    }
}