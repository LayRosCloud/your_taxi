package com.leafall.yourtaxi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EncodingService {
    private final PasswordEncoder encoder;

    public String encode(String password) {
        return encoder.encode(password);
    }

    public boolean isMatch(String notEncoded, String encoded) {
        return encoder.matches(notEncoded, encoded);
    }
}
