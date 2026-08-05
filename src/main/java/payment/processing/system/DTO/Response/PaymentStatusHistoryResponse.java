package payment.processing.system.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import payment.processing.system.Model.Enums.PaymentStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusHistoryResponse {
    private Long historyId;
    private Long paymentId;
    private PaymentStatus oldStatus;
    private PaymentStatus newStatus;
    private String remarks;
    private LocalDateTime changedAt;
}
