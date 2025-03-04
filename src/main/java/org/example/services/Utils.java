package org.example.services;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.ShownBorders;
import org.nocrala.tools.texttablefmt.Table;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Utils {
    public static int validateIntegerInput(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("b")) {
                return -1; // Return -1 to indicate the admin wants to go back
            }

            try {
                int value = Integer.parseInt(input);
                if (value < min || value > max) {
                    System.out.println("Input out of range. Please enter a number between " + min + " and " + max + ".");
                } else {
                    return value;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid numeric value.");
            }
        }
    }

//    // Helper method to validate price input
//    public static double validatePriceInput(Scanner scanner, String prompt) {
//        while (true) {
//            System.out.print(prompt);
//            String input = scanner.nextLine().trim();
//
//            try {
//                double price = Double.parseDouble(input);
//                if (price < 0) {
//                    System.out.println("Price cannot be negative. Please enter a valid positive number.");
//                } else {
//                    return price;
//                }
//            } catch (NumberFormatException e) {
//                System.out.println("Invalid input. Please enter a valid numeric value.");
//            }
//        }
//    }

//    // Helper method to display items as a formatted table
//    public static void displayItemsAsTable(List<Map<String, Object>> items) {
//        if (items.isEmpty()) {
//            System.out.println("No items available.");
//            return;
//        }
//
//        Table table = new Table(8, BorderStyle.UNICODE_HEAVY_BOX, ShownBorders.ALL);
//
//        String[] header1 = {"Category"};
//        String[] headers = {"ID", "Name", "Description", "Size" , "Base Price", "Sell Price" , "Discount", "Final Price"};
//
//
//        for (String header : headers) {
//            table.addCell(header, new CellStyle(CellStyle.HorizontalAlign.CENTER));
//        }
//
//        for (Map<String, Object> menuItem : items) {
//            double discount = menuItem.containsKey("discount_price") ? (double) menuItem.get("discount_price") : 0.0;
//            double finalPrice = (double) menuItem.get("sell_price") - (double) menuItem.get("discount");
//            String size = menuItem.containsKey("size") ? (String) menuItem.get("size") : "N/A";
//
//            table.addCell(menuItem.get("item_id").toString());
//            table.addCell(truncate((String) menuItem.get("name"), 20));
//            table.addCell(truncate((String) menuItem.get("description"), 30));
//            table.addCell(size);
//            table.addCell(String.format("%.2f", menuItem.get("base_price")));
//            table.addCell(String.format("%.2f", menuItem.get("sell_price")));
//            table.addCell(String.format("%.2f", menuItem.get("discount")));
//            table.addCell(String.format("%.2f", finalPrice));
//
//        }
//
//        System.out.println(table.render());
//    }

    // Helper method to print a separator line
    public static void printSeparator(int... columnWidths) {
        StringBuilder separator = new StringBuilder();
        for (int width : columnWidths) {
            separator.append("+").append("-".repeat(width + 1));
        }
        separator.append("+");
        System.out.println(separator);
    }

    // Helper method to truncate strings to fit column width
    public static String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength - 3) + "..." : text;
    }

    public static double validateDoubleInput(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
