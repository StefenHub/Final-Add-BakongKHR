package org.example.utils;

import org.nocrala.tools.texttablefmt.*;

public class TableFormat {
    /**
     * Generates a formatted table with a title, centered in the console.
     *
     * @param title The title of the table.
     * @param rows  The table rows (single-column).
     * @return The formatted table as a String.
     */
    public static String createTable(String title, String... rows) {
        final String BLUE = "\u001B[34m";
        final String RESET = "\u001B[0m";
        final String BOLD_GREEN = "\033[1;92m";

        Table table = new Table(1, BorderStyle.UNICODE_ROUND_BOX_WIDE, ShownBorders.ALL);
        CellStyle centerStyle = new CellStyle(CellStyle.HorizontalAlign.CENTER);

        // Add Title (Centered)
        String formattedTitle = BOLD_GREEN + title + RESET;
        table.addCell(formattedTitle, centerStyle);

        // Add Rows
        for (String row : rows) {
            table.addCell(row, centerStyle);
        }

        return table.render();
    }
}
