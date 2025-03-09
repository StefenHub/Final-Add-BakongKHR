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
import org.example.utils.ColorFormatter;
import org.example.utils.ConsoleFormatter;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class QRCode {
    private static final String API_URL = "https://api-bakong.nbc.gov.kh/v1/check_transaction_by_md5";
    private static final String AUTH_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJkYXRhIjp7ImlkIjoiYzIwOWMwNDYzNjBlNDEwMSJ9LCJpYXQiOjE3NDA5Njg2MzksImV4cCI6MTc0ODc0NDYzOX0.Xv_eSRBagCkTg_WEDAP89WluTHr6W4acUu7Sn_oTfbY";

    private JFrame frame;
    private JLabel statusLabel;
    public static void QRCodePayment() {
        try {
            new QRCode().generateAndDisplayQRCode();
        } catch (WriterException e) {
            System.out.println("\t❌ Error: Failed to generate QR Code: " + e.getMessage());
        }
    }

    public void generateAndDisplayQRCode() throws WriterException {
        IndividualInfo individualInfo = new IndividualInfo();
        individualInfo.setAccountInformation("010513288");
        individualInfo.setBakongAccountId("dina_pisethi31@aclb");
        individualInfo.setAcquiringBank("ABA");
        individualInfo.setCurrency(KHQRCurrency.USD);
        individualInfo.setAmount(1.0);
        individualInfo.setMerchantName("ROS Cambodia");
        individualInfo.setMerchantCity("Phnom Penh");

        KHQRResponse<KHQRData> response = BakongKHQR.generateIndividual(individualInfo);

        if (response.getKHQRStatus().getCode() == 0) {
            String qrText = response.getData().getQr();
            String md5 = response.getData().getMd5();

            if (qrText == null || qrText.isEmpty()) {
                System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("⚠️ Error: QR Code Data is empty!", ColorFormatter.YELLOW + ColorFormatter.BOLD)));
                return;
            }

            BufferedImage qrImage = generateQRImage(qrText, 300, 300);
            displayQRPopup(qrImage, md5);
        } else {
            System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("❌ Error: " + response.getKHQRStatus().getMessage(), ColorFormatter.RED + ColorFormatter.BOLD)));

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
        frame.setVisible(true);

        // Start payment check in a separate thread
        new Thread(() -> waitForPayment(md5)).start();

        // Start Countdown Timer
        new Thread(() -> startCountdown(60)).start();
    }

    private void startCountdown(int initialTime) {
        AtomicInteger i = new AtomicInteger(initialTime);
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("Waiting for Payment... (" + i.get() + "s)");
            });
            i.getAndDecrement();
            if (i.get() < 0) {
                scheduler.shutdownNow();
            }
        }, 1, 1, TimeUnit.SECONDS);
    }


    private void waitForPayment(String md5) {
        int attempts = 0;
        int waitTime = 5000; // Start with 500ms
        int maxAttempts = 35 * 1000 / waitTime; //

        while (attempts < maxAttempts) {
            try {
                Thread.sleep(waitTime);

                if (validateMd5(md5)) {
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("\t✅ Payment Successful!");
                        JOptionPane.showMessageDialog(frame, "\tPayment Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        frame.dispose();
                    });
                    return;
                }

                waitTime = Math.min(waitTime * 1, 5000); // Max wait time = 5s

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            attempts++;
        }

        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("\t❌ Payment Timed Out");
            JOptionPane.showMessageDialog(frame, "\tPayment Timed Out", "Error", JOptionPane.ERROR_MESSAGE);
            frame.dispose();
        });
    }

    private boolean validateMd5(String md5) {
        try {
            HttpClient httpClient = HttpClient.newHttpClient();
            ObjectMapper objectMapper = new ObjectMapper();
            String requestBody = objectMapper.writeValueAsString(new Md5Request(md5));

            int maxAttempts = 1;
            int attempt = 0;
            int interval = 3; // seconds

            while (attempt < maxAttempts) {
                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(API_URL))
                        .header("Authorization", AUTH_TOKEN)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JsonNode jsonResponse = objectMapper.readTree(response.body());
                    int responseCode = jsonResponse.path("responseCode").asInt();
                    String responseMessage = jsonResponse.path("responseMessage").asText();

                    if (responseCode == 0) {
                        System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("✅ Success: " + responseMessage, ColorFormatter.GREEN + ColorFormatter.BOLD)));

//                        System.out.println("\t🔹 Data: " + jsonResponse.path("data").toPrettyString());
                        return true;
                    } else {
//                        System.out.println("\t⚠️ Retry " + (attempt + 1) + "/" + maxAttempts + " - " + responseMessage);
                    }
                } else {
                    System.out.println(ConsoleFormatter.centerText(ColorFormatter.colorText("❌ API Error: " + response.statusCode() + " - " + response.body(), ColorFormatter.RED + ColorFormatter.BOLD)));
                }

                attempt++;
                TimeUnit.SECONDS.sleep(interval);
            }

//            System.out.println("\t⏳ Transaction validation failed after multiple attempts.");
        } catch (Exception e) {
            System.err.println(ConsoleFormatter.centerText(ColorFormatter.colorText("Exception: " + e.getMessage(), ColorFormatter.RED + ColorFormatter.BOLD)));
        }

        return false;
    }

    private static class Md5Request {
        private String md5;

        public Md5Request(String md5) {
            this.md5 = md5;
        }

        public String getMd5() {
            return md5;
        }

        public void setMd5(String md5) {

            this.md5 = md5;
        }
    }

    // test QR
    public static void main(String[] args) {
        QRCodePayment();
    }

}