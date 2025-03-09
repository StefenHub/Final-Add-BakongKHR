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

    final String RESET = "\u001B[0m";
    final String WHITE_BORDER = "\033[97m"; // White border

    int consoleWidth = 180; // Console width
    int tableWidth = 100; // Wider table width
    int leftPadding = (consoleWidth - tableWidth) / 2;
    String padding = " ".repeat(leftPadding);

    public void adminPanel() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            Table table = new Table(1, BorderStyle.UNICODE_BOX_DOUBLE_BORDER_WIDE, ShownBorders.ALL);
            table.setColumnWidth(0, 80, 90); // Explicitly setting column width wider

            CellStyle centerStyle = new CellStyle(CellStyle.HorizontalAlign.CENTER);
            CellStyle leftStyle = new CellStyle(CellStyle.HorizontalAlign.LEFT); // Left alignment for numbers

            // Title Row (Centered)
            table.addCell(ColorFormatter.colorText("WELCOME TO ADMIN DASHBOARD", ColorFormatter.BLUE + ColorFormatter.BOLD), centerStyle);

            // Menu Options (Numbers Left-Aligned)
            String[] options = {
                    "1.  Manage Items",
                    "2.  Manage Categories",
                    "3.  Manage Staff",
                    "4.  Manage Report",
                    "0.  Exit"
            };

            for (String option : options) {
                table.addCell(ColorFormatter.colorText(option.trim(), ColorFormatter.BLUE + ColorFormatter.BOLD), leftStyle); // Left-align & reset colors properly
            }

            // Print the Table with WHITE Borders
            String[] tableLines = table.render().split("\n");
            for (String line : tableLines) {
                System.out.println(WHITE_BORDER + padding + line + RESET);
            }

            int choice = Utils.validateIntegerInput(scanner, ConsoleFormatter.centerText(ColorFormatter.colorText("👉 Enter your choice ([b] to go back): ", ColorFormatter.GREEN + ColorFormatter.BOLD)), 0, 4);
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
                case 0:
                    DisplayUI.displayUI();
                    return;
                default:
                    System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("❌ Invalid choice! Please try again.", ColorFormatter.RED + ColorFormatter.BOLD)));
            }
        }
    }

    public void itemPanel(Scanner scanner) {
        while (true) {
            Table table = new Table(1, BorderStyle.UNICODE_BOX_DOUBLE_BORDER_WIDE, ShownBorders.ALL);
            table.setColumnWidth(0, 80, 90); // Explicitly setting column width wider

            CellStyle centerStyle = new CellStyle(CellStyle.HorizontalAlign.CENTER);
            CellStyle leftStyle = new CellStyle(CellStyle.HorizontalAlign.LEFT); // Left alignment for numbers

            // Title Row (Centered)
            table.addCell(ColorFormatter.colorText("ITEM MANAGEMENT PANEL", ColorFormatter.BLUE + ColorFormatter.BOLD), centerStyle);

            // Menu Options (Numbers Left-Aligned)
            String[] options = {
                    "1.  Add Menu Items",
                    "2.  Update Menu Items",
                    "3.  Remove Menu Items",
                    "4.  View All Menu Items",
                    "5.  Display Items by Category",
                    "0.  Exit"
            };

            for (String option : options) {
                table.addCell(ColorFormatter.colorText(option.trim(), ColorFormatter.BLUE + ColorFormatter.BOLD), leftStyle); // Left-align & reset colors properly
            }

            // Print the Table with WHITE Borders
            String[] tableLines = table.render().split("\n");
            for (String line : tableLines) {
                System.out.println(WHITE_BORDER + padding + line + RESET);
            }

            int option = Utils.validateIntegerInput(scanner, ConsoleFormatter.centerText(ColorFormatter.colorText("👉 Enter your choice: ", ColorFormatter.GREEN + ColorFormatter.BOLD)), 0, 5);
            if (option == -1) continue;

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
                case 0:
                    return;
                default:
                    System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("❌ Invalid choice! Please try again.", ColorFormatter.RED + ColorFormatter.BOLD)));
            }
        }
    }

    // test the adminPanel method
    public static void main(String[] args) {
        new AdminController().adminPanel();
    }
}