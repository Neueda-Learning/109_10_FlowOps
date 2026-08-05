package payment.processing.system.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import payment.processing.system.Model.Enums.PaymentStatus;
import payment.processing.system.Model.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByReceiver_ReceiverIdOrderByCreatedAtDesc(Long receiverId);

    List<Payment> findByStatus(PaymentStatus status);

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    @Query("select p from Payment p where p.createdAt between :start and :end")
    List<Payment> findByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    long countByStatus(PaymentStatus status);

    @Query("select coalesce(sum(p.amount), 0) from Payment p where p.status = payment.processing.system.Model.Enums.PaymentStatus.COMPLETED " +
            "and p.updatedAt between :start and :end")
    BigDecimal sumCompletedAmountBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("select p.receiver.department as departmentName, coalesce(sum(p.amount), 0) as totalAmount " +
            "from Payment p where p.status = payment.processing.system.Model.Enums.PaymentStatus.COMPLETED group by p.receiver.department")
    List<DepartmentExpenditure> sumCompletedAmountByDepartment();

    @Query("select p.currency.currencyCode as currencyCode, count(p) as paymentCount from Payment p group by p.currency.currencyCode")
    List<CurrencyPaymentCount> countPaymentsByCurrency();

    @Query("select function('year', p.updatedAt) as year, function('month', p.updatedAt) as month, " +
            "coalesce(sum(p.amount), 0) as totalAmount " +
            "from Payment p where p.status = payment.processing.system.Model.Enums.PaymentStatus.COMPLETED " +
            "group by function('year', p.updatedAt), function('month', p.updatedAt) " +
            "order by function('year', p.updatedAt), function('month', p.updatedAt)")
    List<MonthlyTrend> monthlySalaryTrend();

    List<Payment> findTop10ByOrderByCreatedAtDesc();

    interface DepartmentExpenditure {
        String getDepartmentName();
        BigDecimal getTotalAmount();
    }

    interface CurrencyPaymentCount {
        String getCurrencyCode();
        Long getPaymentCount();
    }

    interface MonthlyTrend {
        Integer getYear();
        Integer getMonth();
        BigDecimal getTotalAmount();
    }
}

