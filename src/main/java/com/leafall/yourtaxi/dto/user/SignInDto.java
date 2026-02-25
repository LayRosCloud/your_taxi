package com.leafall.yourtaxi.dto.user;

import lombok.Data;

import java.util.UUID;

@Data
public class SignInDto {
    private String email;
    private String password;
}
