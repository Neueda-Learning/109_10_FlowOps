package com.payment.processing_system.service;

import com.payment.processing_system.dto.CurrencySummary;
import com.payment.processing_system.dto.DashboardSummaryResponse;
import com.payment.processing_system.entity.Payment;
import com.payment.processing_system.entity.PaymentStatus;
import com.payment.processing_system.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final PaymentRepository paymentRepository;

    public DashboardServiceImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public DashboardSummaryResponse getDashboardSummary() {
        DashboardSummaryResponse response = new DashboardSummaryResponse();
        List<Payment> allPayments = paymentRepository.findAll();

        // Total payments
        response.setTotalPayments(allPayments.size());

        // Total amount
        response.setTotalAmount(paymentRepository.getTotalAmount());

        // Successful = COMPLETED
        response.setSuccessfulPayments(paymentRepository.countByStatus(PaymentStatus.COMPLETED));

        // Failed payments
        response.setFailedPayments(paymentRepository.countByStatus(PaymentStatus.FAILED));

        // Scheduled payments
        response.setScheduledPayments(allPayments.stream().filter(p -> Boolean.TRUE.equals(p.getIsScheduled())).count());

        // Refunded payments
        response.setRefundedPayments(paymentRepository.countByStatus(PaymentStatus.REFUNDED));

        // Today's payments
        LocalDate today = LocalDate.now();
        List<Payment> todayPayments = allPayments.stream()
                .filter(p -> p.getCreatedAt() != null && p.getCreatedAt().toLocalDate().equals(today))
                .toList();
        response.setTodayPayments(todayPayments.size());
        response.setTodayAmount(todayPayments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        // Currency summaries
        List<CurrencySummary> currencySummaries = calculateCurrencySummaries(allPayments);
        response.setCurrencySummaries(currencySummaries);

        return response;
    }

    private List<CurrencySummary> calculateCurrencySummaries(List<Payment> allPayments) {

        Map<String, List<Payment>> groupedByCurrency = allPayments.stream()
                .collect(Collectors.groupingBy(Payment::getCurrency));

        return groupedByCurrency.entrySet().stream()
                .map(entry -> {
                    String currency = entry.getKey();
                    List<Payment> payments = entry.getValue();

                    BigDecimal total = payments.stream()
                            .map(Payment::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return new CurrencySummary(currency, total, payments.size());
                })
                .collect(Collectors.toList());
    }
}

