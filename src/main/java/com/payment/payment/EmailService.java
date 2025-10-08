package com.payment.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Sends a payment confirmation email to the user.
     * This method is called by the webhook service upon successful payment.
     * @param payment The completed payment object containing all details.
     */
    public void sendPaymentConfirmation(Payment payment) {
        if (payment == null || payment.getEmail() == null) {
            System.err.println("Could not send email: payment or email is null.");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(payment.getEmail());
            message.setSubject("Your Payment Confirmation");
            
            // Construct the email body
            String emailBody = String.format(
                "Dear %s,\n\n" +
                "Thank you for your payment. Here are your transaction details:\n\n" +
                "Transaction ID: %d\n" +
                "Amount: %.2f PHP\n" +
                "Status: %s\n\n" +
                "We appreciate your business.\n\n" +
                "Sincerely,\n" +
                "Your Company",
                payment.getName(),
                payment.getId(),
                payment.getAmount(),
                payment.getStatus()
            );
            
            message.setText(emailBody);
            
            mailSender.send(message);
            System.out.println("Payment confirmation email sent to " + payment.getEmail());

        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}