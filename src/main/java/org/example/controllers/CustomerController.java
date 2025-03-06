package org.example.controllers;
import org.example.views.MenuViewer.CustomerMenuViewer;
import org.example.services.OrderService;
import org.example.services.PaymentService;

import java.util.*;

public class CustomerController {
    private Scanner scanner = new Scanner(System.in);
    private OrderService orderService = new OrderService();
    private PaymentService paymentService = new PaymentService();

    public CustomerController(Scanner scanner, OrderService orderService) {
        this.scanner = scanner;
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    public void start() {
        while (true) {
            System.out.print("""
                        \u001B[34m╔═════════════════════════════════════════╗
                        ║  \u001B[36m         Welcome to Our Shop     \u001B[34m      ║
                        ╠═════════════════════════════════════════╣
                        ║   \u001B[33m[1]. 📂 View All Items by Category\u001B[34m    ║ 
                        ║   \u001B[33m[2]. 🛒 Order Now\u001B[34m                     ║ 
                        ║   \u001B[33m[3]. 🛍️ View Cart\u001B[34m                     ║ 
                        ║   \u001B[33m[4]. 💳 Confirm and Pay\u001B[34m               ║ 
                        ║   \u001B[31m[5]. ❌ Exit\u001B[34m                          ║ 
                        ╚═════════════════════════════════════════╝ \u001B[0m
                    """);

            int choice = validateIntegerInput("\t👉 Enter your choice: ", 5);
            switch (choice) {
                case 1 -> CustomerMenuViewer.viewMenuItemsCustomer();
                case 2 -> addItemToCart();
                case 3 -> viewCartWithEditOptions();
                case 4 -> confirmAndPay();
                case 5 -> {
                    System.out.println("\tThank you for visiting! Goodbye!");
                    return;
                }
                default -> System.out.println("\tInvalid choice. Please try again.");
            }
        }
    }

    private void addItemToCart() {
        while (true) {
            List<String> categories = orderService.getCategories();
            if (categories.isEmpty()) {
                System.out.println("\tNo categories available.");
                return;
            }

            System.out.println("\n\t--- Select a Category ---");
            for (int i = 0; i < categories.size(); i++) {
                System.out.println((i + 1) + ". " + categories.get(i));
            }

            int categoryChoice = validateIntegerInput("\tEnter the category number ([b] to go back): ", categories.size());
            if (categoryChoice == -1) break;

            String selectedCategory = categories.get(categoryChoice - 1);
            orderService.displayItemsByCategory(selectedCategory);

            System.out.print("\tEnter the ID of the item to add to your cart ([b] to go back): ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("b")) continue;

            try {
                int itemId = Integer.parseInt(input);
                int quantity = validateIntegerInput("\tEnter the quantity: ", 100);

                Map<String, Object> item = orderService.getItemById(itemId);
                if (item == null) {
                    System.out.println("\tInvalid item ID. No item found.");
                    continue;
                }

                orderService.addItemToCart(item, quantity);
                System.out.println("\tItem added to cart successfully!");

                System.out.print("\tDo you want to add another item? (y/n): ");
                if (!scanner.nextLine().trim().equalsIgnoreCase("y")) break;
            } catch (NumberFormatException e) {
                System.out.println("\tInvalid input. Please enter a valid numeric value.");
            }
        }
    }

    private void viewCartWithEditOptions() {
        while (true) {
            int orderId = 123;
            orderService.viewCart();
            if (orderService.isCartEmpty()) {
                System.out.println("\t\tYour cart is empty. Nothing to edit.");
                return;
            }

            System.out.print("""
                                \u001B[34m
                                ╔════════════════════════════════════╗
                                ║             Cart Options            ║
                                ╠════════════════════════════════════╣
                                ║   \u001B[33m[1]. ✏️ Edit Quantity of an Item\u001B[34m ║
                                ║   \u001B[33m[2]. 🗑️ Remove an Item from Cart\u001B[34m ║
                                ║   \u001B[31m[3]. 🔙 Go Back\u001B[34m                  ║
                                ╚════════════════════════════════════╝\u001B[0m
                            """);
            int cartOption = validateIntegerInput("\t👉 Enter your choice: ", 3);
            switch (cartOption) {
                case 1 -> editQuantityInCart();
                case 2 -> removeItemFromCart();
                case 3 -> {
                    return;
                }
                default -> System.out.println("\tInvalid choice. Please try again.");
            }
        }
    }

    private void editQuantityInCart() {
        System.out.print("\tEnter the ID of the item to edit quantity ([b] to go back): ");
        String input = scanner.nextLine().trim();
        if (input.equalsIgnoreCase("b")) return;

        try {
            int itemId = Integer.parseInt(input);
            int newQuantity = validateIntegerInput("\tEnter the new quantity: ", 100);

            if (orderService.updateCartItemQuantity(itemId, newQuantity)) {
                System.out.println("\tQuantity updated successfully!");
            } else {
                System.out.println("\tInvalid item ID. No changes made.");
            }
        } catch (NumberFormatException e) {
            System.out.println("\tInvalid input. Please enter a valid numeric value.");
        }
    }

    private void removeItemFromCart() {
        System.out.print("\tEnter the ID of the item to remove ([b] to go back): ");
        String input = scanner.nextLine().trim();
        if (input.equalsIgnoreCase("b")) return;

        try {
            int itemId = Integer.parseInt(input);
            if (orderService.removeItemFromCart(itemId)) {
                System.out.println("\tItem removed from cart successfully!");
            } else {
                System.out.println("\tInvalid item ID. No changes made.");
            }
        } catch (NumberFormatException e) {
            System.out.println("\tInvalid input. Please enter a valid numeric value.");
        }
    }

    private void confirmAndPay() {
        orderService.viewCart();
        if (!confirmOrder()) {
            System.out.println("\tOrder canceled.");
            return;
        }

        System.out.println("\t\n--- Payment Process ---");
        paymentService.processPaymentCustomer();

        int paymentMethod = 1; // QR Code is the only option
        int orderId = orderService.placeOrder(paymentMethod);

        if (orderId == -1) {
            System.out.println("\t\tFailed to place order. Please try again.");
            return;
        }

        if (orderService.processPayment(orderId, paymentMethod)) {
            orderService.generateReceipt(orderId, paymentMethod);
            System.out.println("\tPayment successful. Thank you for your order!");
        } else {
            System.out.println("\tPayment failed. Please try again.");
        }
    }

    private boolean confirmOrder() {
        System.out.print("\tDo you want to confirm your order? (y/n): ");
        return scanner.nextLine().trim().equalsIgnoreCase("y");
    }

    private int validateIntegerInput(String prompt, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("b")) {
                return -1; // Go back
            }

            try {
                int value = Integer.parseInt(input);
                if (value >= 1 && value <= max) {
                    return value;
                } else {
                    System.out.println("\t❌ Input out of range. Please enter a number between " + 1 + " and " + max + ".");
                }
            } catch (NumberFormatException e) {
                System.out.println("\t❌ Invalid input. Please enter a valid numeric value.");
            }
        }
    }

    // test customer controller
    public static void main(String[] args) {
        CustomerController customerController = new CustomerController(new Scanner(System.in), new OrderService());
        customerController.start();
    }
}