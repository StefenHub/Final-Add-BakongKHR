//package org.example.services;
//
//import com.google.zxing.BarcodeFormat;
//import com.google.zxing.WriterException;
//import com.google.zxing.client.j2se.MatrixToImageWriter;
//import com.google.zxing.common.BitMatrix;
//import com.google.zxing.qrcode.QRCodeWriter;
//import kh.gov.nbc.bakong_khqr.BakongKHQR;
//import kh.gov.nbc.bakong_khqr.model.IndividualInfo;
//import kh.gov.nbc.bakong_khqr.model.KHQRCurrency;
//import kh.gov.nbc.bakong_khqr.model.KHQRData;
//import kh.gov.nbc.bakong_khqr.model.KHQRResponse;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.image.BufferedImage;
//import java.io.IOException;
//
//public class QRCode {
//    String bakongUrl = "https://api-bakong.nbc.gov.kh//v1/check_transaction_by_md5";
//
//    public static void main(String[] args) throws IOException, WriterException {
//        // Create payment request
//        IndividualInfo individualInfo = new IndividualInfo();
//
//        // Populate IndividualInfo
//        individualInfo.setAccountInformation("010513288");
//        individualInfo.setBakongAccountId("sreng_chipor@aclb");
//        individualInfo.setAcquiringBank("ABA");
//        individualInfo.setCurrency(KHQRCurrency.valueOf("USD"));
//        individualInfo.setAmount(0.01);
//        individualInfo.setMerchantName("ROS Cambodia");
//        individualInfo.setMerchantCity("Phnom Penh");
//
//        // Generate QR from Bakong API
//        KHQRResponse<KHQRData> response = BakongKHQR.generateIndividual(individualInfo);
//
//        if (response.getKHQRStatus().getCode() == 0) {
//            String qrText = response.getData().getQr();
//            String md5 = response.getData().getMd5();
//            System.out.println("\n🔹 QR Code Data: " + qrText);
//            System.out.println("🔹 MD5: " + md5);
//
//            if (qrText == null || qrText.isEmpty()) {
//                System.out.println("⚠️ Error: QR Code Data is empty!");
//                return;
//            }
//
//            // Generate QR Image
//            BufferedImage qrImage = generateQRImage(qrText, 300, 300);
//            displayQRPopup(qrImage);  // Show popup
//
//        } else {
//            System.out.println("\n❌ Error: " + response.getKHQRStatus().getMessage());
//        }
//    }
//
//    // Generate QR Code as BufferedImage using MatrixToImageWriter
//    public static BufferedImage generateQRImage(String text, int width, int height) throws WriterException {
//        QRCodeWriter qrCodeWriter = new QRCodeWriter();
//        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
//        return MatrixToImageWriter.toBufferedImage(bitMatrix); // Correct conversion
//    }
//
//    // Display QR Code in a JFrame popup
//}
