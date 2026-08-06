package com.payment.processing_system.controller;

import com.payment.processing_system.dto.*;
import com.payment.processing_system.entity.PaymentStatus;
import com.payment.processing_system.entity.PaymentType;
import com.payment.processing_system.service.PaymentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        return paymentService.createPayment(request);
    }

    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public BulkPaymentResponse createBulkPayments(@Valid @RequestBody BulkPaymentRequest request) {
        return paymentService.createBulkPayments(request);
    }

    @GetMapping("/{id}")
    public PaymentResponse getPayment(@PathVariable Long id) {
        return paymentService.getPaymentById(id);
    }

    @GetMapping("/history")
    public List<PaymentResponse> paymentHistory(
            @RequestParam(required = false) PaymentType paymentType,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) String paymentReference) {
        return paymentService.getPaymentHistory(paymentType, status, paymentReference);
    }

    @GetMapping
    public List<PaymentResponse> listPayments(
            @RequestParam(required = false) PaymentType type,
            @RequestParam(required = false) PaymentStatus status) {
        return paymentService.listPayments(type, status);
    }

    @GetMapping("/pageable")
    public Page<PaymentResponse> listPaymentsPageable(
            @RequestParam(required = false) PaymentType type,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) String currency,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("ASC") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        return paymentService.listPaymentsPageable(type, status, currency, pageable);
    }

    @PostMapping("/{id}/validate")
    public PaymentResponse validatePayment(@PathVariable Long id) {
        return paymentService.validatePayment(id);
    }

    @PostMapping("/{id}/send")
    public PaymentResponse sendPayment(@PathVariable Long id) {
        return paymentService.sendPayment(id);
    }

    @PostMapping("/{id}/complete")
    public PaymentResponse completePayment(@PathVariable Long id) {
        return paymentService.completePayment(id);
    }

    @PostMapping("/{id}/refund")
    public PaymentResponse refundPayment(@PathVariable Long id) {
        return paymentService.refundPayment(id);
    }

    @PostMapping("/{id}/refund/partial")
    public RefundResponse partialRefund(@PathVariable Long id, @Valid @RequestBody RefundRequest request) {
        return paymentService.partialRefund(id, request);
    }

    @GetMapping("/{id}/refunds")
    public List<RefundResponse> getRefundHistory(@PathVariable Long id) {
        return paymentService.getRefundHistory(id);
    }

    @PostMapping("/{id}/schedule")
    public PaymentResponse schedulePayment(
            @PathVariable Long id,
            @Valid @RequestBody SchedulePaymentRequest request) {
        return paymentService.schedulePayment(id, request.getScheduledFor());
    }

    @PostMapping("/{id}/fail")
    public PaymentResponse failPayment(@PathVariable Long id, @Valid @RequestBody FailPaymentRequest request) {
        return paymentService.failPayment(id, request.getReason());
    }
}


