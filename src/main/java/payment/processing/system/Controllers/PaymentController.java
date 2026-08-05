package payment.processing.system.Controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import payment.processing.project.DTO.Request.PayAllRequest;
import payment.processing.project.DTO.Request.PayDepartmentRequest;
import payment.processing.project.DTO.Request.PaySingleRequest;
import payment.processing.project.DTO.Response.PaymentResponse;
import payment.processing.project.Services.PaymentService;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment", description = "Salary payment processing APIs")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/pay/receiver")
    @Operation(summary = "Pay a single receiver (employee)")
    public ResponseEntity<PaymentResponse> payReceiver(@Valid @RequestBody PaySingleRequest request) {
        return ResponseEntity.ok(paymentService.payReceiver(request));
    }

    @PostMapping("/pay/department")
    @Operation(summary = "Pay all active receivers in a department")
    public ResponseEntity<List<PaymentResponse>> payDepartment(@Valid @RequestBody PayDepartmentRequest request) {
        return ResponseEntity.ok(paymentService.payDepartment(request));
    }

    @PostMapping("/pay/all")
    @Operation(summary = "Pay all active receivers")
    public ResponseEntity<List<PaymentResponse>> payAll(@Valid @RequestBody PayAllRequest request) {
        return ResponseEntity.ok(paymentService.payAllReceivers(request));
    }

    @GetMapping
    @Operation(summary = "Get complete payment history")
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a payment by id")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @GetMapping("/receiver/{receiverId}")
    @Operation(summary = "Get payment history for a specific receiver")
    public ResponseEntity<List<PaymentResponse>> getPaymentHistory(@PathVariable Long receiverId) {
        return ResponseEntity.ok(paymentService.getPaymentHistoryForReceiver(receiverId));
    }

    @PostMapping("/{id}/retry")
    @Operation(summary = "Retry a failed payment")
    public ResponseEntity<PaymentResponse> retryPayment(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.retryPayment(id));
    }
}

