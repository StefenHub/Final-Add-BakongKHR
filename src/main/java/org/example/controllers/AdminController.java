package org.example.controllers;

import java.util.Scanner;

import org.example.services.*;
import org.example.utils.Utils;
import org.example.views.menuViewer.AdminMenuViewer;
import org.example.services.UpdateMenuItem;
import org.example.views.DisplayUI;

public class AdminController {
    public void adminPanel() {
        Scanner scanner = new Scanner(System.in);

        while (true) {

            System.out.print("""
                                \u001B[34m
                                ╔═════════════════════════════════════════╗
                                ║  \u001B[36m            Admin Dashboard         \u001B[34m   ║
                                ╠═════════════════════════════════════════╣
                                ║   \u001B[33m[1].  🧑‍💻 Manage Items\u001B[34m                 ║
                                ║   \u001B[33m[2].  🏷️ Manage Categories\u001B[34m            ║
                                ║   \u001B[33m[3].  👥 Manage Staff\u001B[34m                 ║
                                ║   \u001B[33m[4].  📊 Manage Report\u001B[34m                ║
                                ║   \u001B[31m[5].  ❌ Exit\u001B[34m                         ║
                                ╚═════════════════════════════════════════╝ \u001B[0m
                            """);
            int choice = Utils.validateIntegerInput(scanner, "\t👉 Enter your choice ([b] to go back): ", 1, 9);
            if (choice == -1) continue;

            switch (choice) {
                case 1:
                    itemPanel(scanner);
                    break;
                case 2:
                    CategoryManager.manageCategories(scanner);
                    break;

                case 3:
                    StaffManager.manageStaff(scanner);
                    break;
                case 4:
                    ReportManager.manageReports();
                    break;
                case 5:
                    DisplayUI.displayUI();
                    break;
                default:
                    System.out.println("\tInvalid choice. Please try again.");
            }
        }
    }
    public void itemPanel(Scanner scanner){
        System.out.print("""
                            \u001B[34m
                            ╔════════════════════════════════════════╗
                            ║      🍽️ Item Management Panel          ║
                            ╠════════════════════════════════════════╣
                            ║   \u001B[33m[1]. ➕ Add Menu Items\u001B[34m               ║
                            ║   \u001B[33m[2]. ✏️ Update Menu Items\u001B[34m            ║
                            ║   \u001B[33m[3]. 🗑️ Remove Menu Items\u001B[34m            ║
                            ║   \u001B[33m[4]. 👀 View All Menu Items\u001B[34m          ║
                            ║   \u001B[33m[5]. 🥘 Display Items by Category\u001B[34m    ║
                            ║   \u001B[31m[6]. ❌ Exit\u001B[34m                         ║
                            ╚════════════════════════════════════════╝\u001B[0m
                        """);
        int option = Utils.validateIntegerInput(scanner, "\t👉 Enter your choice: ", 1, 6);
        switch (option) {
            case 1:
                MenuItemManager.addMenuItem(scanner);
                break;
            case 2:
                UpdateMenuItem.updateMenuItem(scanner);
                break;
            case 3:
                MenuItemManager.deleteMenuItem(scanner);
                break;
            case 4:
                AdminMenuViewer.viewMenuItemsAdmin();
                break;
            case 5:
                MenuItemManager.viewMenuItemsByCategorySeparately();
                break;
            case 6:
                adminPanel();
                break;
        }
    }
}
