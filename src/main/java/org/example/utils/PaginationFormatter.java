package org.example.utils;

import java.util.Scanner;

public class PaginationFormatter {
    private int currentPage;
    private final int totalPages;
    private final Scanner scanner;

    public PaginationFormatter(int totalPages) {
        this.currentPage = 1;
        this.totalPages = totalPages;
        this.scanner = new Scanner(System.in);
    }

    public void printPaginationInfo() {
        String pageInfo = ColorFormatter.colorText("📄 Page " + currentPage + " of " + totalPages, ColorFormatter.CYAN);
        String paginationControls = ColorFormatter.colorText("[N] Next", ColorFormatter.GREEN) + "  |  " +
                ColorFormatter.colorText("[P] Previous", ColorFormatter.YELLOW) + "  |  " +
                ColorFormatter.colorText("[E] Exit", ColorFormatter.RED);
        String chooseOption = ColorFormatter.colorText("👉 Choose an option: ", ColorFormatter.PURPLE);

        System.out.println("\n" + ConsoleFormatter.centerText(pageInfo));
        System.out.println(ConsoleFormatter.centerText(paginationControls));
        System.out.print(ConsoleFormatter.centerText(chooseOption));
    }

    public String getUserChoice() {
        return scanner.next().trim().toLowerCase();
    }

    public boolean handlePagination() {
        while (true) {
            printPaginationInfo();
            String choice = getUserChoice();
            switch (choice) {
                case "n":
                    if (currentPage < totalPages) {
                        currentPage++;
                        return true;
                    } else {
                        ConsoleFormatter.printErrorMessage("❌ Already on the last page.");
                    }
                    break;
                case "p":
                    if (currentPage > 1) {
                        currentPage--;
                        return true;
                    } else {
                        ConsoleFormatter.printErrorMessage("❌ Already on the first page.");
                    }
                    break;
                case "e":
                    ConsoleFormatter.printSuccessMessage("🚪 Exiting menu view.");
                    return false;
                default:
                    ConsoleFormatter.printErrorMessage("⚠️ Invalid input! Try again.");
            }
        }
    }

    public int getCurrentPage() {
        return currentPage;
    }
}
