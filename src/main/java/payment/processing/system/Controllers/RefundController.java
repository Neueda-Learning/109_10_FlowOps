package payment.processing.system.Controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import payment.processing.system.DTO.Request.RefundRequest;
import payment.processing.system.DTO.Response.RefundResponse;
import payment.processing.system.Services.RefundService;

import java.util.List;

@RestController
@RequestMapping("/api/refunds")
@RequiredArgsConstructor
@Tag(name = "Refund", description = "Refund lifecycle management APIs")
public class RefundController {

    private final RefundService refundService;


    @PostMapping
    @Operation(summary = "Request a refund for a payment")
    public ResponseEntity<RefundResponse> requestRefund(@Valid @RequestBody RefundRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(refundService.requestRefund(request));
    }

    @PostMapping("/{id}/validate")
    @Operation(summary = "Validate a requested refund")
    public ResponseEntity<RefundResponse> validateRefund(@PathVariable Long id) {
        return ResponseEntity.ok(refundService.validateRefund(id));
    }

    @PostMapping("/{id}/process")
    @Operation(summary = "Process a validated refund through to completion")
    public ResponseEntity<RefundResponse> processRefund(@PathVariable Long id) {
        return ResponseEntity.ok(refundService.processRefund(id));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a refund by id")
    public ResponseEntity<RefundResponse> getRefund(@PathVariable Long id) {
        return ResponseEntity.ok(refundService.getRefundById(id));
    }

    @GetMapping
    @Operation(summary = "List all refunds")
    public ResponseEntity<List<RefundResponse>> getAllRefunds() {
        return ResponseEntity.ok(refundService.getAllRefunds());
    }

    @GetMapping("/payment/{paymentId}")
    @Operation(summary = "List refunds associated with a payment")
    public ResponseEntity<List<RefundResponse>> getRefundsForPayment(@PathVariable Long paymentId) {
        return ResponseEntity.ok(refundService.getRefundsForPayment(paymentId));
    }
}

