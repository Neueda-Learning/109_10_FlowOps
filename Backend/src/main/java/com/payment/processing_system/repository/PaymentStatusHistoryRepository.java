package com.payment.processing_system.repository;

import com.payment.processing_system.entity.PaymentStatusHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentStatusHistoryRepository extends JpaRepository<PaymentStatusHistory, Long> {

    List<PaymentStatusHistory> findByPaymentIdOrderByChangedAtAsc(Long paymentId);
}

