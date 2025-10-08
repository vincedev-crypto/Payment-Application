package com.payment.payment;

import com.payment.payment.validation.LuhnCheck;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Entity
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "Full name cannot be empty")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotNull(message = "Amount is mandatory")
    private BigDecimal amount;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^(\\+63|0)9\\d{9}$", message = "Invalid Philippine phone number format")
    private String phone;

    @NotBlank(message = "Address is required")
    private String address;

    private String city;
    private String country;
    private String postalCode;
    private String method;
    private String gcashNumber;
    private String bdoAccount;
    private String paymayaNumber;
    private String status;
    
    // TRANSIENT CARD FIELDS (FORM BINDING)
    @Transient 
    private String cardName; 
    
    @Transient
    private String expiry; 

    @Transient
    private String cvv; 

    @Transient // This field will not be stored in the database
    @LuhnCheck // Custom validation annotation
    private String cardNumber;

    // --- Getters & Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getGcashNumber() { return gcashNumber; }
    public void setGcashNumber(String gcashNumber) { this.gcashNumber = gcashNumber; }

    public String getBdoAccount() { return bdoAccount; }
    public void setBdoAccount(String bdoAccount) { this.bdoAccount = bdoAccount; }

    public String getPaymayaNumber() { return paymayaNumber; }
    public void setPaymayaNumber(String paymayaNumber) { this.paymayaNumber = paymayaNumber; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCardName() { return cardName; }
    public void setCardName(String cardName) { this.cardName = cardName; }

    public String getExpiry() { return expiry; }
    public void setExpiry(String expiry) { this.expiry = expiry; }
    
    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }

    public String getCardNumber() { return cardNumber; }
    
    // CRITICAL FIX: Sanitize the input by keeping ONLY digits (0-9).
    public void setCardNumber(String cardNumber) { 
        if (cardNumber != null) {
            // This regex keeps ONLY digits (0-9) and removes everything else (spaces, dashes, dots, slashes, etc.)
            this.cardNumber = cardNumber.replaceAll("[^\\d]", "");
        } else {
            this.cardNumber = null;
        }
    }
}