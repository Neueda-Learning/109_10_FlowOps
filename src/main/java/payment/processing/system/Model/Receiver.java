package payment.processing.system.Model;

import jakarta.persistence.*;
import lombok.*;
import payment.processing.system.Model.Enums.EmploymentStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "receiver")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Receiver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "receiver_id")
    private Long receiverId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", unique = true, nullable = false)
    private Account account;

    @Column(name = "employee_id", unique = true, nullable = false, length = 20)
    private String employeeId;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(length = 100)
    private String email;

    @Column(length = 100)
    private String department;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status", length = 20)
    private EmploymentStatus employmentStatus;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}

