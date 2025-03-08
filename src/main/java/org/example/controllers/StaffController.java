package org.example.controllers;

import org.example.services.OrderManager;
import org.example.services.OrderService;

import java.sql.*;

import java.text.SimpleDateFormat;
import java.util.Scanner;

public class StaffController {
    private final Scanner scanner;
    private final OrderService orderService;

    public StaffController(Scanner scanner, OrderService orderService) {
        this.scanner = scanner;
        this.orderService = orderService;
    }

    //------------------- Start the staffController interaction menu -------------------
    public void start() {
        while (true) {
            System.out.print("""
                        \u001B[34m╔═════════════════════════════════════════╗
                        ║  \u001B[36m          🏢 Staff Dashboard      \u001B[34m     ║
                        ╠═════════════════════════════════════════╣
                        ║   \u001B[33m[1]. 📋 View All Customer Orders\u001B[34m      ║ 
                        ║   \u001B[33m[2]. 🛒 Place an Order for a Customer\u001B[34m ║ 
                        ║   \u001B[31m[0]. ❌ Exit\u001B[34m                          ║ 
                        ╚═════════════════════════════════════════╝\u001B[0m
                    """);

            int choice = validateIntegerInput(scanner, "\t👉 Enter your choice: ", 1, 3);
            switch (choice) {
                case 1:
                    OrderManager.viewAllCustomerOrders();
                    break;
                case 2:
                    CustomerController customerController = new CustomerController(scanner, orderService);
                    customerController.start();
                    break;
                case 0:
                    System.out.println("\tExiting staffController menu...");
                    return;
                default:
                    System.out.println("\tInvalid choice. Please try again.");
            }
        }
    }

    // Utility method to format date properly
    private String formatDate(Date date) {
        if (date == null) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm");
        return sdf.format(date);
    }



    //------------------- Method to validate integer input within a range -------------------
    private int validateIntegerInput(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("b")) {
                return -1;
            }
            try {
                int value = Integer.parseInt(input);
                if (value < min || value > max) {
                    System.out.println("\tInput out of range. Please enter a number between " + min + " and " + max + ".");
                } else {
                    return value;
                }
            } catch (NumberFormatException e) {
                System.out.println("\tInvalid input. Please enter a valid numeric value.");
            }
        }
    }

}
