package payment.processing.system.Services.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import payment.processing.system.DTO.Request.ScheduleRequest;
import payment.processing.system.DTO.Response.ScheduledPaymentResponse;
import payment.processing.system.Exception.ResourceNotFoundException;
import payment.processing.system.Model.Account;
import payment.processing.system.Model.Currency;
import payment.processing.system.Model.Enums.AccountType;
import payment.processing.system.Model.Enums.Frequency;
import payment.processing.system.Model.Receiver;
import payment.processing.system.Model.ScheduledPayment;
import payment.processing.system.Repository.AccountRepository;
import payment.processing.system.Repository.ScheduledPaymentRepository;
import payment.processing.system.Services.CurrencyService;
import payment.processing.system.Services.PaymentService;
import payment.processing.system.Services.ReceiverService;
import payment.processing.system.Services.ScheduledPaymentService;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduledPaymentServiceImpl implements ScheduledPaymentService {

    private static final String SCHEDULER_TRIGGER = "SCHEDULER";
    private static final String SALARY_PAYMENT_TYPE = "SALARY";

    private final ScheduledPaymentRepository scheduledPaymentRepository;
    private final ReceiverService receiverService;
    private final AccountRepository accountRepository;
    private final CurrencyService currencyService;
    private final PaymentService paymentService;

    @Override
    @Transactional
    public ScheduledPaymentResponse createSchedule(ScheduleRequest request) {
        Receiver receiver = receiverService.getEntityById(request.getReceiverId());
        Currency currency = currencyService.getEntityByCode(request.getCurrencyCode());
        Account senderAccount = findSenderAccount(request.getCurrencyCode());

        ScheduledPayment schedule = ScheduledPayment.builder()
                .senderAccount(senderAccount)
                .receiver(receiver)
                .amount(request.getAmount())
                .currency(currency)
                .frequency(request.getFrequency())
                .nextExecutionDate(request.getNextExecutionDate())
                .build();

        return toResponse(scheduledPaymentRepository.save(schedule));
    }

    @Override
    public List<ScheduledPaymentResponse> getAllSchedules() {
        return scheduledPaymentRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public ScheduledPaymentResponse getScheduleById(Long id) {
        return toResponse(getEntityById(id));
    }

    @Override
    @Transactional
    public void cancelSchedule(Long id) {
        ScheduledPayment schedule = getEntityById(id);
        schedule.setIsActive(false);
        scheduledPaymentRepository.save(schedule);
    }

    @Override
    @Transactional
    public void executeDuePayments() {
        LocalDate today = LocalDate.now();
        List<ScheduledPayment> duePayments = scheduledPaymentRepository.findDuePayments(today);
        for (ScheduledPayment schedule : duePayments) {
            try {
                paymentService.processPaymentForReceiver(
                        schedule.getReceiver(),
                        schedule.getAmount(),
                        schedule.getCurrency().getCurrencyCode(),
                        SALARY_PAYMENT_TYPE,
                        "Scheduled salary payment",
                        true,
                        SCHEDULER_TRIGGER);
            } catch (Exception ex) {
                log.error("Failed to execute scheduled payment id={}: {}", schedule.getScheduleId(), ex.getMessage(), ex);
                continue;
            }
            schedule.setLastExecutionDate(today);
            schedule.setNextExecutionDate(computeNextExecutionDate(schedule.getNextExecutionDate(), schedule.getFrequency()));
            scheduledPaymentRepository.save(schedule);
        }
    }

    private LocalDate computeNextExecutionDate(LocalDate from, Frequency frequency) {
        return switch (frequency) {
            case WEEKLY -> from.plusWeeks(1);
            case MONTHLY -> from.plusMonths(1);
        };
    }

    private Account findSenderAccount(payment.processing.system.Model.Enums.CurrencyCode currencyCode) {
        return accountRepository.findFirstByAccountTypeAndCurrency_CurrencyCode(AccountType.BUSINESS, currencyCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No company BUSINESS account configured for currency: " + currencyCode));
    }

    private ScheduledPayment getEntityById(Long id) {
        return scheduledPaymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scheduled payment not found with id: " + id));
    }

    private ScheduledPaymentResponse toResponse(ScheduledPayment schedule) {
        return ScheduledPaymentResponse.builder()
                .scheduleId(schedule.getScheduleId())
                .senderAccountId(schedule.getSenderAccount().getAccountId())
                .receiverId(schedule.getReceiver().getReceiverId())
                .receiverName(schedule.getReceiver().getFullName())
                .amount(schedule.getAmount())
                .currencyCode(schedule.getCurrency().getCurrencyCode())
                .frequency(schedule.getFrequency())
                .nextExecutionDate(schedule.getNextExecutionDate())
                .lastExecutionDate(schedule.getLastExecutionDate())
                .isActive(schedule.getIsActive())
                .createdAt(schedule.getCreatedAt())
                .build();
    }
}

