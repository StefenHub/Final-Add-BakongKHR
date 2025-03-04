package org.example.models;

public class User {
    private String username;
    private String password;
    private String role;
    private String name;
    private String phone;



    public User(String username, String password, String role, String name, String phone) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.name = name;
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }
}
