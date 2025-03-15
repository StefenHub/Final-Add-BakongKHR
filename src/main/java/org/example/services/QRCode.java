package org.example.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
 import com.google.zxing.qrcode.QRCodeWriter;
import kh.gov.nbc.bakong_khqr.BakongKHQR;
import kh.gov.nbc.bakong_khqr.model.*;
import lombok.Getter;
import lombok.Setter;
import org.example.utils.ColorFormatter;
import org.example.utils.ConsoleFormatter;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class QRCode {
    private static final String API_URL = "https://api-bakong.nbc.gov.kh/v1/check_transaction_by_md5";
    private static final String AUTH_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJkYXRhIjp7ImlkIjoiYzIwOWMwNDYzNjBlNDEwMSJ9LCJpYXQiOjE3NDA5Njg2MzksImV4cCI6MTc0ODc0NDYzOX0.Xv_eSRBagCkTg_WEDAP89WluTHr6W4acUu7Sn_oTfbY";

    private JFrame frame;
    private JLabel statusLabel;

////    public static String QRCodePayment(double grandTotal) {
////        boolean paymentSuccessful = false;
////        Scanner scanner = new Scanner(System.in);
////
////        while (!paymentSuccessful) {
////            try {
////                new QRCode().generateAndDisplayQRCode(grandTotal);
////                paymentSuccessful = true;
////            } catch (WriterException e) {
////                System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText(
////                        "❌ Error: Failed to generate QR Code: " + e.getMessage(),
////                        ColorFormatter.RED + ColorFormatter.BOLD)));
////                System.out.print(ConsoleFormatter.centerText(ColorFormatter.colorText(
////                        "Do you want to retry? (yes/no): ", ColorFormatter.YELLOW + ColorFormatter.BOLD)));
////                String retry = scanner.nextLine().trim().toLowerCase();
////                if (!retry.equals("yes")) {
////                    break;
////                }
////            }
////        }
////        return null;
////    }
//
    @Getter
    private static String latestMd5 = null; // Store the latest MD5 hash

    public String generateAndDisplayQRCode(double grandTotal) throws WriterException {
        IndividualInfo individualInfo = new IndividualInfo();
        individualInfo.setAccountInformation("010513288");
        individualInfo.setBakongAccountId("dina_pisethi31@aclb");
        individualInfo.setAcquiringBank("ABA");
        individualInfo.setCurrency(KHQRCurrency.USD);
        individualInfo.setAmount(grandTotal);
        individualInfo.setMerchantName("ROS Cambodia");
        individualInfo.setMerchantCity("Phnom Penh");

        KHQRResponse<KHQRData> response = BakongKHQR.generateIndividual(individualInfo);

        if (response.getKHQRStatus().getCode() == 0) {
            String qrText = response.getData().getQr();
            latestMd5 = response.getData().getMd5(); // Store MD5 when QR is generated

            if (qrText == null || qrText.isEmpty()) {
                System.out.println("⚠️ Error: QR Code Data is empty!");
                return null;
            }

            BufferedImage qrImage = generateQRImage(qrText, 300, 300);
            displayQRPopup(qrImage, latestMd5);
            return latestMd5; // Return the MD5 for immediate use
        } else {
            System.out.println("❌ Error: " + response.getKHQRStatus().getMessage());
            return null;
        }
    }

    private BufferedImage generateQRImage(String text, int width, int height) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    private void displayQRPopup(BufferedImage image, String md5) {
        frame = new JFrame("Scan QR Code");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // "Scan Now!!" Label (Above QR Code)
        JLabel scanNowLabel = new JLabel("Scan Now!!", SwingConstants.CENTER);
        scanNowLabel.setFont(new Font("Arial", Font.BOLD, 18));
        scanNowLabel.setForeground(Color.BLUE);

        // QR Code Image
        JLabel qrLabel = new JLabel(new ImageIcon(image));

        // Countdown Timer Status Label (Below QR Code)
        statusLabel = new JLabel("Waiting for Payment... (60s)", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));

        // Add components to frame
        frame.add(scanNowLabel, BorderLayout.NORTH);
        frame.add(qrLabel, BorderLayout.CENTER);
        frame.add(statusLabel, BorderLayout.SOUTH);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setAlwaysOnTop(true);
        frame.setVisible(true);

        // Start payment check in a separate thread
        new Thread(() -> waitForPayment(md5)).start();

        // Start Countdown Timer
        new Thread(() -> startCountdown(60)).start(); // 60 seconds
    }

    private void startCountdown(int initialTime) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        AtomicInteger timeLeft = new AtomicInteger(initialTime);

        scheduler.scheduleAtFixedRate(() -> {
            SwingUtilities.invokeLater(() -> {
                if (timeLeft.get() > 0) {
                    statusLabel.setText("Waiting for Payment... (" + timeLeft.get() + "s)");
                    timeLeft.decrementAndGet();
                } else {
                    scheduler.shutdown();
                }
            });
        }, 1, 1, TimeUnit.SECONDS);
    }


    private void waitForPayment(String md5) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        long startTime = System.currentTimeMillis();
        long timeout = 60 * 1000; // 60 seconds

        Runnable task = () -> {
            if (System.currentTimeMillis() - startTime >= timeout) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("❌ Payment Timed Out");
                    JOptionPane.showMessageDialog(frame, "Payment Timed Out", "Error", JOptionPane.ERROR_MESSAGE);
                    frame.dispose();
                });
                scheduler.shutdown();
                return;
            }

            if (validateMd5(md5)) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("✅ Payment Successful!");
                    JOptionPane.showMessageDialog(frame, "Payment Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    frame.dispose();
                    placeOrder();
                });
                scheduler.shutdown();
            }
        };

        scheduler.scheduleAtFixedRate(task, 2, 2, TimeUnit.SECONDS); // Check every 2 seconds
    }


    private void placeOrder() {
        // Simulate placing the order
        System.out.println("🛒 Placing Order...");
        int orderId = (int) (Math.random() * 100); // Generate a random order ID
        System.out.println("✅ Order placed successfully! Order ID: " + orderId);
    }



    private boolean validateMd5(String md5) {
        HttpClient httpClient = HttpClient.newHttpClient();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            String requestBody = objectMapper.writeValueAsString(new Md5Request(md5));

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", AUTH_TOKEN)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            CompletableFuture<HttpResponse<String>> responseFuture = httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString());

            responseFuture.thenAccept(response -> {
                if (response.statusCode() == 200) {
                    try {
                        JsonNode jsonResponse = objectMapper.readTree(response.body());
                        int responseCode = jsonResponse.path("responseCode").asInt();
                        String responseMessage = jsonResponse.path("responseMessage").asText();

                        if (responseCode == 0) {
                            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText(
                                    "✅ Success: " + responseMessage, ColorFormatter.GREEN + ColorFormatter.BOLD)));
                            latestMd5 = null; // Reset MD5 after success
                        }
                    } catch (Exception e) {
                        System.err.println("❌ JSON Parsing Error: " + e.getMessage());
                    }
                } else {
                    System.out.println("❌ API Error: " + response.statusCode() + " - " + response.body());
                }
            }).exceptionally(e -> {
                System.err.println("❌ Request Failed: " + e.getMessage());
                return null;
            });

            return latestMd5 == null; // Check if payment was validated

        } catch (Exception e) {
            System.err.println("❌ Exception: " + e.getMessage());
            return false;
        }
    }

    public String getMD5() {
        return latestMd5;
    }

    @Setter
    @Getter
    private static class Md5Request {
        private String md5;

        public Md5Request(String md5) {
            this.md5 = md5;
        }

    }


}
