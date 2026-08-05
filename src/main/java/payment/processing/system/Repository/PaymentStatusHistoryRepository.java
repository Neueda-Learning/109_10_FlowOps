package payment.processing.system.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import payment.processing.system.Model.PaymentStatusHistory;

import java.util.List;

public interface PaymentStatusHistoryRepository extends JpaRepository<PaymentStatusHistory, Long> {
    List<PaymentStatusHistory> findByPayment_PaymentIdOrderByChangedAtAsc(Long paymentId);
}

