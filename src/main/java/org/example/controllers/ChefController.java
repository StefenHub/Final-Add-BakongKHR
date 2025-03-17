package org.example.controllers;

import com.google.zxing.WriterException;
import org.example.utils.*;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.ShownBorders;
import org.nocrala.tools.texttablefmt.Table;

import java.sql.*;
import java.util.Scanner;

import static org.example.views.DisplayUI.displayUI;

public class ChefController {
    private final Scanner scanner;
    private static final String RESET = "\u001B[0m";
    private static final String BOLD_BLUE = "\033[1;34m"; // Blue title
    private static final String WHITE_BORDER = "\033[97m"; // White border
    private static final String BLUE = "\u001B[34m"; // Blue for padding

    private static final int consoleWidth = 180; // Console width
    private static final int tableWidth = 100; // Wider table width
    private static final String padding = " ".repeat((consoleWidth - tableWidth) / 2);
    private PaginationFormatter pagination;

    public ChefController(Scanner scanner) {
        this.scanner = scanner;
    }

    // Start the chefController interaction menu
    public void start() throws WriterException {
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
                    "4.  Exit"
            };

            for (String option : options) {
                table.addCell(BOLD_BLUE + option.trim() + RESET, leftStyle); // Left-align & reset colors properly
            }

            // Print the Table with WHITE Borders
            String[] tableLines = table.render().split("\n");
            for (String line : tableLines) {
                System.out.println(WHITE_BORDER + line + RESET);
            }

            int choice = InputValidator.validateIntegerInput(scanner, (ColorFormatter.colorText("👉 Enter your choice: ", ColorFormatter.GREEN + ColorFormatter.BOLD)), 0, 3);
            if (choice == -1) {
                System.out.println((ColorFormatter.colorText("⚠️ Invalid input! Try again.", ColorFormatter.YELLOW + ColorFormatter.BOLD)));
                continue;
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
                case 4:
                    System.out.println((ColorFormatter.colorText("Exiting chefController menu...", ColorFormatter.GREEN + ColorFormatter.BOLD)));
                    displayUI();
                default:
                    System.out.println((ColorFormatter.colorText("Invalid choice. Please try again.", ColorFormatter.RED + ColorFormatter.BOLD)));
            }
        }
    }

    // View all orders
    private void viewAllOrders() {
        int totalItems = getTotalItems("SELECT COUNT(*) FROM orders");
        pagination = new PaginationFormatter(totalItems, 10); // Initialize pagination with default items per page

        while (true) {
            displayOrders("SELECT order_id, order_date, status FROM orders ORDER BY order_date DESC LIMIT ? OFFSET ?", pagination.getCurrentPage());

            System.out.println((ColorFormatter.colorText("📄 Page " + pagination.getCurrentPage() + " of " + pagination.getTotalPages(), ColorFormatter.GREEN + ColorFormatter.BOLD)));
            System.out.println((ColorFormatter.colorText("[N] Next  |  [P] Previous  |  [C] Change items per page  |  [E] Exit", ColorFormatter.GREEN + ColorFormatter.BOLD)));

            String choice = scanner.next().toLowerCase();
            if (choice.equals("n")) {
                pagination.nextPage();
            } else if (choice.equals("p")) {
                pagination.previousPage();
            } else if (choice.equals("c")) {
                changeItemsPerPage();
            } else if (choice.equals("e")) {
                break;
            } else {
                System.out.println((ColorFormatter.colorText("⚠️ Invalid input! Try again.", ColorFormatter.YELLOW + ColorFormatter.BOLD)));
            }
        }
    }

    // View pending orders
    private void viewPendingOrders() {
        int totalItems = getTotalItems("SELECT COUNT(*) FROM orders WHERE status = 'pending'");
        pagination = new PaginationFormatter(totalItems, 10); // Initialize pagination with default items per page

        while (true) {
            displayOrders("SELECT order_id, order_date, status FROM orders WHERE status = 'pending' ORDER BY order_date ASC LIMIT ? OFFSET ?", pagination.getCurrentPage());

            System.out.println((ColorFormatter.colorText("📄 Page " + pagination.getCurrentPage() + " of " + pagination.getTotalPages(), ColorFormatter.GREEN + ColorFormatter.BOLD)));
            System.out.println((ColorFormatter.colorText("[N] Next  |  [P] Previous  |  [C] Change items per page  |  [E] Exit", ColorFormatter.GREEN + ColorFormatter.BOLD)));
            String choice = scanner.next().toLowerCase();
            if (choice.equals("n")) {
                pagination.nextPage();
            } else if (choice.equals("p")) {
                pagination.previousPage();
            } else if (choice.equals("c")) {
                changeItemsPerPage();
            } else if (choice.equals("e")) {
                break;
            } else {
                System.out.println((ColorFormatter.colorText("⚠️ Invalid input! Try again.", ColorFormatter.YELLOW + ColorFormatter.BOLD)));
            }
        }
    }

    // Display orders with pagination
    private void displayOrders(String query, int page) {
        int offset = (page - 1) * pagination.getItemsPerPage();

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, pagination.getItemsPerPage());
            stmt.setInt(2, offset);
            ResultSet rs = stmt.executeQuery();

            Table table = new Table(3, BorderStyle.UNICODE_ROUND_BOX_WIDE, ShownBorders.ALL);
            table.addCell("Order ID", new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell("Order Date", new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell("Status", new CellStyle(CellStyle.HorizontalAlign.CENTER));

            while (rs.next()) {
                table.addCell(String.valueOf(rs.getInt("order_id")), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(rs.getTimestamp("order_date").toString(), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(rs.getString("status"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
            }

            System.out.println(table.render());

        } catch (SQLException e) {
            System.out.println((ColorFormatter.colorText("❌ Error retrieving orders: " + e.getMessage(), ColorFormatter.RED + ColorFormatter.BOLD)));
            e.printStackTrace();
        }
    }

    // Get total items for pagination
    private int getTotalItems(String countQuery) {
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countQuery)) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.out.println((ColorFormatter.colorText("❌ Error retrieving total items: " + e.getMessage(), ColorFormatter.RED + ColorFormatter.BOLD)));
            e.printStackTrace();
        }
        return 0;
    }

    // Change items per page
    private void changeItemsPerPage() {
        System.out.println((ColorFormatter.colorText("Enter the number of items per page: ", ColorFormatter.GREEN + ColorFormatter.BOLD)));
        int itemsPerPage = InputValidator.validateIntegerInput(scanner, "", 1, Integer.MAX_VALUE);
        if (itemsPerPage > 0) {
            pagination.setItemsPerPage(itemsPerPage);
            pagination.reset();
        } else {
            System.out.println((ColorFormatter.colorText("⚠️ Invalid input! Try again.", ColorFormatter.YELLOW + ColorFormatter.BOLD)));
        }
    }

    // Update order status
    private void updateOrderStatus() {
        viewAllOrders();
        int orderId = InputValidator.validateIntegerInput(scanner, (ColorFormatter.colorText("Enter the Order ID: ", ColorFormatter.GREEN + ColorFormatter.BOLD)), 1, Integer.MAX_VALUE);
        if (orderId == -1) {
            System.out.println((ColorFormatter.colorText("⚠️ Invalid input! Try again.", ColorFormatter.YELLOW + ColorFormatter.BOLD)));
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
            System.out.println(WHITE_BORDER + line + RESET);
        }

        int statusChoice = InputValidator.validateIntegerInput(scanner, (ColorFormatter.colorText("Enter your choice: ", ColorFormatter.GREEN + ColorFormatter.BOLD)), 1, 3);
        if (statusChoice == -1) {
            System.out.println((ColorFormatter.colorText("⚠️ Invalid input! Try again.", ColorFormatter.YELLOW + ColorFormatter.BOLD)));
            return;
        }
        String newStatus;
        switch (statusChoice) {
            case 1 -> newStatus = "Preparing";
            case 2 -> newStatus = "Ready";
            case 3 -> newStatus = "Completed";
            default -> {
                System.out.println((ColorFormatter.colorText("Invalid choice. Status not updated.", ColorFormatter.RED + ColorFormatter.BOLD)));
                return;
            }
        }

        String query = "UPDATE orders SET status = ? WHERE order_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, newStatus);
            pstmt.setInt(2, orderId);
            int rowsUpdated = pstmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println((ColorFormatter.colorText("✅ Order status updated successfully!", ColorFormatter.GREEN + ColorFormatter.BOLD)));
            } else {
                System.out.println((ColorFormatter.colorText("❌ Order ID not found. Status not updated.", ColorFormatter.RED + ColorFormatter.BOLD)));
            }

        } catch (SQLException e) {
            System.out.println((ColorFormatter.colorText("❌ Error updating order status: " + e.getMessage(), ColorFormatter.RED + ColorFormatter.BOLD)));
            e.printStackTrace();
        }
    }

    // test the chefController method
    public static void main(String[] args) throws WriterException {
        new ChefController(new Scanner(System.in)).start();
    }
}