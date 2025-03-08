package org.example.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleFormatter {

    public static int getTerminalWidth() {
        try {
            Process process = new ProcessBuilder("tput", "cols").start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String widthStr = reader.readLine();
            return widthStr != null ? Integer.parseInt(widthStr) : 180;
        } catch (IOException | NumberFormatException e) {
            return 180; // Default width if detection fails
        }
    }

    public static String centerText(String text) {
        int terminalWidth = getTerminalWidth();
        int padding = Math.max((terminalWidth - text.length()) / 2, 0);
        return " ".repeat(padding) + text;
    }

    public static void printCenteredTable(String renderedTable) {
        int terminalWidth = getTerminalWidth();

        // Calculate maximum width of the table
        int maxTableWidth = 0;
        for (String line : renderedTable.split("\n")) {
            maxTableWidth = Math.max(maxTableWidth, line.length());
        }

        // Calculate padding based on max table width
        int padding = Math.max((terminalWidth - maxTableWidth) / 2, 0);

        for (String line : renderedTable.split("\n")) {
            System.out.println(" ".repeat(padding) + line);
        }
    }


    public static void printCategoryHeader(String category) {
        System.out.println("\n" + centerText("📌 Category: " + category));
    }

    public static void printSeparator() {
        System.out.println(centerText("═".repeat(50))); // Creates a visual separator
    }
}
