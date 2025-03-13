package org.example.services;

import org.example.utils.ConsoleFormatter;
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

import static org.example.utils.ColorFormatter.GREEN;

public class StaffManager {

    static final String RESET = "\u001B[0m";
    static final String BOLD_BLUE = "\033[1;34m"; // Blue title
    static final String BRIGHT_WHITE = "\033[97m"; // White text for options
    static final String WHITE_BORDER = "\033[97m"; // White border
    static final String BLUE = "\u001B[34m"; // Blue for padding
    static final String RED = "\033[1;31m"; // Red for errors

    static int consoleWidth = 180; // Console width
    static int tableWidth = 100; // Wider table width
    static int leftPadding = (consoleWidth - tableWidth) / 2;
    static String padding = " ".repeat(leftPadding);

    public static void addStaff(Scanner scanner) {
        String fullName;
        do {
            System.out.print(ConsoleFormatter.centerText(GREEN + "Enter full name: " + RESET));
            fullName = scanner.nextLine().trim();
            if (!NAME_PATTERN.matcher(fullName).matches()) {
                System.out.println(ConsoleFormatter.centerText(RED + "❌ Invalid full name! Only letters and spaces are allowed." + RESET));
            }
        } while (!NAME_PATTERN.matcher(fullName).matches());

        String username;
        do {
            System.out.print(ConsoleFormatter.centerText(GREEN + "Enter username: " + RESET));
            username = scanner.nextLine().trim();
            if (!USERNAME_PATTERN.matcher(username).matches()) {
                System.out.println(ConsoleFormatter.centerText(RED + "❌ Invalid username! Must be lowercase letters and numbers only." + RESET));
            }
        } while (!USERNAME_PATTERN.matcher(username).matches());

        String password;
        do {
            System.out.print(ConsoleFormatter.centerText(GREEN + "Enter password: " + RESET));
            password = scanner.nextLine().trim();
            if (!PASSWORD_PATTERN.matcher(password).matches()) {
                System.out.println(ConsoleFormatter.centerText(RED + "❌ Invalid password! Must be at least 8 characters, contain uppercase, lowercase, number, and special character." + RESET));
            }
        } while (!PASSWORD_PATTERN.matcher(password).matches());

        String email;
        do {
            System.out.print(ConsoleFormatter.centerText(GREEN + "Enter email: " + RESET));
            email = scanner.nextLine().trim();
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                System.out.println(ConsoleFormatter.centerText(RED + "❌ Invalid email! Must start with a letter and end with @gmail.com." + RESET));
            }
        } while (!EMAIL_PATTERN.matcher(email).matches());

        String phoneNumber;
        do {
            System.out.print(ConsoleFormatter.centerText(GREEN + "Enter phone number: " + RESET));
            phoneNumber = scanner.nextLine().trim();
            if (!PHONE_PATTERN.matcher(phoneNumber).matches()) {
                System.out.println(ConsoleFormatter.centerText(RED + "❌ Invalid phone number! Must start with 0 and be between 9 to 11 digits." + RESET));
            }
        } while (!PHONE_PATTERN.matcher(phoneNumber).matches());

        String role;
        do {
            System.out.print(ConsoleFormatter.centerText(GREEN + "Enter role: " + RESET));
            role = scanner.nextLine().trim();
            if (!VALID_ROLES.contains(role.toLowerCase())) {
                System.out.println(ConsoleFormatter.centerText(RED + "❌ Invalid role! Must be one of: staff, admin, chef." + RESET));
            }
        } while (!VALID_ROLES.contains(role.toLowerCase()));

        addStaff(UUID.randomUUID().toString(), fullName, username, password, email, phoneNumber, role, Timestamp.valueOf(LocalDateTime.now()));
    }

    private static void addStaff(String uuid, String fullName, String username, String password, String email, String phoneNumber, String role, Timestamp timestamp) {
        String sql = "INSERT INTO users (uuid, full_name, username, password, email, phone_number, role, date_time_added) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, UUID.fromString(uuid));
            pstmt.setString(2, fullName);
            pstmt.setString(3, username);
            pstmt.setString(4, BCrypt.hashpw(password, BCrypt.gensalt()));
            pstmt.setString(5, email);
            pstmt.setString(6, phoneNumber);
            pstmt.setString(7, role);
            pstmt.setTimestamp(8, timestamp);
            pstmt.executeUpdate(); // Ensure the data is saved to the database
            System.out.println(ConsoleFormatter.centerText(BOLD_BLUE + "✅ Staff added successfully!" + RESET));
        } catch (SQLException e) {
            System.out.println(ConsoleFormatter.centerText(RED + "⚠️ Database connection error: " + e.getMessage() + RESET));
        }
    }

    public static void updateStaff(Scanner scanner) {
        System.out.print(ConsoleFormatter.centerText("Enter staffController UUID: "));
        UUID uuid = UUID.fromString(scanner.nextLine().trim());

        String newName;
        do {
            System.out.print(ConsoleFormatter.centerText("Enter new full name: "));
            newName = scanner.nextLine().trim();
            if (!NAME_PATTERN.matcher(newName).matches()) {
                System.out.println(ConsoleFormatter.centerText(RED + "❌ Invalid full name! Only letters and spaces are allowed." + RESET));
            }
        } while (!NAME_PATTERN.matcher(newName).matches());

        String newPhone;
        do {
            System.out.print(ConsoleFormatter.centerText("Enter new phone: "));
            newPhone = scanner.nextLine().trim();
            if (!PHONE_PATTERN.matcher(newPhone).matches()) {
                System.out.println(ConsoleFormatter.centerText(RED + "❌ Invalid phone number! Must start with 0 and be between 9 to 11 digits." + RESET));
            }
        } while (!PHONE_PATTERN.matcher(newPhone).matches());

        String newRole;
        do {
            System.out.print(ConsoleFormatter.centerText("Enter new role: "));
            newRole = scanner.nextLine().trim();
            if (!VALID_ROLES.contains(newRole.toLowerCase())) {
                System.out.println(ConsoleFormatter.centerText(RED + "❌ Invalid role! Must be one of: staffController, admin, kitchen." + RESET));
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
                System.out.println(ConsoleFormatter.centerText(BOLD_BLUE + "✅ Staff updated successfully!" + RESET));
            } else {
                System.out.println(ConsoleFormatter.centerText(RED + "❌ Staff not found!" + RESET));
            }
        } catch (SQLException e) {
            System.out.println(ConsoleFormatter.centerText(RED + "⚠️ Database connection error: " + e.getMessage() + RESET));
        }
    }

    public static void removeStaff(UUID uuid) {
        String sql = "DELETE FROM users WHERE uuid = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, uuid);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println(ConsoleFormatter.centerText(BOLD_BLUE + "✅ Staff removed successfully!" + RESET));
            } else {
                System.out.println(ConsoleFormatter.centerText(RED + "❌ Staff not found!" + RESET));
            }
        } catch (SQLException e) {
            System.out.println(ConsoleFormatter.centerText(RED + "⚠️ Database connection error: " + e.getMessage() + RESET));
        }
    }

    public static void manageStaff(Scanner scanner) {
        while (true) {
            // Create a table with a SINGLE wide column
            Table table = new Table(1, BorderStyle.UNICODE_BOX_DOUBLE_BORDER_WIDE, ShownBorders.ALL);
            table.setColumnWidth(0, 80, 90); // Explicitly setting column width wider

            CellStyle centerStyle = new CellStyle(CellStyle.HorizontalAlign.CENTER);
            CellStyle leftStyle = new CellStyle(CellStyle.HorizontalAlign.LEFT); // Left alignment for numbers

            // Title Row (Centered)
            table.addCell(BOLD_BLUE + "Restaurant Ordering System" + RESET, centerStyle);

            // Menu Options (Numbers Left-Aligned)
            String[] options = {
                    "1.  Add Staff",
                    "2.  View All Staff",
                    "3.  Update Staff",
                    "4.  Remove Staff",
                    "5.  Exit"
            };

            for (String option : options) {
                table.addCell(BOLD_BLUE + option.trim() + RESET, leftStyle); // Left-align & reset colors properly
            }

            // Print the Table with WHITE Borders
            String[] tableLines = table.render().split("\n");
            for (String line : tableLines) {
                System.out.println(WHITE_BORDER + padding + line + RESET);
            }

            int choice = Utils.validateIntegerInput(scanner, ConsoleFormatter.centerText(BOLD_BLUE + "👉 Enter your choice: " + RESET), 1, 5);
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
                    System.out.print(ConsoleFormatter.centerText("\tEnter staffController UUID: "));
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
            System.out.println(ConsoleFormatter.centerText(table.render()));
        } catch (SQLException e) {
            System.out.println(ConsoleFormatter.centerText(RED + "���️ Database connection error: " + e.getMessage() + RESET));
        }
    }

    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z ]+$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-z0-9]+$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_.-]*@gmail\\.com$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0\\d{8,10}$");
    private static final Set<String> VALID_ROLES = new HashSet<>(Arrays.asList("staff", "admin", "chef"));

    // test StaffManager
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        manageStaff(scanner);
        scanner.close();
    }

}