package com.leafall.yourtaxi.dto.user;

import lombok.Data;

import java.util.UUID;

@Data
public class VerifyEmailDto {
    private UUID id;
    private String code;
}
