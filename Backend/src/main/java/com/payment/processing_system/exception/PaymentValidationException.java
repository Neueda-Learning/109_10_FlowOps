package com.payment.processing_system.exception;

public class PaymentValidationException extends RuntimeException {

    public PaymentValidationException(String message) {
        super(message);
    }
}

