package payment.processing.system.Services.Impl;

import payment.processing.system.DTO.Request.ScheduleRequest;
import payment.processing.system.DTO.Response.ScheduledPaymentResponse;

import java.util.List;

public interface ScheduledPaymentService {

    ScheduledPaymentResponse createSchedule(ScheduleRequest request);

    List<ScheduledPaymentResponse> getAllSchedules();

    ScheduledPaymentResponse getScheduleById(Long id);

    void cancelSchedule(Long id);

    /**
     * Executes all scheduled payments whose nextRunDate is due. Invoked by the
     * background scheduler task (or manually for testing).
     */
    void executeDuePayments();
}

