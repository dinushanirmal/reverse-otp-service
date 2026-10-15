package com.example.reverseotp.util;

public final class PhoneNumberUtil {

    private PhoneNumberUtil() {
    }

    /**
     * Cleans phone number by removing all whitespace, dashes, dots, and parentheses.
     */
    public static String clean(String phoneNumber) {
        if (phoneNumber == null) {
            return "";
        }
        return phoneNumber.replaceAll("[\\s\\-\\.\\(\\)]", "").trim();
    }

    /**
     * Normalizes a phone number to standard format with leading + if international,
     * or stripped digits for comparison.
     */
    public static String normalize(String phoneNumber) {
        String cleaned = clean(phoneNumber);
        if (cleaned.startsWith("00")) {
            cleaned = "+" + cleaned.substring(2);
        }
        return cleaned;
    }

    /**
     * Compares two phone numbers to see if they match, taking into account
     * international prefixes (+, 00) or missing leading plus.
     */
    public static boolean matches(String phone1, String phone2) {
        if (phone1 == null || phone2 == null) {
            return false;
        }

        String p1 = normalize(phone1);
        String p2 = normalize(phone2);

        if (p1.equalsIgnoreCase(p2)) {
            return true;
        }

        // Compare purely numeric strings
        String digits1 = p1.replaceAll("[^0-9]", "");
        String digits2 = p2.replaceAll("[^0-9]", "");

        if (digits1.equals(digits2)) {
            return true;
        }

        // Suffix comparison for cases like 0771234567 vs 94771234567 (last 9 digits match)
        if (digits1.length() >= 9 && digits2.length() >= 9) {
            String suffix1 = digits1.substring(digits1.length() - 9);
            String suffix2 = digits2.substring(digits2.length() - 9);
            return suffix1.equals(suffix2);
        }

        return false;
    }
}
