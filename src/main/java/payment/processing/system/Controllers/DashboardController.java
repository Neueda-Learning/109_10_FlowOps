package payment.processing.system.Controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import payment.processing.project.DTO.Response.DashboardSummaryResponse;
import payment.processing.project.Services.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Analytics dashboard APIs")
public class DashboardController {

    private final DashboardService dashboardService;




    @GetMapping("/summary")
    @Operation(summary = "Get the full analytics dashboard summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary() {
        return ResponseEntity.ok(dashboardService.getDashboardSummary());
    }
}

