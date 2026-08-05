package payment.processing.system.Model.Enums;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Payment lifecycle status (matches FlowOps schema).
 *
 * CREATED -> VALIDATED -> SENT -> COMPLETED
 * FAILED is reachable from CREATED, VALIDATED or SENT (processing failure).
 * A FAILED payment can be retried, which moves it back to CREATED.
 */
public enum PaymentStatus {
    CREATED,
    VALIDATED,
    SENT,
    COMPLETED,
    FAILED;

    private static final Map<PaymentStatus, Set<PaymentStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(PaymentStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(CREATED, EnumSet.of(VALIDATED, FAILED));
        ALLOWED_TRANSITIONS.put(VALIDATED, EnumSet.of(SENT, FAILED));
        ALLOWED_TRANSITIONS.put(SENT, EnumSet.of(COMPLETED, FAILED));
        ALLOWED_TRANSITIONS.put(COMPLETED, EnumSet.noneOf(PaymentStatus.class));
        ALLOWED_TRANSITIONS.put(FAILED, EnumSet.of(CREATED));
    }

    public boolean canTransitionTo(PaymentStatus target) {
        return ALLOWED_TRANSITIONS.getOrDefault(this, EnumSet.noneOf(PaymentStatus.class)).contains(target);
    }
}

