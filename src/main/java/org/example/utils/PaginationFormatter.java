package org.example.utils;

import java.util.Scanner;

public class PaginationFormatter {
    private int currentPage;
    private int totalItems;
    private int itemsPerPage;
    private Scanner scanner;

    public PaginationFormatter(int totalItems, int defaultItemsPerPage) {
        this.totalItems = totalItems;
        this.itemsPerPage = defaultItemsPerPage;
        this.currentPage = 1;
        this.scanner = new Scanner(System.in);
    }

    public int getTotalPages() {
        return (int) Math.ceil((double) totalItems / itemsPerPage);
    }

    public void printPaginationInfo() {
        int totalPages = getTotalPages();
        String pageInfo = ColorFormatter.colorText("📄 Page " + currentPage + " of " + totalPages, ColorFormatter.GREEN);
        System.out.print(ColorFormatter.colorText("You're on Pagination View", ColorFormatter.BLUE));
        String paginationControls = ColorFormatter.colorText("[N] Next", ColorFormatter.GREEN) + "  |  " +
                ColorFormatter.colorText("[P] Previous", ColorFormatter.GREEN) + "  |  " +
                ColorFormatter.colorText("[C] Change Items per Page", ColorFormatter.GREEN) + "  |  " +
                ColorFormatter.colorText("\n[E] Exit from Pagination View", ColorFormatter.RED);
        String chooseOption = ColorFormatter.colorText("👉 Choose an option: ", ColorFormatter.PURPLE);

        System.out.println(ConsoleFormatter.centerText(pageInfo));
        System.out.println(ConsoleFormatter.centerText(paginationControls));
        System.out.print(ConsoleFormatter.centerText(chooseOption));
    }

    public String getUserChoice() {
        return scanner.next().trim().toLowerCase();
    }

    public void changeItemsPerPage() {
        while (true) {
            System.out.print(ColorFormatter.colorText("🔢 Enter items per page: ", ColorFormatter.BLUE));
            String input = scanner.next().trim();
            try {
                int newItemsPerPage = Integer.parseInt(input);
                if (newItemsPerPage > 0) {
                    itemsPerPage = newItemsPerPage;
                    currentPage = 1;
                    ConsoleFormatter.printSuccessMessage("✅ Items per page updated to " + newItemsPerPage);
                    break;
                } else {
                    ConsoleFormatter.printErrorMessage("❌ Must be a positive number!");
                }
            } catch (NumberFormatException e) {
                ConsoleFormatter.printErrorMessage("⚠️ Invalid input! Please enter a valid number.");
            }
        }
    }

    public boolean handlePagination() {
        while (true) {
            printPaginationInfo();
            String choice = getUserChoice();
            int totalPages = getTotalPages();

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
                case "c":
                    changeItemsPerPage();
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

    public int getItemsPerPage() {
        return itemsPerPage;
    }

    public void nextPage() {
        currentPage++;
    }

    public void previousPage() {
        currentPage--;
    }

    public void setItemsPerPage(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
        currentPage = 1;
    }

    public void reset() {
        currentPage = 1;
        itemsPerPage = 10;
    }

    public void updateTotalPages(int totalPages, int itemsPerPage) {
        this.totalItems = totalPages * itemsPerPage;
    }
}