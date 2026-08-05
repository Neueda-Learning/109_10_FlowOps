package payment.processing.system.Model.Enums;

import jakarta.persistence.*;
import lombok.*;
import payment.processing.system.Model.Enums.AccountStatus;
import payment.processing.system.Model.Enums.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id", nullable = false)
    private Bank bank;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_code", nullable = false)
    private Currency currency;

    @Column(name = "account_number", unique = true, nullable = false, length = 50)
    private String accountNumber;

    @Column(name = "account_holder_name", nullable = false, length = 100)
    private String accountHolderName;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", length = 20)
    private AccountType accountType;

    @Column(name = "country_code", length = 3)
    private String countryCode;

    @Column(precision = 15, scale = 2)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AccountStatus status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = AccountStatus.ACTIVE;
        }
        if (this.balance == null) {
            this.balance = BigDecimal.ZERO;
        }
    }
}

