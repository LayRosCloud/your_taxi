package com.leafall.yourtaxi.exceptions;

public class BadRequestException extends ApiError {

    public BadRequestException() {
        super(400, "base.error.bad-request");
    }

    public BadRequestException(String message) {
        super(400, message);
    }
}
