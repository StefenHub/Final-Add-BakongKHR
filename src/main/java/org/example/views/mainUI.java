package org.example.views;

import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.ShownBorders;
import org.nocrala.tools.texttablefmt.Table;

public class mainUI {
    final String RESET = "\u001B[0m";
    final String BOLD_BLUE = "\033[1;34m"; // Blue title
    final String BRIGHT_WHITE = "\033[97m"; // White text for options
    final String WHITE_BORDER = "\033[97m"; // White border
    final String BLUE = "\u001B[34m"; // Blue for padding

    int consoleWidth = 180; // Console width
    int tableWidth = 100; // Wider table width
    int leftPadding = (consoleWidth - tableWidth) / 2;
    String padding = " ".repeat(leftPadding);

    // Centering "ROS CAMBODIA" ASCII Art
    String[] rosCambodia = {
            "██╗ ██████╗  ██████╗ ███████╗",
            "    ██╔══██╗██╔═══██╗██╔════╝",
            "██║ ██████╔╝██║   ██║███████╗",
            "██║ ██╔══██╗██║   ██║╚════██║",
            "██║ ██║  ██║╚██████╔╝███████║",
            "╚═╝ ╚═╝  ╚═╝ ╚═════╝ ╚══════╝"
    };

    public mainUI() {
        for (String line : rosCambodia) {
            int rosPadding = (consoleWidth - line.length()) / 2; // Calculate padding for centering
            System.out.println("\t\t\t\t\t\t\t"+ BOLD_BLUE + line + RESET);
        }

        // Create a table with a SINGLE wide column
        Table table = new Table(1, BorderStyle.UNICODE_BOX_DOUBLE_BORDER_WIDE, ShownBorders.ALL);
        table.setColumnWidth(0, 80, 90); // Explicitly setting column width wider

        CellStyle centerStyle = new CellStyle(CellStyle.HorizontalAlign.CENTER);
        CellStyle leftStyle = new CellStyle(CellStyle.HorizontalAlign.LEFT); // Left alignment for numbers

        // Title Row (Centered)
        table.addCell(BOLD_BLUE + "Restaurant Ordering System" + RESET, centerStyle);

        // Menu Options (Numbers Left-Aligned)
        String[] options = {
                "1.  Staff",
                "2.  Chef",
                "3.  Customer",
                "4.  Admin",
                "5.  Exit"
        };

        for (String option : options) {
            table.addCell(BOLD_BLUE + option.trim() + RESET, leftStyle); // Left-align & reset colors properly
        }

        // Print the Table with WHITE Borders
        String[] tableLines = table.render().split("\n");
        for (String line : tableLines) {
            System.out.println(WHITE_BORDER + line + RESET);
        }
    }


    public static void asciiUI() {
        new mainUI();
    }
}