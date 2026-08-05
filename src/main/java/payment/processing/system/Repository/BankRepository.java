package payment.processing.system.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import payment.processing.system.Model.Bank;

import java.util.Optional;

public interface BankRepository extends JpaRepository<Bank, Long> {
    Optional<Bank> findBySwiftCode(String swiftCode);
    boolean existsBySwiftCode(String swiftCode);
}

