package payment.processing.system.Services.Impl;

import payment.processing.system.Model.Enums.PaymentStatus;
import payment.processing.system.Model.Payment;

public interface PaymentStatusHistoryService {

    /**
     * Records a status transition in the audit trail. Does not validate the transition itself -
     * callers are expected to have already verified it via PaymentStatus.canTransitionTo.
     * The "triggeredBy" context is folded into the remarks text since the FlowOps schema's
     * payment_status_history table has no dedicated triggered_by column.
     */
    void recordTransition(Payment payment, PaymentStatus oldStatus, PaymentStatus newStatus,
                           String remarks, String triggeredBy);
}

