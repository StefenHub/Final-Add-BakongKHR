package org.example.views.menuViewer;

import org.example.utils.ConsoleFormatter;
import org.example.utils.DatabaseConnection;
import org.example.utils.PaginationFormatter;
import org.nocrala.tools.texttablefmt.*;

import java.sql.*;

public class AdminMenuViewer {
    private static final int ITEMS_PER_PAGE = 10;

    public static void viewMenuItemsAdmin() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            int totalPages = getTotalPages(conn);
            if (totalPages == 0) {
                System.out.println("\n" + ConsoleFormatter.centerText("📭 No menu items available."));
                return;
            }

            PaginationFormatter paginator = new PaginationFormatter(totalPages);

            while (true) {
                displayMenuItems(conn, paginator.getCurrentPage());

                if (!paginator.handlePagination()) {
                    break; // Exit loop when user selects 'E'
                }
            }
        } catch (SQLException e) {
            System.out.println(ConsoleFormatter.centerText("⚠️ Database connection error: " + e.getMessage()));
        }
    }

    private static void displayMenuItems(Connection conn, int page) {
        int offset = (page - 1) * ITEMS_PER_PAGE;
        String sql = "SELECT * FROM menuitemsadmin ORDER BY category, name LIMIT ? OFFSET ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, ITEMS_PER_PAGE);
            stmt.setInt(2, offset);
            ResultSet rs = stmt.executeQuery();

            if (!rs.isBeforeFirst()) {
                System.out.println("\n" + ConsoleFormatter.centerText("🚫 No items found."));
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

            if (table != null) {
                ConsoleFormatter.printCenteredTable(table.render());
            }
        } catch (SQLException e) {
            System.out.println(ConsoleFormatter.centerText("⚠️ Error retrieving menu items: " + e.getMessage()));
        }
    }

    private static Table createTable() {
        Table table = new Table(6, BorderStyle.UNICODE_ROUND_BOX_WIDE, ShownBorders.ALL);
        CellStyle centerStyle = new CellStyle(CellStyle.HorizontalAlign.CENTER);

        table.addCell(" No. ", centerStyle);
        table.addCell(" Name ", centerStyle);
        table.addCell(" Description ", centerStyle);
        table.addCell(" Size ", centerStyle);
        table.addCell(" Sell Price ", centerStyle);
        table.addCell(" Discount ", centerStyle);

        return table;
    }

    private static String trimDescription(String description) {
        return (description.length() > 50) ? description.substring(0, 47) + "..." : description;
    }

    private static int getTotalPages(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM menuitemsadmin";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int totalItems = rs.getInt(1);
                return (totalItems == 0) ? 0 : (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);
            }
        }
        return 1;
    }

    public static void main(String[] args) {
        viewMenuItemsAdmin();
    }
}
