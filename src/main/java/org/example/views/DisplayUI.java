package org.example.views;
import org.example.controllers.AdminController;
import org.example.controllers.CustomerController;
import org.example.controllers.Staff;
import org.example.services.OrderService;
import org.example.services.Utils;

import java.util.Scanner;




public class DisplayUI {
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
                    System.out.println("Staff Section");
                    Staff staff = new Staff(scanner, orderService);
                    staff.start();
                    break;
                case 2:
                    System.out.println("Kitchen Section - Feature Coming Soon!");
                    break;
                case 3:
                    System.out.println("Customer Section");
                    CustomerController customerController = new CustomerController(scanner, orderService);
                    customerController.start();
                    break;
                case 4:
                    System.out.println("Admin Section");
                    new AdminController().adminPanel();
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
}
