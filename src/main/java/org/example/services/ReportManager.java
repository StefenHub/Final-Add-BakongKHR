package org.example.services;

import org.example.utils.ColorFormatter;
import org.example.utils.ConsoleFormatter;
import org.example.utils.DatabaseUtil;
import org.example.utils.Utils;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.ShownBorders;
import org.nocrala.tools.texttablefmt.Table;

import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        Logger logger = Logger.getLogger(ReportManager.class.getName());

        // ✅ Validate Dates
        if (startDate == null || endDate == null) {
            logger.warning("❌ Error: Start Date or End Date is null!");
            return;
        }

        // ✅ Optimized Query: Use correct table joins
        String sql = "SELECT oi.item_id, mi.name, SUM(oi.quantity) AS total_quantity, " +
                "SUM(oi.sell_price * oi.quantity) AS total_sales " +
                "FROM order_items oi " +
                "JOIN orders o ON oi.order_id = o.order_id " +
                "JOIN menuItemsAdmin mi ON oi.item_id = mi.item_id " +
                "WHERE DATE(o.order_date) BETWEEN ? AND ? " +
                "GROUP BY oi.item_id, mi.name " +
                "ORDER BY total_quantity DESC";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(startDate));
            pstmt.setDate(2, Date.valueOf(endDate));

            // ✅ Improved Logging
            logger.info((ColorFormatter.colorText(
                    String.format("📊 Running Sales Report for period: %s to %s", startDate, endDate),
                    ColorFormatter.GREEN + ColorFormatter.BOLD)));

            ResultSet rs = pstmt.executeQuery();

            // ✅ Print Report Header
            System.out.println((
                    ColorFormatter.colorText("--- Sales Report ---", ColorFormatter.GREEN + ColorFormatter.BOLD)));

            String header = String.format("%-10s %-30s %-15s %-15s", "Item ID", "Name", "Quantity Sold", "Total Sales");
            System.out.println((ColorFormatter.colorText(header, ColorFormatter.GREEN + ColorFormatter.BOLD)));
            System.out.println((ColorFormatter.colorText("---------------------------------------------------------------", ColorFormatter.GREEN + ColorFormatter.BOLD)));

            // ✅ Track Grand Total Sales
            double grandTotalSales = 0;
            boolean hasData = false;

            while (rs.next()) {
                hasData = true;
                int itemId = rs.getInt("item_id");
                String name = rs.getString("name");
                int totalQuantity = rs.getInt("total_quantity");
                double totalSales = rs.getDouble("total_sales");

                grandTotalSales += totalSales; // ✅ Accumulate total sales

                String formattedRow = String.format("%-10d %-30s %-15d $%-14.2f", itemId, name, totalQuantity, totalSales);
                System.out.println((ColorFormatter.colorText(formattedRow, ColorFormatter.GREEN + ColorFormatter.BOLD)));
            }

            if (!hasData) {
                System.out.println((
                        ColorFormatter.colorText("No sales data available for the specified period.", ColorFormatter.RED + ColorFormatter.BOLD)));
            } else {
                // ✅ Print Grand Total Sales at the End
                System.out.println((ColorFormatter.colorText("---------------------------------------------------------------", ColorFormatter.GREEN + ColorFormatter.BOLD)));
                String totalSalesMsg = String.format("Grand Total Sales: $%.2f", grandTotalSales);
                System.out.println((
                        ColorFormatter.colorText(totalSalesMsg, ColorFormatter.GREEN + ColorFormatter.BOLD)));
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "❌ An error occurred while generating the sales report.", e);
        }
    }


    public static void generateRevenueReport(LocalDate startDate, LocalDate endDate) {
        Logger logger = Logger.getLogger(ReportManager.class.getName());

        if (startDate == null || endDate == null) {
            logger.warning("❌ Error: Start Date or End Date is null!");
            return;
        }

        String sql = "SELECT SUM((oi.sell_price - COALESCE(oi.discount, 0)) * oi.quantity) AS total_revenue " +
                "FROM order_items oi " +
                "JOIN orders o ON oi.order_id = o.order_id " + // Fix: Ensure we use correct table for date
                "WHERE DATE(o.order_date) BETWEEN ? AND ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(startDate));
            pstmt.setDate(2, Date.valueOf(endDate));

            logger.info("Executing Revenue Report Query...");
            logger.info("Start Date: " + startDate + ", End Date: " + endDate);

            ResultSet rs = pstmt.executeQuery();

            System.out.println((
                    ColorFormatter.colorText("--- Revenue Report ---", ColorFormatter.GREEN + ColorFormatter.BOLD)));

            if (rs.next()) {
                double totalRevenue = rs.getDouble("total_revenue");

                // Fix: Format string before applying color
                String revenueMessage = String.format("Total Revenue from %s to %s: $%.2f", startDate, endDate, totalRevenue);
                System.out.println((
                        ColorFormatter.colorText(revenueMessage, ColorFormatter.GREEN + ColorFormatter.BOLD)));
            } else {
                System.out.println((
                        ColorFormatter.colorText("No revenue data available for the specified period.", ColorFormatter.RED + ColorFormatter.BOLD)));
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "❌ An error occurred while generating the revenue report.", e);
        }
    }


    public static void generateProfitReport(LocalDate startDate, LocalDate endDate) {
        // Ensure dates are not null
        if (startDate == null || endDate == null) {
            System.out.println((
                    ColorFormatter.colorText("❌ Error: Start Date or End Date is null!", ColorFormatter.RED + ColorFormatter.BOLD)));
            return;
        }

        // Corrected SQL query with proper joins
        String sql = "SELECT COALESCE(SUM((oi.sell_price - mi.base_price - COALESCE(oi.discount, 0)) * oi.quantity), 0) AS total_profit " +
                "FROM order_items oi " +
                "JOIN orders o ON oi.order_id = o.order_id " +  // Ensure order_date comes from 'orders'
                "JOIN menuItemsAdmin mi ON oi.item_id = mi.item_id " +  // Ensure base_price comes from 'menuItemsAdmin'
                "WHERE DATE(o.order_date) BETWEEN ? AND ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(startDate));
            pstmt.setDate(2, Date.valueOf(endDate));

            // Debugging: Print the dates to verify they are correct
            System.out.println("Executing Profit Report Query...");
            System.out.println("Start Date: " + startDate);
            System.out.println("End Date: " + endDate);

            ResultSet rs = pstmt.executeQuery();

            System.out.println((
                    ColorFormatter.colorText("--- Profit Report ---", ColorFormatter.GREEN + ColorFormatter.BOLD)));

            if (rs.next()) {
                double totalProfit = rs.getDouble("total_profit");

                if (totalProfit == 0) {
                    System.out.println((
                            ColorFormatter.colorText("No profit generated for the specified period.", ColorFormatter.RED + ColorFormatter.BOLD)));
                } else {
                    String profitMessage = String.format("Total Profit from %s to %s: $%.2f", startDate, endDate, totalProfit);
                    System.out.println((
                            ColorFormatter.colorText(profitMessage, ColorFormatter.GREEN + ColorFormatter.BOLD)));
                }
            } else {
                System.out.println(ConsoleFormatter.centerText(
                        ColorFormatter.colorText("No profit data available for the specified period.", ColorFormatter.RED + ColorFormatter.BOLD)));
            }
        } catch (SQLException e) {
            System.out.println(ConsoleFormatter.centerText(
                    ColorFormatter.colorText("❌ An error occurred while generating the profit report.", ColorFormatter.RED + ColorFormatter.BOLD)));
            e.printStackTrace();
        }
    }



    private static LocalDate[] promptForDateRange(Scanner scanner) {
        while (true) {
            try {
                System.out.print((ColorFormatter.colorText("Enter start date (YYYY-MM-DD): ", ColorFormatter.GREEN + ColorFormatter.BOLD)));
                LocalDate startDate = LocalDate.parse(scanner.nextLine().trim());
                System.out.print(ConsoleFormatter.centerText(ColorFormatter.colorText("Enter end date (YYYY-MM-DD): ", ColorFormatter.GREEN + ColorFormatter.BOLD)));
                LocalDate endDate = LocalDate.parse(scanner.nextLine().trim());

                if (startDate.isAfter(endDate)) {
                    System.out.println((ColorFormatter.colorText("❌ Start date cannot be after end date. Please try again.", ColorFormatter.RED + ColorFormatter.BOLD)));
                    continue;
                }

                return new LocalDate[]{startDate, endDate};
            } catch (Exception e) {
                System.out.println((ColorFormatter.colorText("❌ Invalid date format. Please enter the date in YYYY-MM-DD format.", ColorFormatter.RED + ColorFormatter.BOLD)));
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
                System.out.println(reportManager.WHITE_BORDER + line + reportManager.RESET);
            }

            int choice = Utils.validateIntegerInput(scanner, (ColorFormatter.colorText("👉 Enter your choice: ", ColorFormatter.GREEN + ColorFormatter.BOLD)), 1, 4);
            if (choice == -1) {
                System.out.println((ColorFormatter.colorText("⚠️ Invalid input! Try again.", ColorFormatter.YELLOW + ColorFormatter.BOLD)));
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
                    System.out.println((ColorFormatter.colorText("⚠️Invalid choice! Please try again.", ColorFormatter.RED + ColorFormatter.BOLD)));
            }
        }
    }
}