package org.example.controllers;

import org.example.services.OrderManager;
import org.example.utils.DatabaseConnection;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.ShownBorders;
import org.nocrala.tools.texttablefmt.Table;
import org.example.services.OrderService;

import java.sql.*;

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
                    OrderManager.viewAllCustomerOrders();
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
        String query = "SELECT order_id, order_items.name, order_items.quantity, order_items.size, order_items.description, order_date FROM order_items";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            Table table = new Table(5, BorderStyle.UNICODE_ROUND_BOX_WIDE, ShownBorders.ALL);
            table.addCell("No.", new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell("Order ID", new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell("Name", new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell("Quantity", new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell("Size", new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell("Description", new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell("Order Date", new CellStyle(CellStyle.HorizontalAlign.CENTER));
            int count = 1;
            while (rs.next()) {
                table.addCell(String.valueOf(count++), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(rs.getString("order_id"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(rs.getString("name"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(rs.getString("quantity"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(rs.getString("size"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(rs.getString("description"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(rs.getString("order_date"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
            }

            System.out.println("\n--------- CUSTOMER ORDERS ---------");
            if (count == 1) {
                System.out.println("No orders found.");
            } else {
                System.out.println(table.render());
            }

        } catch (SQLException e) {
            System.err.println("❌ Error retrieving customer orders: " + e.getMessage());
            e.printStackTrace();
        }
    }


    //------------------- Method to validate integer input within a range -------------------
    private int validateIntegerInput(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("[b]")) {
                return -1;
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
