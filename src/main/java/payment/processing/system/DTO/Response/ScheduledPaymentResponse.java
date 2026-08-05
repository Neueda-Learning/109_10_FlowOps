package payment.processing.system.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import payment.processing.system.Model.Enums.CurrencyCode;
import payment.processing.system.Model.Enums.Frequency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledPaymentResponse {
    private Long scheduleId;
    private Long senderAccountId;
    private Long receiverId;
    private String receiverName;
    private BigDecimal amount;
    private CurrencyCode currencyCode;
    private Frequency frequency;
    private LocalDate nextExecutionDate;
    private LocalDate lastExecutionDate;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
