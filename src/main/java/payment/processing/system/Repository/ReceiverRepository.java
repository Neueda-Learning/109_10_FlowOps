package payment.processing.system.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import payment.processing.system.Model.Enums.EmploymentStatus;
import payment.processing.system.Model.Receiver;

import java.util.List;
import java.util.Optional;

public interface ReceiverRepository extends JpaRepository<Receiver, Long> {

    Optional<Receiver> findByEmployeeId(String employeeId);

    boolean existsByEmployeeId(String employeeId);

    List<Receiver> findByDepartment(String department);

    List<Receiver> findByDepartmentAndEmploymentStatus(String department, EmploymentStatus status);

    List<Receiver> findByEmploymentStatus(EmploymentStatus status);

    @Query("select r.department as departmentName, count(r) as receiverCount from Receiver r group by r.department")
    List<DepartmentReceiverCount> countReceiversByDepartment();

    interface DepartmentReceiverCount {
        String getDepartmentName();
        Long getReceiverCount();
    }
}

