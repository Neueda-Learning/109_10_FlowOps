package com.payment.processing_system.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboardSummaryResponse {

    private long totalPayments;
    private BigDecimal totalAmount;
    private long successfulPayments;
    private long failedPayments;
    private long scheduledPayments;
    private long refundedPayments;
    private long todayPayments;
    private BigDecimal todayAmount;
    private List<CurrencySummary> currencySummaries;

    public long getTotalPayments() {
        return totalPayments;
    }

    public void setTotalPayments(long totalPayments) {
        this.totalPayments = totalPayments;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public long getSuccessfulPayments() {
        return successfulPayments;
    }

    public void setSuccessfulPayments(long successfulPayments) {
        this.successfulPayments = successfulPayments;
    }

    public long getFailedPayments() {
        return failedPayments;
    }

    public void setFailedPayments(long failedPayments) {
        this.failedPayments = failedPayments;
    }

    public long getScheduledPayments() {
        return scheduledPayments;
    }

    public void setScheduledPayments(long scheduledPayments) {
        this.scheduledPayments = scheduledPayments;
    }

    public long getRefundedPayments() {
        return refundedPayments;
    }

    public void setRefundedPayments(long refundedPayments) {
        this.refundedPayments = refundedPayments;
    }

    public long getTodayPayments() {
        return todayPayments;
    }

    public void setTodayPayments(long todayPayments) {
        this.todayPayments = todayPayments;
    }

    public BigDecimal getTodayAmount() {
        return todayAmount;
    }

    public void setTodayAmount(BigDecimal todayAmount) {
        this.todayAmount = todayAmount;
    }

    public List<CurrencySummary> getCurrencySummaries() {
        return currencySummaries;
    }

    public void setCurrencySummaries(List<CurrencySummary> currencySummaries) {
        this.currencySummaries = currencySummaries;
    }
}

