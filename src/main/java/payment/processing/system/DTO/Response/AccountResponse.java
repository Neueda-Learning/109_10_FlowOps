package payment.processing.system.DTO.Response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import payment.processing.system.Model.Enums.AccountStatus;
import payment.processing.system.Model.Enums.AccountType;
import payment.processing.system.Model.Enums.CurrencyCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
    private Long accountId;
    private Long bankId;
    private String bankName;
    private CurrencyCode currencyCode;
    private String accountNumber;
    private String accountHolderName;
    private AccountType accountType;
    private String countryCode;
    private BigDecimal balance;
    private AccountStatus status;
    private LocalDateTime createdAt;
}


