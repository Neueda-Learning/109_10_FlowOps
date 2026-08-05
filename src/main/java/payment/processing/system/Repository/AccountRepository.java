package payment.processing.system.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import payment.processing.system.Model.Account;
import payment.processing.system.Model.Enums.AccountType;
import payment.processing.system.Model.Enums.CurrencyCode;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNumber(String accountNumber);
    boolean existsByAccountNumber(String accountNumber);
    List<Account> findByAccountType(AccountType accountType);
    Optional<Account> findFirstByAccountTypeAndCurrency_CurrencyCode(AccountType accountType, CurrencyCode currencyCode);
}

