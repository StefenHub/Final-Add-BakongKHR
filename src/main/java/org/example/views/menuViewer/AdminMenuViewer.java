package org.example.views.menuViewer;

import org.example.utils.ConsoleFormatter;
import org.example.utils.DatabaseConnection;
import org.example.utils.PaginationFormatter;
import org.nocrala.tools.texttablefmt.*;

import java.sql.*;

public class AdminMenuViewer {
    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";

    public static void viewMenuItemsAdmin() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            int totalItems = getTotalItemCount(conn);
            if (totalItems == 0) {
                System.out.println(RED + "📭 No menu items available." + RESET);
                return;
            }

            PaginationFormatter paginator = new PaginationFormatter(totalItems, 5); // Default items per page = 5

            while (true) {
                displayMenuItems(conn, paginator.getCurrentPage(), paginator.getItemsPerPage());

                boolean shouldContinue = paginator.handlePagination();
                if (!shouldContinue) break; // Exit if user chooses to quit
            }
        } catch (SQLException e) {
            System.out.println(ConsoleFormatter.centerText(RED + "⚠️ Database connection error: " + e.getMessage() + RESET));
        }
    }

    private static int getTotalItemCount(Connection conn) throws SQLException {
        String countSql = "SELECT COUNT(*) FROM menuitemsadmin";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countSql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private static void displayMenuItems(Connection conn, int page, int itemsPerPage) {
        int offset = (page - 1) * itemsPerPage;
        String sql = "SELECT mi.item_id, mi.name, mi.description, c.name AS category_name, mi.size, mi.base_price, mi.sell_price, mi.discount " +
                "FROM menuitemsadmin mi " +
                "JOIN categories c ON mi.category_id = c.id " +
                "ORDER BY c.name, mi.name LIMIT ? OFFSET ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, itemsPerPage);
            stmt.setInt(2, offset);
            ResultSet rs = stmt.executeQuery();

            if (!rs.isBeforeFirst()) {
                System.out.println(ConsoleFormatter.centerText(RED + "🚫 No items found." + RESET));
                return;
            }

            String currentCategory = "";
            Table table = null;
            int count = offset + 1; // Start numbering from the correct offset

            while (rs.next()) {
                String category = rs.getString("category_name");
                if (!category.equals(currentCategory)) {
                    if (table != null) {
                        ConsoleFormatter.printCenteredTable(table.render());
                    }
                    currentCategory = category;
                    ConsoleFormatter.printCategoryHeader(currentCategory);
                    table = createTable();
                }

                table.addCell(String.valueOf(count++), new CellStyle(CellStyle.HorizontalAlign.CENTER)); // Continuous numbering
                table.addCell(rs.getString("name"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(trimDescription(rs.getString("description")), new CellStyle(CellStyle.HorizontalAlign.LEFT));
                table.addCell(rs.getString("size"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(String.format("$%.2f", rs.getDouble("base_price")), new CellStyle(CellStyle.HorizontalAlign.RIGHT));
                table.addCell(String.format("$%.2f", rs.getDouble("sell_price")), new CellStyle(CellStyle.HorizontalAlign.RIGHT));
                table.addCell(String.format("$%.2f", rs.getDouble("discount")), new CellStyle(CellStyle.HorizontalAlign.RIGHT));
            }

            ConsoleFormatter.printCenteredTable(table.render());
        } catch (SQLException e) {
            System.out.println(ConsoleFormatter.centerText(RED + "⚠️ Error retrieving menu items: " + e.getMessage() + RESET));
        }
    }


    private static Table createTable() {
        Table table = new Table(7, BorderStyle.UNICODE_ROUND_BOX_WIDE, ShownBorders.ALL);
        table.addCell("No.", new CellStyle(CellStyle.HorizontalAlign.CENTER));
        table.addCell("Name", new CellStyle(CellStyle.HorizontalAlign.CENTER));
        table.addCell("Description", new CellStyle(CellStyle.HorizontalAlign.CENTER));
        table.addCell("Size", new CellStyle(CellStyle.HorizontalAlign.CENTER));
        table.addCell("Base Price", new CellStyle(CellStyle.HorizontalAlign.CENTER));
        table.addCell("Sell Price", new CellStyle(CellStyle.HorizontalAlign.CENTER));
        table.addCell("Discount", new CellStyle(CellStyle.HorizontalAlign.CENTER));
        return table;
    }

    private static String trimDescription(String description) {
        int maxLength = 30;
        return description.length() > maxLength ? description.substring(0, maxLength - 3) + "..." : description;
    }

    public static void main(String[] args) {
        viewMenuItemsAdmin();
    }
}
