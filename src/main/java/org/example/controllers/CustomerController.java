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
            System.out.println("\n--- Customer Menu ---");
            System.out.println("1. View All Items by Category");
            System.out.println("2. Order Now");
            System.out.println("3. View Cart");
            System.out.println("4. Confirm and Pay");
            System.out.println("5. Exit");

            int choice = validateIntegerInput("Enter your choice: ", 5);
            switch (choice) {
                case 1 -> CustomerMenuViewer.viewMenuItemsCustomer();
                case 2 -> addItemToCart();
                case 3 -> viewCartWithEditOptions();
                case 4 -> confirmAndPay();
                case 5 -> {
                    System.out.println("Thank you for visiting! Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void addItemToCart() {
        while (true) {
            List<String> categories = orderService.getCategories();
            if (categories.isEmpty()) {
                System.out.println("No categories available.");
                return;
            }

            System.out.println("\n--- Select a Category ---");
            for (int i = 0; i < categories.size(); i++) {
                System.out.println((i + 1) + ". " + categories.get(i));
            }

            int categoryChoice = validateIntegerInput("Enter the category number ([b] to go back): ", categories.size());
            if (categoryChoice == -1) break;

            String selectedCategory = categories.get(categoryChoice - 1);
            orderService.displayItemsByCategory(selectedCategory);

            System.out.print("Enter the ID of the item to add to your cart ([b] to go back): ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("b")) continue;

            try {
                int itemId = Integer.parseInt(input);
                int quantity = validateIntegerInput("Enter the quantity: ", 100);

                Map<String, Object> item = orderService.getItemById(itemId);
                if (item == null) {
                    System.out.println("Invalid item ID. No item found.");
                    continue;
                }

                orderService.addItemToCart(item, quantity);
                System.out.println("Item added to cart successfully!");

                System.out.print("Do you want to add another item? (y/n): ");
                if (!scanner.nextLine().trim().equalsIgnoreCase("y")) break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid numeric value.");
            }
        }
    }

    private void viewCartWithEditOptions() {
        while (true) {
            int orderId = 123;
            orderService.viewCart();
            if (orderService.isCartEmpty()) {
                System.out.println("Your cart is empty. Nothing to edit.");
                return;
            }

            System.out.println("\n--- Cart Options ---");
            System.out.println("1. Edit Quantity of an Item");
            System.out.println("2. Remove an Item from Cart");
            System.out.println("3. Go Back");

            int cartOption = validateIntegerInput("Enter your choice: ", 3);
            switch (cartOption) {
                case 1 -> editQuantityInCart();
                case 2 -> removeItemFromCart();
                case 3 -> {
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void editQuantityInCart() {
        System.out.print("Enter the ID of the item to edit quantity ([b] to go back): ");
        String input = scanner.nextLine().trim();
        if (input.equalsIgnoreCase("b")) return;

        try {
            int itemId = Integer.parseInt(input);
            int newQuantity = validateIntegerInput("Enter the new quantity: ", 100);

            if (orderService.updateCartItemQuantity(itemId, newQuantity)) {
                System.out.println("Quantity updated successfully!");
            } else {
                System.out.println("Invalid item ID. No changes made.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid numeric value.");
        }
    }

    private void removeItemFromCart() {
        System.out.print("Enter the ID of the item to remove ([b] to go back): ");
        String input = scanner.nextLine().trim();
        if (input.equalsIgnoreCase("b")) return;

        try {
            int itemId = Integer.parseInt(input);
            if (orderService.removeItemFromCart(itemId)) {
                System.out.println("Item removed from cart successfully!");
            } else {
                System.out.println("Invalid item ID. No changes made.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid numeric value.");
        }
    }

    private void confirmAndPay() {
        orderService.viewCart();
        if (!confirmOrder()) {
            System.out.println("Order canceled.");
            return;
        }

        System.out.println("\n--- Payment Process ---");
        paymentService.processPaymentCustomer();

        int paymentMethod = 1; // QR Code is the only option
        int orderId = orderService.placeOrder(paymentMethod);

        if (orderId == -1) {
            System.out.println("Failed to place order. Please try again.");
            return;
        }

        if (orderService.processPayment(orderId, paymentMethod)) {
            orderService.generateReceipt(orderId, paymentMethod);
            System.out.println("Payment successful. Thank you for your order!");
        } else {
            System.out.println("Payment failed. Please try again.");
        }
    }

    private boolean confirmOrder() {
        System.out.print("Do you want to confirm your order? (y/n): ");
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
                    System.out.println("❌ Input out of range. Please enter a number between " + 1 + " and " + max + ".");
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid input. Please enter a valid numeric value.");
            }
        }
    }

    // test customer controller
    public static void main(String[] args) {
        CustomerController customerController = new CustomerController(new Scanner(System.in), new OrderService());
        customerController.start();
    }
}