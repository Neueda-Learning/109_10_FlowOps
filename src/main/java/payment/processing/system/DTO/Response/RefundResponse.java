package payment.processing.system.DTO.Response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import payment.processing.system.Model.Enums.RefundStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundResponse {
    private Long refundId;
    private Long paymentId;
    private BigDecimal refundAmount;
    private String reason;
    private RefundStatus refundStatus;
    private LocalDateTime requestedAt;
    private LocalDateTime completedAt;
}

