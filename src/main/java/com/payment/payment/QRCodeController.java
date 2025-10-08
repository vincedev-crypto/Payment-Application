package com.payment.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
public class QRCodeController {

    @Autowired
    private QRCodeService qrCodeService;

    @Autowired
    private PayMongoService payMongoService;

    @Autowired
    private PaymentRepository paymentRepository;

    @GetMapping("/generate-dynamic-qr")
    public ResponseEntity<byte[]> generateDynamicQRCode(
            @RequestParam String amount,
            @RequestParam(defaultValue = "QR Code Payment") String name,
            @RequestParam(defaultValue = "user@example.com") String email,
            @RequestParam(defaultValue = "09000000000") String phone) {
        
        try {
            Payment payment = createPendingPayment(amount, name, email, phone, "gcash");
            String checkoutUrl = payMongoService.createGcashSource(payment);
            byte[] image = qrCodeService.generateQRCode(checkoutUrl, 250, 250);
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(image);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/generate-paymaya-qr")
    public ResponseEntity<byte[]> generatePayMayaQRCode(
            @RequestParam String amount,
            @RequestParam(defaultValue = "QR Code Payment") String name,
            @RequestParam(defaultValue = "user@example.com") String email,
            @RequestParam(defaultValue = "09000000000") String phone) {
        
        try {
            // ## THIS IS THE FIX ##
            // Change "maya" back to "paymaya"
            Payment payment = createPendingPayment(amount, name, email, phone, "paymaya");
            String checkoutUrl = payMongoService.createPayMayaSource(payment);
            byte[] image = qrCodeService.generateQRCode(checkoutUrl, 250, 250);
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(image);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    private Payment createPendingPayment(String amount, String name, String email, String phone, String method) {
        Payment payment = new Payment();
        payment.setName(name);
        payment.setEmail(email);
        payment.setPhone(phone);
        payment.setAddress("N/A"); 
        payment.setAmount(new BigDecimal(amount));
        payment.setStatus("PENDING");
        payment.setMethod(method); 
        return paymentRepository.save(payment);
    }
}