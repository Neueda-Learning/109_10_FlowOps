package payment.processing.system.Services.Impl;

import payment.processing.system.DTO.Request.PayAllRequest;
import payment.processing.system.DTO.Request.PayDepartmentRequest;
import payment.processing.system.DTO.Request.PaySingleRequest;
import payment.processing.system.DTO.Response.PaymentResponse;
import payment.processing.system.Model.Enums.CurrencyCode;
import payment.processing.system.Model.Receiver;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {

    PaymentResponse payReceiver(PaySingleRequest request);

    List<PaymentResponse> payDepartment(PayDepartmentRequest request);

    List<PaymentResponse> payAllReceivers(PayAllRequest request);

    List<PaymentResponse> getAllPayments();

    List<PaymentResponse> getPaymentHistoryForReceiver(Long receiverId);

    PaymentResponse getPaymentById(Long id);

    PaymentResponse retryPayment(Long paymentId);

    /**
     * Creates a payment record and drives it through the full lifecycle
     * (CREATED -> VALIDATED -> SENT -> COMPLETED, or FAILED on validation error).
     * Used both by direct pay operations and by the payment scheduler.
     */
    PaymentResponse processPaymentForReceiver(Receiver receiver, BigDecimal amount, CurrencyCode currencyCode,
                                               String paymentType, String reference, boolean scheduled,
                                               String triggeredBy);
}

