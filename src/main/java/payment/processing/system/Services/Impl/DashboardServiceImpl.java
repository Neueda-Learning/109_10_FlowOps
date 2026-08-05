package payment.processing.system.Services.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import payment.processing.system.DTO.Response.DashboardSummaryResponse;
import payment.processing.system.DTO.Response.PaymentResponse;
import payment.processing.system.Model.Enums.PaymentStatus;
import payment.processing.system.Model.Enums.RefundStatus;
import payment.processing.system.Model.Payment;
import payment.processing.system.Repository.PaymentRepository;
import payment.processing.system.Repository.ReceiverRepository;
import payment.processing.system.Repository.RefundRepository;
import payment.processing.system.Repository.ScheduledPaymentRepository;
import payment.processing.system.Services.DashboardService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final PaymentRepository paymentRepository;
    private final ReceiverRepository receiverRepository;
    private final RefundRepository refundRepository;
    private final ScheduledPaymentRepository scheduledPaymentRepository;

    @Override
    public DashboardSummaryResponse getDashboardSummary() {
        long totalReceivers = receiverRepository.count();

        Map<String, Long> departmentWiseReceiverCount = new LinkedHashMap<>();
        for (ReceiverRepository.DepartmentReceiverCount row : receiverRepository.countReceiversByDepartment()) {
            departmentWiseReceiverCount.put(row.getDepartmentName(), row.getReceiverCount());
        }

        Map<String, BigDecimal> departmentSalaryExpenditure = new LinkedHashMap<>();
        for (PaymentRepository.DepartmentExpenditure row : paymentRepository.sumCompletedAmountByDepartment()) {
            departmentSalaryExpenditure.put(row.getDepartmentName(), row.getTotalAmount());
        }

        LocalDateTime startOfMonth = YearMonth.now().atDay(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();
        BigDecimal monthlySalaryPaid = paymentRepository.sumCompletedAmountBetween(startOfMonth, now);

        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        long todaysPayments = paymentRepository.findByCreatedAtBetween(startOfToday, now).size();

        long pendingScheduledPayments = scheduledPaymentRepository.countByIsActiveTrue();
        long completedPayments = paymentRepository.countByStatus(PaymentStatus.COMPLETED);
        long failedPayments = paymentRepository.countByStatus(PaymentStatus.FAILED);
        long refundedPayments = refundRepository.countByRefundStatus(RefundStatus.COMPLETED);

        Map<String, Long> paymentsByCurrency = new LinkedHashMap<>();
        for (PaymentRepository.CurrencyPaymentCount row : paymentRepository.countPaymentsByCurrency()) {
            paymentsByCurrency.put(row.getCurrencyCode(), row.getPaymentCount());
        }

        List<DashboardSummaryResponse.MonthlyTrendPoint> monthlyTrends = paymentRepository.monthlySalaryTrend().stream()
                .map(row -> DashboardSummaryResponse.MonthlyTrendPoint.builder()
                        .month(String.format("%04d-%02d", row.getYear(), row.getMonth()))
                        .totalAmount(row.getTotalAmount())
                        .build())
                .toList();

        long totalPayments = paymentRepository.count();
        double successRate = totalPayments == 0 ? 0.0 : (completedPayments * 100.0) / totalPayments;
        double failureRate = totalPayments == 0 ? 0.0 : (failedPayments * 100.0) / totalPayments;

        List<PaymentResponse> recentPayments = paymentRepository.findTop10ByOrderByCreatedAtDesc().stream()
                .map(this::toPaymentResponse)
                .toList();

        return DashboardSummaryResponse.builder()
                .totalReceivers(totalReceivers)
                .departmentWiseReceiverCount(departmentWiseReceiverCount)
                .departmentSalaryExpenditure(departmentSalaryExpenditure)
                .monthlySalaryPaid(monthlySalaryPaid)
                .todaysPayments(todaysPayments)
                .pendingScheduledPayments(pendingScheduledPayments)
                .completedPayments(completedPayments)
                .failedPayments(failedPayments)
                .refundedPayments(refundedPayments)
                .paymentsByCurrency(paymentsByCurrency)
                .monthlyTrends(monthlyTrends)
                .successRate(successRate)
                .failureRate(failureRate)
                .recentPayments(recentPayments)
                .build();
    }

    private PaymentResponse toPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .senderAccountId(payment.getSenderAccount().getAccountId())
                .receiverId(payment.getReceiver().getReceiverId())
                .receiverName(payment.getReceiver().getFullName())
                .amount(payment.getAmount())
                .currencyCode(payment.getCurrency().getCurrencyCode())
                .paymentType(payment.getPaymentType())
                .status(payment.getStatus())
                .idempotencyKey(payment.getIdempotencyKey())
                .reference(payment.getReference())
                .errorCode(payment.getErrorCode())
                .errorMessage(payment.getErrorMessage())
                .scheduled(payment.getScheduled())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .statusHistory(List.of())
                .build();
    }
}

