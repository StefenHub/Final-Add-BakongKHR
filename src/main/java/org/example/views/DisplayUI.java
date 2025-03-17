package org.example.views;

import com.google.zxing.WriterException;
import org.example.controllers.AdminController;
import org.example.controllers.ChefController;
import org.example.controllers.CustomerController;
import org.example.controllers.StaffController;
import org.example.services.OrderService;
import org.example.utils.*;

import java.util.Scanner;

public class DisplayUI {
    public static void displayUI() throws WriterException {
        Scanner scanner = new Scanner(System.in);
        OrderService orderService = new OrderService();

        boolean firstTime = true;

        while (true) {
            if (firstTime) {
                mainUI.asciiUI();
                firstTime = false;
            }

            // Improve input prompt spacing
            System.out.print((ColorFormatter.colorText("👉 Enter your choice: ", ColorFormatter.GREEN + ColorFormatter.BOLD)));

            int choice = Utils.validateIntegerInput(scanner, "", 0, 5);
            if (choice == -1) return; // Exit condition

            switch (choice) {
                case 1:
                    if (AuthUtils.login(scanner)) {
                        StaffController staffController = new StaffController(scanner, orderService);
                        staffController.start();
                    } else {
                        ConsoleFormatter.printErrorMessage("❌ Access Denied: Incorrect Staff credentials.");
                    }
                    break;

                case 2:
                    if (AuthUtils.login(scanner)) {
                        ChefController chefController = new ChefController(scanner);
                        chefController.start();
                    } else {
                        ConsoleFormatter.printErrorMessage("❌ Incorrect credentials for Kitchen staff.");
                    }
                    break;

                case 3:
                    CustomerController customerController = new CustomerController(scanner, orderService);
                    customerController.start();
                    break;

                case 4:
                    if (AuthUtils.login(scanner)) {
                        new AdminController().adminPanel(scanner);
                    } else {
                        ConsoleFormatter.printErrorMessage("❌ Access Denied: Incorrect Admin credentials.");
                    }
                    break;

                case 5:
                    ConsoleFormatter.printSuccessMessage("✅ Exiting... Thank you for using our system!");
                    scanner.close();
                    return;

                default:
                    ConsoleFormatter.printErrorMessage(
                            ColorFormatter.colorText("❌ Invalid choice! Please try again.", ColorFormatter.RED + ColorFormatter.BOLD)
                    );
            }
        }
    }

    // test the displayUI method
    public static void main(String[] args) throws WriterException {
        displayUI();
    }
}