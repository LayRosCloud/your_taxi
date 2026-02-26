package com.leafall.yourtaxi.utils;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

public abstract class SecurityUtils {
    public static UUID getCurrentUserId() {
        var security = SecurityContextHolder.getContext().getAuthentication();
        var userDetails = (UserDetails)security.getPrincipal();
        return UUID.fromString(userDetails.getUsername());
    }
}
