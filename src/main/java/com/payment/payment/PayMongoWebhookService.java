package com.payment.payment;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class PayMongoWebhookService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private EmailService emailService;

    public void handleWebhookEvent(String payload, String signature) throws Exception {
        // Log the payload for debugging
        System.out.println("Received webhook payload: " + payload);

        JSONObject webhookRoot = new JSONObject(payload);
        JSONObject webhookData = webhookRoot.getJSONObject("data");
        String eventType = webhookData.getJSONObject("attributes").getString("type");
        
        // This object holds the attributes of the data (Source, Payment, or Checkout Session)
        JSONObject eventAttributes = webhookData.getJSONObject("attributes").getJSONObject("data").getJSONObject("attributes");
        
        // Use a mutable JSONObject reference for payment details
        JSONObject paymentAttributes = eventAttributes;

        // 🌟 FIX 1: Check for the specific Checkout Session event
        if ("checkout_session.payment.paid".equalsIgnoreCase(eventType)) {
            
            // For successful checkout sessions, the payment object is nested under "payment"
            if (eventAttributes.has("payment")) {
                paymentAttributes = eventAttributes.getJSONObject("payment").getJSONObject("attributes");
            } else {
                System.err.println("Checkout Session event is missing nested 'payment' object.");
                return;
            }
            
            // Now proceed to update status with the extracted payment attributes
            updatePaymentStatus(paymentAttributes, "PAID");

        } else if ("payment.paid".equalsIgnoreCase(eventType)) {
            // This handles direct 'payment.paid' events
            updatePaymentStatus(paymentAttributes, "PAID");
            
        } else if ("payment.failed".equalsIgnoreCase(eventType) || "source.failed".equalsIgnoreCase(eventType)) {
            updatePaymentStatus(paymentAttributes, "FAILED");
            
        } else {
            System.out.println("Ignoring webhook event of type: " + eventType);
        }
    }

    private void updatePaymentStatus(JSONObject eventAttributes, String newStatus) {
        
        // The logic for finding 'metadata' and 'internal_payment_id' remains the same
        if (eventAttributes.has("metadata")) {
            JSONObject metadata = eventAttributes.getJSONObject("metadata");
            if (!metadata.has("internal_payment_id")) {
                System.err.println("Webhook metadata is missing 'internal_payment_id'.");
                return;
            }
            String internalPaymentIdStr = metadata.getString("internal_payment_id");

            Long internalPaymentId = Long.parseLong(internalPaymentIdStr);
            Optional<Payment> paymentOptional = paymentRepository.findById(internalPaymentId);

            if (paymentOptional.isPresent()) {
                Payment payment = paymentOptional.get();

                // Only update status if it's pending (to prevent overwriting final status)
                if ("PENDING".equalsIgnoreCase(payment.getStatus())) {
                    payment.setStatus(newStatus);
                    paymentRepository.save(payment);

                    System.out.printf("Payment ID %d has been updated to %s.%n", payment.getId(), newStatus);

                    if ("PAID".equalsIgnoreCase(newStatus)) {
                        emailService.sendPaymentConfirmation(payment);
                    }
                } else {
                    System.out.printf("Payment ID %d status already %s, skipping update to %s.%n", payment.getId(), payment.getStatus(), newStatus);
                }
            } else {
                System.err.println("Webhook received for non-existent Payment ID: " + internalPaymentId);
            }
        } else {
            System.err.println("Webhook data is missing 'metadata' object.");
        }
    }
}