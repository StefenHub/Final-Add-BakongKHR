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
        String pageInfo = "📄 Page " + currentPage + " of " + totalPages;
        String paginationControls = "[N] Next  |  [P] Previous  |  [E] Exit";
        String chooseOption = "👉 Choose an option: ";

        // Get the longest string for alignment reference
        int maxLength = Math.max(pageInfo.length(), Math.max(paginationControls.length(), chooseOption.length()));

        System.out.println("\n" + ConsoleFormatter.centerText(pageInfo));
        System.out.println(ConsoleFormatter.centerText(paginationControls));
        System.out.print(ConsoleFormatter.centerText(chooseOption)); // Ensures perfect alignment
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
                        System.out.println(ConsoleFormatter.centerText("❌ Already on the last page."));
                    }
                    break;
                case "p":
                    if (currentPage > 1) {
                        currentPage--;
                        return true;
                    } else {
                        System.out.println(ConsoleFormatter.centerText("❌ Already on the first page."));
                    }
                    break;
                case "e":
                    System.out.println(ConsoleFormatter.centerText("🚪 Exiting menu view."));
                    return false;
                default:
                    System.out.println(ConsoleFormatter.centerText("⚠️ Invalid input! Try again."));
            }
        }
    }

    public int getCurrentPage() {
        return currentPage;
    }
}
