package org.example.controllers;

import java.util.Scanner;

import org.example.services.*;
import org.example.views.DisplayUI;

public class AdminController {
    public void adminPanel() {
        Scanner scanner = new Scanner(System.in);

        while (true) {

            System.out.println("\n============ Admin Panel ============");
            System.out.println("1. Add Menu Items");
            System.out.println("2. Update Menu Items");
            System.out.println("3. Remove Menu Items");
            System.out.println("4. View All Menu Items");
            System.out.println("5. Display Items by Category");
            System.out.println("6. Manage Categories");
            System.out.println("7. Manage Staff");
            System.out.println("8. Report Menu");
            System.out.println("9. Exit");

            // ------------ Validate menu choice ------------

            int choice = Utils.validateIntegerInput(scanner, "Enter your choice ([b] to go back): ", 1, 9);
            if (choice == -1) continue;

            switch (choice) {
                case 1:
                    MenuItemManager.addMenuItem(scanner);
                    break;

                case 2:
                    MenuItemManager.updateMenuItem(scanner);
                    break;

                case 3:
                    MenuItemManager.deleteMenuItem(scanner);
                    break;

                case 4:
                    MenuItemManager.viewMenuItemsAdmin();
                    break;

                case 5:
                    MenuItemManager.viewMenuItemsByCategorySeparately();
                    break;

                case 6:
                    CategoryManager.manageCategories(scanner);
                    break;

                case 7:
                    StaffManager.manageStaff(scanner);
                    break;
                case 8:
                    ReportManager.manageReports();
                    break;
                case 9:
                    DisplayUI.displayUI();
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}
