package payment.processing.system.Model.Enums;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Refund lifecycle status (matches FlowOps schema).
 *
 * REQUESTED -> APPROVED -> COMPLETED
 * REJECTED is reachable from REQUESTED or APPROVED.
 */
public enum RefundStatus {
    REQUESTED,
    APPROVED,
    COMPLETED,
    REJECTED;

    private static final Map<RefundStatus, Set<RefundStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(RefundStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(REQUESTED, EnumSet.of(APPROVED, REJECTED));
        ALLOWED_TRANSITIONS.put(APPROVED, EnumSet.of(COMPLETED, REJECTED));
        ALLOWED_TRANSITIONS.put(COMPLETED, EnumSet.noneOf(RefundStatus.class));
        ALLOWED_TRANSITIONS.put(REJECTED, EnumSet.noneOf(RefundStatus.class));
    }

    public boolean canTransitionTo(RefundStatus target) {
        return ALLOWED_TRANSITIONS.getOrDefault(this, EnumSet.noneOf(RefundStatus.class)).contains(target);
    }
}

