package com.payment.payment;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootController {

    @GetMapping("/") // This will correctly map to the application root (https://...onrender.com/)
    public String redirectToTickets() {
        return "redirect:/tickets";
    }
}