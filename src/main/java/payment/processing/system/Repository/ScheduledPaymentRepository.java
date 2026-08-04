package payment.processing.system.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import payment.processing.system.Model.ScheduledPayment;

import java.time.LocalDate;
import java.util.List;

public interface ScheduledPaymentRepository extends JpaRepository<ScheduledPayment, Long> {

    @Query("select sp from ScheduledPayment sp where sp.isActive = true and sp.nextExecutionDate <= :today")
    List<ScheduledPayment> findDuePayments(@Param("today") LocalDate today);

    long countByIsActiveTrue();

    List<ScheduledPayment> findByReceiver_Department(String department);
}

