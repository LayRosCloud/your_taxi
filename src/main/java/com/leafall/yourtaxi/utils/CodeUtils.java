package com.leafall.yourtaxi.utils;

import java.security.SecureRandom;

public abstract class CodeUtils {

    public static String generateCode() {
        var secureRandom = new SecureRandom();
        return String.format("%06d", secureRandom.nextInt(1000000));
    }
}
