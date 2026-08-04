package payment.processing.system.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import payment.processing.system.Model.Enums.RefundStatus;
import payment.processing.system.Model.Refund;

import java.util.List;

public interface RefundRepository extends JpaRepository<Refund, Long> {
    List<Refund> findByPayment_PaymentId(Long paymentId);
    List<Refund> findByRefundStatus(RefundStatus refundStatus);
    long countByRefundStatus(RefundStatus refundStatus);
}

