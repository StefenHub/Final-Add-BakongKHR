package org.example.views;

import org.example.controllers.AdminController;
import org.example.controllers.ChefController;
import org.example.controllers.CustomerController;
import org.example.controllers.StaffController;
import org.example.services.OrderService;
import org.example.utils.*;

import java.util.Scanner;

public class DisplayUI {
    public static void displayUI() {
        Scanner scanner = new Scanner(System.in);
        OrderService orderService = new OrderService();

        final String ADMIN_PASSWORD = "admin123";
        final String STAFF_PASSWORD = "staff123";
        final String CHEF_PASSWORD = "chef123";

        boolean firstTime = true;

        while (true) {
            if (firstTime) {
                mainUI.asciiUI();
                firstTime = false;
            }

            // Improve input prompt spacing
            System.out.print(ConsoleFormatter.centerText(ColorFormatter.colorText("👉 Enter your choice ([b] to go back): ", ColorFormatter.GREEN + ColorFormatter.BOLD)));

            int choice = Utils.validateIntegerInput(scanner, "", 0, 4);
            if (choice == -1) return; // Exit condition

            switch (choice) {
                case 1:
                    if (AuthUtils.authenticateUser(scanner, STAFF_PASSWORD, "Staff")) {
                        StaffController staffController = new StaffController(scanner, orderService);
                        staffController.start();
                    }
                    break;
                case 2:
                    if (AuthUtils.authenticateUser(scanner, CHEF_PASSWORD, "Kitchen")) {
                        ChefController chefController = new ChefController(scanner);
                        chefController.start();
                    }
                    break;
                case 3:
                    CustomerController customerController = new CustomerController(scanner, orderService);
                    customerController.start();
                    break;
                case 4:
                    if (AuthUtils.authenticateUser(scanner, ADMIN_PASSWORD, "Admin")) {
                        new AdminController().adminPanel();
                    }
                    break;
                case 0:
                    ConsoleFormatter.printSuccessMessage("✅ Exiting... Thank you for using our system!");
                    scanner.close();
                    return;
                default:
                    ConsoleFormatter.printErrorMessage((ColorFormatter.colorText("❌ Invalid choice! Please try again.", ColorFormatter.RED + ColorFormatter.BOLD)));
            }
        }
    }

    // test the displayUI method
    public static void main(String[] args) {
        displayUI();
    }
}