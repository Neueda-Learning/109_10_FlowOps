package payment.processing.system.DTO.Response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankResponse {
    private Long bankId;
    private String bankName;
    private String swiftCode;
    private String countryCode;
    private Boolean isActive;
    private LocalDateTime createdAt;
}