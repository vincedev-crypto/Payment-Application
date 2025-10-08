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
        System.out.println("Received webhook payload: " + payload);

        JSONObject webhookData = new JSONObject(payload).getJSONObject("data");
        String eventType = webhookData.getJSONObject("attributes").getString("type");
        JSONObject eventAttributes = webhookData.getJSONObject("attributes").getJSONObject("data").getJSONObject("attributes");

        if ("payment.paid".equalsIgnoreCase(eventType)) {
            // This is the actual success event.
            updatePaymentStatus(eventAttributes, "PAID");
        } else if ("payment.failed".equalsIgnoreCase(eventType) || "source.failed".equalsIgnoreCase(eventType)) {
            updatePaymentStatus(eventAttributes, "FAILED");
        } else {
            System.out.println("Ignoring webhook event of type: " + eventType);
        }
    }

    private void updatePaymentStatus(JSONObject eventAttributes, String newStatus) {
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

                if ("PENDING".equalsIgnoreCase(payment.getStatus())) {
                    payment.setStatus(newStatus);
                    paymentRepository.save(payment);

                    System.out.printf("Payment ID %d has been updated to %s.%n", payment.getId(), newStatus);

                    if ("PAID".equalsIgnoreCase(newStatus)) {
                        emailService.sendPaymentConfirmation(payment);
                    }
                }
            } else {
                System.err.println("Webhook received for non-existent Payment ID: " + internalPaymentId);
            }
        } else {
            System.err.println("Webhook data is missing 'metadata' object.");
        }
    }
}