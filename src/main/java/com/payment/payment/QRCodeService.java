package com.payment.payment;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class QRCodeService {

    /**
     * Generates a QR code image from a given text string.
     * @param text The text to encode (e.g., a URL from PayMongo).
     * @param width The desired width of the QR code image.
     * @param height The desired height of the QR code image.
     * @return A byte array representing the QR code image in PNG format.
     */
    public byte[] generateQRCode(String text, int width, int height) throws WriterException, IOException {
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("QR code text cannot be empty.");
        }
        
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return pngOutputStream.toByteArray();
    }
}