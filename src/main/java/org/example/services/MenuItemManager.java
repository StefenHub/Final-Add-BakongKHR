package org.example.services;

import org.example.utils.DatabaseConnection;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.ShownBorders;
import org.nocrala.tools.texttablefmt.Table;

import java.sql.*;
import java.util.*;

public class MenuItemManager {
    // ------------------ View All Menu Items ------------------
    public static void viewMenuItemsAdmin() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM menuitemsadmin ORDER BY category, name";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                System.out.println("\n--------- MENU ITEMS ---------");
                String currentCategory = "";
                Table table = null;

                int count = 1;
                while (rs.next()) {
                    String category = rs.getString("category");
                    if (!category.equals(currentCategory)) {
                        if (table != null) {
                            System.out.println(table.render());
                        }
                        currentCategory = category;
                        System.out.println("\n--- " + currentCategory + " ---");
                        table = new Table(8, BorderStyle.UNICODE_ROUND_BOX_WIDE, ShownBorders.ALL);
                        table.addCell("No.", new CellStyle(CellStyle.HorizontalAlign.CENTER));
                        table.addCell("Name", new CellStyle(CellStyle.HorizontalAlign.CENTER));
                        table.addCell("Description", new CellStyle(CellStyle.HorizontalAlign.CENTER));
                        table.addCell("Item ID", new CellStyle(CellStyle.HorizontalAlign.CENTER));
                        table.addCell("Size", new CellStyle(CellStyle.HorizontalAlign.CENTER));
                        table.addCell("Base Price", new CellStyle(CellStyle.HorizontalAlign.CENTER));
                        table.addCell("Sell Price", new CellStyle(CellStyle.HorizontalAlign.CENTER));
                        table.addCell("Discount", new CellStyle(CellStyle.HorizontalAlign.CENTER));
                        count = 1; // Reset count for new category
                    }
                    table.addCell(String.valueOf(count++), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                    table.addCell(rs.getString("name"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                    table.addCell(rs.getString("description"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                    table.addCell(String.valueOf(rs.getInt("item_id")), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                    table.addCell(rs.getString("size"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                    table.addCell(String.format("$%.2f", rs.getDouble("base_price")), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                    table.addCell(String.format("$%.2f", rs.getDouble("sell_price")), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                    table.addCell(String.format("$%.2f", rs.getDouble("discount")), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                }
                if (table != null) {
                    System.out.println(table.render());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ------------------ View All Menu Items ------------------
    public static void viewMenuItemsCustomer() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT item_id, name, description, category, size, sell_price, discount FROM menuitemsadmin ORDER BY category, name";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                System.out.println("\n--------- MENU ITEMS ---------");
                String currentCategory = "";
                Table table = null;

                int count = 1;
                while (rs.next()) {
                    String category = rs.getString("category");
                    if (!category.equals(currentCategory)) {
                        if (table != null) {
                            System.out.println(table.render());
                        }
                        currentCategory = category;
                        System.out.println("\n--- " + currentCategory + " ---");
                        table = new Table(7, BorderStyle.UNICODE_ROUND_BOX_WIDE, ShownBorders.ALL);
                        table.addCell("No.", new CellStyle(CellStyle.HorizontalAlign.CENTER));
                        table.addCell("Name", new CellStyle(CellStyle.HorizontalAlign.CENTER));
                        table.addCell("Description", new CellStyle(CellStyle.HorizontalAlign.CENTER));
                        table.addCell("Item ID", new CellStyle(CellStyle.HorizontalAlign.CENTER));
                        table.addCell("Size", new CellStyle(CellStyle.HorizontalAlign.CENTER));
                        table.addCell("Sell Price", new CellStyle(CellStyle.HorizontalAlign.CENTER));
                        table.addCell("Discount", new CellStyle(CellStyle.HorizontalAlign.CENTER));
                        count = 1; // Reset count for new category
                    }
                    table.addCell(String.valueOf(count++), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                    table.addCell(rs.getString("name"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                    table.addCell(rs.getString("description"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                    table.addCell(String.valueOf(rs.getInt("item_id")), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                    table.addCell(rs.getString("size"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                    table.addCell(String.format("$%.2f", rs.getDouble("sell_price")), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                    table.addCell(String.format("$%.2f", rs.getDouble("discount")), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                }
                if (table != null) {
                    System.out.println(table.render());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ------------------ Update Menu Item ------------------
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

    // ------------------ Delete Menu Item ------------------
    public static void deleteMenuItem(Scanner scanner) {
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

            // Step 3: Enter Item ID to Delete
            System.out.print("Enter the Item ID to delete: ");
            int itemId = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            // Step 4: Delete the Item
            String deleteSql = "DELETE FROM menuitemsadmin WHERE item_id = ?";
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, itemId);
                int rowsDeleted = deleteStmt.executeUpdate();

                if (rowsDeleted > 0) {
                    System.out.println("✅ Menu item deleted successfully!");
                } else {
                    System.out.println("❌ Item ID not found.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private static final List<String> VALID_CATEGORIES = Arrays.asList("Appetizers", "Main Course", "Beverages", "Desserts");

    public static String validateCategory(Scanner scanner) {
        System.out.println("\n--- Select a Category ---");
        for (int i = 0; i < VALID_CATEGORIES.size(); i++) {
            System.out.println((i + 1) + ". " + VALID_CATEGORIES.get(i));
        }

        while (true) {
            System.out.print("Enter the category number: ");
            String input = scanner.nextLine().trim();

            try {
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= VALID_CATEGORIES.size()) {
                    return VALID_CATEGORIES.get(choice - 1); // Return selected category
                } else {
                    System.out.println("❌ Invalid choice. Please select a number between 1 and " + VALID_CATEGORIES.size() + ".");
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid input. Please enter a numeric value.");
            }
        }
    }

    // ------------------ Add Multiple Menu Items ------------------
    public static void addMenuItem(Scanner scanner) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Step 1: Fetch and Display Categories
            List<String> categories = CategoryManager.getCategories();
            if (categories.isEmpty()) {
                System.out.println("❌ No categories available. Please add a category first.");
                return;
            }

            System.out.println("\n--- Select a Category ---");
            CategoryManager.displayCategories();

            // Step 2: Validate Category Selection
            int categoryChoice = Utils.validateIntegerInput(scanner, "Enter the category number ([b] to go back): ", 1, categories.size());
            if (categoryChoice == -1) return; // User chose to go back

            String category = categories.get(categoryChoice - 1);

            // Step 3: Validate Item Name
            String name;
            while (true) {
                System.out.print("Enter item name: ");
                name = scanner.nextLine().trim();
                if (name.isEmpty()) {
                    System.out.println("❌ Item name cannot be empty. Please try again.");
                } else if (!name.matches("[a-zA-Z0-9\\s]+")) {
                    System.out.println("❌ Invalid item name. Only alphanumeric characters and spaces are allowed.");
                } else {
                    break; // Valid name
                }
            }

            // Step 4: Optional Description
            System.out.print("Enter item description (optional): ");
            String description = scanner.nextLine().trim();

            // Step 5: Validate Size
            String size;
            while (true) {
                System.out.print("Enter size [(S, M, L, XL, etc.) or press Enter to skip]: ");
                size = scanner.nextLine().trim().toUpperCase();
                if (size.isEmpty()) {
                    size = null; // Allow skipping size
                    break;
                } else if (!size.matches("[A-Z]+")) {
                    System.out.println("❌ Invalid size. Only uppercase letters (e.g., S, M, L, XL) are allowed.");
                } else {
                    break; // Valid size
                }
            }

            // Step 6: Validate Base Price
            double basePrice;
            while (true) {
                System.out.print("Enter base price: ");
                String basePriceInput = scanner.nextLine().trim();
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

            // Step 7: Validate Sell Price
            double sellPrice;
            while (true) {
                System.out.print("Enter sell price: ");
                String sellPriceInput = scanner.nextLine().trim();
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

            // Step 8: Validate Discount
            double discount = 0.0;
            while (true) {
                System.out.print("Enter discount (press Enter to skip): ");
                String discountInput = scanner.nextLine().trim();
                if (discountInput.isEmpty()) {
                    break; // No discount
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

            // Step 9: Insert into Database
            String sql = "INSERT INTO menuitemsadmin (name, description, category, size, base_price, sell_price, discount) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setString(2, description);
                pstmt.setString(3, category);
                pstmt.setString(4, size); // Null if skipped
                pstmt.setDouble(5, basePrice);
                pstmt.setDouble(6, sellPrice);
                pstmt.setDouble(7, discount);
                pstmt.executeUpdate();
                System.out.println("✅ Menu item added successfully!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void viewMenuItemsByCategorySeparately() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT DISTINCT category FROM menuitemsadmin ORDER BY category")) {

            while (rs.next()) {
                String category = rs.getString("category");
                System.out.println("\n--- " + category + " ---");
                displayItemsByCategory1(category);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void displayItemsByCategory(String category) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM menuitemsadmin WHERE category = ? ORDER BY name";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, category);
                try (ResultSet rs = pstmt.executeQuery()) {
                    System.out.println("\n--- " + category + " ---");
                    Table table = new Table(5, BorderStyle.DEMO, ShownBorders.ALL);
                    table.addCell("ID", new CellStyle(CellStyle.HorizontalAlign.CENTER));
                    table.addCell("Name", new CellStyle(CellStyle.HorizontalAlign.CENTER));
                    table.addCell("Category", new CellStyle(CellStyle.HorizontalAlign.CENTER));
                    table.addCell("Price", new CellStyle(CellStyle.HorizontalAlign.CENTER));
                    table.addCell("Discount", new CellStyle(CellStyle.HorizontalAlign.CENTER));

                    while (rs.next()) {
                        table.addCell(String.valueOf(rs.getInt("item_id")), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                        table.addCell(rs.getString("name"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                        table.addCell(rs.getString("category"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                        table.addCell(String.format("$%.2f", rs.getDouble("sell_price")), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                        table.addCell(String.format("$%.2f", rs.getDouble("discount")), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                    }
                    System.out.println(table.render());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static List<Map<String, Object>> getAllMenuItems() {
        List<Map<String, Object>> menuItems = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM menuitemsadmin";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("item_id", rs.getInt("item_id"));
                    item.put("name", rs.getString("name"));
                    item.put("description", rs.getString("description"));
                    item.put("category", rs.getString("category"));
                    item.put("size", rs.getString("size"));
                    item.put("base_price", rs.getDouble("base_price"));
                    item.put("sell_price", rs.getDouble("sell_price"));
                    item.put("discount", rs.getDouble("discount"));
                    menuItems.add(item);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return menuItems;
    }

    private static String getCategoryFromNumber(int categoryNumber) {
        switch (categoryNumber) {
            case 1:
                return "Appetizers";
            case 2:
                return "Main Course";
            case 3:
                return "Beverages";
            case 4:
                return "Desserts";
            default:
                return "Unknown";
        }
    }

    public static void displayItemsByCategory1(String category) {
        Scanner scanner = new Scanner(System.in);
        OrderService orderService = new OrderService();

        List<String> categories = orderService.getCategories();
        if (categories.isEmpty()) {
            System.out.println("No categories available.");
            return;
        }

        System.out.println("\n--- Select a Category ---");
        for (int i = 0; i < categories.size(); i++) {
            System.out.println((i + 1) + ". " + categories.get(i));
        }

        int categoryChoice = Utils.validateIntegerInput(scanner, "Enter the category number ([b] to go back): ", 1, categories.size());
        if (categoryChoice == -1) return;

        String selectedCategory = categories.get(categoryChoice - 1);
        orderService.displayItemsByCategory(selectedCategory);
    }

}
