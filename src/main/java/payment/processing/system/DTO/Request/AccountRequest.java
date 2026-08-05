package payment.processing.system.DTO.Request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import payment.processing.system.Model.Enums.AccountStatus;
import payment.processing.system.Model.Enums.AccountType;
import payment.processing.system.Model.Enums.CurrencyCode;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountRequest {

    @NotNull(message = "Bank id is required")
    private Long bankId;

    @NotNull(message = "Currency code is required")
    private CurrencyCode currencyCode;

    @NotBlank(message = "Account number is required")
    private String accountNumber;

    @NotBlank(message = "Account holder name is required")
    private String accountHolderName;

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    private String countryCode;

    private BigDecimal balance;

    private AccountStatus status;
}
