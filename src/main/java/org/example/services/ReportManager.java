package org.example.services;

import org.example.utils.ColorFormatter;
import org.example.utils.ConsoleFormatter;
import org.example.utils.DatabaseConnection;
import org.example.utils.Utils;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.ShownBorders;
import org.nocrala.tools.texttablefmt.Table;

import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;

public class ReportManager {
    final String RESET = "\u001B[0m";
    final String BOLD_BLUE = "\033[1;34m"; // Blue title
    final String BRIGHT_WHITE = "\033[97m"; // White text for options
    final String WHITE_BORDER = "\033[97m"; // White border
    final String BLUE = "\u001B[34m"; // Blue for padding

    int consoleWidth = 180; // Console width
    int tableWidth = 100; // Wider table width
    int leftPadding = (consoleWidth - tableWidth) / 2;
    String padding = " ".repeat(leftPadding);

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

            ResultSet rs = pstmt.executeQuery();

            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("--- Sales Report ---", ColorFormatter.GREEN + ColorFormatter.BOLD)));
            System.out.printf(ConsoleFormatter.centerText(ColorFormatter.colorText("%-10s %-30s %-15s %-15s%n", ColorFormatter.GREEN + ColorFormatter.BOLD)), "Item ID", "Name", "Quantity Sold", "Total Sales");
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("---------------------------------------------------------------", ColorFormatter.GREEN + ColorFormatter.BOLD)));

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                int itemId = rs.getInt("item_id");
                String name = rs.getString("name");
                int totalQuantity = rs.getInt("total_quantity");
                double totalSales = rs.getDouble("total_sales");

                System.out.printf(ConsoleFormatter.centerText(ColorFormatter.colorText("%-10d %-30s %-15d $%-15.2f%n", ColorFormatter.GREEN + ColorFormatter.BOLD)), itemId, name, totalQuantity, totalSales);
            }

            if (!hasData) {
                System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("No sales data available for the specified period.", ColorFormatter.RED + ColorFormatter.BOLD)));
            }
        } catch (SQLException e) {
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("❌ An error occurred while generating the sales report.", ColorFormatter.RED + ColorFormatter.BOLD)));
            e.printStackTrace();
        }
    }

    public static void generateRevenueReport(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT SUM((sell_price - discount) * quantity) AS total_revenue " +
                "FROM order_items " +
                "WHERE DATE(order_date) BETWEEN ? AND ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(startDate));
            pstmt.setDate(2, Date.valueOf(endDate));

            ResultSet rs = pstmt.executeQuery();

            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("--- Revenue Report ---", ColorFormatter.GREEN + ColorFormatter.BOLD)));
            if (rs.next()) {
                double totalRevenue = rs.getDouble("total_revenue");
                System.out.printf(ConsoleFormatter.centerText(ColorFormatter.colorText("Total Revenue from %s to %s: $%.2f%n", ColorFormatter.GREEN + ColorFormatter.BOLD)), startDate, endDate, totalRevenue);
            } else {
                System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("No revenue data available for the specified period.", ColorFormatter.RED + ColorFormatter.BOLD)));
            }
        } catch (SQLException e) {
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("❌ An error occurred while generating the revenue report.", ColorFormatter.RED + ColorFormatter.BOLD)));
            e.printStackTrace();
        }
    }

    public static void generateProfitReport(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT SUM((sell_price - base_price - COALESCE(discount, 0)) * quantity) AS total_profit " +
                "FROM order_items " +
                "WHERE DATE(order_date) BETWEEN ? AND ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(startDate));
            pstmt.setDate(2, Date.valueOf(endDate));

            ResultSet rs = pstmt.executeQuery();

            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("--- Profit Report ---", ColorFormatter.GREEN + ColorFormatter.BOLD)));
            if (rs.next()) {
                double totalProfit = rs.getDouble("total_profit");
                if (totalProfit == 0) {
                    System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("No profit generated for the specified period.", ColorFormatter.RED + ColorFormatter.BOLD)));
                } else {
                    System.out.printf(ConsoleFormatter.centerText(ColorFormatter.colorText("Total Profit from %s to %s: $%.2f%n", ColorFormatter.GREEN + ColorFormatter.BOLD)), startDate, endDate, totalProfit);
                }
            } else {
                System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("No profit data available for the specified period.", ColorFormatter.RED + ColorFormatter.BOLD)));
            }
        } catch (SQLException e) {
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("❌ An error occurred while generating the profit report.", ColorFormatter.RED + ColorFormatter.BOLD)));
            e.printStackTrace();
        }
    }

    private static LocalDate[] promptForDateRange(Scanner scanner) {
        while (true) {
            try {
                System.out.print(ConsoleFormatter.centerText(ColorFormatter.colorText("Enter start date (YYYY-MM-DD): ", ColorFormatter.GREEN + ColorFormatter.BOLD)));
                LocalDate startDate = LocalDate.parse(scanner.nextLine().trim());
                System.out.print(ConsoleFormatter.centerText(ColorFormatter.colorText("Enter end date (YYYY-MM-DD): ", ColorFormatter.GREEN + ColorFormatter.BOLD)));
                LocalDate endDate = LocalDate.parse(scanner.nextLine().trim());

                if (startDate.isAfter(endDate)) {
                    System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("❌ Start date cannot be after end date. Please try again.", ColorFormatter.RED + ColorFormatter.BOLD)));
                    continue;
                }

                return new LocalDate[]{startDate, endDate};
            } catch (Exception e) {
                System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("❌ Invalid date format. Please enter the date in YYYY-MM-DD format.", ColorFormatter.RED + ColorFormatter.BOLD)));
            }
        }
    }

    public static void manageReports() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            ReportManager reportManager = new ReportManager();

            // Create a table with a SINGLE wide column
            Table table = new Table(1, BorderStyle.UNICODE_BOX_DOUBLE_BORDER_WIDE, ShownBorders.ALL);
            table.setColumnWidth(0, 80, 90); // Explicitly setting column width wider

            CellStyle centerStyle = new CellStyle(CellStyle.HorizontalAlign.CENTER);
            CellStyle leftStyle = new CellStyle(CellStyle.HorizontalAlign.LEFT); // Left alignment for numbers

            // Title Row (Centered)
            table.addCell(reportManager.BOLD_BLUE + "Restaurant Ordering System" + reportManager.RESET, centerStyle);

            // Menu Options (Numbers Left-Aligned)
            String[] options = {
                    "1.  Generate Sales Report",
                    "2.  Generate Revenue Report",
                    "3.  Generate Profit Report",
                    "4.  Back to Main Menu"
            };

            for (String option : options) {
                table.addCell(reportManager.BOLD_BLUE + option.trim() + reportManager.RESET, leftStyle); // Left-align & reset colors properly
            }

            // Print the Table with WHITE Borders
            String[] tableLines = table.render().split("\n");
            for (String line : tableLines) {
                System.out.println(reportManager.WHITE_BORDER + reportManager.padding + line + reportManager.RESET);
            }

            int choice = Utils.validateIntegerInput(scanner, ConsoleFormatter.centerText(ColorFormatter.colorText("👉 Enter your choice: ", ColorFormatter.GREEN + ColorFormatter.BOLD)), 1, 4);
            if (choice == -1) {
                System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("⚠️ Invalid input! Try again.", ColorFormatter.YELLOW + ColorFormatter.BOLD)));
                continue;
            }

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
                    System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("Invalid choice! Please try again.", ColorFormatter.RED + ColorFormatter.BOLD)));
            }
        }
    }
}