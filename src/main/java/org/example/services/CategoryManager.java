package org.example.services;

import org.example.utils.DatabaseConnection;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.ShownBorders;
import org.nocrala.tools.texttablefmt.Table;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CategoryManager {

    // --------------- Fetch all categories from the database ---------------
    public static List<String> getCategories() {
        List<String> categories = new ArrayList<>();
        try (Connection conn = DatabaseConnection.connect()) {
            if (conn == null) {
                System.out.println("❌ Failed to connect to the database.");
                return categories;
            }

            // Fetch categories ordered by insertion order (id)
            String sql = "SELECT name FROM categories ORDER BY id";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    categories.add(rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Database error: " + e.getMessage());
        }
        return categories;
    }

    // --------------- Display categories in a table ---------------
    public static void displayCategories() {
        List<String> categories = getCategories();
        if (categories.isEmpty()) {
            System.out.println("No categories available.");
            return;
        }

        Table table = new Table(2, BorderStyle.UNICODE_BOX_WIDE, ShownBorders.ALL);
        table.addCell("ID", new CellStyle(CellStyle.HorizontalAlign.CENTER));
        table.addCell("Category Name", new CellStyle(CellStyle.HorizontalAlign.CENTER));

        for (int i = 0; i < categories.size(); i++) {
            table.addCell(String.valueOf(i + 1), new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell(categories.get(i), new CellStyle(CellStyle.HorizontalAlign.CENTER));
        }
        System.out.println(table.render());
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
        System.out.print("Enter the name of the new category ([b] to go back): ");
        String newCategory = scanner.nextLine().trim();

        if (newCategory.equalsIgnoreCase("b")) return;
        if (newCategory.isEmpty()) {
            System.out.println("❌ Category name cannot be empty.");
            return;
        }
        if (!newCategory.matches("[a-zA-Z0-9\\s]+")) {
            System.out.println("❌ Invalid category name. Only alphanumeric characters and spaces are allowed.");
            return;
        }

        try (Connection conn = DatabaseConnection.connect()) {
            if (conn == null) {
                System.out.println("❌ Failed to connect to the database.");
                return;
            }

            String sql = "INSERT INTO categories (name) VALUES (?) ON CONFLICT (name) DO NOTHING";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, newCategory);
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    System.out.println("✅ Category '" + newCategory + "' added successfully!");
                } else {
                    System.out.println("⚠️ Category already exists.");
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Database error: " + e.getMessage());
        }
    }

    // Remove categories
    public static void removeCategory(Scanner scanner) {
        displayCategories();
        List<String> categories = getCategories();
        if (categories.isEmpty()) return;

        int categoryNumber = Utils.validateIntegerInput(scanner, "Enter the number of the category to remove ([b] to go back): ", 1, categories.size());
        if (categoryNumber == -1) return;

        String categoryToRemove = getCategoryFromNumber(categoryNumber);
        if (categoryToRemove.equals("Unknown")) {
            System.out.println("❌ Invalid category number.");
            return;
        }

        System.out.print("Are you sure you want to remove the category '" + categoryToRemove + "' and all its items? (y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (!confirm.equals("y")) {
            System.out.println("⚠️ Removal canceled.");
            return;
        }

        try (Connection conn = DatabaseConnection.connect()) {
            if (conn == null) {
                System.out.println("❌ Failed to connect to the database.");
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
                    System.out.println("✅ Category '" + categoryToRemove + "' and all its items removed successfully!");
                } else {
                    System.out.println("❌ Failed to remove category.");
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Database error: " + e.getMessage());
        }
    }

    // Manage categories
    public static void manageCategories(Scanner scanner) {
        while (true) {
            System.out.println("\n--- Manage Categories ---");
            System.out.println("1. Add Category");
            System.out.println("2. Remove Category");
            System.out.println("3. View Categories");
            System.out.println("4. Back");

            int choice = Utils.validateIntegerInput(scanner, "Enter your choice ([b] to go back): ", 1, 4);
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
                case 4:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}