package org.example.utils;

import java.util.Scanner;

public class Utils {
    public static int validateIntegerInput(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            // Check if the user wants to go back
            if (input.equalsIgnoreCase("b")) {
                return -1;  // Special return value to indicate "go back"
            }

            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                } else {
                    System.out.println(ColorFormatter.colorText("⚠ Invalid input! Please enter a number between " + min + " and " + max + ".", ColorFormatter.YELLOW + ColorFormatter.BOLD));
                }
            } catch (NumberFormatException e) {
                System.out.println(ColorFormatter.colorText("⚠ Invalid input! Please enter a valid number or [b] to go back.", ColorFormatter.YELLOW + ColorFormatter.BOLD));
            }
        }
    }


}
