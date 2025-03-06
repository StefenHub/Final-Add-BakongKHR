package org.example.views;
import org.example.controllers.AdminController;
import org.example.controllers.CustomerController;
import org.example.controllers.Kitchen;
import org.example.controllers.Staff;
import org.example.services.OrderService;
import org.example.utils.Utils;

import java.util.Scanner;


public class DisplayUI {
    public static void displayUI() {
        Scanner scanner = new Scanner(System.in);
        OrderService orderService = new OrderService(); // Ensure menuItems is passed
        final String ADMIN_PASSWORD = "admin123";
        final String STAFF_PASSWORD = "staff123";
        final String KITCHEN_PASSWORD = "kitchen123";

        while (true) {
            System.out.print("""
                                  \u001B[34m
                                  ╔════════════════════════════════════════════╗
                                  ║       🍽️ Restaurant Management System      ║ 
                                  ╠════════════════════════════════════════════╣
                                  ║   \u001B[33m[1]. 👨‍🍳 Staff\u001B[34m                            ║
                                  ║   \u001B[33m[2]. 🔥 Kitchen\u001B[34m                          ║
                                  ║   \u001B[33m[3]. 🍽️ Customer\u001B[34m                         ║
                                  ║   \u001B[33m[4]. 🛠️ Admin\u001B[34m                            ║
                                  ║   \u001B[31m[5]. ❌ Exit\u001B[34m                             ║
                                  ╚════════════════════════════════════════════╝\u001B[0m 
                              """);
            System.out.print("\t\u001B[34m👉 Enter your choice ([b] to go back):");
            int choice = Utils.validateIntegerInput(scanner, "", 1, 5);
            if (choice == -1) return;

            switch (choice) {
                case 1:
                    if (authenticateUser(scanner, STAFF_PASSWORD, "Staff")) {
                        Staff staff = new Staff(scanner, orderService);
                        staff.start();
                    }
                    break;
                case 2:
                    if (authenticateUser(scanner, KITCHEN_PASSWORD, "Kitchen")) {
                        Kitchen kitchen = new Kitchen(scanner);
                        kitchen.start();
                    }
                    break;
                case 3:
                    CustomerController customerController = new CustomerController(scanner, orderService);
                    customerController.start();
                    break;
                case 4:
                    if (authenticateUser(scanner, ADMIN_PASSWORD, "Admin")) {
                        new AdminController().adminPanel();
                    }
                    break;
                case 5:
                    System.out.println("\tExiting... Thank you for using our system!");
                    scanner.close();
                    return;
                default:
                    System.out.println("\tInvalid choice! Please try again.");
            }
        }
    }

    // Authentication method for Admin, Staff, and Kitchen
    private static boolean authenticateUser(Scanner scanner, String correctPassword, String role) {
        System.out.print("\tEnter " + role + " Password: ");
        String inputPassword = scanner.nextLine().trim();

        if (inputPassword.equals(correctPassword)) {
            System.out.println("\t✅ " + role + " Login Successful!");
            return true;
        } else {
            System.out.println("\t❌ Incorrect Password! Access Denied.");
            return false;
        }
    }
}
