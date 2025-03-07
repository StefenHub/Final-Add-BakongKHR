package org.example.services;

import java.sql.*;
import java.util.Scanner;

import org.example.utils.DatabaseConnection;
import org.nocrala.tools.texttablefmt.*;

public class OrderManager {
    private static final int PAGE_SIZE = 10; // Number of orders per page

    public static void viewAllCustomerOrders() {
        Scanner scanner = new Scanner(System.in);
        int currentPage = 1;
        int totalPages = getTotalPages();

        while (true) {
            displayOrders(currentPage);
            System.out.println("\t📄 Page " + currentPage + " of " + totalPages);
            System.out.println("\t🚪 Options: [➡️ N] Next | [⬅️ P] Previous | [❌ Q] Quit");
            System.out.print("\t👉 Enter choice: ");
            String choice = scanner.nextLine().trim().toLowerCase();

            if (choice.equals("n") && currentPage < totalPages) {
                currentPage++;
            } else if (choice.equals("p") && currentPage > 1) {
                currentPage--;
            } else if (choice.equals("q")) {
                System.out.println("\t❌Exiting pagination view. ");
                break;
            } else {
                System.out.println("\t❌Invalid input. Please enter [➡️ N], [⬅️ P], or [❌ Q].");
            }
        }
    }

    private static void displayOrders(int page) {
        int offset = (page - 1) * PAGE_SIZE;
        String query = "SELECT order_id,name , size, quantity, description, order_date FROM order_items ORDER BY order_date DESC LIMIT ? OFFSET ?";
        int consoleWidth = 100;
        int tableWidth = 90;
        int leftPadding = (consoleWidth - tableWidth) / 2;
        String padding = " ".repeat(leftPadding);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, PAGE_SIZE);
            pstmt.setInt(2, offset);
            ResultSet rs = pstmt.executeQuery();

            Table table = new Table(7, BorderStyle.UNICODE_ROUND_BOX_WIDE, ShownBorders.ALL);
            table.addCell("No.", new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell("Order ID", new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell("Item Name", new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell("Size", new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell("Quantity", new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell("Description", new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell("Order Date", new CellStyle(CellStyle.HorizontalAlign.CENTER));

            int count = offset + 1;
            while (rs.next()) {
                table.addCell(String.valueOf(count++), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(rs.getString("order_id"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(rs.getString("name"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(rs.getString("size"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(rs.getString("quantity"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(rs.getString("description"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(rs.getString("order_date"), new CellStyle(CellStyle.HorizontalAlign.CENTER));

            }
            String[] tableLines = table.render().split("\n");
            for (String line : tableLines) {
                System.out.println(padding + line);
            }
//            System.out.println("\n\t--------- CUSTOMER ORDERS ---------");
//            System.out.println(table.render());

        } catch (SQLException e) {
            System.err.println("\t❌ Error retrieving customer orders: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static int getTotalPages() {
        String query = "SELECT COUNT(*) AS total FROM orders";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                int totalOrders = rs.getInt("total");
                return (int) Math.ceil((double) totalOrders / PAGE_SIZE);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error fetching order count: " + e.getMessage());
        }
        return 1;
    }
}
