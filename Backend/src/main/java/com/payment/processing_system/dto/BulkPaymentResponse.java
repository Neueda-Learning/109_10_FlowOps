package com.payment.processing_system.dto;

import java.util.List;

public class BulkPaymentResponse {

    private int totalCount;
    private int successCount;
    private int failureCount;
    private List<BulkPaymentItemResult> results;

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

    public List<BulkPaymentItemResult> getResults() {
        return results;
    }

    public void setResults(List<BulkPaymentItemResult> results) {
        this.results = results;
    }
}

