package org.example.utils;

import java.util.Scanner;

public class InputValidator {
    public static int validateIntegerInput(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("b")) {
                return -1; // Return -1 to indicate the user wants to go back
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
}