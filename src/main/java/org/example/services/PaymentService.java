package org.example.services;

public class PaymentService {
    public void processPayment(int paymentMethod) {
        if (paymentMethod == 1) {
            System.out.println("Processing payment through QR Code...");
            // Add QR code payment logic here
        } else if (paymentMethod == 2) {
            System.out.println("Processing payment through Cash...");
            // Add cash payment logic here
        }
        System.out.println("Payment processed successfully!");
    }

    public void QRCodePayment() {
        System.out.println("Processing payment through QR Code...");
        // Add QR code payment logic here
        System.out.println("Payment processed successfully!");
    }

    public void cashPayment() {
        System.out.println("Processing payment through Cash...");
        // Add cash payment logic here
        System.out.println("Payment processed successfully!");
    }
}
