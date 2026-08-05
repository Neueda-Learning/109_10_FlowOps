package payment.processing.system.DTO.Response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import payment.processing.system.Model.Enums.CurrencyCode;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyResponse {
    private CurrencyCode currencyCode;
    private String currencyName;
    private String currencySymbol;
    private Boolean isSupported;
}
