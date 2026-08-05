package payment.processing.system.Services;

import payment.processing.system.DTO.Request.ReceiverRequest;
import payment.processing.system.DTO.Response.ReceiverResponse;
import payment.processing.system.Model.Receiver;

import java.util.List;

public interface ReceiverService {

    ReceiverResponse createReceiver(ReceiverRequest request);

    ReceiverResponse updateReceiver(Long id, ReceiverRequest request);

    ReceiverResponse getReceiverById(Long id);

    List<ReceiverResponse> getAllReceivers();

    List<ReceiverResponse> getReceiversByDepartment(String department);

    void deleteReceiver(Long id);

    Receiver getEntityById(Long id);
}

