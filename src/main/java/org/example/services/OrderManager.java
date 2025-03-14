package org.example.services;

import org.example.utils.ConsoleFormatter;
import org.example.utils.DatabaseConnection;
import org.example.utils.ColorFormatter;
import org.example.utils.PaginationFormatter;
import org.nocrala.tools.texttablefmt.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OrderManager {

    public static void viewAllCustomerOrders() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            int totalItems = getTotalOrderItems(conn);
            PaginationFormatter pagination = new PaginationFormatter(totalItems, 10);

            while (true) {
                displayAllOrders(conn, pagination.getCurrentPage(), pagination.getItemsPerPage());
                if (!pagination.handlePagination()) {
                    break;
                }
            }
        } catch (SQLException e) {
            ConsoleFormatter.printErrorMessage("❌ Error retrieving customer orders: " + e.getMessage());
        }
    }

    private static int getTotalOrderItems(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM order_items";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private static void displayAllOrders(Connection conn, int currentPage, int itemsPerPage) throws SQLException {
        int offset = (currentPage - 1) * itemsPerPage;
        String sql = "SELECT oi.order_item_id, oi.order_id, oi.description, oi.quantity, oi.sell_price " +
                "FROM order_items oi LIMIT ? OFFSET ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, itemsPerPage);
            stmt.setInt(2, offset);
            ResultSet rs = stmt.executeQuery();

            if (!rs.isBeforeFirst()) {
                System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("🚫 No orders found.", ColorFormatter.RED)));
                return;
            }

            Table table = createTable();
            int count = offset + 1;

            while (rs.next()) {
                table.addCell(String.valueOf(count++), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(String.valueOf(rs.getInt("order_id")), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(rs.getString("description"), new CellStyle(CellStyle.HorizontalAlign.LEFT));
                table.addCell(String.valueOf(rs.getInt("quantity")), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(String.format("$%.2f", rs.getDouble("sell_price")), new CellStyle(CellStyle.HorizontalAlign.RIGHT));
            }

            ConsoleFormatter.printCenteredTable(table.render());
        }
    }

    private static Table createTable() {
        Table table = new Table(5, BorderStyle.UNICODE_ROUND_BOX_WIDE, ShownBorders.ALL);
        table.addCell("No.", new CellStyle(CellStyle.HorizontalAlign.CENTER));
        table.addCell("Order ID", new CellStyle(CellStyle.HorizontalAlign.CENTER));
        table.addCell("Description", new CellStyle(CellStyle.HorizontalAlign.CENTER));
        table.addCell("Quantity", new CellStyle(CellStyle.HorizontalAlign.CENTER));
        table.addCell("Sell Price", new CellStyle(CellStyle.HorizontalAlign.CENTER));
        return table;
    }

    // test method
    public static void main(String[] args) {
        viewAllCustomerOrders();
    }
}