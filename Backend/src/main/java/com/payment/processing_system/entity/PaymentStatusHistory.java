package com.payment.processing_system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_status_history")
public class PaymentStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private Long paymentId;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private PaymentStatus previousStatus;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PaymentStatus currentStatus;

    @NotNull
    @Column(nullable = false, updatable = false)
    private LocalDateTime changedAt;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String description;

    @PrePersist
    public void prePersist() {
        if (this.changedAt == null) {
            this.changedAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public PaymentStatus getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(PaymentStatus previousStatus) {
        this.previousStatus = previousStatus;
    }

    public PaymentStatus getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(PaymentStatus currentStatus) {
        this.currentStatus = currentStatus;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

