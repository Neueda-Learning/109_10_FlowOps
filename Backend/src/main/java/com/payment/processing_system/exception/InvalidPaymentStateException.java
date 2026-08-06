package com.payment.processing_system.exception;

import com.payment.processing_system.entity.PaymentStatus;

public class InvalidPaymentStateException extends RuntimeException {

    public InvalidPaymentStateException(Long id, PaymentStatus currentStatus, PaymentStatus expectedStatus) {
        super("Payment " + id + " is in state " + currentStatus + " but expected " + expectedStatus);
    }

    public InvalidPaymentStateException(String message) {
        super(message);
    }
}

