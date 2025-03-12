package org.example.services;

import org.example.utils.ColorFormatter;
import org.example.utils.ConsoleFormatter;

public class PaymentService {
    public void processPaymentStaff(int paymentMethod) {
        if (paymentMethod == 1) {
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("Processing payment through KHQR...", ColorFormatter.GREEN + ColorFormatter.BOLD)));
            QRCodePayment();
        } else if (paymentMethod == 2) {
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("Processing payment through Cash...", ColorFormatter.GREEN + ColorFormatter.BOLD)));
        }
        System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("Payment processed successfully!", ColorFormatter.GREEN + ColorFormatter.BOLD)));
    }

    public boolean processPaymentCustomer() {
        System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("Processing payment through KHQR...", ColorFormatter.GREEN + ColorFormatter.BOLD)));
        QRCodePayment();
        System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("Payment processed successfully!", ColorFormatter.GREEN + ColorFormatter.BOLD)));
        return false;
    }

    public void QRCodePayment() {
        QRCodePayment();
    }

    public void cashPayment() {
        System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("Payment processed successfully!", ColorFormatter.GREEN + ColorFormatter.BOLD)));
    }
}