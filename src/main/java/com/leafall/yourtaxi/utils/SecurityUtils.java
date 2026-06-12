package com.leafall.yourtaxi.utils;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class SecurityUtils {
    public static UUID getCurrentUserId() {
        var security = SecurityContextHolder.getContext().getAuthentication();
        var userDetails = (UserDetails)security.getPrincipal();
        return UUID.fromString(userDetails.getUsername());
    }

    public static String generateString(int size) {
        String symbols = "abcdefghijklmnopqrstuvwxyz";
        return new Random().ints(size, 0, symbols.length())
            .mapToObj(symbols::charAt)
            .map(Object::toString)
            .collect(Collectors.joining());
    }
}
