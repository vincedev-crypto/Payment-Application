package com.payment.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.Optional;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private AuditService auditService;

    @Autowired
    private PayMongoService payMongoService;

    @GetMapping
    public String showPaymentPage(Model model) {
        model.addAttribute("payment", new Payment());
        return "payment";
    }

    @PostMapping("/submit")
    public String processPaymentForm(@Valid @ModelAttribute("payment") Payment payment, BindingResult bindingResult, Model model, HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            return "payment";
        }

        payment.setStatus("PENDING");
        Payment savedPayment = paymentRepository.save(payment);

        String ipAddress = request.getRemoteAddr();
        auditService.logAction("Payment created", "New payment from " + payment.getName(), "User", ipAddress);
        
        try {
            String checkoutUrl;
            if ("gcash".equalsIgnoreCase(payment.getMethod())) {
                checkoutUrl = payMongoService.createGcashSource(savedPayment);
            } else if ("paymaya".equalsIgnoreCase(payment.getMethod())) {
                checkoutUrl = payMongoService.createPayMayaSource(savedPayment);
            } else if ("credit".equalsIgnoreCase(payment.getMethod()) || "debit".equalsIgnoreCase(payment.getMethod())) {
                checkoutUrl = payMongoService.createCardPayment(savedPayment);
            } 
            else {
                model.addAttribute("error", "Invalid payment method selected.");
                return "error-page";
            }
            return "redirect:" + checkoutUrl;
        } catch (Exception e) {
            e.printStackTrace(); 
            model.addAttribute("error", "Payment gateway is currently unavailable. Please try again later.");
            return "error-page";
        }
    }

    // ## THIS IS THE URL CHANGE ##
    @GetMapping("/payment-successful")
    public String showSuccessPage(@RequestParam(required = false) Long paymentId, Model model) {
        if (paymentId == null) {
            model.addAttribute("error", "Payment ID was not provided.");
            return "error-page";
        }

        Optional<Payment> paymentOptional = paymentRepository.findById(paymentId);
        
        if (paymentOptional.isPresent()) {
            model.addAttribute("payment", paymentOptional.get());
            return "receipt";
        } else {
            model.addAttribute("error", "Could not find details for Payment ID: " + paymentId);
            return "error-page";
        }
    }

    @GetMapping("/failed")
    public String showFailedPage() {
        return "error-page";
    }
}