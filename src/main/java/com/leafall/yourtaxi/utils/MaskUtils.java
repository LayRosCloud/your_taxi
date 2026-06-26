package com.leafall.yourtaxi.utils;

public final class MaskUtils {
    public static String maskPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return phone;
        }

        String digits = phone.replaceAll("\\D", "");

        if (digits.length() < 4) {
            return phone;
        }
        String lastTwo = digits.substring(digits.length() - 2);
        if (digits.startsWith("375") && digits.length() == 12) {
            return "+375 (**) ***-**-" + lastTwo;
        }
        if (digits.startsWith("7")) {
            return "+7 (***) ***-**-" + lastTwo;
        }
        // Универсальный формат для других номеров
        int visibleDigits = 4;
        StringBuilder masked = new StringBuilder();
        for (int i = 0; i < digits.length(); i++) {
            if (i < digits.length() - visibleDigits) {
                masked.append('*');
            } else {
                masked.append(digits.charAt(i));
            }
        }

        return masked.toString();
    }

    private static String formatMaskedPhone(String maskedDigits, String original) {
        return maskedDigits;
    }
}
