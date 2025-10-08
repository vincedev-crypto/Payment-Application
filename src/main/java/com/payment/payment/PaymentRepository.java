package com.payment.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import java.math.BigDecimal;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    // This new method will search by name or email
    List<Payment> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String nameKeyword, String emailKeyword);
    
    // Find by payment method
    List<Payment> findByMethod(String method);
    
    // Find by amount range - Updated to use BigDecimal
    List<Payment> findByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);

    // Find by status
    List<Payment> findByStatus(String status);
}