package payment.processing.system.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import payment.processing.system.Model.Enums.EmploymentStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiverResponse {
    private Long receiverId;
    private Long accountId;
    private String employeeId;
    private String fullName;
    private String email;
    private String department;
    private EmploymentStatus employmentStatus;
    private LocalDateTime createdAt;
}

