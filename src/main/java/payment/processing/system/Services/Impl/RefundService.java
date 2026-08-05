package payment.processing.system.Services.Impl;

import payment.processing.system.DTO.Request.RefundRequest;
import payment.processing.system.DTO.Response.RefundResponse;

import java.util.List;

public interface RefundService {

    RefundResponse requestRefund(RefundRequest request);

    RefundResponse validateRefund(Long refundId);

    RefundResponse processRefund(Long refundId);

    RefundResponse getRefundById(Long refundId);

    List<RefundResponse> getAllRefunds();

    List<RefundResponse> getRefundsForPayment(Long paymentId);
}

