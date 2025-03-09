package org.example.utils;

import java.util.Scanner;

public class AuthUtils {
    // Authentication method for Admin, Staff, and Kitchen
    public static boolean authenticateUser(Scanner scanner, String correctPassword, String role) {
        System.out.print(ConsoleFormatter.centerText(ColorFormatter.colorText("🔑 Enter " + role + " Password: ", ColorFormatter.CYAN)));
        String inputPassword = scanner.nextLine().trim();

        if (inputPassword.equals(correctPassword)) {
            ConsoleFormatter.printSuccessMessage("✅ " + role + " Login Successful!");
            return true;
        } else {
            ConsoleFormatter.printErrorMessage("❌ Incorrect Password! Access Denied.");
            return false;
        }
    }
}
