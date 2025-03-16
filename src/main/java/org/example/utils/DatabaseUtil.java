package org.example.utils;

        import java.sql.Connection;
        import java.sql.DriverManager;
        import java.sql.SQLException;
        import java.sql.PreparedStatement;
        import java.sql.ResultSet;

        public class DatabaseUtil {

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

            public static String getPasswordFromDatabase(String role) {
                String query = "SELECT password FROM users WHERE role = ? LIMIT 1"; // Ensuring only one result is fetched

                try (Connection conn = DatabaseUtil.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(query)) {

                    stmt.setString(1, role);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            return rs.getString("password");
                        } else {
                            ConsoleFormatter.printErrorMessage("❌ No user found with role: " + role);
                            return "";
                        }
                    }
                } catch (SQLException e) {
                    ConsoleFormatter.printErrorMessage("❌ Database error: " + e.getMessage());
                    return "";
                }
            }
        }