package payment.processing.system.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import payment.processing.system.Model.Currency;
import payment.processing.system.Model.Enums.CurrencyCode;

public interface CurrencyRepository extends JpaRepository<Currency, CurrencyCode> {
}

