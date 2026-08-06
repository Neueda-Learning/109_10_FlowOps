package com.payment.processing_system.dto;

import jakarta.validation.constraints.NotBlank;

public class FailPaymentRequest {

    @NotBlank
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}

