package com.leafall.yourtaxi.exception;

public class RateLimitException extends ApiError {
    public RateLimitException() {
        super(429, "base.error.too-many-request");
    }

    public RateLimitException(String message) {
        super(429, message);
    }
}
