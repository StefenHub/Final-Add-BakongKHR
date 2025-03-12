package org.example.controllers;

import org.example.views.menuViewer.CustomerMenuViewer;
import org.example.services.OrderService;
import org.example.services.PaymentService;
import org.example.utils.ColorFormatter;
import org.example.utils.ConsoleFormatter;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.ShownBorders;
import org.nocrala.tools.texttablefmt.Table;

import java.util.*;

public class CustomerController {

    private final Scanner scanner;
    private final OrderService orderService;
    private final PaymentService paymentService;
    final String RESET = "\u001B[0m";
    final String BOLD_BLUE = "\033[1;34m"; // Blue title
    final String WHITE_BORDER = "\033[97m"; // White border

    int consoleWidth = 180; // Console width
    int tableWidth = 100; // Wider table width
    int leftPadding = (consoleWidth - tableWidth) / 2;
    String padding = " ".repeat(leftPadding);

    public CustomerController(Scanner scanner, OrderService orderService) {
        this.scanner = scanner;
        this.orderService = orderService;
        this.paymentService = new PaymentService();
    }

    private String formatText(String text, String color) {
        return ConsoleFormatter.centerText(ColorFormatter.colorText(text, color));
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
                    "0.  Exit"
            };

            for (String option : options) {
                table.addCell(BOLD_BLUE + option.trim() + RESET, leftStyle); // Left-align & reset colors properly
            }

            // Print the Table with WHITE Borders
            String[] tableLines = table.render().split("\n");
            for (String line : tableLines) {
                System.out.println(WHITE_BORDER + padding + line + RESET);
            }

            int choice = validateIntegerInput(ConsoleFormatter.centerText(ColorFormatter.colorText("👉 Enter your choice: ", ColorFormatter.GREEN + ColorFormatter.BOLD)), 0, 5, false);
            switch (choice) {
                case 1 -> CustomerMenuViewer.viewMenuItemsCustomer();
                case 2 -> addItemToCart();
                case 3 -> viewCartWithEditOptions();
                case 4 -> confirmAndPay();
                case 0 -> {
                    System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("Thank you for visiting! Goodbye!", ColorFormatter.GREEN + ColorFormatter.BOLD)));
                    return;
                }
                default -> System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("Invalid choice. Please try again.", ColorFormatter.RED + ColorFormatter.BOLD)));
            }
        }
    }

    private void addItemToCart() {
        while (true) {
            List<Map<String, Object>> categories = orderService.getCategories();
            if (categories.isEmpty()) {
                System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("❌ No categories available.", ColorFormatter.RED + ColorFormatter.BOLD)));
                return;
            }

            // Display categories in a formatted table
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("\n📋 --- Available Categories ---", ColorFormatter.CYAN + ColorFormatter.BOLD)));
            Table table = new Table(2, BorderStyle.UNICODE_ROUND_BOX_WIDE, ShownBorders.ALL);
            table.addCell("No.", new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell("Category Name", new CellStyle(CellStyle.HorizontalAlign.CENTER)); // ✅ Fixed label

            for (int i = 0; i < categories.size(); i++) {
                table.addCell(String.valueOf(i + 1), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell((String) categories.get(i).get("name"), new CellStyle(CellStyle.HorizontalAlign.LEFT)); // ✅ Extract category name correctly
            }

            System.out.println(table.render());

            // Select category
            int categoryChoice = validateIntegerInput(ConsoleFormatter.centerText(ColorFormatter.colorText("🔢 Enter category number ([b] to go back): ", ColorFormatter.GREEN + ColorFormatter.BOLD)), 1, categories.size(), true);
            if (categoryChoice == -1) {
                System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("↩ Going back to the previous menu...", ColorFormatter.YELLOW + ColorFormatter.BOLD)));
                break;
            }

            // Get selected category
            Map<String, Object> selectedCategory = categories.get(categoryChoice - 1);
            int selectedCategoryId = (int) selectedCategory.get("id"); // ✅ Get category ID
            String selectedCategoryName = (String) selectedCategory.get("name"); // ✅ Get category name

            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("\n📂 Displaying items for category: " + selectedCategoryName, ColorFormatter.BLUE + ColorFormatter.BOLD)));
            orderService.displayItemsByCategory(String.valueOf(selectedCategoryId), scanner); // ✅ Pass category ID as String

            // Prompt user to enter item ID
            System.out.print(ConsoleFormatter.centerText(ColorFormatter.colorText("🛒 Enter the ID of the item to add to cart ([b] to go back): ", ColorFormatter.GREEN + ColorFormatter.BOLD)));
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("b")) continue;

            try {
                int itemId = Integer.parseInt(input);
                int quantity = validateIntegerInput(ConsoleFormatter.centerText(ColorFormatter.colorText("🔢 Enter quantity: ", ColorFormatter.GREEN + ColorFormatter.BOLD)), 1, 100, false);

                Map<String, Object> item = orderService.getItemById(itemId);
                if (item == null) {
                    System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("❌ Invalid item ID. No item found.", ColorFormatter.RED + ColorFormatter.BOLD)));
                    continue;
                }

                orderService.addItemToCart(item, quantity);
                System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("✅ Item added to cart successfully!", ColorFormatter.GREEN + ColorFormatter.BOLD)));

                // Ask if user wants to add another item
                System.out.print(ConsoleFormatter.centerText(ColorFormatter.colorText("➕ Add another item? (y/n): ", ColorFormatter.GREEN + ColorFormatter.BOLD)));
                if (!scanner.nextLine().trim().equalsIgnoreCase("y")) break;

            } catch (NumberFormatException e) {
                System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("❌ Invalid input. Please enter a valid number.", ColorFormatter.RED + ColorFormatter.BOLD)));
            }
        }
    }

    private void viewCartWithEditOptions() {
        while (true) {
            orderService.viewCart();
            if (orderService.isCartEmpty()) {
                System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("Your cart is empty. Nothing to edit.", ColorFormatter.RED + ColorFormatter.BOLD)));
                return;
            }

            // Create a table with a SINGLE wide column
            Table table = new Table(1, BorderStyle.UNICODE_BOX_DOUBLE_BORDER_WIDE, ShownBorders.ALL);
            table.setColumnWidth(0, 80, 90); // Explicitly setting column width wider

            CellStyle centerStyle = new CellStyle(CellStyle.HorizontalAlign.CENTER);
            CellStyle leftStyle = new CellStyle(CellStyle.HorizontalAlign.LEFT); // Left alignment for numbers

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
                table.addCell(BOLD_BLUE + option.trim() + RESET, leftStyle); // Left-align & reset colors properly
            }

            // Print the Table with WHITE Borders
            String[] tableLines = table.render().split("\n");
            for (String line : tableLines) {
                System.out.println(WHITE_BORDER + padding + line + RESET);
            }

            int cartOption = validateIntegerInput(ConsoleFormatter.centerText(ColorFormatter.colorText("👉 Enter your choice: ", ColorFormatter.GREEN + ColorFormatter.BOLD)), 1, 4, false);
            switch (cartOption) {
                case 1 -> editQuantityInCart();
                case 2 -> removeItemFromCart();
                case 3 -> confirmAndPay();
                case 4 -> {
                    return;
                }
                default -> System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("Invalid choice. Please try again.", ColorFormatter.RED + ColorFormatter.BOLD)));
            }
        }
    }

    private void editQuantityInCart() {
        System.out.print(ConsoleFormatter.centerText(ColorFormatter.colorText("Enter the ID of the item to edit quantity ([b] to go back): ", ColorFormatter.GREEN + ColorFormatter.BOLD)));
        String input = scanner.nextLine().trim();
        if (input.equalsIgnoreCase("b")) return;

        try {
            int itemId = Integer.parseInt(input);
            int newQuantity = validateIntegerInput(ConsoleFormatter.centerText(ColorFormatter.colorText("Enter the new quantity: ", ColorFormatter.GREEN + ColorFormatter.BOLD)), 1, 100, false);

            if (orderService.updateCartItemQuantity(itemId, newQuantity)) {
                System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("Quantity updated successfully!", ColorFormatter.GREEN + ColorFormatter.BOLD)));
            } else {
                System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("Invalid item ID. No changes made.", ColorFormatter.RED + ColorFormatter.BOLD)));
            }
        } catch (NumberFormatException e) {
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("Invalid input. Please enter a valid numeric value.", ColorFormatter.RED + ColorFormatter.BOLD)));
        }
    }

    private void removeItemFromCart() {
        System.out.print(ConsoleFormatter.centerText(ColorFormatter.colorText("Enter the ID of the item to remove ([b] to go back): ", ColorFormatter.GREEN + ColorFormatter.BOLD)));
        String input = scanner.nextLine().trim();
        if (input.equalsIgnoreCase("b")) return;

        try {
            int itemId = Integer.parseInt(input);
            if (orderService.removeItemFromCart(itemId)) {
                System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("Item removed from cart successfully!", ColorFormatter.GREEN + ColorFormatter.BOLD)));
            } else {
                System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("Invalid item ID. No changes made.", ColorFormatter.RED + ColorFormatter.BOLD)));
            }
        } catch (NumberFormatException e) {
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("Invalid input. Please enter a valid numeric value.", ColorFormatter.RED + ColorFormatter.BOLD)));
        }
    }

    private void confirmAndPay() {
        // Check if cart is empty before proceeding
        if (orderService.isCartEmpty()) {
            System.out.println(formatText("❌ Your cart is empty. Add items before proceeding to payment.", ColorFormatter.RED + ColorFormatter.BOLD));
            return;
        }

        orderService.viewCart();

        if (!confirmOrder()) {
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("❌ Order canceled.", ColorFormatter.RED + ColorFormatter.BOLD)));
            return;
        }

        System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("\n💳 --- Payment Process ---", ColorFormatter.CYAN + ColorFormatter.BOLD)));

        int paymentMethod = 1; // QR Code is the only option
        int orderId = orderService.placeOrder(paymentMethod);

        if (orderId == -1) {
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("❌ Failed to place order. Please try again.", ColorFormatter.RED + ColorFormatter.BOLD)));
            return;
        }

        // Process payment
        boolean paymentSuccessful = paymentService.processPaymentCustomer();

        // Ensure receipt is generated only if payment is successful
        if (paymentSuccessful && orderService.processPayment(orderId, paymentMethod)) {
            orderService.generateReceipt(orderId, paymentMethod);
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("✅ Payment successful. Thank you for your order!", ColorFormatter.GREEN + ColorFormatter.BOLD)));
        } else {
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("❌ Payment failed. Please try again.", ColorFormatter.RED + ColorFormatter.BOLD)));
        }
    }

    // ✅ Added cart confirmation before placing the order
    private boolean confirmOrder() {
        System.out.print(ConsoleFormatter.centerText(ColorFormatter.colorText("🛒 Do you want to confirm your order? (y/n): ", ColorFormatter.GREEN + ColorFormatter.BOLD)));
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

    // test customer controller
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            OrderService orderService = new OrderService();
            CustomerController customerController = new CustomerController(scanner, orderService);
            customerController.start();
        } catch (Exception e) {
            System.err.println(ConsoleFormatter.centerText(ColorFormatter.colorText("❌ An error occurred: " + e.getMessage(), ColorFormatter.RED + ColorFormatter.BOLD)));
            e.printStackTrace();
            System.exit(1); // Ensure the application exits with a non-zero status
        }

    }
}