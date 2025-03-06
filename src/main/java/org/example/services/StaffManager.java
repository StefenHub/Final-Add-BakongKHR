package org.example.services;

import org.example.utils.DatabaseConnection;
import org.example.utils.Utils;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

public class StaffManager {

    public static void addStaff(Scanner scanner) {
        String full_name;
        do {
            System.out.print("Enter full name: ");
            full_name = scanner.nextLine().trim();
            if (!NAME_PATTERN.matcher(full_name).matches()) {
                System.out.println("❌ Invalid full name! Only letters and spaces are allowed.");
            }
        } while (!NAME_PATTERN.matcher(full_name).matches());

        String userName;
        do {
            System.out.print("Enter username: ");
            userName = scanner.nextLine().trim();
            if (!USERNAME_PATTERN.matcher(userName).matches()) {
                System.out.println("❌ Invalid username! Must be lowercase letters and numbers only.");
            }
        } while (!USERNAME_PATTERN.matcher(userName).matches());

        String password;
        do {
            System.out.print("Enter password: ");
            password = scanner.nextLine().trim();
            if (!PASSWORD_PATTERN.matcher(password).matches()) {
                System.out.println("❌ Invalid password! Must be at least 8 characters, contain uppercase, lowercase, number, and special character.");
            }
        } while (!PASSWORD_PATTERN.matcher(password).matches());

        String email;
        do {
            System.out.print("Enter email: ");
            email = scanner.nextLine().trim();
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                System.out.println("❌ Invalid email! Must start with a letter and end with @gmail.com.");
            }
        } while (!EMAIL_PATTERN.matcher(email).matches());

        String phone_number;
        do {
            System.out.print("Enter phone number: ");
            phone_number = scanner.nextLine().trim();
            if (!PHONE_PATTERN.matcher(phone_number).matches()) {
                System.out.println("❌ Invalid phone number! Must start with 0 and be between 9 to 11 digits.");
            }
        } while (!PHONE_PATTERN.matcher(phone_number).matches());

        String role;
        do {
            System.out.print("Enter role: ");
            role = scanner.nextLine().trim();
            if (!VALID_ROLES.contains(role.toLowerCase())) {
                System.out.println("❌ Invalid role! Must be one of: staff, admin, kitchen.");
            }
        } while (!VALID_ROLES.contains(role.toLowerCase()));

        addStaff(UUID.randomUUID().toString(), full_name, userName, password, email, phone_number, role, Timestamp.valueOf(LocalDateTime.now()));
    }

    private static void addStaff(String string, String fullName, String userName, String password, String email, String phoneNumber, String role, Timestamp timestamp) {
        String sql = "INSERT INTO users (uuid, full_name, userName, password, email, phone_number, role, date_time_added) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, UUID.fromString(string));
            pstmt.setString(2, fullName);
            pstmt.setString(3, userName);
            pstmt.setString(4, BCrypt.hashpw(password, BCrypt.gensalt()));
            pstmt.setString(5, email);
            pstmt.setString(6, phoneNumber);
            pstmt.setString(7, role);
            pstmt.setTimestamp(8, timestamp);
            pstmt.executeUpdate();
            System.out.println("Staff added successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateStaff(Scanner scanner) {
        System.out.print("Enter staff UUID: ");
        UUID uuid = UUID.fromString(scanner.nextLine().trim());

        String newName;
        do {
            System.out.print("Enter new full name: ");
            newName = scanner.nextLine().trim();
            if (!NAME_PATTERN.matcher(newName).matches()) {
                System.out.println("❌ Invalid full name! Only letters and spaces are allowed.");
            }
        } while (!NAME_PATTERN.matcher(newName).matches());

        String newPhone;
        do {
            System.out.print("Enter new phone: ");
            newPhone = scanner.nextLine().trim();
            if (!PHONE_PATTERN.matcher(newPhone).matches()) {
                System.out.println("❌ Invalid phone number! Must start with 0 and be between 9 to 11 digits.");
            }
        } while (!PHONE_PATTERN.matcher(newPhone).matches());

        String newRole;
        do {
            System.out.print("Enter new role: ");
            newRole = scanner.nextLine().trim();
            if (!VALID_ROLES.contains(newRole.toLowerCase())) {
                System.out.println("❌ Invalid role! Must be one of: staff, admin, kitchen.");
            }
        } while (!VALID_ROLES.contains(newRole.toLowerCase()));

        updateStaff(uuid, newName, newPhone, newRole);
    }

    private static void updateStaff(UUID uuid, String newName, String newPhone, String newRole) {
        String sql = "UPDATE users SET full_name = ?, phone_number = ?, role = ? WHERE uuid = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newName);
            pstmt.setString(2, newPhone);
            pstmt.setString(3, newRole);
            pstmt.setObject(4, uuid);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Staff updated successfully!");
            } else {
                System.out.println("Staff not found!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeStaff(UUID uuid) {
        String sql = "DELETE FROM users WHERE uuid = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, uuid);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Staff removed successfully!");
            } else {
                System.out.println("Staff not found!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void manageStaff(Scanner scanner) {
        while (true) {
            System.out.println("\n--- Staff Management ---");
            System.out.println("1. Add Staff");
            System.out.println("2. View All Staff");
            System.out.println("3. Update Staff");
            System.out.println("4. Remove Staff");
            System.out.println("5. Exit");

            int choice = Utils.validateIntegerInput(scanner, "Enter your choice: ", 1, 5);
            switch (choice) {
                case 1:
                    addStaff(scanner);
                    break;
                case 2:
//                    viewAllStaff();
                    break;
                case 3:
                    updateStaff(scanner);
                    break;
                case 4:
                    System.out.print("Enter staff UUID: ");
                    UUID uuidToRemove = UUID.fromString(scanner.nextLine().trim());
                    removeStaff(uuidToRemove);
                    break;
                case 5:
                    return;
            }
        }
    }

    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z ]+$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-z0-9]+$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_.-]*@gmail\\.com$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0\\d{8,10}$");
    private static final Set<String> VALID_ROLES = new HashSet<>(Arrays.asList("staff", "admin", "kitchen"));

    // test StaffManager
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        manageStaff(scanner);
        scanner.close();
    }

}