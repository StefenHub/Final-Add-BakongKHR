package org.example.services;

import com.google.zxing.WriterException;
import org.example.utils.ColorFormatter;
import org.example.utils.ConsoleFormatter;

import java.util.Scanner;

public class PaymentService {

//    public static String QRCodePayment(double grandTotal) throws WriterException {
//        Scanner scanner = new Scanner(System.in);
//        String md5 = null;
//        long startTime = System.currentTimeMillis(); // Track start time
//        long timeout = 60 * 1000; // 60 seconds
//
//        System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText(
//                "💰 Total Payment Amount: $" + grandTotal, ColorFormatter.YELLOW + ColorFormatter.BOLD)));
//
//        QRCode qrCode = new QRCode();
//        md5 = qrCode.generateAndDisplayQRCode(grandTotal); // ✅ Generates QR Code and attempts to return MD5
//
//        while (md5 == null && (System.currentTimeMillis() - startTime) < timeout) {
//            try {
//                Thread.sleep(2000); // ✅ Wait 2 seconds before checking again
//                md5 = QRCode.getLatestMd5(); // ✅ Implement a method to check if MD5 is available
//
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//                System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText(
//                        "❌ Payment process interrupted.", ColorFormatter.RED + ColorFormatter.BOLD)));
//                return null;
//            }
//        }
//
//        if (md5 == null) {
//            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText(
//                    "⏳ Payment Timed Out! Please try again.", ColorFormatter.RED + ColorFormatter.BOLD)));
//            return null;
//        }
//
//        return md5;
//    }

    public static void QRCodePayment(double grandTotal) throws WriterException {
        Scanner scanner = new Scanner(System.in);
        String md5 = null;
        long startTime = System.currentTimeMillis();
        long timeout = 60 * 1000; // 60 seconds

        System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText(
                "💰 Total Payment Amount: $" + grandTotal, ColorFormatter.YELLOW + ColorFormatter.BOLD)));

        QRCode qrCode = new QRCode();
        md5 = qrCode.generateAndDisplayQRCode(grandTotal);

        while (md5 == null && (System.currentTimeMillis() - startTime) < timeout) {
            try {
                Thread.sleep(2000); // Wait 2 seconds before checking again
                md5 = QRCode.getLatestMd5();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText(
                        "❌ Payment process interrupted.", ColorFormatter.RED + ColorFormatter.BOLD)));
                return;
            }
        }

        if (md5 != null) {
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText(
                    "✅ Payment Successful!", ColorFormatter.GREEN + ColorFormatter.BOLD)));
        } else {
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText(
                    "⏳ Payment Timed Out! Would you like to retry? (yes/no)", ColorFormatter.RED + ColorFormatter.BOLD)));

            String userChoice = scanner.nextLine().trim().toLowerCase();
            if (userChoice.equals("yes")) {
                QRCodePayment(grandTotal); // Retry payment
            } else {
                System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText(
                        "❌ Payment Canceled. Your cart is still saved.", ColorFormatter.RED + ColorFormatter.BOLD)));
            }
        }
    }

    public static void CashPayment(){
        System.out.println("Please pay the cashier directly.");
    }



}