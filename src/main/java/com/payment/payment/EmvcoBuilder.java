package com.payment.payment;

import java.util.StringJoiner;

public class EmvcoBuilder {

    // Represents a single data object in the EMVCo string
    static class EmvcoField {
        private final String id;
        private final String value;

        public EmvcoField(String id, String value) {
            this.id = id;
            this.value = value;
        }

        @Override
        public String toString() {
            // Format: ID (2 chars) + Length (2 chars) + Value
            return id + String.format("%02d", value.length()) + value;
        }
    }

    public static String generatePayload(String amount, String merchantId, String merchantName, String merchantCity) {
        StringJoiner payload = new StringJoiner("");

        // Static EMVCo data
        payload.add(new EmvcoField("00", "01").toString()); // Payload Format Indicator
        payload.add(new EmvcoField("01", "12").toString()); // Point of Initiation Method (Dynamic QR)
        
        // Merchant Account Information for GCash
        StringJoiner gcashMerchantInfo = new StringJoiner("");
        gcashMerchantInfo.add(new EmvcoField("00", "co.gcash.qr").toString());
        gcashMerchantInfo.add(new EmvcoField("01", merchantId).toString());
        payload.add(new EmvcoField("26", gcashMerchantInfo.toString()).toString());

        // Transaction Details
        payload.add(new EmvcoField("52", "5812").toString()); // Merchant Category Code (e.g., 5812 for Restaurants)
        payload.add(new EmvcoField("53", "608").toString());  // Transaction Currency (PHP)
        payload.add(new EmvcoField("54", amount).toString()); // Transaction Amount
        payload.add(new EmvcoField("58", "PH").toString());   // Country Code
        payload.add(new EmvcoField("59", merchantName).toString()); // Merchant Name
        payload.add(new EmvcoField("60", merchantCity).toString());   // Merchant City

        // CRC Calculation (Must be the last field)
        String payloadToCalculate = payload.toString() + "6304"; // "6304" is the ID and length for CRC
        String crc = CRC16.calculate(payloadToCalculate);
        
        payload.add(new EmvcoField("63", crc).toString());

        return payload.toString();
    }
}