package com.payment.processing_system.repository;

import com.payment.processing_system.entity.Payment;
import com.payment.processing_system.entity.PaymentStatus;
import com.payment.processing_system.entity.PaymentType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentReference(String paymentReference);

    List<Payment> findByIsScheduledTrue();

    List<Payment> findByPaymentType(PaymentType paymentType);

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByPaymentReferenceContainingIgnoreCase(String text);

    List<Payment> findByPaymentTypeAndStatus(PaymentType paymentType, PaymentStatus status);

    List<Payment> findByCurrency(String currency);

    List<Payment> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Payment> findByScheduledForBefore(LocalDateTime date);

    // Pagination support
    Page<Payment> findAll(Pageable pageable);

    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);

    Page<Payment> findByPaymentType(PaymentType paymentType, Pageable pageable);

    Page<Payment> findByCurrency(String currency, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = ?1")
    long countByStatus(PaymentStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p")
    java.math.BigDecimal getTotalAmount();

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = ?1")
    java.math.BigDecimal getTotalAmountByStatus(PaymentStatus status);
}



