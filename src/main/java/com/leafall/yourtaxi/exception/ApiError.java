package com.leafall.yourtaxi.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ApiError extends RuntimeException {
    private int status;
    private String message;

}
