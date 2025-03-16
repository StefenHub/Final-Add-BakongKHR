package org.example.services;

import com.google.zxing.WriterException;
import org.example.utils.ColorFormatter;
import org.example.utils.ConsoleFormatter;

import javax.swing.*;
import java.util.Scanner;

import static org.example.services.QRCode.validateMd5;

public class PaymentService {


    public void processQRPayment(double grandTotal) throws WriterException {
        Scanner scanner = new Scanner(System.in);
        boolean paymentCompleted = false;

        while (!paymentCompleted) {
            long startTime = System.currentTimeMillis();
            long timeout = 60 * 1000; // 60 seconds
            String md5 = null;

            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText(
                    "💰 Total Payment Amount: $" + grandTotal, ColorFormatter.YELLOW + ColorFormatter.BOLD)));

            QRCode qrCode = new QRCode();

            try {
                md5 = qrCode.generateAndDisplayQRCode(grandTotal);
            } catch (Exception e) {
                System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText(
                        "⚠ Error generating QR Code. Please try again later.", ColorFormatter.RED + ColorFormatter.BOLD)));
                return;
            }

            // Wait for the QR code payment to be confirmed within the timeout period
            System.out.println("DEBUG: Waiting for payment confirmation...");
            while ((System.currentTimeMillis() - startTime) < timeout) {
                try {
                    Thread.sleep(2000); // Check every 2 seconds

                    if (QRCode.validateMd5(md5)) {
                        System.out.println("✅ Payment Successful!");
                        paymentCompleted = true;
                        break;
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText(
                            "❌ Payment process interrupted.", ColorFormatter.RED + ColorFormatter.BOLD)));
                    return;
                }
            }

            // If payment is still not confirmed after timeout
            if (!paymentCompleted) {
                System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText(
                        "⏳ Payment Timed Out! Would you like to retry? (yes/no)", ColorFormatter.RED + ColorFormatter.BOLD)));

                String userChoice = scanner.nextLine().trim().toLowerCase();
                if (!userChoice.equals("yes")) {
                    System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText(
                            "❌ Payment Canceled. Your cart is still saved.", ColorFormatter.RED + ColorFormatter.BOLD)));
                    return;
                }
            }
        }
    }





    public void processCashPayment(double grandTotalUSD, boolean isStaff) {
        Scanner scanner = new Scanner(System.in);
        double exchangeRate = 4100.0; // Example: 1 USD = 4100 KHR
        double totalPaid = 0.0;
        double remainingAmount = grandTotalUSD;
        String currencySymbol = "$";
        boolean isPayingInKHR = false;

        // Ask user to choose payment currency
        displayMessage("💵 Choose payment currency: \n1. USD ($)\n2. KHR (៛)", ColorFormatter.YELLOW);
        int currencyChoice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        if (currencyChoice == 2) {
            isPayingInKHR = true;
            remainingAmount *= exchangeRate; // Convert to KHR
            currencySymbol = "៛"; // Change currency symbol
        }

        displayMessage("💰 Total Amount Due: " + currencySymbol + String.format("%.2f", remainingAmount), ColorFormatter.CYAN);

        while (remainingAmount > 0) {
            try {
                displayMessage("\n💵 Enter cash received from customer in " + currencySymbol + ": ", ColorFormatter.YELLOW);
                double cashReceived = Double.parseDouble(scanner.nextLine().trim());

                if (cashReceived <= 0) {
                    displayMessage("❌ Invalid amount. Please enter a positive number.", ColorFormatter.RED);
                    continue;
                }

                totalPaid += cashReceived;
                remainingAmount -= cashReceived;

                if (remainingAmount > 0) {
                    displayMessage("⚠️ Partial payment received. Remaining amount: " + currencySymbol + String.format("%.2f", remainingAmount), ColorFormatter.YELLOW);
                } else {
                    double change = Math.abs(remainingAmount); // Convert remaining negative to positive (change to return)

                    displayMessage("\n✅ Payment Successful!", ColorFormatter.GREEN);
                    displayMessage("💵 Total Paid: " + currencySymbol + String.format("%.2f", totalPaid), ColorFormatter.BLUE);

                    if (change > 0) {
                        displayMessage("💰 Change to return to customer: " + currencySymbol + String.format("%.2f", change), ColorFormatter.CYAN);
                        displayMessage("🛑 Staff must return " + currencySymbol + String.format("%.2f", change) + " in correct denominations.", ColorFormatter.RED);
                    }

                    displayMessage("\n🧾 Transaction Complete. Thank you for your purchase!", ColorFormatter.GREEN);
                    break; // Exit loop as payment is completed
                }

            } catch (NumberFormatException e) {
                displayMessage("❌ Invalid input. Please enter a valid numeric amount.", ColorFormatter.RED);
            }
        }
    }

    private void displayMessage(String s, String yellow) {
        System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText(s, yellow + ColorFormatter.BOLD)));
    }

    // test processQRPayment method
    public static void main(String[] args) throws WriterException {
        PaymentService paymentService = new PaymentService();
        paymentService.processQRPayment(0.01);
    }
}