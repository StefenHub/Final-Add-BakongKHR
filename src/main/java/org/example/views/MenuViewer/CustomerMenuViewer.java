package org.example.views.MenuViewer;

import org.example.utils.DatabaseConnection;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.ShownBorders;
import org.nocrala.tools.texttablefmt.Table;

import java.sql.*;
import java.util.Scanner;

public class CustomerMenuViewer {
    private static final int ITEMS_PER_PAGE = 25; // Number of items per page

    public static void viewMenuItemsCustomer() {
        try (Connection conn = DatabaseConnection.getConnection();
             Scanner scanner = new Scanner(System.in)) {

            int currentPage = 1;
            int totalPages = getTotalPages(conn);

            while (true) {
                displayMenuItems(conn, currentPage);

                // Pagination controls
                System.out.println("\n\t📄 Page " + currentPage + " of " + totalPages);
                System.out.println("\t[➡️] Next  |  [⬅️] Previous  |  [❌] Exit");
                System.out.print("\t👉 Choose an option: ");
                String choice = scanner.next().toLowerCase();

                if (choice.equals("n") && currentPage < totalPages) {
                    currentPage++;
                } else if (choice.equals("p") && currentPage > 1) {
                    currentPage--;
                } else if (choice.equals("e")) {
                    System.out.println("\tExiting menu view.");
                    break;
                } else {
                    System.out.println("\tInvalid input! Try again.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void displayMenuItems(Connection conn, int page) {
        int offset = (page - 1) * ITEMS_PER_PAGE;
        String sql = "SELECT item_id, name, description, category, size, sell_price, discount " +
                "FROM menuitemsadmin ORDER BY category, name LIMIT ? OFFSET ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, ITEMS_PER_PAGE);
            stmt.setInt(2, offset);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n\t--------- MENU ITEMS ---------");
            String currentCategory = "";
            Table table = null;
            int count = 1;

            while (rs.next()) {
                String category = rs.getString("category");
                if (!category.equals(currentCategory)) {
                    if (table != null) {
                        System.out.println(table.render());
                    }
                    currentCategory = category;
                    System.out.println("\n\t--- " + currentCategory + " ---");
                    table = new Table(7, BorderStyle.UNICODE_ROUND_BOX_WIDE, ShownBorders.ALL);
                    table.addCell("No.", new CellStyle(CellStyle.HorizontalAlign.CENTER));
                    table.addCell("Name", new CellStyle(CellStyle.HorizontalAlign.CENTER));
                    table.addCell("Item ID", new CellStyle(CellStyle.HorizontalAlign.CENTER));
                    table.addCell("Description", new CellStyle(CellStyle.HorizontalAlign.CENTER));
                    table.addCell("Size", new CellStyle(CellStyle.HorizontalAlign.CENTER));
                    table.addCell("Sell Price", new CellStyle(CellStyle.HorizontalAlign.CENTER));
                    table.addCell("Discount", new CellStyle(CellStyle.HorizontalAlign.CENTER));
                    count = 1; // Reset count for new category
                }
                table.addCell(String.valueOf(count++), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(rs.getString("name"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(String.valueOf(rs.getInt("item_id")), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(rs.getString("description"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(rs.getString("size"), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(String.format("$%.2f", rs.getDouble("sell_price")), new CellStyle(CellStyle.HorizontalAlign.CENTER));
                table.addCell(String.format("$%.2f", rs.getDouble("discount")), new CellStyle(CellStyle.HorizontalAlign.CENTER));
            }
            if (table != null) {
                System.out.println(table.render());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static int getTotalPages(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM menuitemsadmin";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int totalItems = rs.getInt(1);
                return (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);
            }
        }
        return 1;
    }

    // test class
    public static void main(String[] args) {
        viewMenuItemsCustomer();
    }
}
