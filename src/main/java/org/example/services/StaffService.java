package org.example.services;

import org.example.controllers.StaffController;
import org.example.utils.ColorFormatter;
import org.example.utils.ConsoleFormatter;
import org.example.views.menuViewer.CustomerMenuViewer;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.ShownBorders;
import org.nocrala.tools.texttablefmt.Table;

import java.util.*;

public class StaffService {

    private Scanner scanner;
    private OrderService orderService;
    private PaymentService paymentService;
    final String RESET = "\u001B[0m";
    final String BOLD_BLUE = "\033[1;34m"; // Blue title
    final String WHITE_BORDER = "\033[97m"; // White border

    int consoleWidth = 180; // Console width
    int tableWidth = 100; // Wider table width
    int leftPadding = (consoleWidth - tableWidth) / 2;
    String padding = " ".repeat(leftPadding);
    private boolean paymentSuccessful;

    public StaffService(Scanner scanner, OrderService orderService) {
        this.scanner = scanner;
        this.orderService = orderService;
        this.paymentService = new PaymentService();
    }

    public StaffService() {
        this.scanner = scanner;
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    private String formatText(String text, String color) {
        return (ColorFormatter.colorText(text, color));
    }

    public void start() {
        while (true) {
            // Create a table with a SINGLE wide column
            Table table = new Table(1, BorderStyle.UNICODE_BOX_DOUBLE_BORDER_WIDE, ShownBorders.ALL);
            table.setColumnWidth(0, 80, 90); // Explicitly setting column width wider

            CellStyle centerStyle = new CellStyle(CellStyle.HorizontalAlign.CENTER);
            CellStyle leftStyle = new CellStyle(CellStyle.HorizontalAlign.LEFT); // Left alignment for numbers

            // Title Row (Centered)
            table.addCell(BOLD_BLUE + "Welcome to Our Shop " + RESET, centerStyle);

            // Menu Options (Numbers Left-Aligned)
            String[] options = {
                    "1.  View All Menu",
                    "2.  Order Now",
                    "3.  View Cart",
                    "4.  Confirm and Pay",
                    "5.  Exit"
            };

            for (String option : options) {
                table.addCell(BOLD_BLUE + option.trim() + RESET, leftStyle); // Left-align & reset colors properly
            }

            // Print the Table with WHITE Borders
            String[] tableLines = table.render().split("\n");
            for (String line : tableLines) {
                System.out.println(WHITE_BORDER + line + RESET);
            }

            int choice = validateIntegerInput((ColorFormatter.colorText("👉 Enter your choice: ", ColorFormatter.GREEN + ColorFormatter.BOLD)), 0, 5, false);
            switch (choice) {
                case 1 -> CustomerMenuViewer.viewMenuItemsCustomer();
                case 2 -> addItemToCart();
                case 3 -> viewCartWithEditOptions();
                case 4 -> confirmAndPay();
                case 5 -> {
                    System.out.println((ColorFormatter.colorText("Thank you for visiting! Goodbye!", ColorFormatter.GREEN + ColorFormatter.BOLD)));
                    return;
                }
                default -> System.out.println((ColorFormatter.colorText("Invalid choice. Please try again.", ColorFormatter.RED + ColorFormatter.BOLD)));
            }
        }
    }

    private void addItemToCart() {
        while (true) {
            List<Map<String, Object>> categories = orderService.getCategories();
            if (categories.isEmpty()) {
                System.out.println((ColorFormatter.colorText("❌ No categories available.", ColorFormatter.RED + ColorFormatter.BOLD)));
                return;
            }

            // Display categories in a formatted table
            System.out.println((ColorFormatter.colorText("\n📋 --- Available Categories ---", ColorFormatter.CYAN + ColorFormatter.BOLD)));
            Table table = new Table(2, BorderStyle.UNICODE_ROUND_BOX_WIDE, ShownBorders.ALL);
            table.addCell("No.", new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell("Category Name", new CellStyle(CellStyle.HorizontalAlign.CENTER));

            for (int i = 0; i < categories.size(); i++) {
                table.addCell(String.valueOf(i + 1), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell((String) categories.get(i).get("name"), new CellStyle(CellStyle.HorizontalAlign.LEFT)); // ✅ Extract category name correctly
            }

            System.out.println(table.render());

            // Select category
            int categoryChoice = validateIntegerInput((ColorFormatter.colorText("🔢 Enter category number ([b] to go back): ", ColorFormatter.GREEN + ColorFormatter.BOLD)), 1, categories.size(), true);
            if (categoryChoice == -1) {
                System.out.println((ColorFormatter.colorText("↩ Going back to the previous menu...", ColorFormatter.YELLOW + ColorFormatter.BOLD)));
                break;
            }

            // Get selected category
            Map<String, Object> selectedCategory = categories.get(categoryChoice - 1);
            int selectedCategoryId = (int) selectedCategory.get("id"); // ✅ Get category ID
            String selectedCategoryName = (String) selectedCategory.get("name"); // ✅ Get category name

            System.out.println((ColorFormatter.colorText("📂 Displaying items for category: " + selectedCategoryName, ColorFormatter.BLUE + ColorFormatter.BOLD)));
            orderService.displayItemsByCategory(String.valueOf(selectedCategoryId), scanner); // ✅ Pass category ID as String

            // Prompt user to enter item ID
            System.out.print((ColorFormatter.colorText("🛒 Enter the ID of the item to add to cart ([b] to go back): ", ColorFormatter.GREEN + ColorFormatter.BOLD)));
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("b")) continue;

            try {
                int itemId = Integer.parseInt(input);
                int quantity = validateIntegerInput((ColorFormatter.colorText("🔢 Enter quantity: ", ColorFormatter.GREEN + ColorFormatter.BOLD)), 1, 100, false);

                Map<String, Object> item = orderService.getItemById(itemId);
                if (item == null) {
                    System.out.println((ColorFormatter.colorText("❌ Invalid item ID. No item found.", ColorFormatter.RED + ColorFormatter.BOLD)));
                    continue;
                }

                orderService.addItemToCart(item, quantity);
                System.out.println((ColorFormatter.colorText("✅ Item added to cart successfully!", ColorFormatter.GREEN + ColorFormatter.BOLD)));

                // Ask if user wants to add another item
                System.out.print((ColorFormatter.colorText("➕ Add another item? (y/n): ", ColorFormatter.GREEN + ColorFormatter.BOLD)));
                if (!scanner.nextLine().trim().equalsIgnoreCase("y")) break;

            } catch (NumberFormatException e) {
                System.out.println((ColorFormatter.colorText("❌ Invalid input. Please enter a valid number.", ColorFormatter.RED + ColorFormatter.BOLD)));
            }
        }
    }

    private void viewCartWithEditOptions() {
        while (true) {
            orderService.viewCart();
            if (orderService.isCartEmpty()) {
                System.out.println((ColorFormatter.colorText("Your cart is empty. Nothing to edit.", ColorFormatter.RED + ColorFormatter.BOLD)));
                return;
            }

            Table table = new Table(1, BorderStyle.UNICODE_BOX_DOUBLE_BORDER_WIDE, ShownBorders.ALL);
            table.setColumnWidth(0, 80, 90);

            CellStyle centerStyle = new CellStyle(CellStyle.HorizontalAlign.CENTER);
            CellStyle leftStyle = new CellStyle(CellStyle.HorizontalAlign.LEFT);
            // Title Row (Centered)
            table.addCell(BOLD_BLUE + "Cart Options" + RESET, centerStyle);

            // Menu Options (Numbers Left-Aligned)
            String[] options = {
                    "1. Edit Quantity",
                    "2. Remove an Item",
                    "3. Pay Now",
                    "4. Go Back",
            };

            for (String option : options) {
                table.addCell(BOLD_BLUE + option.trim() + RESET, leftStyle);
            }

            // Print the Table with WHITE Borders
            String[] tableLines = table.render().split("\n");
            for (String line : tableLines) {
                System.out.println(WHITE_BORDER + padding + line + RESET);
            }

            int cartOption = validateIntegerInput((ColorFormatter.colorText("👉 Enter your choice: ", ColorFormatter.GREEN + ColorFormatter.BOLD)), 1, 4, false);
            switch (cartOption) {
                case 1 -> editQuantityInCart();
                case 2 -> removeItemFromCart();
                case 3 -> confirmAndPay();
                case 4 -> {
                    return;
                }
                default -> System.out.println((ColorFormatter.colorText("Invalid choice. Please try again.", ColorFormatter.RED + ColorFormatter.BOLD)));
            }
        }
    }

    private void confirmAndPay() {
        if (orderService.isCartEmpty()) {
            System.out.println((ColorFormatter.colorText("Your cart is empty. Please add items to cart first.", ColorFormatter.RED + ColorFormatter.BOLD)));
            return;
        }

        orderService.viewCart();
        if (!confirmOrder()) {
            System.out.println((ColorFormatter.colorText("🚪 Going back to the previous menu...", ColorFormatter.YELLOW + ColorFormatter.BOLD)));
            return;
        }

        // Select payment method
        System.out.println((ColorFormatter.colorText("💳 Select Payment Method", ColorFormatter.CYAN + ColorFormatter.BOLD)));
        System.out.println((ColorFormatter.colorText("1. QR Code", ColorFormatter.CYAN)));
        System.out.println((ColorFormatter.colorText("2. Cash", ColorFormatter.CYAN)));

        int paymentMethod = validateIntegerInput((ColorFormatter.colorText("👉 Enter your choice: ", ColorFormatter.GREEN + ColorFormatter.BOLD)), 1, 2, false);
        if (paymentMethod == -1) {
            System.out.println( (ColorFormatter.colorText("🚪 Going back to the previous menu...", ColorFormatter.YELLOW + ColorFormatter.BOLD)));
            return;
        }

        int orderId = placeOrder(paymentMethod);
        if (orderId == -1) return;

        processPayment(orderId, paymentMethod);
    }

    private void editQuantityInCart() {
        System.out.print((ColorFormatter.colorText("Enter the ID of the item to edit quantity ([b] to go back): ", ColorFormatter.GREEN + ColorFormatter.BOLD)));
        String input = scanner.nextLine().trim();
        if (input.equalsIgnoreCase("b")) return;

        try {
            int itemId = Integer.parseInt(input);
            int newQuantity = validateIntegerInput((ColorFormatter.colorText("Enter the new quantity: ", ColorFormatter.GREEN + ColorFormatter.BOLD)), 1, 100, false);

            if (orderService.updateCartItemQuantity(itemId, newQuantity)) {
                System.out.println((ColorFormatter.colorText("Quantity updated successfully!", ColorFormatter.GREEN + ColorFormatter.BOLD)));
            } else {
                System.out.println((ColorFormatter.colorText("Invalid item ID. No changes made.", ColorFormatter.RED + ColorFormatter.BOLD)));
            }
        } catch (NumberFormatException e) {
            System.out.println((ColorFormatter.colorText("Invalid input. Please enter a valid numeric value.", ColorFormatter.RED + ColorFormatter.BOLD)));
        }
    }

    private void removeItemFromCart() {
        System.out.print((ColorFormatter.colorText("Enter the ID of the item to remove ([b] to go back): ", ColorFormatter.GREEN + ColorFormatter.BOLD)));
        String input = scanner.nextLine().trim();
        if (input.equalsIgnoreCase("b")) return;

        try {
            int itemId = Integer.parseInt(input);
            if (orderService.removeItemFromCart(itemId)) {
                System.out.println((ColorFormatter.colorText("Item removed from cart successfully!", ColorFormatter.GREEN + ColorFormatter.BOLD)));
            } else {
                System.out.println((ColorFormatter.colorText("Invalid item ID. No changes made.", ColorFormatter.RED + ColorFormatter.BOLD)));
            }
        } catch (NumberFormatException e) {
            System.out.println((ColorFormatter.colorText("Invalid input. Please enter a valid numeric value.", ColorFormatter.RED + ColorFormatter.BOLD)));
        }
    }

    // Places the order and returns the order ID
    private int placeOrder(int paymentMethod) {
        System.out.println("🛒 Placing Order...");
        int orderId = orderService.placeOrder(paymentMethod);

        if (orderId == -1) {
            displayMessage("❌ Order placement failed! Please try again.", ColorFormatter.RED);
        } else {
            System.out.println("✅ Order placed successfully! Order ID: " + orderId);
        }
        return orderId;
    }

    // Handles the payment process
    private void processPayment(int orderId, int paymentMethod) {
        displayMessage("🔄 Processing payment for Order ID: " + orderId, ColorFormatter.YELLOW);

        if (paymentMethod == 1) { // QR Code
            if (orderService.processPayment(orderId, paymentMethod)) {
                displayMessage("✅ Payment successful. Thank you for your order!", ColorFormatter.GREEN);
                orderService.generateReceipt(orderId, paymentMethod);
            } else {
                displayMessage("❌ Payment failed. Please try again.", ColorFormatter.RED);
            }
        } else if (paymentMethod == 2) { // Cash
            displayMessage("💵 Payment received in cash. Thank you for your order!", ColorFormatter.GREEN);
            orderService.generateReceipt(orderId, paymentMethod);
        }
    }

    // Utility method to display formatted messages
    private void displayMessage(String message, String color) {
        System.out.println((ColorFormatter.colorText(message, color + ColorFormatter.BOLD)));
    }

    // Added cart confirmation before placing the order
    private boolean confirmOrder() {
        System.out.print((ColorFormatter.colorText("🛒 Do you want to confirm your order? (y/n): ", ColorFormatter.GREEN + ColorFormatter.BOLD)));
        return scanner.nextLine().trim().equalsIgnoreCase("y");
    }

    // ✅ Validates integer input with a range, allows user to go back
    private int validateIntegerInput(String prompt, int min, int max, boolean allowBack) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (allowBack && input.equalsIgnoreCase("b")) {
                return -1; // Go back
            }

            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                } else {
                    System.out.println(formatText("❌ Input out of range. Enter a number between " + min + " and " + max + ".", ColorFormatter.RED + ColorFormatter.BOLD));
                }
            } catch (NumberFormatException e) {
                System.out.println(formatText("❌ Invalid input. Please enter a valid number.", ColorFormatter.RED + ColorFormatter.BOLD));
            }
        }
    }

    // test staff controller
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            OrderService orderService = new OrderService();
            StaffController staffController = new StaffController(scanner, orderService);
            staffController.start();
        } catch (Exception e) {
            System.err.println((ColorFormatter.colorText("❌ An error occurred: " + e.getMessage(), ColorFormatter.RED + ColorFormatter.BOLD)));
            e.printStackTrace();
            System.exit(1);
        }
    }
}