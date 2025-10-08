package com.payment.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhooks")
public class WebhookController {

    @Autowired
    private PayMongoWebhookService payMongoWebhookService;

    @PostMapping("/paymongo")
    public ResponseEntity<String> handlePayMongoWebhook(@RequestBody String payload, @RequestHeader(value = "Paymongo-Signature", required = false) String signature) {
        try {
            payMongoWebhookService.handleWebhookEvent(payload, signature);
            return ResponseEntity.ok("Webhook received successfully.");
        } catch (Exception e) {
            System.err.println("Webhook processing failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Webhook processing failed.");
        }
    }
}