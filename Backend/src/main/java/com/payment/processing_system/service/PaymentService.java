package com.payment.processing_system.service;

import com.payment.processing_system.dto.*;
import com.payment.processing_system.entity.PaymentStatus;
import com.payment.processing_system.entity.PaymentType;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {

    PaymentResponse createPayment(CreatePaymentRequest request);

    BulkPaymentResponse createBulkPayments(BulkPaymentRequest request);

    PaymentResponse getPaymentById(Long id);

    List<PaymentResponse> listPayments(PaymentType type, PaymentStatus status);

    Page<PaymentResponse> listPaymentsPageable(PaymentType type, PaymentStatus status, String currency, Pageable pageable);

    List<PaymentResponse> getPaymentHistory(PaymentType paymentType, PaymentStatus status, String paymentReference);

    PaymentResponse validatePayment(Long id);

    PaymentResponse sendPayment(Long id);

    PaymentResponse completePayment(Long id);

    PaymentResponse refundPayment(Long id);

    RefundResponse partialRefund(Long id, RefundRequest request);

    List<RefundResponse> getRefundHistory(Long paymentId);

    PaymentResponse schedulePayment(Long paymentId, LocalDateTime scheduledFor);

    void processScheduledPayments();

    PaymentResponse failPayment(Long id, String reason);
}


