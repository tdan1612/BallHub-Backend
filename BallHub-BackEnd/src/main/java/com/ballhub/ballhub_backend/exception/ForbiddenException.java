package com.ballhub.ballhub_backend.exception;

class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
