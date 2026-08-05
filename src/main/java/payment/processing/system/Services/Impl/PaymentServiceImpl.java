package payment.processing.system.Services.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import payment.processing.system.DTO.Request.PayAllRequest;
import payment.processing.system.DTO.Request.PayDepartmentRequest;
import payment.processing.system.DTO.Request.PaySingleRequest;
import payment.processing.system.DTO.Response.PaymentResponse;
import payment.processing.system.DTO.Response.PaymentStatusHistoryResponse;
import payment.processing.system.Exception.InvalidPaymentStateException;
import payment.processing.system.Exception.ResourceNotFoundException;
import payment.processing.system.Model.Account;
import payment.processing.system.Model.Enums.AccountType;
import payment.processing.system.Model.Enums.CurrencyCode;
import payment.processing.system.Model.Enums.EmploymentStatus;
import payment.processing.system.Model.Enums.PaymentStatus;
import payment.processing.system.Model.Payment;
import payment.processing.system.Model.PaymentStatusHistory;
import payment.processing.system.Model.Receiver;
import payment.processing.system.Repository.AccountRepository;
import payment.processing.system.Repository.PaymentRepository;
import payment.processing.system.Repository.ReceiverRepository;
import payment.processing.system.Services.CurrencyService;
import payment.processing.system.Services.PaymentService;
import payment.processing.system.Services.PaymentStatusHistoryService;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {

    private static final String EMPLOYER_TRIGGER = "EMPLOYER";

    private final PaymentRepository paymentRepository;
    private final ReceiverRepository receiverRepository;
    private final AccountRepository accountRepository;
    private final CurrencyService currencyService;
    private final PaymentStatusHistoryService paymentStatusHistoryService;

    @Override
    @Transactional
    public PaymentResponse payReceiver(PaySingleRequest request) {
        Receiver receiver = receiverRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found with id: " + request.getReceiverId()));
        return processPaymentForReceiver(receiver, request.getAmount(), request.getCurrencyCode(),
                request.getPaymentType(), request.getReference(), false, EMPLOYER_TRIGGER);
    }

    @Override
    @Transactional
    public List<PaymentResponse> payDepartment(PayDepartmentRequest request) {
        List<Receiver> receivers = receiverRepository.findByDepartmentAndEmploymentStatus(
                request.getDepartment(), EmploymentStatus.ACTIVE);
        return receivers.stream()
                .map(receiver -> processPaymentForReceiver(receiver, request.getAmount(), request.getCurrencyCode(),
                        request.getPaymentType(), null, false, EMPLOYER_TRIGGER))
                .toList();
    }

    @Override
    @Transactional
    public List<PaymentResponse> payAllReceivers(PayAllRequest request) {
        List<Receiver> receivers = receiverRepository.findByEmploymentStatus(EmploymentStatus.ACTIVE);
        return receivers.stream()
                .map(receiver -> processPaymentForReceiver(receiver, request.getAmount(), request.getCurrencyCode(),
                        request.getPaymentType(), null, false, EMPLOYER_TRIGGER))
                .toList();
    }

    @Override
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public List<PaymentResponse> getPaymentHistoryForReceiver(Long receiverId) {
        return paymentRepository.findByReceiver_ReceiverIdOrderByCreatedAtDesc(receiverId).stream()
                .map(this::toResponse).toList();
    }

    @Override
    public PaymentResponse getPaymentById(Long id) {
        return toResponse(findPaymentById(id));
    }

    @Override
    @Transactional
    public PaymentResponse retryPayment(Long paymentId) {
        Payment payment = findPaymentById(paymentId);
        if (payment.getStatus() != PaymentStatus.FAILED) {
            throw new InvalidPaymentStateException("Only FAILED payments can be retried. Current status: " + payment.getStatus());
        }
        transition(payment, PaymentStatus.CREATED, "Retry initiated by employer", EMPLOYER_TRIGGER);
        payment.setErrorCode(null);
        payment.setErrorMessage(null);
        paymentRepository.save(payment);
        runLifecycle(payment, EMPLOYER_TRIGGER);
        return toResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse processPaymentForReceiver(Receiver receiver, BigDecimal amount, CurrencyCode currencyCode,
                                                       String paymentType, String reference, boolean scheduled,
                                                       String triggeredBy) {
        Account senderAccount = findSenderAccount(currencyCode);

        Payment payment = Payment.builder()
                .senderAccount(senderAccount)
                .receiver(receiver)
                .amount(amount)
                .currency(currencyService.getEntityByCode(currencyCode))
                .paymentType(paymentType)
                .status(PaymentStatus.CREATED)
                .idempotencyKey(UUID.randomUUID().toString())
                .reference(reference)
                .scheduled(scheduled)
                .build();
        payment = paymentRepository.save(payment);
        paymentStatusHistoryService.recordTransition(payment, null, PaymentStatus.CREATED, "Payment created", triggeredBy);

        runLifecycle(payment, triggeredBy);
        return toResponse(payment);
    }

    /**
     * Drives a payment from CREATED through VALIDATED -> SENT -> COMPLETED,
     * or to FAILED if validation does not pass.
     */
    private void runLifecycle(Payment payment, String triggeredBy) {
        Receiver receiver = payment.getReceiver();

        if (receiver.getEmploymentStatus() != EmploymentStatus.ACTIVE) {
            failPayment(payment, "RECEIVER_INACTIVE", "Receiver is not in ACTIVE employment status", triggeredBy);
            return;
        }
        if (payment.getAmount() == null || payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            failPayment(payment, "INVALID_AMOUNT", "Invalid payment amount", triggeredBy);
            return;
        }

        transition(payment, PaymentStatus.VALIDATED, "Payment validated successfully", triggeredBy);
        transition(payment, PaymentStatus.SENT, "Payment sent to bank settlement network", triggeredBy);
        transition(payment, PaymentStatus.COMPLETED, "Payment completed successfully", triggeredBy);
        paymentRepository.save(payment);
    }

    private void failPayment(Payment payment, String errorCode, String errorMessage, String triggeredBy) {
        transition(payment, PaymentStatus.FAILED, errorMessage, triggeredBy);
        payment.setErrorCode(errorCode);
        payment.setErrorMessage(errorMessage);
        paymentRepository.save(payment);
    }

    private void transition(Payment payment, PaymentStatus target, String remarks, String triggeredBy) {
        PaymentStatus current = payment.getStatus();
        if (!current.canTransitionTo(target)) {
            throw new InvalidPaymentStateException(
                    "Invalid payment status transition from " + current + " to " + target);
        }
        payment.setStatus(target);
        paymentStatusHistoryService.recordTransition(payment, current, target, remarks, triggeredBy);
    }

    private Account findSenderAccount(CurrencyCode currencyCode) {
        return accountRepository.findFirstByAccountTypeAndCurrency_CurrencyCode(AccountType.BUSINESS, currencyCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No company BUSINESS account configured for currency: " + currencyCode));
    }

    private Payment findPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
    }

    private PaymentResponse toResponse(Payment payment) {
        List<PaymentStatusHistoryResponse> historyResponses = payment.getStatusHistory() == null ? List.of() :
                payment.getStatusHistory().stream().map(this::toHistoryResponse).toList();
        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .senderAccountId(payment.getSenderAccount().getAccountId())
                .receiverId(payment.getReceiver().getReceiverId())
                .receiverName(payment.getReceiver().getFullName())
                .amount(payment.getAmount())
                .currencyCode(payment.getCurrency().getCurrencyCode())
                .paymentType(payment.getPaymentType())
                .status(payment.getStatus())
                .idempotencyKey(payment.getIdempotencyKey())
                .reference(payment.getReference())
                .errorCode(payment.getErrorCode())
                .errorMessage(payment.getErrorMessage())
                .scheduled(payment.getScheduled())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .statusHistory(historyResponses)
                .build();
    }

    private PaymentStatusHistoryResponse toHistoryResponse(PaymentStatusHistory history) {
        return PaymentStatusHistoryResponse.builder()
                .historyId(history.getHistoryId())
                .paymentId(history.getPayment().getPaymentId())
                .oldStatus(history.getOldStatus())
                .newStatus(history.getNewStatus())
                .remarks(history.getRemarks())
                .changedAt(history.getChangedAt())
                .build();
    }
}

