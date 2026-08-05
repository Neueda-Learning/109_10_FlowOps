package payment.processing.system.DTO.Request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import payment.processing.system.Model.Enums.CurrencyCode;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyRequest {

    @NotNull(message = "Currency code is required")
    private CurrencyCode currencyCode;

    @NotBlank(message = "Currency name is required")
    private String currencyName;

    private String currencySymbol;

    private Boolean isSupported;
}
