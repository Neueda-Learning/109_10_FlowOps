package com.payment.processing_system.dto;

import java.math.BigDecimal;

public class CurrencySummary {

    private String currency;
    private BigDecimal totalAmount;
    private long count;

    public CurrencySummary() {
    }

    public CurrencySummary(String currency, BigDecimal totalAmount, long count) {
        this.currency = currency;
        this.totalAmount = totalAmount;
        this.count = count;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}

