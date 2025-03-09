package org.example.views.menuViewer;

import org.example.utils.ConsoleFormatter;
import org.example.utils.DatabaseConnection;
import org.example.utils.PaginationFormatter;
import org.nocrala.tools.texttablefmt.*;

import java.sql.*;

public class CustomerMenuViewer {
    private static final int ITEMS_PER_PAGE = 10;

    public static void viewMenuItemsCustomer() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            int totalPages = getTotalPages(conn);
            if (totalPages == 0) {
                ConsoleFormatter.printErrorMessage("📭 No menu items available.");
                return;
            }

            PaginationFormatter paginator = new PaginationFormatter(totalPages);

            while (true) {
                displayMenuItems(conn, paginator.getCurrentPage());

                if (!paginator.handlePagination()) {
                    break;
                }
            }
        } catch (SQLException e) {
            ConsoleFormatter.printErrorMessage("⚠️ Database connection error: " + e.getMessage());
        }
    }

    private static int getTotalPages(Connection conn) throws SQLException {
        String countSql = "SELECT COUNT(*) FROM menuitemsadmin";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countSql)) {
            if (rs.next()) {
                int totalItems = rs.getInt(1);
                return (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);
            }
        }
        return 0;
    }

    private static void displayMenuItems(Connection conn, int page) {
        int offset = (page - 1) * ITEMS_PER_PAGE;
        String sql = "SELECT item_id, name, description, category, size, sell_price, discount " +
                "FROM menuitemsadmin ORDER BY category, name LIMIT ? OFFSET ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, ITEMS_PER_PAGE);
            stmt.setInt(2, offset);
            ResultSet rs = stmt.executeQuery();

            if (!rs.isBeforeFirst()) {
                ConsoleFormatter.printErrorMessage("🚫 No items found.");
                return;
            }

            String currentCategory = "";
            Table table = null;
            int count = 1;

            while (rs.next()) {
                String category = rs.getString("category");
                if (!category.equals(currentCategory)) {
                    if (table != null) {
                        ConsoleFormatter.printCenteredTable(table.render());
                    }
                    currentCategory = category;
                    ConsoleFormatter.printCategoryHeader(currentCategory);
                    table = createTable();
                    count = 1;
                }

                table.addCell(String.valueOf(count++), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(rs.getString("name"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(trimDescription(rs.getString("description")), new CellStyle(CellStyle.HorizontalAlign.LEFT));
                table.addCell(rs.getString("size"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(String.format("$%.2f", rs.getDouble("sell_price")), new CellStyle(CellStyle.HorizontalAlign.RIGHT));
                table.addCell(String.format("$%.2f", rs.getDouble("discount")), new CellStyle(CellStyle.HorizontalAlign.RIGHT));
            }

            ConsoleFormatter.printCenteredTable(table.render());
        } catch (SQLException e) {
            ConsoleFormatter.printErrorMessage("⚠️ Error retrieving menu items: " + e.getMessage());
        }
    }

    private static Table createTable() {
        Table table = new Table(6, BorderStyle.UNICODE_BOX_WIDE, ShownBorders.ALL);
        table.addCell("No.", new CellStyle(CellStyle.HorizontalAlign.CENTER));
        table.addCell("Name", new CellStyle(CellStyle.HorizontalAlign.CENTER));
        table.addCell("Description", new CellStyle(CellStyle.HorizontalAlign.CENTER));
        table.addCell("Size", new CellStyle(CellStyle.HorizontalAlign.CENTER));
        table.addCell("Sell Price", new CellStyle(CellStyle.HorizontalAlign.CENTER));
        table.addCell("Discount", new CellStyle(CellStyle.HorizontalAlign.CENTER));
        return table;
    }

    private static String trimDescription(String description) {
        int maxLength = 30;
        if (description.length() > maxLength) {
            return description.substring(0, maxLength - 3) + "...";
        }
        return description;
    }

    // test
    public static void main(String[] args) {
        viewMenuItemsCustomer();
    }
}