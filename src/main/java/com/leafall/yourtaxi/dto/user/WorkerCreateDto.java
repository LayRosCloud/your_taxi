package com.leafall.yourtaxi.dto.user;

import lombok.Data;

@Data
public class WorkerCreateDto {
    private String fullName;
    private String email;
    private String password;
    private String repeatPassword;
    private String phone;
}
