package org.example.services;

import org.example.utils.*;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.ShownBorders;
import org.nocrala.tools.texttablefmt.Table;

import java.sql.*;
import java.util.*;

public class MenuItemManager {

    private static int selectedCategoryId;

    // ------------------ Delete Menu Item ------------------
    public static void deleteMenuItem(Scanner scanner) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            // Step 1: Fetch and Display Categories
            List<Map<String, Object>> categories = CategoryManager.getCategories();
            if (categories.isEmpty()) {
                System.out.println((ColorFormatter.colorText("❌ No categories available. Please add a category first.", ColorFormatter.RED + ColorFormatter.BOLD)));
                return;
            }

            System.out.println((ColorFormatter.colorText("--- Select a Category ---", ColorFormatter.BLUE + ColorFormatter.BOLD)));
            CategoryManager.displayCategories();

            int categoryChoice = Utils.validateIntegerInput(scanner, (ColorFormatter.colorText("👉 Enter the category number ([b] to go back): ", ColorFormatter.GREEN + ColorFormatter.BOLD)), 1, categories.size());
            if (categoryChoice == -1) return;

            int selectedCategoryId = (int) categories.get(categoryChoice - 1).get("id");

            // Step 2: Display Items in the Selected Category
            System.out.println((ColorFormatter.colorText("--- Items in Category: " + selectedCategoryId + " ---", ColorFormatter.BLUE + ColorFormatter.BOLD)));
            String sql = "SELECT * FROM menuitemsadmin WHERE category_id = ? ORDER BY name";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, selectedCategoryId);
                ResultSet rs = pstmt.executeQuery();

                Table table = new Table(8, BorderStyle.UNICODE_BOX_WIDE, ShownBorders.ALL);
                table.addCell("No.", new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell("Item ID", new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell("Name", new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell("Description", new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell("Size", new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell("Base Price", new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell("Sell Price", new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell("Discount", new CellStyle(CellStyle.HorizontalAlign.CENTER));

                int count = 1;
                while (rs.next()) {
                    table.addCell(String.valueOf(count++), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                    table.addCell(String.valueOf(rs.getInt("item_id")), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                    table.addCell(rs.getString("name"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                    table.addCell(rs.getString("description"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                    table.addCell(rs.getString("size"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                    table.addCell(String.format("$%.2f", rs.getDouble("base_price")), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                    table.addCell(String.format("$%.2f", rs.getDouble("sell_price")), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                    table.addCell(String.format("$%.2f", rs.getDouble("discount")), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                }
                if (count == 1) {
                    System.out.println((ColorFormatter.colorText("❌ No items available in the selected category.", ColorFormatter.RED + ColorFormatter.BOLD)));
                    return;
                }
                System.out.println((table.render()));
            }

            // Step 3: Enter Item ID to Delete
            System.out.print((ColorFormatter.colorText("Enter the Item ID to delete: ", ColorFormatter.GREEN + ColorFormatter.BOLD)));
            int itemId;
            try {
                itemId = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println((ColorFormatter.colorText("❌ Invalid input. Please enter a valid numeric value.", ColorFormatter.RED + ColorFormatter.BOLD)));
                return;
            }

            // Step 4: Delete the Item
            String deleteSql = "DELETE FROM menuitemsadmin WHERE item_id = ?";
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, itemId);
                int rowsDeleted = deleteStmt.executeUpdate();

                if (rowsDeleted > 0) {
                    System.out.println((ColorFormatter.colorText("✅ Menu item deleted successfully!", ColorFormatter.GREEN + ColorFormatter.BOLD)));
                } else {
                    System.out.println((ColorFormatter.colorText("❌ Item ID not found.", ColorFormatter.RED + ColorFormatter.BOLD)));
                }
            }
        } catch (SQLException e) {
            System.out.println((ColorFormatter.colorText("❌ Database error: " + e.getMessage(), ColorFormatter.RED + ColorFormatter.BOLD)));
        }
    }

    private static final List<String> VALID_CATEGORIES = Arrays.asList("Appetizers", "Main Course", "Beverages", "Desserts");

    // ------------------ Add Multiple Menu Items ------------------
    public static void addMenuItem(Scanner scanner) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            // Step 1: Fetch and Display Categories
            List<Map<String, Object>> categories = CategoryManager.getCategories();
            if (categories.isEmpty()) {
                System.out.println((ColorFormatter.colorText("❌ No categories available. Please add a category first.", ColorFormatter.RED + ColorFormatter.BOLD)));
                return;
            }

            System.out.println((ColorFormatter.colorText("--- Select a Category ---", ColorFormatter.BLUE + ColorFormatter.BOLD)));
            CategoryManager.displayCategories();

            // Step 2: Validate Category Selection
            int categoryChoice = Utils.validateIntegerInput(scanner, (ColorFormatter.colorText("Enter the category number ([b] to go back): ", ColorFormatter.GREEN + ColorFormatter.BOLD)), 1, categories.size());
            if (categoryChoice == -1) return; // User chose to go back

            selectedCategoryId = (int) categories.get(categoryChoice - 1).get("id");

            // Step 3: Validate Item Name
            String name;
            while (true) {
                System.out.print((ColorFormatter.colorText("Enter item name: ", ColorFormatter.GREEN + ColorFormatter.BOLD)));
                name = scanner.nextLine().trim();
                if (name.isEmpty()) {
                    System.out.println((ColorFormatter.colorText("❌ Item name cannot be empty. Please try again.", ColorFormatter.RED + ColorFormatter.BOLD)));
                } else if (!name.matches("[a-zA-Z0-9\\s]+")) {
                    System.out.println((ColorFormatter.colorText("❌ Invalid item name. Only alphanumeric characters and spaces are allowed.", ColorFormatter.RED + ColorFormatter.BOLD)));
                } else {
                    break; // Valid name
                }
            }

            // Step 4: Optional Description
            System.out.print((ColorFormatter.colorText("Enter item description (optional): ", ColorFormatter.GREEN + ColorFormatter.BOLD)));
            String description = scanner.nextLine().trim();

            // Step 5: Validate Size
            String size;
            while (true) {
                System.out.print((ColorFormatter.colorText("Enter size [(S, M, L, XL, etc.) or press Enter to skip]: ", ColorFormatter.GREEN + ColorFormatter.BOLD)));
                size = scanner.nextLine().trim().toUpperCase();
                if (size.isEmpty()) {
                    size = null; // Allow skipping size
                    break;
                } else if (!size.matches("[A-Z]+")) {
                    System.out.println((ColorFormatter.colorText("❌ Invalid size. Only uppercase letters (e.g., S, M, L, XL) are allowed.", ColorFormatter.RED + ColorFormatter.BOLD)));
                } else {
                    break; // Valid size
                }
            }

            // Step 6: Validate Base Price
            double basePrice;
            while (true) {
                System.out.print((ColorFormatter.colorText("Enter base price: ", ColorFormatter.GREEN + ColorFormatter.BOLD)));
                try {
                    basePrice = Double.parseDouble(scanner.nextLine().trim());
                    if (basePrice <= 0) {
                        System.out.println((ColorFormatter.colorText("❌ Base price must be greater than zero.", ColorFormatter.RED + ColorFormatter.BOLD)));
                    } else {
                        break; // Valid base price
                    }
                } catch (NumberFormatException e) {
                    System.out.println((ColorFormatter.colorText("❌ Invalid input. Please enter a valid numeric value for base price.", ColorFormatter.RED + ColorFormatter.BOLD)));
                }
            }

            // Step 7: Validate Sell Price
            double sellPrice;
            while (true) {
                System.out.print((ColorFormatter.colorText("Enter sell price: ", ColorFormatter.GREEN + ColorFormatter.BOLD)));
                try {
                    sellPrice = Double.parseDouble(scanner.nextLine().trim());
                    if (sellPrice < basePrice) {
                        System.out.println((ColorFormatter.colorText("❌ Sell price must be greater than or equal to the base price.", ColorFormatter.RED + ColorFormatter.BOLD)));
                    } else {
                        break; // Valid sell price
                    }
                } catch (NumberFormatException e) {
                    System.out.println((ColorFormatter.colorText("❌ Invalid input. Please enter a valid numeric value for sell price.", ColorFormatter.RED + ColorFormatter.BOLD)));
                }
            }

            // Step 8: Validate Discount
            double discount = 0.0;
            while (true) {
                System.out.print((ColorFormatter.colorText("Enter discount (press Enter to skip): ", ColorFormatter.GREEN + ColorFormatter.BOLD)));
                String discountInput = scanner.nextLine().trim();
                if (discountInput.isEmpty()) {
                    break; // No discount
                }
                try {
                    discount = Double.parseDouble(discountInput);
                    if (discount < 0 || discount > sellPrice) {
                        System.out.println((ColorFormatter.colorText("❌ Discount must be between 0 and the sell price.", ColorFormatter.RED + ColorFormatter.BOLD)));
                    } else {
                        break; // Valid discount
                    }
                } catch (NumberFormatException e) {
                    System.out.println((ColorFormatter.colorText("❌ Invalid input. Please enter a valid numeric value for discount.", ColorFormatter.RED + ColorFormatter.BOLD)));
                }
            }

            // Step 9: Insert into Database
            String sql = "INSERT INTO menuitemsadmin (name, description, category_id, size, base_price, sell_price, discount) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setString(2, description);
                pstmt.setInt(3, selectedCategoryId); // Use category ID
                pstmt.setString(4, size); // Null if skipped
                pstmt.setDouble(5, basePrice);
                pstmt.setDouble(6, sellPrice);
                pstmt.setDouble(7, discount);
                pstmt.executeUpdate();
                System.out.println((ColorFormatter.colorText("✅ Menu item added successfully!", ColorFormatter.GREEN + ColorFormatter.BOLD)));
            }
        } catch (SQLException e) {
            System.out.println((ColorFormatter.colorText("❌ Database error: " + e.getMessage(), ColorFormatter.RED + ColorFormatter.BOLD)));
        }
    }

    public static void viewMenuItemsByCategorySeparately() {
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name FROM categories ORDER BY id")) {

            while (rs.next()) {
                int categoryId = rs.getInt("id");
                String categoryName = rs.getString("name");

                System.out.println("\n\t📂 --- " + categoryName + " ---");
                displayItemsByCategory1(categoryId);
            }
        } catch (SQLException e) {
            System.out.println((ColorFormatter.colorText("❌ Error retrieving categories: " + e.getMessage(), ColorFormatter.RED + ColorFormatter.BOLD)));
            e.printStackTrace();
        }
    }

    private static void displayItemsByCategory1(int categoryId) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "SELECT * FROM menuitemsadmin WHERE category_id = ? ORDER BY name";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, categoryId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    System.out.println("\n📂 --- Category: " + categoryId + " ---");

                    Table table = new Table(5, BorderStyle.UNICODE_BOX_WIDE, ShownBorders.ALL);
                    table.addCell("ID", new CellStyle(CellStyle.HorizontalAlign.CENTER));
                    table.addCell("Name", new CellStyle(CellStyle.HorizontalAlign.CENTER));
                    table.addCell("Category ID", new CellStyle(CellStyle.HorizontalAlign.CENTER));
                    table.addCell("Price", new CellStyle(CellStyle.HorizontalAlign.CENTER));
                    table.addCell("Discount", new CellStyle(CellStyle.HorizontalAlign.CENTER));

                    boolean hasItems = false;
                    while (rs.next()) {
                        hasItems = true;
                        table.addCell(String.valueOf(rs.getInt("item_id")), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                        table.addCell(rs.getString("name"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                        table.addCell(String.valueOf(rs.getInt("category_id")), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                        table.addCell(String.format("$%.2f", rs.getDouble("sell_price")), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                        table.addCell(String.format("$%.2f", rs.getDouble("discount")), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                    }

                    if (!hasItems) {
                        System.out.println((ColorFormatter.colorText("❌ No items found in this category.", ColorFormatter.RED + ColorFormatter.BOLD)));
                    } else {
                        System.out.println(table.render());
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println((ColorFormatter.colorText("❌ Database error: " + e.getMessage(), ColorFormatter.RED + ColorFormatter.BOLD)));
            e.printStackTrace();
        }
    }
}