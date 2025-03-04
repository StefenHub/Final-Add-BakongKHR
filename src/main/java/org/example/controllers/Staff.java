package org.example.controllers;

import org.example.utils.DatabaseConnection;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.ShownBorders;
import org.nocrala.tools.texttablefmt.Table;
import org.example.services.OrderService;

import java.sql.*;
import java.util.*;

import java.util.Scanner;

public class Staff {
    private final Scanner scanner;
    private final OrderService orderService;

    public Staff(Scanner scanner, OrderService orderService) {
        this.scanner = scanner;
        this.orderService = orderService;
    }

    //------------------- Start the staff interaction menu -------------------
    public void start() {
        while (true) {
            System.out.println("\n--- Staff Menu ---");
            System.out.println("1. View All Customer Orders");
            System.out.println("2. Place an Order for a Customer");
            System.out.println("3. Exit");

            int choice = validateIntegerInput(scanner, "Enter your choice: ", 1, 3);
            switch (choice) {
                case 1:
                    viewAllCustomerOrders();
                    break;
                case 2:
                    // Use the existing OrderService instance to create a CustomerController
                    CustomerController customerController = new CustomerController(scanner, orderService);
                    customerController.start();
                    break;
                case 3:
                    System.out.println("Exiting staff menu. Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    //------------------- View all customer orders -------------------
    public void viewAllCustomerOrders() {
        String query = "SELECT order_id, payment_method, total_amount, order_date FROM orders";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            System.out.println("--- Customer Orders ---");
            while (rs.next()) {
                int orderId = rs.getInt("order_id");
                String paymentMethod = rs.getString("payment_method");
                double totalAmount = rs.getDouble("total_amount");
                Timestamp orderDate = rs.getTimestamp("order_date");

                System.out.println("Order ID: " + orderId);
                System.out.println("Payment Method: " + paymentMethod);
                System.out.println("Total Amount: " + totalAmount);
                System.out.println("Order Date: " + orderDate);
                System.out.println("-----------------------");
            }
        } catch (SQLException e) {
            System.out.println("❌ Error retrieving customer orders: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //------------------- Method to validate integer input within a range -------------------
    private int validateIntegerInput(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("[b]")) {
                return -1; // Return -1 to indicate the user wants to go back
            }

            try {
                int value = Integer.parseInt(input);
                if (value < min || value > max) {
                    System.out.println("Input out of range. Please enter a number between " + min + " and " + max + ".");
                } else {
                    return value;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid numeric value.");
            }
        }
    }

}
