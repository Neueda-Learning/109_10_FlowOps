package com.payment.processing_system.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class SchedulePaymentRequest {

    @NotNull
    private LocalDateTime scheduledFor;

    public LocalDateTime getScheduledFor() {
        return scheduledFor;
    }

    public void setScheduledFor(LocalDateTime scheduledFor) {
        this.scheduledFor = scheduledFor;
    }
}