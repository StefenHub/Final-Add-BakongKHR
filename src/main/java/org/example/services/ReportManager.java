package org.example.services;

import org.example.utils.DatabaseConnection;
import org.example.utils.Utils;

import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;

public class ReportManager {

    public static void generateSalesReport(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT item_id, name, SUM(quantity) AS total_quantity, SUM(sell_price * quantity) AS total_sales " +
                "FROM order_items " +
                "WHERE DATE(order_date) BETWEEN ? AND ? " +
                "GROUP BY item_id, name " +
                "ORDER BY total_quantity DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(startDate));
            pstmt.setDate(2, Date.valueOf(endDate));

            //System.out.println("\n\tFetching sales report for date range: " + startDate + " to " + endDate);

            ResultSet rs = pstmt.executeQuery();

            System.out.println("\n\t--- Sales Report ---");
            System.out.printf("\t%-10s %-30s %-15s %-15s%n", "Item ID", "Name", "Quantity Sold","Discount", "Total Sales");
            System.out.println("\t---------------------------------------------------------------");

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                int itemId = rs.getInt("item_id");
                String name = rs.getString("name");
                int totalQuantity = rs.getInt("total_quantity");
                double discount = rs.getDouble("discount");
                double totalSales = rs.getDouble("total_sales");

                System.out.printf("%-10d %-30s %-15d $%-15.2f%n", itemId, name, totalQuantity, totalSales);
            }

            if (!hasData) {
                System.out.println("\tNo sales data available for the specified period.");
            }
        } catch (SQLException e) {
            System.out.println("\t❌ An error occurred while generating the sales report.");
            e.printStackTrace();
        }
    }

    public static void generateRevenueReport(LocalDate startDate, LocalDate endDate) {
        // Use order_items table for accurate revenue calculation
        String sql = "SELECT SUM((sell_price - discount) * quantity) AS total_revenue " +
                "FROM order_items " +
                "WHERE DATE(order_date) BETWEEN ? AND ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(startDate));
            pstmt.setDate(2, Date.valueOf(endDate));

            //System.out.println("\n\tFetching revenue report for date range: " + startDate + " to " + endDate);

            ResultSet rs = pstmt.executeQuery();

            System.out.println("\n\t--- Revenue Report ---");
            if (rs.next()) {
                double totalRevenue = rs.getDouble("total_revenue");
                System.out.printf("\tTotal Revenue from %s to %s: $%.2f%n", startDate, endDate, totalRevenue);
            } else {
                System.out.println("\tNo revenue data available for the specified period.");
            }
        } catch (SQLException e) {
            System.out.println("\t❌ An error occurred while generating the revenue report.");
            e.printStackTrace();
        }
    }

    public static void generateProfitReport(LocalDate startDate, LocalDate endDate) {
        // Use the correct profit formula based on business logic
        String sql = "SELECT SUM((sell_price - base_price - COALESCE(discount, 0)) * quantity) AS total_profit " +
                "FROM order_items " +
                "WHERE DATE(order_date) BETWEEN ? AND ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(startDate));
            pstmt.setDate(2, Date.valueOf(endDate));

            //System.out.println("\n\tFetching profit report for date range: " + startDate + " to " + endDate);
            //System.out.println("\tExecuting SQL: " + pstmt.toString());

            ResultSet rs = pstmt.executeQuery();

            System.out.println("\n\t--- Profit Report ---");
            if (rs.next()) {
                double totalProfit = rs.getDouble("total_profit");
                if (totalProfit == 0) {
                    System.out.println("\tNo profit generated for the specified period.");
                } else {
                    System.out.printf("\tTotal Profit from %s to %s: $%.2f%n", startDate, endDate, totalProfit);
                }
            } else {
                System.out.println("\tNo profit data available for the specified period.");
            }
        } catch (SQLException e) {
            System.out.println("\t❌ An error occurred while generating the profit report.");
            e.printStackTrace();
        }
    }

    private static LocalDate[] promptForDateRange(Scanner scanner) {
        while (true) {
            try {
                System.out.print("\tEnter start date (YYYY-MM-DD): ");
                LocalDate startDate = LocalDate.parse(scanner.nextLine().trim());
                System.out.print("\tEnter end date (YYYY-MM-DD): ");
                LocalDate endDate = LocalDate.parse(scanner.nextLine().trim());

                if (startDate.isAfter(endDate)) {
                    System.out.println("\t❌ Start date cannot be after end date. Please try again.");
                    continue;
                }

                return new LocalDate[]{startDate, endDate};
            } catch (Exception e) {
                System.out.println("\t❌ Invalid date format. Please enter the date in YYYY-MM-DD format.");
            }
        }
    }

    public static void manageReports() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("""
                                \u001B[34m
                                ╔════════════════════════════════════╗
                                ║       📊 Report Management         ║
                                ╠════════════════════════════════════╣
                                ║   \u001B[33m[1]. 💰 Generate Sales Report\u001B[34m     ║
                                ║   \u001B[33m[2]. 💵 Generate Revenue Report\u001B[34m   ║
                                ║   \u001B[33m[3]. 📈 Generate Profit Report\u001B[34m    ║
                                ║   \u001B[31m[4]. 🔙 Back to Main Menu\u001B[34m         ║
                                ╚════════════════════════════════════╝ \u001B[0m
                            """);


            int choice = Utils.validateIntegerInput(scanner, "\t👉 Enter your choice: ", 1, 4);
            if (choice == -1) return;

            switch (choice) {
                case 1:
                    LocalDate[] salesDates = promptForDateRange(scanner);
                    generateSalesReport(salesDates[0], salesDates[1]);
                    break;
                case 2:
                    LocalDate[] revenueDates = promptForDateRange(scanner);
                    generateRevenueReport(revenueDates[0], revenueDates[1]);
                    break;
                case 3:
                    LocalDate[] profitDates = promptForDateRange(scanner);
                    generateProfitReport(profitDates[0], profitDates[1]);
                    break;
                case 4:
                    return;
                default:
                    System.out.println("\tInvalid choice! Please try again.");
            }
        }
    }
}