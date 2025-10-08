package com.payment.payment;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
@RequestMapping("/tickets")
public class TicketController {

    // 🌟 FIX 1: Add method to redirect the root path ("/") to the ticket page
    @GetMapping("/") 
    public String redirectToTickets() {
        return "redirect:/tickets";
    }

    @GetMapping
    public String showTicketPage() {
        return "tickets";
    }

    @PostMapping("/select")
    public String selectTickets(@RequestParam("numberOfTickets") int numberOfTickets) {
        BigDecimal ticketPrice = new BigDecimal("500.00");
        BigDecimal totalAmount = ticketPrice.multiply(new BigDecimal(numberOfTickets));
        return "redirect:/payment?amount=" + totalAmount;
    }
}