package com.artipro.exception;

public class PubMedApiException extends RuntimeException {
    public PubMedApiException(String message) {
        super(message);
    }

    public PubMedApiException(String message, Throwable cause) {
        super(message, cause);
    }
}