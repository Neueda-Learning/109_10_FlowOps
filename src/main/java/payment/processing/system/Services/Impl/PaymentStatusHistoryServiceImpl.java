package payment.processing.system.Services.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import payment.processing.system.Model.Enums.PaymentStatus;
import payment.processing.system.Model.Payment;
import payment.processing.system.Model.PaymentStatusHistory;
import payment.processing.system.Repository.PaymentStatusHistoryRepository;
import payment.processing.system.Services.PaymentStatusHistoryService;




@Service
@RequiredArgsConstructor
public class PaymentStatusHistoryServiceImpl implements PaymentStatusHistoryService {

    private final PaymentStatusHistoryRepository paymentStatusHistoryRepository;

    @Override
    @Transactional
    public void recordTransition(Payment payment, PaymentStatus oldStatus, PaymentStatus newStatus,
                                  String remarks, String triggeredBy) {
        String combinedRemarks = triggeredBy == null || triggeredBy.isBlank()
                ? remarks
                : "[" + triggeredBy + "] " + remarks;

        PaymentStatusHistory history = PaymentStatusHistory.builder()
                .payment(payment)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .remarks(combinedRemarks)
                .build();
        paymentStatusHistoryRepository.save(history);

        // Keep the in-memory bidirectional association in sync, since the managed Payment
        // instance may be reused later in the same transaction (e.g. to build a response DTO).
        if (payment.getStatusHistory() != null) {
            payment.getStatusHistory().add(history);
        }
    }
}

