package com.payment.payment;

public class CRC16 {

    /**
     * Calculates the CRC-16/CCITT-FALSE checksum for the given data.
     * @param data The input data string.
     * @return A 4-character hexadecimal string representing the checksum.
     */
    public static String calculate(String data) {
        int crc = 0xFFFF; // initial value
        byte[] bytes = data.getBytes();

        for (byte b : bytes) {
            crc ^= (b & 0xFF) << 8;
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x8000) != 0) {
                    crc = (crc << 1) ^ 0x1021; // polynomial
                } else {
                    crc <<= 1;
                }
            }
        }
        
        // Format as a 4-character hex string
        return String.format("%04X", crc & 0xFFFF);
    }
}