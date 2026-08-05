package payment.processing.system.DTO.Request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import payment.processing.system.Model.Enums.CurrencyCode;
import payment.processing.system.Model.Enums.Frequency;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleRequest {

    @NotNull(message = "Receiver id is required")
    private Long receiverId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "Currency code is required")
    private CurrencyCode currencyCode;

    @NotNull(message = "Frequency is required")
    private Frequency frequency;

    @NotNull(message = "Next execution date is required")
    private LocalDate nextExecutionDate;
}

