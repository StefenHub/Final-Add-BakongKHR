package org.example.services;

public class PaymentService {
    public void processPaymentStaff(int paymentMethod) {
        if (paymentMethod == 1) {
            System.out.println("\tProcessing payment through KHQR...");
            QRCodePayment();
        } else if (paymentMethod == 2) {
            System.out.println("\tProcessing payment through Cash...");
        }
        System.out.println("\tPayment processed successfully!");
    }

    public void processPaymentCustomer() {
        System.out.println("\tProcessing payment through KHQR...");
        QRCodePayment();
        System.out.println("\tPayment processed successfully!");
    }

    public void QRCodePayment() {
        QRCode.QRCodePayment();
    }

    public void cashPayment() {

        System.out.println("\tPayment processed successfully!");
    }
}
