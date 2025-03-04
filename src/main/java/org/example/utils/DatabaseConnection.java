package org.example.utils;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    public static Connection connect() {
        Connection conn = null;
        try {
            // Load the PostgreSQL driver
            Class.forName("org.postgresql.Driver");
            // Establish the connection
            conn = DriverManager.getConnection("jdbc:postgresql://202.178.125.77:3333/ros_db", "postgres", "1234567890");
        } catch (ClassNotFoundException e) {
            System.out.println("❌ PostgreSQL JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("❌ Database connection failed: " + e.getMessage());
        }
        return conn;
    }

    public static Connection getConnection() {
        return connect();
    }
}
