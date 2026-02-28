package com.leafall.yourtaxi.exception;

public class ConflictException extends ApiError {

    public ConflictException() {
        super(409, "base.error.conflict");
    }

    public ConflictException(String message) {
        super(409, message);
    }
}
