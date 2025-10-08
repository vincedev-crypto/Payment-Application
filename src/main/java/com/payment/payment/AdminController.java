package com.payment.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin") // Group all admin-related URLs
public class AdminController {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private AuditService auditService;

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        // Fetch only paid payments
        List<Payment> paidPayments = paymentRepository.findByStatus("PAID");
        model.addAttribute("payments", paidPayments);
        return "admin-dashboard";
    }

    @GetMapping("/payment/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid payment Id:" + id));
        model.addAttribute("payment", payment);
        return "edit-payment";
    }

    @PostMapping("/payment/update/{id}")
    public String updatePayment(@PathVariable Long id, @Valid @ModelAttribute("payment") Payment payment, BindingResult result, HttpServletRequest request) {
        if (result.hasErrors()) {
            return "edit-payment";
        }
        payment.setId(id);
        paymentRepository.save(payment);

        String ipAddress = request.getRemoteAddr();
        auditService.logAction("Payment updated", "Updated payment with ID: " + id, "Admin", ipAddress);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/payment/delete/{id}")
    public String deletePayment(@PathVariable Long id, HttpServletRequest request) {
        paymentRepository.deleteById(id);
        String ipAddress = request.getRemoteAddr();
        auditService.logAction("Payment deleted", "Deleted payment with ID: " + id, "Admin", ipAddress);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/payment/export")
    public void exportToCsv(HttpServletResponse response, HttpServletRequest request) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"payments.csv\"");
        List<Payment> payments = paymentRepository.findAll();

        response.getWriter().write("ID,Name,Email,Amount,Method,Status\n");
        for (Payment payment : payments) {
            String line = String.format("%d,%s,%s,%.2f,%s,%s\n",
                payment.getId(), payment.getName(), payment.getEmail(),
                payment.getAmount(), payment.getMethod(), payment.getStatus());
            response.getWriter().write(line);
        }

        String ipAddress = request.getRemoteAddr();
        auditService.logAction("Data export", "Exported all payment data to CSV", "Admin", ipAddress);
    }

    @GetMapping("/api/payments")
    @ResponseBody
    public ResponseEntity<List<Payment>> getAllPayments(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount,
            HttpServletRequest request) {

        List<Payment> payments;
        String ipAddress = request.getRemoteAddr();

        // 1. Start with initial data retrieval (filtered by keyword if present)
        if (keyword != null && !keyword.isEmpty()) {
            payments = paymentRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword);
        } else {
            payments = paymentRepository.findAll();
        }
        
        String auditMessage = "Data view";

        // 2. Apply Method Filter
        if (method != null && !method.isEmpty()) {
            payments = payments.stream()
                .filter(p -> p.getMethod() != null && p.getMethod().equalsIgnoreCase(method))
                .collect(Collectors.toList());
            auditMessage = "Filtered payments by method: " + method;
        }

        // 3. Apply Amount Range Filter
        if (minAmount != null && maxAmount != null) {
            final BigDecimal min = new BigDecimal(minAmount);
            final BigDecimal max = new BigDecimal(maxAmount);
            
            payments = payments.stream()
                .filter(p -> p.getAmount() != null && p.getAmount().compareTo(min) >= 0 && p.getAmount().compareTo(max) <= 0)
                .collect(Collectors.toList());
            
            // Adjust audit message based on what filtering occurred
            auditMessage = (auditMessage.equals("Data view") || auditMessage.startsWith("Searched")) 
                           ? auditMessage + " and by amount range: " + minAmount + "-" + maxAmount
                           : "Filtered by amount range: " + minAmount + "-" + maxAmount;
        }
        
        // Final Audit Logging
        auditService.logAction("Data search", auditMessage, "Admin", ipAddress);

        return new ResponseEntity<>(payments, HttpStatus.OK);
    }
    
    @GetMapping("/audit-logs")
    public String showAuditLogsPage() {
        return "audit-logs";
    }
    
    @GetMapping("/api/logs")
    @ResponseBody
    public ResponseEntity<List<Log>> getAllLogs() {
        List<Log> logs = auditService.getAllLogs();
        return new ResponseEntity<>(logs, HttpStatus.OK);
    }

    @PostMapping("/payment/update-status")
    public String updatePaymentStatus(@RequestParam Long paymentId, @RequestParam String status) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid payment ID:" + paymentId));
        payment.setStatus(status);
        paymentRepository.save(payment);
        return "redirect:/admin/dashboard";
    }
}