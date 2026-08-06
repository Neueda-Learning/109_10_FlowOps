package com.payment.processing_system.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class BulkPaymentRequest {

    @NotEmpty(message = "Payment list cannot be empty")
    @Valid
    private List<CreatePaymentRequest> payments;

    public List<CreatePaymentRequest> getPayments() {
        return payments;
    }

    public void setPayments(List<CreatePaymentRequest> payments) {
        this.payments = payments;
    }
}

