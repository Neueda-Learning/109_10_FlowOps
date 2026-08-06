package com.payment.processing_system.service;

import com.payment.processing_system.dto.CurrencySummary;
import com.payment.processing_system.dto.DashboardSummaryResponse;

public interface DashboardService {

    DashboardSummaryResponse getDashboardSummary();
}

