package payment.processing.system.Services.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import payment.processing.system.DTO.Request.BankRequest;
import payment.processing.system.DTO.Response.BankResponse;
import payment.processing.system.Exception.DuplicateResourceException;
import payment.processing.system.Exception.ResourceNotFoundException;
import payment.processing.system.Model.Bank;
import payment.processing.system.Repository.BankRepository;
import payment.processing.system.Services.BankService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BankServiceImpl implements BankService {

    private final BankRepository bankRepository;

    @Override
    @Transactional
    public BankResponse createBank(BankRequest request) {
        if (bankRepository.existsBySwiftCode(request.getSwiftCode())) {
            throw new DuplicateResourceException("Bank already exists with SWIFT code: " + request.getSwiftCode());
        }
        Bank bank = Bank.builder()
                .bankName(request.getBankName())
                .swiftCode(request.getSwiftCode())
                .countryCode(request.getCountryCode())
                .isActive(request.getIsActive() == null ? true : request.getIsActive())
                .build();
        return toResponse(bankRepository.save(bank));
    }

    @Override
    public List<BankResponse> getAllBanks() {
        return bankRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public BankResponse getBankById(Long bankId) {
        return toResponse(getEntityById(bankId));
    }

    @Override
    public Bank getEntityById(Long bankId) {
        return bankRepository.findById(bankId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank not found with id: " + bankId));
    }

    private BankResponse toResponse(Bank bank) {
        return BankResponse.builder()
                .bankId(bank.getBankId())
                .bankName(bank.getBankName())
                .swiftCode(bank.getSwiftCode())
                .countryCode(bank.getCountryCode())
                .isActive(bank.getIsActive())
                .createdAt(bank.getCreatedAt())
                .build();
    }
}

