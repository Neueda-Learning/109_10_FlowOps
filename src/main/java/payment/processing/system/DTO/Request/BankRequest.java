package payment.processing.system.DTO.Request;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankRequest {

    @NotBlank(message = "Bank name is required")
    private String bankName;

    @NotBlank(message = "SWIFT code is required")
    private String swiftCode;

    @NotBlank(message = "Country code is required")
    private String countryCode;

    private Boolean isActive;
}
