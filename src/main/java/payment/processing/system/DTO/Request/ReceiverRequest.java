package payment.processing.system.DTO.Request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import payment.processing.system.Model.Enums.DepartmentName;
import payment.processing.system.Model.Enums.EmploymentStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiverRequest {

    @NotNull(message = "Linked account id is required")
    private Long accountId;

    @NotBlank(message = "Employee id is required")
    private String employeeId;

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String email;

    @NotNull(message = "Department is required")
    private DepartmentName department;

    @NotNull(message = "Employment status is required")
    private EmploymentStatus employmentStatus;
}

