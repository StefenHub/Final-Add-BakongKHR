package org.example.services;

import org.example.utils.ColorFormatter;
import org.example.utils.ConsoleFormatter;
import org.example.utils.DatabaseConnection;
import org.example.utils.PaginationFormatter;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.ShownBorders;
import org.nocrala.tools.texttablefmt.Table;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class OrderService {
    private final List<Map<String, Object>> cart;

    public OrderService() {
        this.cart = new ArrayList<>();
    }

    // Fetch all menu items from the database
    public List<Map<String, Object>> getMenuItems() {
        List<Map<String, Object>> menuItems = new ArrayList<>();
        String sql = "SELECT * FROM menuitemsadmin ORDER BY category_id, name";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Map<String, Object> item = new HashMap<>();
                item.put("item_id", rs.getInt("item_id"));
                item.put("name", rs.getString("name"));
                item.put("description", rs.getString("description"));
                item.put("category", rs.getInt("category_id"));
                item.put("size", rs.getString("size"));
                item.put("base_price", rs.getDouble("base_price"));
                item.put("sell_price", rs.getDouble("sell_price"));
                item.put("discount", rs.getDouble("discount"));
                menuItems.add(item);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error fetching menu items: " + e.getMessage());
            e.printStackTrace();
        }

        return menuItems;
    }

    // Display menu items in a table format with pagination
    public void displayMenuItems(List<Map<String, Object>> items, Scanner scanner) {
        if (items.isEmpty()) {
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("No items available.", ColorFormatter.RED + ColorFormatter.BOLD)));
            return;
        }

        int itemsPerPage = 5;
        PaginationFormatter paginator = new PaginationFormatter(items.size(), itemsPerPage);

        try (Connection conn = DatabaseConnection.getConnection()) {
            while (true) {
                displayPage(items, paginator.getCurrentPage(), paginator.getItemsPerPage(), paginator.getTotalPages());
                boolean shouldContinue = paginator.handlePagination();
                if (!shouldContinue) break; // Exit the loop if the user chooses to exit
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void displayPage(List<Map<String, Object>> items, int currentPage, int itemsPerPage, int totalPages) {
        int start = (currentPage - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, items.size());

        Table table = new Table(7, BorderStyle.UNICODE_BOX_WIDE, ShownBorders.ALL);
        table.addCell("ID", new CellStyle(CellStyle.HorizontalAlign.CENTER));
        table.addCell("Name", new CellStyle(CellStyle.HorizontalAlign.CENTER));
        table.addCell("Description", new CellStyle(CellStyle.HorizontalAlign.CENTER));
        table.addCell("Category", new CellStyle(CellStyle.HorizontalAlign.CENTER));
        table.addCell("Size", new CellStyle(CellStyle.HorizontalAlign.CENTER));
        table.addCell("Sell Price", new CellStyle(CellStyle.HorizontalAlign.CENTER));
        table.addCell("Discount", new CellStyle(CellStyle.HorizontalAlign.CENTER));

        for (int i = start; i < end; i++) {
            Map<String, Object> item = items.get(i);
            table.addCell(String.valueOf(item.get("item_id")), new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell((String) item.get("name"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell((String) item.get("description"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell(String.valueOf(item.get("category")), new CellStyle(CellStyle.HorizontalAlign.CENTER)); // Fix: Corrected category_id
            table.addCell((String) item.get("size"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell(String.format("$%.2f", item.get("sell_price")), new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell(String.format("$%.2f", item.get("discount")), new CellStyle(CellStyle.HorizontalAlign.CENTER));
        }

        System.out.println(table.render());
        System.out.printf("\tPage %d of %d%n", currentPage, totalPages);
    }

    // Get list of categories
    public List<Map<String, Object>> getCategories() {
        List<Map<String, Object>> categories = new ArrayList<>();
        String sql = "SELECT id, name FROM categories ORDER BY id"; // ✅ Fetch category names

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Map<String, Object> category = new HashMap<>();
                category.put("id", rs.getInt("id"));
                category.put("name", rs.getString("name"));
                categories.add(category);
            }
        } catch (SQLException e) {
            System.out.println("❌ Error retrieving categories: " + e.getMessage());
        }

        return categories;
    }

    // Display items by category
    public void displayItemsByCategory(String selectedCategory, Scanner scanner) {
        List<Map<String, Object>> items = getMenuItems();
        List<Map<String, Object>> itemsByCategory = new ArrayList<>();

        for (Map<String, Object> item : items) {
            if (String.valueOf(item.get("category")).equals(selectedCategory)) { // Fix: Type conversion
                itemsByCategory.add(item);
            }
        }
        displayMenuItems(itemsByCategory, scanner);
    }

    // Generate a receipt
    public void generateReceipt(int orderId, int paymentMethod) {
        String query = "SELECT order_id, total_price, order_date FROM orders WHERE order_id = ?"; // Fix: Changed total_amount to total_price

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("\n\t--- Receipt ---");
                    System.out.println("\tOrder ID: " + rs.getInt("order_id"));
                    System.out.println("\tTotal Amount: $" + rs.getDouble("total_price"));
                    System.out.println("\tOrder Date: " + rs.getTimestamp("order_date"));
                    System.out.println("\tPayment Method: " + (paymentMethod == 1 ? "Credit Card" : paymentMethod == 2 ? "PayPal" : "Cash"));
                } else {
                    System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("❌ Order ID not found.", ColorFormatter.RED + ColorFormatter.BOLD)));
                }
            }
        } catch (SQLException e) {
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("❌ Database error: " + e.getMessage(), ColorFormatter.RED + ColorFormatter.BOLD)));
            e.printStackTrace();
        }
    }

    // Remove an item from the cart
    public boolean removeItemFromCart(int itemId) {
        boolean removed = cart.removeIf(item -> (int) item.get("item_id") == itemId); // Optimized with lambda
        System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText(removed ? "✅ Item removed successfully!" : "❌ Item not found.", removed ? ColorFormatter.GREEN : ColorFormatter.RED)));
        return removed;
    }

    public Map<String, Object> getItemById(int itemId) {
        List<Map<String, Object>> items = getMenuItems();
        for (Map<String, Object> item : items) {
            if ((int) item.get("item_id") == itemId) {
                return item;
            }
        }
        return null;
    }

    // Add an item to the cart
    public void addItemToCart(Map<String, Object> item, int quantity) {
        if (item == null) {
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("❌ Error: Attempted to add a null item to the cart.", ColorFormatter.RED + ColorFormatter.BOLD)));
            return;
        }

        if (!item.containsKey("base_price")) {
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("⚠️ Warning: base_price is missing for item: " + item, ColorFormatter.YELLOW + ColorFormatter.BOLD)));
        }

        Map<String, Object> cartItem = new HashMap<>(item);
        cartItem.put("quantity", quantity);
        cart.add(cartItem);
    }

    public void viewCart() {
        if (cart.isEmpty()) {
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("❌ Your cart is empty.", ColorFormatter.RED + ColorFormatter.BOLD)));
            return;
        }

        double grandTotal = 0;

        Timestamp order_date = Timestamp.valueOf(LocalDateTime.now()); // Example order date, replace with actual value
        System.out.println("📜 RECEIPT ");
        String order_id = "ORD" + order_date.getTime();
        System.out.println("Order ID: " + order_id);
        System.out.println("Order Date: " + order_date);
        System.out.println("-------------------------------------------------------------");

        // Table Header
        System.out.printf("\t%-5s %-20s %6s %10s %10s%n",
                "ID", "Description", "Qty", "Disc ($)", "Total ($)");
        System.out.println("-------------------------------------------------------------");

        for (Map<String, Object> cartItem : cart) {
            int itemId = (int) cartItem.get("item_id");
            String name = (String) cartItem.get("name");
            int quantity = (int) cartItem.get("quantity");
            double sellPrice = (double) cartItem.get("sell_price");
            double discount = (double) cartItem.get("discount");
            double unitPrice = sellPrice - discount;
            double totalPrice = unitPrice * quantity;
            grandTotal += totalPrice;

            // Truncate long names to fit within 20 characters
            int nameMaxLength = 20;
            String formattedName = name.length() > nameMaxLength
                    ? name.substring(0, nameMaxLength - 3) + "..."
                    : name;

            // Print the item row
            System.out.printf("\t%-5d %-20s %6d %10.2f %10.2f%n",
                    itemId, formattedName, quantity, discount, totalPrice);
        }

        // Print Footer
        System.out.println("─────────────────────────────────────────────────────");
        System.out.printf("%-5s %-20s %6s %10s %10.2f%n", "", "", "", "Total ($)", grandTotal);
        System.out.println("═════════════════════════════════════════════════════");
        System.out.println("🎉 Thank you for shopping with us! 🎉");
    }


    // Check if the cart is empty
    public boolean isCartEmpty() {
        return cart.isEmpty();
    }

    // Process payment using the chosen payment method
    public boolean processPayment(int orderId, int paymentMethod) {
        try {
            double grandTotal = calculateTotalAmount();  // ✅ Get total order amount
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("🔄 Redirecting to QR Payment...", ColorFormatter.YELLOW + ColorFormatter.BOLD)));
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("Payment confirmation pending...", ColorFormatter.GREEN + ColorFormatter.BOLD)));

            // ✅ Pass grandTotal to QR Code Payment
            PaymentService paymentService = new PaymentService();
            String md5 = paymentService.QRCodePayment(grandTotal);

            Thread.sleep(2000);

            clearCart();  // ✅ Clear the cart after successful payment
            return true;
        } catch (Exception e) {
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("❌ Payment failed: " + e.getMessage(), ColorFormatter.RED + ColorFormatter.BOLD)));
            e.printStackTrace();
            return false;
        }
    }


    public double calculateTotalAmount() {
        double totalAmount = 0;
        for (Map<String, Object> item : cart) {
            double sellPrice = item.get("sell_price") != null ? (double) item.get("sell_price") : 0;
            double discount = item.get("discount") != null ? (double) item.get("discount") : 0;
            int quantity = item.get("quantity") != null ? (int) item.get("quantity") : 0;
            totalAmount += (sellPrice - discount) * quantity;
        }
        return totalAmount;
    }

    private void clearCart() {
        cart.clear();
        System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("✅ Cart cleared successfully!", ColorFormatter.GREEN + ColorFormatter.BOLD)));
    }

    // Update the quantity of an item in the cart
    public boolean updateCartItemQuantity(int itemId, int newQuantity) {
        for (Map<String, Object> item : cart) {
            if ((int) item.get("item_id") == itemId) {
                item.put("quantity", newQuantity);
                System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("✅ Cart item updated successfully!", ColorFormatter.RED + ColorFormatter.BOLD)));
                return true;
            }
        }
        System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("❌ Item ID not found in the cart.", ColorFormatter.RED + ColorFormatter.BOLD)));
        return false;
    }

    public int placeOrder(int paymentMethod) {
        if (cart.isEmpty()) {
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("❌ Your cart is empty. Please add items before placing an order.", ColorFormatter.RED + ColorFormatter.BOLD)));
            return -1;
        }

        double totalAmount = calculateTotalAmount();
        String paymentMethodString = "QR Code";
        Timestamp orderTimestamp = Timestamp.valueOf(LocalDateTime.now());

        String orderQuery = "INSERT INTO orders (payment_method, total_price, order_date) VALUES (?, ?, ?)";
        String orderItemQuery = "INSERT INTO order_items (order_id, item_id, name, description, size, quantity, sell_price, discount, total_price) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement orderStmt = conn.prepareStatement(orderQuery, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement orderItemStmt = conn.prepareStatement(orderItemQuery)) {

                // Insert order
                orderStmt.setString(1, paymentMethodString);
                orderStmt.setDouble(2, totalAmount);
                orderStmt.setTimestamp(3, orderTimestamp);
                orderStmt.executeUpdate();

                // Get generated order ID
                try (ResultSet generatedKeys = orderStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int orderId = generatedKeys.getInt(1);


                        for (Map<String, Object> item : cart) {
                            if (item.get("sell_price") == null || item.get("discount") == null) {
                                System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("❌ Missing price data for item: " + item, ColorFormatter.RED + ColorFormatter.BOLD)));
                                continue;
                            }

                            int itemId = ((Number) item.get("item_id")).intValue();
                            int quantity = item.get("quantity") != null ? ((Number) item.get("quantity")).intValue() : 1; // Default to 1 if null
                            double sellPrice = ((Number) item.get("sell_price")).doubleValue();
                            double discount = ((Number) item.get("discount")).doubleValue();
                            double totalPrice = (sellPrice - discount) * quantity;

                            orderItemStmt.setInt(1, orderId);
                            orderItemStmt.setInt(2, itemId);
                            orderItemStmt.setString(3, (String) item.get("name"));
                            orderItemStmt.setString(4, (String) item.get("description"));
                            orderItemStmt.setString(5, (String) item.get("size"));
                            orderItemStmt.setInt(6, quantity);
                            orderItemStmt.setDouble(7, sellPrice);
                            orderItemStmt.setDouble(8, discount);
                            orderItemStmt.setDouble(9, totalPrice);

                            orderItemStmt.addBatch(); // Add to batch
                        }
                        orderItemStmt.executeBatch(); // Execute batch

                        conn.commit(); // Commit transaction
                        return orderId;
                    } else {
                        throw new SQLException("❌ Failed to retrieve order ID.");
                    }
                }
            } catch (SQLException e) {
                conn.rollback();
                System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("❌ Database error occurred: " + e.getMessage(), ColorFormatter.RED + ColorFormatter.BOLD)));
                e.printStackTrace();
                return -2;
            }
        } catch (SQLException e) {
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("❌ Database error occurred: " + e.getMessage(), ColorFormatter.RED + ColorFormatter.BOLD)));
            e.printStackTrace();
            return -2;
        } catch (Exception e) {
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("❌ Unexpected error: " + e.getMessage(), ColorFormatter.RED + ColorFormatter.BOLD)));
            e.printStackTrace();
            return -3;
        }
    }
}