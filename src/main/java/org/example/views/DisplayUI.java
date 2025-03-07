package org.example.views;

import org.example.controllers.AdminController;
import org.example.controllers.CustomerController;
import org.example.controllers.Kitchen;
import org.example.controllers.Staff;
import org.example.services.OrderService;
import org.example.utils.Utils;

import java.util.Scanner;

public class DisplayUI {
    // Static passwords for authentication
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String STAFF_PASSWORD = "staff123";
    private static final String KITCHEN_PASSWORD = "kitchen123";

    public static void displayUI() {
        Scanner scanner = new Scanner(System.in);
        OrderService orderService = new OrderService(); // Ensure menuItems is passed

        while (true) {
            System.out.println("\nWelcome to the Restaurant Management System!");
            System.out.println("1. Staff");
            System.out.println("2. Kitchen");
            System.out.println("3. Customer");
            System.out.println("4. Admin");
            System.out.println("5. Exit");

            int choice = Utils.validateIntegerInput(scanner, "Enter your choice: ", 1, 5);
            if (choice == -1) return;

            switch (choice) {
                case 1:
                    if (authenticateUser(scanner, STAFF_PASSWORD, "Staff")) {
                        System.out.println("Staff Section");
                        Staff staff = new Staff(scanner, orderService);
                        staff.start();
                    }
                    break;
                case 2:
                    if (authenticateUser(scanner, KITCHEN_PASSWORD, "Kitchen")) {
                        System.out.println("Kitchen Section");
                        Kitchen kitchen = new Kitchen(scanner);
                        kitchen.start();
                    }
                    break;
                case 3:
                    System.out.println("Customer Section");
                    CustomerController customerController = new CustomerController(scanner, orderService);
                    customerController.start();
                    break;
                case 4:
                    if (authenticateUser(scanner, ADMIN_PASSWORD, "Admin")) {
                        System.out.println("Admin Section");
                        new AdminController().adminPanel();
                    }
                    break;
                case 5:
                    System.out.println("Exiting... Thank you for using our system!");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice! Please try again.");
            }
        }
    }

    // Authentication method for Admin, Staff, and Kitchen
    private static boolean authenticateUser(Scanner scanner, String correctPassword, String role) {
        System.out.print("Enter " + role + " Password: ");
        String inputPassword = scanner.nextLine().trim();

        if (inputPassword.equals(correctPassword)) {
            System.out.println("✅ " + role + " Login Successful!");
            return true;
        } else {
            System.out.println("❌ Incorrect Password! Access Denied.");
            return false;
        }
    }
}
