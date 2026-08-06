package com.payment.processing_system.repository;

import com.payment.processing_system.entity.Refund;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RefundRepository extends JpaRepository<Refund, Long> {

    List<Refund> findByPaymentId(Long paymentId);

    @Query("SELECT COALESCE(SUM(r.refundAmount), 0) FROM Refund r WHERE r.paymentId = ?1")
    java.math.BigDecimal getTotalRefundedAmount(Long paymentId);
}

