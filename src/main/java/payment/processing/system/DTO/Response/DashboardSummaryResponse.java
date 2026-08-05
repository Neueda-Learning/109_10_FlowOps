package payment.processing.system.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {
    private long totalReceivers;
    private Map<String, Long> departmentWiseReceiverCount;
    private Map<String, BigDecimal> departmentSalaryExpenditure;
    private BigDecimal monthlySalaryPaid;
    private long todaysPayments;
    private long pendingScheduledPayments;
    private long completedPayments;
    private long failedPayments;
    private long refundedPayments;
    private Map<String, Long> paymentsByCurrency;
    private List<MonthlyTrendPoint> monthlyTrends;
    private double successRate;
    private double failureRate;
    private List<PaymentResponse> recentPayments;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyTrendPoint {
        private String month;
        private BigDecimal totalAmount;
    }
}

