package org.example.services;

import com.google.zxing.WriterException;
import org.example.utils.ColorFormatter;
import org.example.utils.ConsoleFormatter;

import java.util.Scanner;

public class PaymentService {
    public boolean CustomerPayment(double grandTotal) {
        try {
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText(
                    "Processing payment through KHQR...", ColorFormatter.GREEN + ColorFormatter.BOLD)));

            String md5 = QRCodePayment(grandTotal);

            if (md5 == null) {
                System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText(
                        "❌ Payment process was canceled.", ColorFormatter.RED + ColorFormatter.BOLD)));
                return false;
            }

            // ✅ Wait for payment validation
            QRCode qrCode = new QRCode();
            boolean isPaid = qrCode.validateMd5(md5);

            if (isPaid) {
                System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText(
                        "✅ Payment processed successfully!", ColorFormatter.GREEN + ColorFormatter.BOLD)));
                return true;
            } else {
                System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText(
                        "❌ Payment verification failed. Please try again.", ColorFormatter.RED + ColorFormatter.BOLD)));
                return false;
            }

        } catch (Exception e) {
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText(
                    "❌ Payment failed. Please try again.", ColorFormatter.RED + ColorFormatter.BOLD)));
            e.printStackTrace();
            return false;
        }
    }

    public static String QRCodePayment(double grandTotal) throws WriterException {
        Scanner scanner = new Scanner(System.in);
        String md5 = null;
        long startTime = System.currentTimeMillis(); // Track start time
        long timeout = 60 * 1000; // 60 seconds

        System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText(
                "💰 Total Payment Amount: $" + grandTotal, ColorFormatter.YELLOW + ColorFormatter.BOLD)));

        QRCode qrCode = new QRCode();
        md5 = qrCode.generateAndDisplayQRCode(grandTotal); // ✅ Generates QR Code and attempts to return MD5

        while (md5 == null && (System.currentTimeMillis() - startTime) < timeout) {
            try {
                Thread.sleep(2000); // ✅ Wait 2 seconds before checking again
                md5 = QRCode.getLatestMd5(); // ✅ Implement a method to check if MD5 is available

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText(
                        "❌ Payment process interrupted.", ColorFormatter.RED + ColorFormatter.BOLD)));
                return null;
            }
        }

        if (md5 == null) {
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText(
                    "⏳ Payment Timed Out! Please try again.", ColorFormatter.RED + ColorFormatter.BOLD)));
            return null;
        }

        return md5;
    }

    // test QR Code
    public static void main(String[] args) throws WriterException {
        double grandTotal = 0.01; // Example grand total
        QRCodePayment(grandTotal);
    }


}