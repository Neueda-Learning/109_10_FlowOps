package payment.processing.system.Config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import payment.processing.system.Services.ScheduledPaymentService;

/**
 * Polls for due scheduled salary payments and executes them automatically.
 * Cron expression is configurable via scheduler.scheduled-payments.cron.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledPaymentRunner {

    private final ScheduledPaymentService scheduledPaymentService;

    @Scheduled(cron = "${scheduler.scheduled-payments.cron:0 * * * * *}")
    public void runDuePayments() {
        log.info("Checking for due scheduled salary payments...");
        scheduledPaymentService.executeDuePayments();
    }
}


