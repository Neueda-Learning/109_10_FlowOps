package payment.processing.system.Model;

import jakarta.persistence.*;
import lombok.*;
import payment.processing.system.Model.Enums.CurrencyCode;

@Entity
@Table(name = "currency")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Currency {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "currency_code", length = 3)
    private CurrencyCode currencyCode;

    @Column(name = "currency_name", nullable = false, length = 50)
    private String currencyName;

    @Column(name = "currency_symbol", length = 5)
    private String currencySymbol;

    @Column(name = "is_supported")
    private Boolean isSupported;
}

