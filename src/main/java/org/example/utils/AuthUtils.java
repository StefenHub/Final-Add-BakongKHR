package org.example.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    // Method for user login
    public static boolean login(Scanner scanner) {
        System.out.print(ConsoleFormatter.centerText(ColorFormatter.colorText("🔑 Enter Username: ", ColorFormatter.CYAN)));
        String username = scanner.nextLine().trim();

        System.out.print(ConsoleFormatter.centerText(ColorFormatter.colorText("🔑 Enter Password: ", ColorFormatter.CYAN)));
        String password = scanner.nextLine().trim();

        String query = "SELECT password FROM users WHERE username = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next() && rs.getString("password").equals(password)) {
                ConsoleFormatter.printSuccessMessage("✅ Login Successful!");
                return true;
            } else {
                ConsoleFormatter.printErrorMessage("❌ Incorrect Username or Password! Access Denied.");
                return false;
            }
        } catch (SQLException e) {
            ConsoleFormatter.printErrorMessage("❌ Database error: " + e.getMessage());
            return false;
        }
    }

    // Method for user registration
    public static void register(Scanner scanner) {
        System.out.print(ConsoleFormatter.centerText(ColorFormatter.colorText("🔑 Enter New Username: ", ColorFormatter.CYAN)));
        String username = scanner.nextLine().trim();

        if (!StaffValidation.isValidUsername(username)) {
            ConsoleFormatter.printErrorMessage("❌ Invalid Username! Must be alphanumeric and 5-20 characters long.");
            return;
        }

        String checkQuery = "SELECT username FROM users WHERE username = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {

            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                ConsoleFormatter.printErrorMessage("❌ Username already exists! Please choose a different username.");
                return;
            }
        } catch (SQLException e) {
            ConsoleFormatter.printErrorMessage("❌ Database error: " + e.getMessage());
            return;
        }

        System.out.print(ConsoleFormatter.centerText(ColorFormatter.colorText("🔑 Enter New Password: ", ColorFormatter.CYAN)));
        String password = scanner.nextLine().trim();

        if (!StaffValidation.isValidPassword(password)) {
            ConsoleFormatter.printErrorMessage("❌ Invalid Password! Must be at least 8 characters long, contain 1 uppercase, 1 lowercase, 1 number, and 1 special character.");
            return;
        }

        String insertQuery = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {

            insertStmt.setString(1, username);
            insertStmt.setString(2, password);
            insertStmt.executeUpdate();
            ConsoleFormatter.printSuccessMessage("✅ Registration Successful!");
        } catch (SQLException e) {
            ConsoleFormatter.printErrorMessage("❌ Database error: " + e.getMessage());
        }
    }
}