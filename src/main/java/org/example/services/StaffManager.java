package org.example.services;

import org.example.utils.DatabaseConnection;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.ShownBorders;
import org.nocrala.tools.texttablefmt.Table;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class StaffManager {

    private static final int PAGE_SIZE = 10;

    // Add a new staff member
    public static void addStaff(String uuid, String full_name, String userName, String password, String email, String phone_number, String role, Timestamp date_time_added) {
        String sql = "INSERT INTO users (uuid, full_name, userName, password, email, phone_number, role, date_time_added) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, UUID.randomUUID());
            pstmt.setString(2, full_name);
            pstmt.setString(3, userName);
            pstmt.setString(4, password);
            pstmt.setString(5, email);
            pstmt.setString(6, phone_number);
            pstmt.setString(7, role);
            pstmt.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.executeUpdate();
            System.out.println("Staff added successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // View all staff members
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

    public static void updateStaff(UUID uuid, String newName, String newPhone, String newRole) {
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
                    System.out.print("Enter full name: ");
                    String full_name = scanner.nextLine().trim();
                    System.out.print("Enter username: ");
                    String userName = scanner.nextLine().trim();
                    System.out.print("Enter password: ");
                    String password = scanner.nextLine().trim();
                    System.out.print("Enter email: ");
                    String email = scanner.nextLine().trim();
                    System.out.print("Enter phone: ");
                    String phone = scanner.nextLine().trim();
                    System.out.print("Enter role: ");
                    String role = scanner.nextLine().trim();
                    addStaff(UUID.randomUUID().toString(), full_name, userName, password, email, phone, role, Timestamp.valueOf(LocalDateTime.now()));
                    break;
                case 2:
                    viewAllStaff();
                    break;
                case 3:
                    System.out.print("Enter staff UUID: ");
                    UUID uuid = UUID.fromString(scanner.nextLine().trim());
                    System.out.print("Enter new full name: ");
                    String newName = scanner.nextLine().trim();
                    System.out.print("Enter new phone: ");
                    String newPhone = scanner.nextLine().trim();
                    System.out.print("Enter new role: ");
                    String newRole = scanner.nextLine().trim();
                    updateStaff(uuid, newName, newPhone, newRole);
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
}