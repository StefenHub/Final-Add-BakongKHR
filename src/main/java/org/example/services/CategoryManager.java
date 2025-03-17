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
import java.util.*;

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
    public static List<Map<String, Object>> getCategories() {
        List<Map<String, Object>> categories = new ArrayList<>();

        try (Connection conn = DatabaseUtil.connect()) {
            if (conn == null) {
                throw new SQLException("❌ Failed to connect to the database.");
            }

            String sql = "SELECT id, name FROM categories ORDER BY id";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> category = new HashMap<>();
                    category.put("id", rs.getInt("id"));
                    category.put("name", rs.getString("name"));
                    categories.add(category);
                }
            }
        } catch (SQLException e) {
            System.out.println((ColorFormatter.colorText("❌ Database error: " + e.getMessage(), ColorFormatter.RED + ColorFormatter.BOLD)));
            e.printStackTrace(); // ✅ Print full stack trace for debugging
        }
        return categories;
    }


    // --------------- Display categories in a table ---------------
    public static void displayCategories() {
        List<Map<String, Object>> categories = getCategories();
        if (categories.isEmpty()) {
            System.out.println((ColorFormatter.colorText("❌ No categories available.", ColorFormatter.RED + ColorFormatter.BOLD)));
            return;
        }

        Table table = new Table(2, BorderStyle.UNICODE_BOX_WIDE, ShownBorders.ALL);
        table.addCell(ColorFormatter.colorText("ID", ColorFormatter.BLUE + ColorFormatter.BOLD), new CellStyle(CellStyle.HorizontalAlign.CENTER));
        table.addCell(ColorFormatter.colorText("Category Name", ColorFormatter.BLUE + ColorFormatter.BOLD), new CellStyle(CellStyle.HorizontalAlign.CENTER));
        for (Map<String, Object> category : categories) {
            table.addCell(ColorFormatter.colorText(String.valueOf(category.get("id")), ColorFormatter.BLUE + ColorFormatter.BOLD), new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell(ColorFormatter.colorText((String) category.get("name"), ColorFormatter.BLUE + ColorFormatter.BOLD), new CellStyle(CellStyle.HorizontalAlign.CENTER));
        }

        System.out.println(ConsoleFormatter.centerText(table.render()));
    }

    // Add categories
    public static void addCategory(Scanner scanner) {
        displayCategories();
        System.out.print((ColorFormatter.colorText("Enter the name of the new category ([b] to go back): ", ColorFormatter.GREEN + ColorFormatter.BOLD)));
        String newCategory = scanner.nextLine().trim();

        if (newCategory.equalsIgnoreCase("b")) return;
        if (newCategory.isEmpty()) {
            System.out.println((ColorFormatter.colorText("❌ Category name cannot be empty.", ColorFormatter.RED + ColorFormatter.BOLD)));
            return;
        }
        if (!newCategory.matches("[a-zA-Z0-9\\s]+")) {
            System.out.println((ColorFormatter.colorText("❌ Invalid category name. Only alphanumeric characters and spaces are allowed.", ColorFormatter.RED + ColorFormatter.BOLD)));
            return;
        }

        try (Connection conn = DatabaseUtil.connect()) {
            if (conn == null) {
                System.out.println((ColorFormatter.colorText("❌ Failed to connect to the database.", ColorFormatter.RED + ColorFormatter.BOLD)));
                return;
            }

            String sql = "INSERT INTO categories (name) VALUES (?) ON CONFLICT (name) DO NOTHING";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, newCategory);
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    System.out.println((ColorFormatter.colorText("✅ Category '" + newCategory + "' added successfully!", ColorFormatter.GREEN + ColorFormatter.BOLD)));
                } else {
                    System.out.println((ColorFormatter.colorText("⚠️ Category already exists.", ColorFormatter.YELLOW + ColorFormatter.BOLD)));
                }
            }
        } catch (SQLException e) {
            System.out.println((ColorFormatter.colorText("❌ Database error: " + e.getMessage(), ColorFormatter.RED + ColorFormatter.BOLD)));
        }
    }

    // Remove categories
    public static void removeCategory(Scanner scanner) {
        displayCategories();
        List<Map<String, Object>> categories = getCategories();
        if (categories.isEmpty()) return;

        int categoryNumber = Utils.validateIntegerInput(scanner,
                (ColorFormatter.colorText("Enter the number of the category to remove ([b] to go back): ",
                        ColorFormatter.GREEN + ColorFormatter.BOLD)), 1, categories.size());

        if (categoryNumber == -1) return;

        // ✅ Get category ID instead of name
        Map<String, Object> selectedCategory = categories.get(categoryNumber - 1);
        int categoryId = (int) selectedCategory.get("id");
        String categoryName = (String) selectedCategory.get("name");

        System.out.print((ColorFormatter.colorText(
                "Are you sure you want to remove the category '" + categoryName + "' and all its items? (y/n): ",
                ColorFormatter.GREEN + ColorFormatter.BOLD)));

        String confirm = scanner.nextLine().trim().toLowerCase();
        if (!confirm.equals("y")) {
            System.out.println((ColorFormatter.colorText("⚠️ Removal canceled.", ColorFormatter.YELLOW + ColorFormatter.BOLD)));
            return;
        }

        try (Connection conn = DatabaseUtil.connect()) {
            if (conn == null) {
                System.out.println((ColorFormatter.colorText("❌ Failed to connect to the database.", ColorFormatter.RED + ColorFormatter.BOLD)));
                return;
            }

            conn.setAutoCommit(false); // ✅ Start transaction

            // ✅ 1. Remove menu items under this category
            String deleteMenuItems = "DELETE FROM menuitemsadmin WHERE category_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteMenuItems)) {
                pstmt.setInt(1, categoryId); // ✅ Use category ID
                pstmt.executeUpdate();
            }

            // ✅ 2. Remove the category itself
            String deleteCategory = "DELETE FROM categories WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteCategory)) {
                pstmt.setInt(1, categoryId); // ✅ Use category ID
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    conn.commit(); // ✅ Commit transaction
                    System.out.println((ColorFormatter.colorText("✅ Category '" + categoryName + "' and all its items removed successfully!", ColorFormatter.GREEN + ColorFormatter.BOLD)));
                } else {
                    conn.rollback(); // ✅ Rollback if deletion failed
                    System.out.println((ColorFormatter.colorText("❌ Failed to remove category.", ColorFormatter.RED + ColorFormatter.BOLD)));
                }
            }
        } catch (SQLException e) {
            System.out.println((ColorFormatter.colorText("❌ Database error: " + e.getMessage(), ColorFormatter.RED + ColorFormatter.BOLD)));
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

            int choice = Utils.validateIntegerInput(scanner, (ColorFormatter.colorText("👉 Enter your choice ([b] to go back): ", ColorFormatter.GREEN + ColorFormatter.BOLD)), 0, 3);
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
                    System.out.println((ColorFormatter.colorText("❌ Invalid choice! Please try again.", ColorFormatter.RED + ColorFormatter.BOLD)));
            }
        }
    }

    // test method
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        manageCategories(scanner);
    }
}