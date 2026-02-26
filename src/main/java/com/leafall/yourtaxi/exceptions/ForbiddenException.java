package com.leafall.yourtaxi.exceptions;

public class ForbiddenException extends ApiError {
    public ForbiddenException() {
        super(403, "base.error.forbidden");
    }

    public ForbiddenException(String message) {
        super(403, message);
    }
}
