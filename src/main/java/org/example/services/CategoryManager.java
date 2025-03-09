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
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CategoryManager {

    static final String RESET = "\u001B[0m";
    static final String BOLD_BLUE = "\033[1;34m"; // Blue title
    static final String BRIGHT_WHITE = "\033[97m"; // White text for options
    static final String WHITE_BORDER = "\033[97m"; // White border
    static final String BLUE = "\u001B[34m"; // Blue for padding

    static int consoleWidth = 180; // Console width
    static int tableWidth = 100; // Wider table width
    static int leftPadding = (consoleWidth - tableWidth) / 2;
    static String padding = " ".repeat(leftPadding);

    // --------------- Fetch all categories from the database ---------------
    public static List<String> getCategories() {
        List<String> categories = new ArrayList<>();
        try (Connection conn = DatabaseConnection.connect()) {
            if (conn == null) {
                System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("\t❌ Failed to connect to the database.", ColorFormatter.RED + ColorFormatter.BOLD)));
                return categories;
            }

            // Fetch distinct categories from menuitemsadmin table
            String sql = "SELECT DISTINCT category FROM menuitemsadmin ORDER BY category";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    categories.add(rs.getString("category"));
                }
            }
        } catch (SQLException e) {
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("\t❌ Database error: " + e.getMessage(), ColorFormatter.RED + ColorFormatter.BOLD)));
        }
        return categories;
    }

    // --------------- Display categories in a table ---------------
    public static void displayCategories() {
        List<String> categories = getCategories();
        if (categories.isEmpty()) {
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("\tNo categories available.", ColorFormatter.RED + ColorFormatter.BOLD)));
            return;
        }

        Table table = new Table(2, BorderStyle.UNICODE_BOX_WIDE, ShownBorders.ALL);
        table.addCell(ColorFormatter.colorText("ID", ColorFormatter.BLUE + ColorFormatter.BOLD), new CellStyle(CellStyle.HorizontalAlign.CENTER));
        table.addCell(ColorFormatter.colorText("Category Name", ColorFormatter.BLUE + ColorFormatter.BOLD), new CellStyle(CellStyle.HorizontalAlign.CENTER));

        for (int i = 0; i < categories.size(); i++) {
            table.addCell(ColorFormatter.colorText(String.valueOf(i + 1), ColorFormatter.BLUE + ColorFormatter.BOLD), new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell(ColorFormatter.colorText(categories.get(i), ColorFormatter.BLUE + ColorFormatter.BOLD), new CellStyle(CellStyle.HorizontalAlign.CENTER));
        }
        System.out.println(ConsoleFormatter.centerText(table.render()));
    }

    // --------------- Get category by index ---------------
    public static String getCategoryFromNumber(int categoryNumber) {
        List<String> categories = getCategories();
        if (categoryNumber < 1 || categoryNumber > categories.size()) {
            return "Unknown";
        }
        return categories.get(categoryNumber - 1);
    }

    // Add categories
    public static void addCategory(Scanner scanner) {
        displayCategories();
        System.out.print(ConsoleFormatter.centerText(ColorFormatter.colorText("Enter the name of the new category ([b] to go back): ", ColorFormatter.GREEN + ColorFormatter.BOLD)));
        String newCategory = scanner.nextLine().trim();

        if (newCategory.equalsIgnoreCase("b")) return;
        if (newCategory.isEmpty()) {
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("❌ Category name cannot be empty.", ColorFormatter.RED + ColorFormatter.BOLD)));
            return;
        }
        if (!newCategory.matches("[a-zA-Z0-9\\s]+")) {
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("❌ Invalid category name. Only alphanumeric characters and spaces are allowed.", ColorFormatter.RED + ColorFormatter.BOLD)));
            return;
        }

        try (Connection conn = DatabaseConnection.connect()) {
            if (conn == null) {
                System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("❌ Failed to connect to the database.", ColorFormatter.RED + ColorFormatter.BOLD)));
                return;
            }

            String sql = "INSERT INTO categories (name) VALUES (?) ON CONFLICT (name) DO NOTHING";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, newCategory);
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("✅ Category '" + newCategory + "' added successfully!", ColorFormatter.GREEN + ColorFormatter.BOLD)));
                } else {
                    System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("⚠️ Category already exists.", ColorFormatter.YELLOW + ColorFormatter.BOLD)));
                }
            }
        } catch (SQLException e) {
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("❌ Database error: " + e.getMessage(), ColorFormatter.RED + ColorFormatter.BOLD)));
        }
    }

    // Remove categories
    public static void removeCategory(Scanner scanner) {
        displayCategories();
        List<String> categories = getCategories();
        if (categories.isEmpty()) return;

        int categoryNumber = Utils.validateIntegerInput(scanner, ConsoleFormatter.centerText(ColorFormatter.colorText("Enter the number of the category to remove ([b] to go back): ", ColorFormatter.GREEN + ColorFormatter.BOLD)), 1, categories.size());
        if (categoryNumber == -1) return;

        String categoryToRemove = getCategoryFromNumber(categoryNumber);
        if (categoryToRemove.equals("Unknown")) {
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("❌ Invalid category number.", ColorFormatter.RED + ColorFormatter.BOLD)));
            return;
        }

        System.out.print(ConsoleFormatter.centerText(ColorFormatter.colorText("Are you sure you want to remove the category '" + categoryToRemove + "' and all its items? (y/n): ", ColorFormatter.GREEN + ColorFormatter.BOLD)));
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (!confirm.equals("y")) {
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("⚠️ Removal canceled.", ColorFormatter.YELLOW + ColorFormatter.BOLD)));
            return;
        }

        try (Connection conn = DatabaseConnection.connect()) {
            if (conn == null) {
                System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("❌ Failed to connect to the database.", ColorFormatter.RED + ColorFormatter.BOLD)));
                return;
            }

            // 1. Remove menu items under this category
            String deleteMenuItems = "DELETE FROM menuitemsadmin WHERE category = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteMenuItems)) {
                pstmt.setString(1, categoryToRemove);
                pstmt.executeUpdate();
            }

            // 2. Remove the category itself
            String deleteCategory = "DELETE FROM categories WHERE name = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteCategory)) {
                pstmt.setString(1, categoryToRemove);
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("✅ Category '" + categoryToRemove + "' and all its items removed successfully!", ColorFormatter.GREEN + ColorFormatter.BOLD)));
                } else {
                    System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("❌ Failed to remove category.", ColorFormatter.RED + ColorFormatter.BOLD)));
                }
            }
        } catch (SQLException e) {
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("❌ Database error: " + e.getMessage(), ColorFormatter.RED + ColorFormatter.BOLD)));
        }
    }

    // Manage categories
    public static void manageCategories(Scanner scanner) {
        while (true) {

            // Create a table with a SINGLE wide column
            Table table = new Table(1, BorderStyle.UNICODE_BOX_DOUBLE_BORDER_WIDE, ShownBorders.ALL);
            table.setColumnWidth(0, 80, 90); // Explicitly setting column width wider

            CellStyle centerStyle = new CellStyle(CellStyle.HorizontalAlign.CENTER);
            CellStyle leftStyle = new CellStyle(CellStyle.HorizontalAlign.LEFT); // Left alignment for numbers

            // Title Row (Centered)
            table.addCell(BOLD_BLUE + "MANAGE CATEGORIES" + RESET, centerStyle);

            // Menu Options (Numbers Left-Aligned)
            String[] options = {
                    "1.  Add Category",
                    "2.  Remove Category",
                    "3.  View Categories",
                    "0.  Back"
            };

            for (String option : options) {
                table.addCell(BOLD_BLUE + option.trim() + RESET, leftStyle); // Left-align & reset colors properly
            }

            // Print the Table with WHITE Borders
            String[] tableLines = table.render().split("\n");
            for (String line : tableLines) {
                System.out.println(WHITE_BORDER + padding + line + RESET);
            }

            int choice = Utils.validateIntegerInput(scanner, ConsoleFormatter.centerText(ColorFormatter.colorText("👉 Enter your choice ([b] to go back): ", ColorFormatter.GREEN + ColorFormatter.BOLD)), 0, 3);
            if (choice == -1) return;

            switch (choice) {
                case 1:
                    addCategory(scanner);
                    break;
                case 2:
                    removeCategory(scanner);
                    break;
                case 3:
                    displayCategories();
                    break;
                case 0:
                    return;
                default:
                    System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("❌ Invalid choice! Please try again.", ColorFormatter.RED + ColorFormatter.BOLD)));
            }
        }
    }

    // test method
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        manageCategories(scanner);
    }
}