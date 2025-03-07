package org.example.utils;

import java.util.regex.Pattern;

public class StaffValidation {

    // Validate Full Name (Only letters and spaces, at least 3 characters)
    public static boolean isValidFullName(String fullName) {
        return fullName != null && fullName.matches("^[a-zA-Z\\s]{3,50}$");
    }

    // Validate Username (Alphanumeric, 5-20 characters)
    public static boolean isValidUsername(String username) {
        return username != null && username.matches("^[a-zA-Z0-9_]{5,20}$");
    }

    // Validate Email (Standard format)
    public static boolean isValidEmail(String email) {
        return email != null && Pattern.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", email);
    }

    // Validate Password (Min 8 characters, at least 1 uppercase, 1 lowercase, 1 number, 1 special character)
    public static boolean isValidPassword(String password) {
        return password != null && password.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$");
    }

    // Validate Phone Number (Must start with +855 and have 9-12 digits)
    public static boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && phoneNumber.matches("^\\+855[0-9]{8,9}$");
    }

    // Validate Role (Only "staff", "admin", or "chef")
    public static boolean isValidRole(String role) {
        return role != null && (role.equalsIgnoreCase("staff") ||
                role.equalsIgnoreCase("admin") ||
                role.equalsIgnoreCase("chef"));
    }
}
