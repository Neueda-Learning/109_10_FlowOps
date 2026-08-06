package com.payment.processing_system.service;

import com.payment.processing_system.dto.BulkPaymentItemResult;
import com.payment.processing_system.dto.BulkPaymentRequest;
import com.payment.processing_system.dto.BulkPaymentResponse;
import com.payment.processing_system.dto.CreatePaymentRequest;
import com.payment.processing_system.dto.PaymentResponse;
import com.payment.processing_system.dto.RefundRequest;
import com.payment.processing_system.dto.RefundResponse;
import com.payment.processing_system.entity.Payment;
import com.payment.processing_system.entity.PaymentStatus;
import com.payment.processing_system.entity.PaymentStatusHistory;
import com.payment.processing_system.entity.PaymentType;
import com.payment.processing_system.entity.Refund;
import com.payment.processing_system.exception.InvalidPaymentStateException;
import com.payment.processing_system.exception.PaymentNotFoundException;
import com.payment.processing_system.exception.PaymentValidationException;
import com.payment.processing_system.repository.PaymentRepository;
import com.payment.processing_system.repository.PaymentStatusHistoryRepository;
import com.payment.processing_system.repository.RefundRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentStatusHistoryRepository paymentStatusHistoryRepository;
    private final RefundRepository refundRepository;

    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            PaymentStatusHistoryRepository paymentStatusHistoryRepository,
            RefundRepository refundRepository) {
        this.paymentRepository = paymentRepository;
        this.paymentStatusHistoryRepository = paymentStatusHistoryRepository;
        this.refundRepository = refundRepository;
    }

    @Override
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        paymentRepository.findByPaymentReference(request.getPaymentReference())
                .ifPresent(p -> {
                    throw new PaymentValidationException("Payment reference already exists: " + request.getPaymentReference());
                });

        Payment payment = new Payment();
        payment.setPaymentReference(request.getPaymentReference());
        payment.setPaymentType(request.getPaymentType());
        payment.setStatus(PaymentStatus.CREATED);
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency().toUpperCase(Locale.ROOT));
        payment.setRecipientName(request.getRecipientName());
        payment.setRecipientAccount(request.getRecipientAccount());
        Payment savedPayment = paymentRepository.save(payment);
        recordStatusChange(savedPayment.getId(), null, savedPayment.getStatus(), "Payment created");

        String validationError = validateCreatedPayment(savedPayment);
        if (validationError != null) {
            Payment failedPayment = markAsFailed(savedPayment, validationError);
            return toResponse(failedPayment);
        }

        savedPayment = transitionTo(savedPayment, PaymentStatus.VALIDATED, "Payment validated");
        savedPayment = transitionTo(savedPayment, PaymentStatus.SENT, "Payment sent");
        savedPayment = transitionTo(savedPayment, PaymentStatus.COMPLETED, "Payment completed");

        return toResponse(savedPayment);
    }

    @Override
    public BulkPaymentResponse createBulkPayments(BulkPaymentRequest request) {
        BulkPaymentResponse response = new BulkPaymentResponse();
        List<BulkPaymentItemResult> results = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;

        for (CreatePaymentRequest paymentRequest : request.getPayments()) {
            BulkPaymentItemResult item = new BulkPaymentItemResult();
            item.setPaymentReference(paymentRequest.getPaymentReference());
            try {
                PaymentResponse created = createPayment(paymentRequest);
                item.setSuccess(true);
                item.setPayment(created);
                successCount++;
            } catch (RuntimeException ex) {
                item.setSuccess(false);
                item.setErrorMessage(ex.getMessage());
                failureCount++;
            }
            results.add(item);
        }

        response.setTotalCount(request.getPayments().size());
        response.setSuccessCount(successCount);
        response.setFailureCount(failureCount);
        response.setResults(results);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id) {
        return toResponse(getById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> listPayments(PaymentType type, PaymentStatus status) {
        List<Payment> payments;

        if (type != null && status != null) {
            payments = paymentRepository.findByPaymentTypeAndStatus(type, status);
        } else if (type != null) {
            payments = paymentRepository.findByPaymentType(type);
        } else if (status != null) {
            payments = paymentRepository.findByStatus(status);
        } else {
            payments = paymentRepository.findAll();
        }

        return payments.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> listPaymentsPageable(
            PaymentType type,
            PaymentStatus status,
            String currency,
            Pageable pageable) {
        Page<Payment> page;
        if (currency != null && !currency.isBlank()) {
            page = paymentRepository.findByCurrency(currency.toUpperCase(Locale.ROOT), pageable);
        } else if (status != null) {
            page = paymentRepository.findByStatus(status, pageable);
        } else if (type != null) {
            page = paymentRepository.findByPaymentType(type, pageable);
        } else {
            page = paymentRepository.findAll(pageable);
        }
        return page.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentHistory(
            PaymentType paymentType,
            PaymentStatus status,
            String paymentReference) {
        String normalizedReference = paymentReference == null ? null : paymentReference.trim().toLowerCase(Locale.ROOT);

        return paymentRepository.findAll().stream()
                .filter(payment -> paymentType == null || payment.getPaymentType() == paymentType)
                .filter(payment -> status == null || payment.getStatus() == status)
                .filter(payment -> normalizedReference == null
                        || normalizedReference.isEmpty()
                        || payment.getPaymentReference().toLowerCase(Locale.ROOT).contains(normalizedReference))
                .map(this::toResponse)
                .toList();
    }

    @Override
    public PaymentResponse validatePayment(Long id) {
        Payment payment = getById(id);
        assertState(payment, PaymentStatus.CREATED);
        PaymentStatus previousStatus = payment.getStatus();
        payment.setStatus(PaymentStatus.VALIDATED);
        payment.setErrorCode(null);
        Payment savedPayment = paymentRepository.save(payment);

        recordStatusChange(savedPayment.getId(), previousStatus, savedPayment.getStatus(), "Payment validated");

        return toResponse(savedPayment);
    }

    @Override
    public PaymentResponse sendPayment(Long id) {
        Payment payment = getById(id);
        assertState(payment, PaymentStatus.VALIDATED);
        PaymentStatus previousStatus = payment.getStatus();
        payment.setStatus(PaymentStatus.SENT);
        payment.setErrorCode(null);
        Payment savedPayment = paymentRepository.save(payment);

        recordStatusChange(savedPayment.getId(), previousStatus, savedPayment.getStatus(), "Payment sent");

        return toResponse(savedPayment);
    }

    @Override
    public PaymentResponse completePayment(Long id) {
        Payment payment = getById(id);
        assertState(payment, PaymentStatus.SENT);
        PaymentStatus previousStatus = payment.getStatus();
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setErrorCode(null);
        Payment savedPayment = paymentRepository.save(payment);

        recordStatusChange(savedPayment.getId(), previousStatus, savedPayment.getStatus(), "Payment completed");

        return toResponse(savedPayment);
    }

    @Override
    public PaymentResponse refundPayment(Long id) {
        Payment payment = getById(id);

        if (payment.getStatus() != PaymentStatus.COMPLETED && payment.getStatus() != PaymentStatus.REFUNDED) {
            throw new InvalidPaymentStateException("Only COMPLETED or REFUNDED payments can be refunded");
        }

        BigDecimal totalRefundedBefore = refundRepository.getTotalRefundedAmount(id);
        BigDecimal remaining = payment.getAmount().subtract(totalRefundedBefore);
        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPaymentStateException("Payment has already been fully refunded");
        }

        Refund refund = new Refund();
        refund.setPaymentId(id);
        refund.setRefundAmount(remaining);
        refund.setRefundReason("Full refund");
        refundRepository.save(refund);

        PaymentStatus previousStatus = payment.getStatus();
        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setErrorCode(null);
        Payment savedPayment = paymentRepository.save(payment);

        recordStatusChange(savedPayment.getId(), previousStatus, savedPayment.getStatus(), "Payment fully refunded");

        return toResponse(savedPayment);
    }

    @Override
    public RefundResponse partialRefund(Long id, RefundRequest request) {
        Payment payment = getById(id);

        if (payment.getStatus() == PaymentStatus.FAILED) {
            throw new InvalidPaymentStateException("Cannot refund a FAILED payment");
        }
        if (payment.getStatus() != PaymentStatus.COMPLETED && payment.getStatus() != PaymentStatus.REFUNDED) {
            throw new InvalidPaymentStateException("Payment must be COMPLETED or REFUNDED");
        }

        BigDecimal refundedSoFar = refundRepository.getTotalRefundedAmount(id);
        BigDecimal newTotal = refundedSoFar.add(request.getRefundAmount());
        if (newTotal.compareTo(payment.getAmount()) > 0) {
            throw new PaymentValidationException("Refund exceeds original payment amount");
        }

        Refund refund = new Refund();
        refund.setPaymentId(id);
        refund.setRefundAmount(request.getRefundAmount());
        refund.setRefundReason(request.getRefundReason());
        Refund savedRefund = refundRepository.save(refund);

        if (payment.getStatus() != PaymentStatus.REFUNDED) {
            PaymentStatus previousStatus = payment.getStatus();
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
            recordStatusChange(payment.getId(), previousStatus, PaymentStatus.REFUNDED, "Partial refund: " + request.getRefundReason());
        } else {
            recordStatusChange(payment.getId(), PaymentStatus.REFUNDED, PaymentStatus.REFUNDED, "Additional refund: " + request.getRefundReason());
        }

        return buildRefundResponse(savedRefund, payment, newTotal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefundResponse> getRefundHistory(Long paymentId) {
        Payment payment = getById(paymentId);
        List<Refund> refunds = refundRepository.findByPaymentId(paymentId);
        BigDecimal total = refundRepository.getTotalRefundedAmount(paymentId);
        return refunds.stream().map(refund -> buildRefundResponse(refund, payment, total)).toList();
    }

    @Override
    public PaymentResponse schedulePayment(Long paymentId, LocalDateTime scheduledFor) {

        Payment payment = getById(paymentId);

        if (scheduledFor == null || !scheduledFor.isAfter(LocalDateTime.now())) {
            throw new PaymentValidationException("scheduledFor must be in the future");
        }

        payment.setScheduledFor(scheduledFor);
        payment.setIsScheduled(true);

        Payment savedPayment = paymentRepository.save(payment);

        recordStatusChange(
                savedPayment.getId(),
                savedPayment.getStatus(),
                savedPayment.getStatus(),
                "Payment scheduled for " + scheduledFor
        );

        return toResponse(savedPayment);
    }

    @Override
    @Scheduled(fixedDelay = 60000)
    public void processScheduledPayments() {
        List<Payment> duePayments = paymentRepository.findByScheduledForBefore(LocalDateTime.now());
        for (Payment payment : duePayments) {
            if (Boolean.TRUE.equals(payment.getIsScheduled()) && payment.getStatus() == PaymentStatus.CREATED) {
                try {
                    transitionTo(payment, PaymentStatus.VALIDATED, "Scheduled payment validated");
                    transitionTo(payment, PaymentStatus.SENT, "Scheduled payment sent");
                    transitionTo(payment, PaymentStatus.COMPLETED, "Scheduled payment completed");
                    payment.setIsScheduled(false);
                    paymentRepository.save(payment);
                } catch (RuntimeException ex) {
                    markAsFailed(payment, "SCHEDULED_PROCESSING_FAILED");
                }
            }
        }
    }
    @Override
    public PaymentResponse failPayment(Long id, String reason) {
        Payment payment = getById(id);

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new InvalidPaymentStateException("Completed payment cannot be failed");
        }
        if (payment.getStatus() == PaymentStatus.FAILED) {
            throw new InvalidPaymentStateException("Payment is already failed");
        }

        PaymentStatus previousStatus = payment.getStatus();
        payment.setStatus(PaymentStatus.FAILED);
        payment.setErrorCode(reason);
        Payment savedPayment = paymentRepository.save(payment);

        recordStatusChange(savedPayment.getId(), previousStatus, savedPayment.getStatus(), reason);

        return toResponse(savedPayment);
    }

    private Payment getById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));
    }

    private void assertState(Payment payment, PaymentStatus expectedStatus) {
        if (payment.getStatus() != expectedStatus) {
            throw new InvalidPaymentStateException(payment.getId(), payment.getStatus(), expectedStatus);
        }
    }

    private String validateCreatedPayment(Payment payment) {
        if (payment.getAmount() == null || payment.getAmount().signum() <= 0) {
            return "INVALID_AMOUNT";
        }
        if (payment.getCurrency() == null || payment.getCurrency().trim().length() != 3) {
            return "INVALID_CURRENCY";
        }
        return null;
    }

    private Payment transitionTo(Payment payment, PaymentStatus nextStatus, String description) {
        PaymentStatus previousStatus = payment.getStatus();
        payment.setStatus(nextStatus);
        payment.setErrorCode(null);
        Payment savedPayment = paymentRepository.save(payment);
        recordStatusChange(savedPayment.getId(), previousStatus, nextStatus, description);
        return savedPayment;
    }

    private Payment markAsFailed(Payment payment, String errorCode) {
        PaymentStatus previousStatus = payment.getStatus();
        payment.setStatus(PaymentStatus.FAILED);
        payment.setErrorCode(errorCode);
        Payment savedPayment = paymentRepository.save(payment);
        recordStatusChange(savedPayment.getId(), previousStatus, PaymentStatus.FAILED, "Validation failed: " + errorCode);
        return savedPayment;
    }

    private void recordStatusChange(
            Long paymentId,
            PaymentStatus previousStatus,
            PaymentStatus currentStatus,
            String description) {
        PaymentStatusHistory history = new PaymentStatusHistory();
        history.setPaymentId(paymentId);
        history.setPreviousStatus(previousStatus);
        history.setCurrentStatus(currentStatus);
        history.setDescription(description);
        paymentStatusHistoryRepository.save(history);
    }

    private RefundResponse buildRefundResponse(Refund refund, Payment payment, BigDecimal totalRefunded) {
        RefundResponse response = new RefundResponse();
        response.setId(refund.getId());
        response.setPaymentId(refund.getPaymentId());
        response.setPaymentReference(payment.getPaymentReference());
        response.setRefundAmount(refund.getRefundAmount());
        response.setRefundReason(refund.getRefundReason());
        response.setRefundDate(refund.getRefundDate());
        response.setOriginalAmount(payment.getAmount());
        response.setTotalRefunded(totalRefunded);
        response.setRemainingAmount(payment.getAmount().subtract(totalRefunded));
        return response;
    }

    private PaymentResponse toResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setPaymentReference(payment.getPaymentReference());
        response.setPaymentType(payment.getPaymentType());
        response.setStatus(payment.getStatus());
        response.setAmount(payment.getAmount());
        response.setCurrency(payment.getCurrency());
        response.setRecipientName(payment.getRecipientName());
        response.setRecipientAccount(payment.getRecipientAccount());
        response.setErrorCode(payment.getErrorCode());
        response.setCreatedAt(payment.getCreatedAt());
        response.setScheduledFor(payment.getScheduledFor());
        response.setIsScheduled(payment.getIsScheduled());
        return response;
    }
}





