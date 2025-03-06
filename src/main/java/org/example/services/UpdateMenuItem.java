package org.example.services;

import org.example.utils.DatabaseConnection;
import org.example.utils.Utils;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.ShownBorders;
import org.nocrala.tools.texttablefmt.Table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class UpdateMenuItem {
    public static void updateMenuItem(Scanner scanner) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Step 1: Fetch and Display Categories
            List<String> categories = CategoryManager.getCategories();
            if (categories.isEmpty()) {
                System.out.println("❌ No categories available. Please add a category first.");
                return;
            }

            System.out.println("\n--- Select a Category ---");
            CategoryManager.displayCategories();

            int categoryChoice = Utils.validateIntegerInput(scanner, "Enter the category number ([b] to go back): ", 1, categories.size());
            if (categoryChoice == -1) return;

            String selectedCategory = categories.get(categoryChoice - 1);

            // Step 2: Display Items in the Selected Category
            System.out.println("\n--- Items in Category: " + selectedCategory + " ---");
            String sql = "SELECT * FROM menuitemsadmin WHERE category = ? ORDER BY name";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, selectedCategory);
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
                    System.out.println("❌ No items available in the selected category.");
                    return;
                }
                System.out.println(table.render());
            }

            // Step 3: Enter Item ID to Update
            System.out.print("Enter the Item ID to update: ");
            int itemId = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            // Step 4: Fetch Existing Item Details
            String fetchSql = "SELECT * FROM menuitemsadmin WHERE item_id = ?";
            try (PreparedStatement fetchStmt = conn.prepareStatement(fetchSql)) {
                fetchStmt.setInt(1, itemId);
                ResultSet rs = fetchStmt.executeQuery();

                if (!rs.next()) {
                    System.out.println("❌ Item ID not found.");
                    return;
                }

                // Existing values
                String currentName = rs.getString("name");
                String currentDescription = rs.getString("description");
                Double currentBasePrice = rs.getDouble("base_price");
                Double currentSellPrice = rs.getDouble("sell_price");
                Double currentDiscount = rs.getDouble("discount");
                String currentSize = rs.getString("size");

                // Step 5: Update Item Details
                System.out.println("\n--- Updating Menu Item ---");

                // Validate Item Name
                String name;
                while (true) {
                    System.out.print("Enter new name (press Enter to skip): ");
                    name = scanner.nextLine().trim();
                    if (name.isEmpty()) {
                        name = currentName; // Keep existing name if skipped
                        break;
                    } else if (!name.matches("[a-zA-Z0-9\\s]+")) {
                        System.out.println("❌ Invalid item name. Only alphanumeric characters and spaces are allowed.");
                    } else {
                        break; // Valid name
                    }
                }

                // Optional Description
                System.out.print("Enter new description (press Enter to skip): ");
                String description = scanner.nextLine().trim();
                if (description.isEmpty()) {
                    description = currentDescription; // Keep existing description if skipped
                }

                // Validate Size
                String size;
                while (true) {
                    System.out.print("Enter new size [(S, M, L, XL, etc.) or press Enter to skip]: ");
                    size = scanner.nextLine().trim().toUpperCase();
                    if (size.isEmpty()) {
                        size = currentSize; // Keep existing size if skipped
                        break;
                    } else if (!size.matches("[A-Z]+")) {
                        System.out.println("❌ Invalid size. Only uppercase letters (e.g., S, M, L, XL) are allowed.");
                    } else {
                        break; // Valid size
                    }
                }

                // Validate Base Price
                double basePrice = currentBasePrice;
                while (true) {
                    System.out.print("Enter new base price (press Enter to skip): ");
                    String basePriceInput = scanner.nextLine().trim();
                    if (basePriceInput.isEmpty()) {
                        break; // Keep existing base price if skipped
                    }
                    try {
                        basePrice = Double.parseDouble(basePriceInput);
                        if (basePrice <= 0) {
                            System.out.println("❌ Base price must be greater than zero.");
                        } else {
                            break; // Valid base price
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("❌ Invalid input. Please enter a valid numeric value for base price.");
                    }
                }

                // Validate Sell Price
                double sellPrice = currentSellPrice;
                while (true) {
                    System.out.print("Enter new sell price (press Enter to skip): ");
                    String sellPriceInput = scanner.nextLine().trim();
                    if (sellPriceInput.isEmpty()) {
                        break; // Keep existing sell price if skipped
                    }
                    try {
                        sellPrice = Double.parseDouble(sellPriceInput);
                        if (sellPrice < basePrice) {
                            System.out.println("❌ Sell price must be greater than or equal to the base price.");
                        } else {
                            break; // Valid sell price
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("❌ Invalid input. Please enter a valid numeric value for sell price.");
                    }
                }

                // Validate Discount
                double discount = currentDiscount;
                while (true) {
                    System.out.print("Enter new discount (press Enter to skip): ");
                    String discountInput = scanner.nextLine().trim();
                    if (discountInput.isEmpty()) {
                        break; // Keep existing discount if skipped
                    }
                    try {
                        discount = Double.parseDouble(discountInput);
                        if (discount < 0 || discount > sellPrice) {
                            System.out.println("❌ Discount must be between 0 and the sell price.");
                        } else {
                            break; // Valid discount
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("❌ Invalid input. Please enter a valid numeric value for discount.");
                    }
                }

                // Step 6: Update the Database
                String updateSql = "UPDATE menuitemsadmin SET " +
                        "name = ?, " +
                        "description = ?, " +
                        "size = ?, " +
                        "base_price = ?, " +
                        "sell_price = ?, " +
                        "discount = ? " +
                        "WHERE item_id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setString(1, name);
                    updateStmt.setString(2, description);
                    updateStmt.setString(3, size); // Null if skipped
                    updateStmt.setDouble(4, basePrice);
                    updateStmt.setDouble(5, sellPrice);
                    updateStmt.setDouble(6, discount);
                    updateStmt.setInt(7, itemId);
                    updateStmt.executeUpdate();
                    System.out.println("✅ Menu item updated successfully!");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}