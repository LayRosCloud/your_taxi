package com.leafall.yourtaxi.dto.user;

import lombok.Data;

@Data
public class SignUpDto {
    private String fullName;
    private String email;
    private String password;
    private String repeatPassword;
}
