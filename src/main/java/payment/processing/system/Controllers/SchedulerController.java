package payment.processing.system.Controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import payment.processing.project.DTO.Request.ScheduleRequest;
import payment.processing.project.DTO.Response.ScheduledPaymentResponse;
import payment.processing.project.Services.ScheduledPaymentService;

import java.util.List;

@RestController
@RequestMapping("/api/scheduler")
@RequiredArgsConstructor
@Tag(name = "Scheduler", description = "Salary payment scheduling APIs")
public class SchedulerController {

    private final ScheduledPaymentService scheduledPaymentService;


    @PostMapping
    @Operation(summary = "Schedule a recurring salary payment for a receiver")
    public ResponseEntity<ScheduledPaymentResponse> createSchedule(@Valid @RequestBody ScheduleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(scheduledPaymentService.createSchedule(request));
    }

    @GetMapping
    @Operation(summary = "List all scheduled payments")
    public ResponseEntity<List<ScheduledPaymentResponse>> getAllSchedules() {
        return ResponseEntity.ok(scheduledPaymentService.getAllSchedules());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a scheduled payment by id")
    public ResponseEntity<ScheduledPaymentResponse> getSchedule(@PathVariable Long id) {
        return ResponseEntity.ok(scheduledPaymentService.getScheduleById(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel a scheduled payment")
    public ResponseEntity<Void> cancelSchedule(@PathVariable Long id) {
        scheduledPaymentService.cancelSchedule(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/execute-due")
    @Operation(summary = "Manually trigger execution of due scheduled payments (normally runs automatically)")
    public ResponseEntity<Void> executeDuePayments() {
        scheduledPaymentService.executeDuePayments();
        return ResponseEntity.ok().build();
    }
}

