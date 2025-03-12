package org.example.controllers;

import java.util.Scanner;
import org.example.services.*;
import org.example.utils.ColorFormatter;
import org.example.utils.ConsoleFormatter;
import org.example.utils.Utils;
import org.example.views.DisplayUI;
import org.example.views.menuViewer.AdminMenuViewer;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.ShownBorders;
import org.nocrala.tools.texttablefmt.Table;

public class AdminController {

    private static final String RESET = "\u001B[0m";
    private static final String WHITE_BORDER = "\033[97m"; // White border

    private final int consoleWidth = 180; // Console width
    private final int tableWidth = 100; // Wider table width
    private final String padding = " ".repeat((consoleWidth - tableWidth) / 2);

    public void adminPanel(Scanner scanner) {
        while (true) {
            Table table = new Table(1, BorderStyle.UNICODE_BOX_DOUBLE_BORDER_WIDE, ShownBorders.ALL);
            table.setColumnWidth(0, 80, 90);

            CellStyle centerStyle = new CellStyle(CellStyle.HorizontalAlign.CENTER);
            CellStyle leftStyle = new CellStyle(CellStyle.HorizontalAlign.LEFT);

            table.addCell(ColorFormatter.colorText("WELCOME TO ADMIN DASHBOARD", ColorFormatter.BLUE + ColorFormatter.BOLD), centerStyle);

            String[] options = {
                    "1.  Manage Items",
                    "2.  Manage Categories",
                    "3.  Manage Staff",
                    "4.  Manage Report",
                    "5.  Exit"
            };

            for (String option : options) {
                table.addCell(ColorFormatter.colorText(option.trim(), ColorFormatter.BLUE + ColorFormatter.BOLD), leftStyle);
            }

            // Print the Table with WHITE Borders
            String[] tableLines = table.render().split("\n");
            for (String line : tableLines) {
                System.out.println(WHITE_BORDER + padding + line + RESET);
            }

            int choice = Utils.validateIntegerInput(scanner, ConsoleFormatter.centerText(ColorFormatter.colorText("👉 Enter your choice ([b] to go back): ", ColorFormatter.GREEN + ColorFormatter.BOLD)), 1, 5);
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
                    System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("👋 Exiting... Goodbye!", ColorFormatter.GREEN + ColorFormatter.BOLD)));
                    System.exit(0);
                    break;
                default:
                    System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("❌ Invalid choice! Please try again.", ColorFormatter.RED + ColorFormatter.BOLD)));
            }
        }
    }

    public void itemPanel(Scanner scanner) {
        while (true) {
            Table table = new Table(1, BorderStyle.UNICODE_BOX_DOUBLE_BORDER_WIDE, ShownBorders.ALL);
            table.setColumnWidth(0, 80, 90);

            CellStyle centerStyle = new CellStyle(CellStyle.HorizontalAlign.CENTER);
            CellStyle leftStyle = new CellStyle(CellStyle.HorizontalAlign.LEFT);

            table.addCell(ColorFormatter.colorText("ITEM MANAGEMENT PANEL", ColorFormatter.BLUE + ColorFormatter.BOLD), centerStyle);

            String[] options = {
                    "1.  Add Menu Items",
                    "2.  Update Menu Items",
                    "3.  Remove Menu Items",
                    "4.  View All Menu Items",
                    "5.  Display Items by Category",
                    "6.  Exit"
            };

            for (String option : options) {
                table.addCell(ColorFormatter.colorText(option.trim(), ColorFormatter.BLUE + ColorFormatter.BOLD), leftStyle);
            }

            // Print the Table with WHITE Borders
            String[] tableLines = table.render().split("\n");
            for (String line : tableLines) {
                System.out.println(WHITE_BORDER + padding + line + RESET);
            }

//            int option = Utils.validateIntegerInput(scanner, ConsoleFormatter.centerText(ColorFormatter.colorText("👉 Enter your choice: ", ColorFormatter.GREEN + ColorFormatter.BOLD)), 1, 6);
//            if (option == -1) continue;
            int choice = Utils.validateIntegerInput(scanner, ConsoleFormatter.centerText(ColorFormatter.colorText("👉 Enter your choice ([b] to go back): ", ColorFormatter.GREEN + ColorFormatter.BOLD)), 1, 5);
            if (choice == -1) {
                return;  // This will exit the current menu and go back
            }

            switch (choice) {
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
                    return;
                default:
                    System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("❌ Invalid choice! Please try again.", ColorFormatter.RED + ColorFormatter.BOLD)));
            }
        }
    }

    // Main method to start the Admin Panel
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            new AdminController().adminPanel(scanner);
        }
    }
}
