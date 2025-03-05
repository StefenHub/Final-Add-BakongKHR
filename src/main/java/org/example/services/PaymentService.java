package org.example.services;

public class PaymentService {
    public void processPaymentStaff(int paymentMethod) {
        if (paymentMethod == 1) {
            System.out.println("Processing payment through KHQR...");
            QRCodePayment();
        } else if (paymentMethod == 2) {
            System.out.println("Processing payment through Cash...");
        }
        System.out.println("Payment processed successfully!");
    }

    public void processPaymentCustomer(int paymentMethod) {
        System.out.println("Processing payment through KHQR...");
        QRCodePayment();
        System.out.println("Payment processed successfully!");
    }

    public void QRCodePayment() {
        QRCode.QRCodePayment();
    }

    public void cashPayment() {

        System.out.println("Payment processed successfully!");
    }
}
