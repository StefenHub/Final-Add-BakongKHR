package org.example.controllers;

import org.example.services.OrderManager;
import org.example.services.OrderService;
import org.example.utils.ColorFormatter;
import org.example.utils.ConsoleFormatter;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.ShownBorders;
import org.nocrala.tools.texttablefmt.Table;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Scanner;

public class StaffController {
    private final Scanner scanner;
    private final OrderService orderService;
    final String RESET = "\u001B[0m";
    final String BOLD_BLUE = "\033[1;34m"; // Blue title
    final String WHITE_BORDER = "\033[97m"; // White border

    int consoleWidth = 180; // Console width
    int tableWidth = 100; // Wider table width
    int leftPadding = (consoleWidth - tableWidth) / 2;
    String padding = " ".repeat(leftPadding);

    public StaffController(Scanner scanner, OrderService orderService) {
        this.scanner = scanner;
        this.orderService = orderService;
    }

    //------------------- Start the staffController interaction menu -------------------
    public void start() {
        while (true) {

            // Create a table with a SINGLE wide column
            Table table = new Table(1, BorderStyle.UNICODE_BOX_DOUBLE_BORDER_WIDE, ShownBorders.ALL);
            table.setColumnWidth(0, 80, 90); // Explicitly setting column width wider

            CellStyle centerStyle = new CellStyle(CellStyle.HorizontalAlign.CENTER);
            CellStyle leftStyle = new CellStyle(CellStyle.HorizontalAlign.LEFT); // Left alignment for numbers

            // Title Row (Centered)
            table.addCell(BOLD_BLUE + "Welcome to Staff Panel" + RESET, centerStyle);

            // Menu Options (Numbers Left-Aligned)
            String[] options = {
                    "1.  View All Orders",
                    "2.  Place Order",
                    "0.  Exit"
            };

            for (String option : options) {
                table.addCell(BOLD_BLUE + option.trim() + RESET, leftStyle); // Left-align & reset colors properly
            }

            // Print the Table with WHITE Borders
            String[] tableLines = table.render().split("\n");
            for (String line : tableLines) {
                System.out.println(WHITE_BORDER + padding + line + RESET);
            }

            int choice = validateIntegerInput(scanner, ConsoleFormatter.centerText(ColorFormatter.colorText("👉 Enter your choice ([b] to go back): ", ColorFormatter.GREEN + ColorFormatter.BOLD)), 1, 3);
            switch (choice) {
                case 1:
                    OrderManager.viewAllCustomerOrders();
                    break;
                case 2:
                    CustomerController customerController = new CustomerController(scanner, orderService);
                    customerController.start();
                    break;
                case 0:
                    System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("❌ Exiting staffController menu...", ColorFormatter.RED + ColorFormatter.BOLD)));
                    return;
                default:
                    System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("❌ Invalid choice. Please try again.", ColorFormatter.RED + ColorFormatter.BOLD)));
            }
        }
    }

    // Utility method to format date properly
    private String formatDate(Date date) {
        if (date == null) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm");
        return sdf.format(date);
    }

    //------------------- Method to validate integer input within a range -------------------
    private int validateIntegerInput(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("b")) {
                return -1;
            }
            try {
                int value = Integer.parseInt(input);
                if (value < min || value > max) {
                    System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("Input out of range. Please enter a number between " + min + " and " + max + ".", ColorFormatter.RED + ColorFormatter.BOLD)));
                } else {
                    return value;
                }
            } catch (NumberFormatException e) {
                System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("Invalid input. Please enter a valid numeric value.", ColorFormatter.RED + ColorFormatter.BOLD)));
            }
        }
    }

    // test staff controller
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        OrderService orderService = new OrderService();
        StaffController staffController = new StaffController(scanner, orderService);
        staffController.start();
    }
}