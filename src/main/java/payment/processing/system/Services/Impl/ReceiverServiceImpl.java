package payment.processing.system.Services.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import payment.processing.system.DTO.Request.ReceiverRequest;
import payment.processing.system.DTO.Response.ReceiverResponse;
import payment.processing.system.Exception.DuplicateResourceException;
import payment.processing.system.Exception.ResourceNotFoundException;
import payment.processing.system.Model.Account;
import payment.processing.system.Model.Receiver;
import payment.processing.system.Repository.ReceiverRepository;
import payment.processing.system.Services.AccountService;
import payment.processing.system.Services.ReceiverService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReceiverServiceImpl implements ReceiverService {

    private final ReceiverRepository receiverRepository;
    private final AccountService accountService;

    @Override
    @Transactional
    public ReceiverResponse createReceiver(ReceiverRequest request) {
        if (receiverRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new DuplicateResourceException("Receiver already exists with employee id: " + request.getEmployeeId());
        }
        Account account = accountService.getEntityById(request.getAccountId());

        Receiver receiver = Receiver.builder()
                .account(account)
                .employeeId(request.getEmployeeId())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .department(request.getDepartment().name())
                .employmentStatus(request.getEmploymentStatus())
                .build();

        return toResponse(receiverRepository.save(receiver));
    }

    @Override
    @Transactional
    public ReceiverResponse updateReceiver(Long id, ReceiverRequest request) {
        Receiver receiver = getEntityById(id);

        if (!receiver.getEmployeeId().equals(request.getEmployeeId())
                && receiverRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new DuplicateResourceException("Receiver already exists with employee id: " + request.getEmployeeId());
        }

        Account account = accountService.getEntityById(request.getAccountId());

        receiver.setAccount(account);
        receiver.setEmployeeId(request.getEmployeeId());
        receiver.setFullName(request.getFullName());
        receiver.setEmail(request.getEmail());
        receiver.setDepartment(request.getDepartment().name());
        receiver.setEmploymentStatus(request.getEmploymentStatus());

        return toResponse(receiverRepository.save(receiver));
    }

    @Override
    public ReceiverResponse getReceiverById(Long id) {
        return toResponse(getEntityById(id));
    }

    @Override
    public List<ReceiverResponse> getAllReceivers() {
        return receiverRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public List<ReceiverResponse> getReceiversByDepartment(String department) {
        return receiverRepository.findByDepartment(department).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public void deleteReceiver(Long id) {
        receiverRepository.delete(getEntityById(id));
    }

    @Override
    public Receiver getEntityById(Long id) {
        return receiverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found with id: " + id));
    }

    private ReceiverResponse toResponse(Receiver receiver) {
        return ReceiverResponse.builder()
                .receiverId(receiver.getReceiverId())
                .accountId(receiver.getAccount().getAccountId())
                .employeeId(receiver.getEmployeeId())
                .fullName(receiver.getFullName())
                .email(receiver.getEmail())
                .department(receiver.getDepartment())
                .employmentStatus(receiver.getEmploymentStatus())
                .createdAt(receiver.getCreatedAt())
                .build();
    }
}

