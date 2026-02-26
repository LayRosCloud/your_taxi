package com.leafall.yourtaxi.exceptions;

public class NotFoundException extends ApiError {

    public NotFoundException() {
        super(404, "base.error.not-found");
    }

    public NotFoundException(String message) {
        super(404, message);
    }
}
