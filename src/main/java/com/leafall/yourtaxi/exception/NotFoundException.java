package com.leafall.yourtaxi.exception;

public class NotFoundException extends ApiError {

    public NotFoundException() {
        super(404, "base.error.not-found");
    }

    public NotFoundException(String message) {
        super(404, message);
    }
}
