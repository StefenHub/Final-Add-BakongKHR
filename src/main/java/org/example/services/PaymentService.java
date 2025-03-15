package org.example.services;

import com.google.zxing.WriterException;
import org.example.utils.ColorFormatter;
import org.example.utils.ConsoleFormatter;

import java.util.Scanner;

public class PaymentService {

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
                md5 = qrCode.getMD5(); // ✅ Attempts to get MD5 again

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText(
                        "❌ Payment process interrupted.", ColorFormatter.RED + ColorFormatter.BOLD)));
                return md5;
            }
        }

        if (md5 == null) {
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText(
                    "⏳ Payment Timed Out! Please try again.", ColorFormatter.RED + ColorFormatter.BOLD)));
        } else {
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText(
                    "✅ Payment successful. Thank you for your order!", ColorFormatter.GREEN + ColorFormatter.BOLD)));
        }
        return md5;
    }

    // test QR Code
    public static void main(String[] args) throws WriterException {
        double grandTotal = 0.01;
        QRCodePayment(grandTotal);
    }
}
