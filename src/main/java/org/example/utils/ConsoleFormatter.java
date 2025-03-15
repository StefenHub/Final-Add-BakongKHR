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
        for (String line : renderedTable.split("\n")) {
            int padding = Math.max((terminalWidth - line.length()) / 2, 0);
            System.out.println(" ".repeat(padding) + line);
        }
    }

    public static void printCategoryHeader(String category) {
        String coloredCategory = ColorFormatter.colorText("📌 Category: " + category, ColorFormatter.YELLOW);
        System.out.println("\n" + centerText(coloredCategory));
    }

    public static void printSeparator() {
        System.out.println(centerText(ColorFormatter.colorText("═".repeat(50), ColorFormatter.BLUE)));
    }

    public static void printErrorMessage(String message) {
        System.out.println(centerText(ColorFormatter.colorText("⚠️ " + message, ColorFormatter.RED)));
    }

    public static void printSuccessMessage(String message) {
        System.out.println(centerText(ColorFormatter.colorText("✅ " + message, ColorFormatter.GREEN)));
    }

    public static String colorText(String s, String yellow) {
        return null;
    }
}
