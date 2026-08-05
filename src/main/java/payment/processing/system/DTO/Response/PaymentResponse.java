package payment.processing.system.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import payment.processing.system.Model.Enums.CurrencyCode;
import payment.processing.system.Model.Enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long paymentId;
    private Long senderAccountId;
    private Long receiverId;
    private String receiverName;
    private BigDecimal amount;
    private CurrencyCode currencyCode;
    private String paymentType;
    private PaymentStatus status;
    private String idempotencyKey;
    private String reference;
    private String errorCode;
    private String errorMessage;
    private Boolean scheduled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PaymentStatusHistoryResponse> statusHistory;
}
