package payment.processing.system.Services.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import payment.processing.system.DTO.Request.RefundRequest;
import payment.processing.system.DTO.Response.RefundResponse;
import payment.processing.system.Exception.InvalidRefundException;
import payment.processing.system.Exception.ResourceNotFoundException;
import payment.processing.system.Model.Enums.PaymentStatus;
import payment.processing.system.Model.Enums.RefundStatus;
import payment.processing.system.Model.Payment;
import payment.processing.system.Model.Refund;
import payment.processing.system.Repository.PaymentRepository;
import payment.processing.system.Repository.RefundRepository;
import payment.processing.system.Services.RefundService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefundServiceImpl implements RefundService {

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public RefundResponse requestRefund(RefundRequest request) {
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + request.getPaymentId()));

        if (payment.getStatus() != PaymentStatus.COMPLETED && payment.getStatus() != PaymentStatus.FAILED) {
            throw new InvalidRefundException(
                    "Refunds can only be requested for COMPLETED or FAILED payments. Current status: " + payment.getStatus());
        }
        if (request.getRefundAmount().compareTo(payment.getAmount()) > 0) {
            throw new InvalidRefundException("Refund amount cannot exceed the original payment amount");
        }

        Refund refund = Refund.builder()
                .payment(payment)
                .refundAmount(request.getRefundAmount())
                .reason(request.getReason())
                .refundStatus(RefundStatus.REQUESTED)
                .build();

        return toResponse(refundRepository.save(refund));
    }

    @Override
    @Transactional
    public RefundResponse validateRefund(Long refundId) {
        Refund refund = findById(refundId);
        transition(refund, RefundStatus.APPROVED);
        return toResponse(refundRepository.save(refund));
    }

    @Override
    @Transactional
    public RefundResponse processRefund(Long refundId) {
        Refund refund = findById(refundId);
        if (refund.getRefundStatus() == RefundStatus.REQUESTED) {
            transition(refund, RefundStatus.APPROVED);
        }

        BigDecimal amount = refund.getRefundAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            transition(refund, RefundStatus.REJECTED);
        } else {
            transition(refund, RefundStatus.COMPLETED);
            refund.setCompletedAt(LocalDateTime.now());
        }
        return toResponse(refundRepository.save(refund));
    }

    @Override
    public RefundResponse getRefundById(Long refundId) {
        return toResponse(findById(refundId));
    }

    @Override
    public List<RefundResponse> getAllRefunds() {
        return refundRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public List<RefundResponse> getRefundsForPayment(Long paymentId) {
        return refundRepository.findByPayment_PaymentId(paymentId).stream().map(this::toResponse).toList();
    }

    private void transition(Refund refund, RefundStatus target) {
        if (!refund.getRefundStatus().canTransitionTo(target)) {
            throw new InvalidRefundException("Invalid refund status transition from " + refund.getRefundStatus() + " to " + target);
        }
        refund.setRefundStatus(target);
    }

    private Refund findById(Long id) {
        return refundRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Refund not found with id: " + id));
    }

    private RefundResponse toResponse(Refund refund) {
        return RefundResponse.builder()
                .refundId(refund.getRefundId())
                .paymentId(refund.getPayment().getPaymentId())
                .refundAmount(refund.getRefundAmount())
                .reason(refund.getReason())
                .refundStatus(refund.getRefundStatus())
                .requestedAt(refund.getRequestedAt())
                .completedAt(refund.getCompletedAt())
                .build();
    }
}

