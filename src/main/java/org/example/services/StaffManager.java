package org.example.services;

import org.example.utils.DatabaseConnection;
import org.example.utils.Utils;
import org.mindrot.jbcrypt.BCrypt;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.ShownBorders;
import org.nocrala.tools.texttablefmt.Table;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

public class StaffManager {

    public static void addStaff(Scanner scanner) {
        String full_name;
        do {
            System.out.print("\tEnter full name: ");
            full_name = scanner.nextLine().trim();
            if (!NAME_PATTERN.matcher(full_name).matches()) {
                System.out.println("\t❌ Invalid full name! Only letters and spaces are allowed.");
            }
        } while (!NAME_PATTERN.matcher(full_name).matches());

        String userName;
        do {
            System.out.print("\tEnter username: ");
            userName = scanner.nextLine().trim();
            if (!USERNAME_PATTERN.matcher(userName).matches()) {
                System.out.println("\t❌ Invalid username! Must be lowercase letters and numbers only.");
            }
        } while (!USERNAME_PATTERN.matcher(userName).matches());

        String password;
        do {
            System.out.print("\tEnter password: ");
            password = scanner.nextLine().trim();
            if (!PASSWORD_PATTERN.matcher(password).matches()) {
                System.out.println("\t❌ Invalid password! Must be at least 8 characters, contain uppercase, lowercase, number, and special character.");
            }
        } while (!PASSWORD_PATTERN.matcher(password).matches());

        String email;
        do {
            System.out.print("\tEnter email: ");
            email = scanner.nextLine().trim();
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                System.out.println("\t❌ Invalid email! Must start with a letter and end with @gmail.com.");
            }
        } while (!EMAIL_PATTERN.matcher(email).matches());

        String phone_number;
        do {
            System.out.print("\tEnter phone number: ");
            phone_number = scanner.nextLine().trim();
            if (!PHONE_PATTERN.matcher(phone_number).matches()) {
                System.out.println("\t❌ Invalid phone number! Must start with 0 and be between 9 to 11 digits.");
            }
        } while (!PHONE_PATTERN.matcher(phone_number).matches());

        String role;
        do {
            System.out.print("\tEnter role: ");
            role = scanner.nextLine().trim();
            if (!VALID_ROLES.contains(role.toLowerCase())) {
                System.out.println("\t❌ Invalid role! Must be one of: staff, admin, kitchen.");
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
            System.out.println("\tStaff added successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateStaff(Scanner scanner) {
        System.out.print("\tEnter staff UUID: ");
        UUID uuid = UUID.fromString(scanner.nextLine().trim());

        String newName;
        do {
            System.out.print("\tEnter new full name: ");
            newName = scanner.nextLine().trim();
            if (!NAME_PATTERN.matcher(newName).matches()) {
                System.out.println("\t❌ Invalid full name! Only letters and spaces are allowed.");
            }
        } while (!NAME_PATTERN.matcher(newName).matches());

        String newPhone;
        do {
            System.out.print("\tEnter new phone: ");
            newPhone = scanner.nextLine().trim();
            if (!PHONE_PATTERN.matcher(newPhone).matches()) {
                System.out.println("\t❌ Invalid phone number! Must start with 0 and be between 9 to 11 digits.");
            }
        } while (!PHONE_PATTERN.matcher(newPhone).matches());

        String newRole;
        do {
            System.out.print("\tEnter new role: ");
            newRole = scanner.nextLine().trim();
            if (!VALID_ROLES.contains(newRole.toLowerCase())) {
                System.out.println("\t❌ Invalid role! Must be one of: staff, admin, kitchen.");
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
                System.out.println("\tStaff updated successfully!");
            } else {
                System.out.println("\tStaff not found!");
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
                System.out.println("\tStaff removed successfully!");
            } else {
                System.out.println("\tStaff not found!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void manageStaff(Scanner scanner) {
        while (true) {
            System.out.print("""
                                \u001B[34m
                                ╔════════════════════════════════════╗
                                ║       👥 Staff Management          ║
                                ╠════════════════════════════════════╣
                                ║   \u001B[33m[1]. ➕ Add Staff\u001B[34m                ║
                                ║   \u001B[33m[2]. 👀 View All Staff\u001B[34m           ║
                                ║   \u001B[33m[3]. ✏️ Update Staff\u001B[34m             ║
                                ║   \u001B[33m[4]. 🗑️ Remove Staff\u001B[34m             ║
                                ║   \u001B[31m[5]. ❌ Exit\u001B[34m                     ║
                                ╚════════════════════════════════════╝ \u001B[0m
                            """);

            int choice = Utils.validateIntegerInput(scanner, "\t👉 Enter your choice: ", 1, 5);
            switch (choice) {
                case 1:
                    addStaff(scanner);
                    break;
                case 2:
                    viewAllStaff();
                    break;
                case 3:
                    updateStaff(scanner);
                    break;
                case 4:
                    System.out.print("\tEnter staff UUID: ");
                    UUID uuidToRemove = UUID.fromString(scanner.nextLine().trim());
                    removeStaff(uuidToRemove);
                    break;
                case 5:
                    return;
            }
        }
    }

    public static void viewAllStaff() {
        String sql = "SELECT * FROM users";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            Table table = new Table(9, BorderStyle.UNICODE_BOX_WIDE, ShownBorders.ALL);
            table.addCell("No.", new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell("UUID", new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell("Full Name", new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell("Username", new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell("Password", new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell("Email", new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell("Phone Number", new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell("Role", new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell("Date Added", new CellStyle(CellStyle.HorizontalAlign.CENTER));

            int count = 1;
            while (rs.next()) {
                table.addCell(String.valueOf(count++), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(rs.getObject("uuid").toString(), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(rs.getString("full_name"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(rs.getString("userName"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(rs.getString("password"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(rs.getString("email"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(rs.getString("phone_number"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(rs.getString("role"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(rs.getTimestamp("date_time_added").toString(), new CellStyle(CellStyle.HorizontalAlign.CENTER));
            }
            System.out.println(table.render());
        } catch (SQLException e) {
            e.printStackTrace();
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