package com.leafall.yourtaxi.exceptions;

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
