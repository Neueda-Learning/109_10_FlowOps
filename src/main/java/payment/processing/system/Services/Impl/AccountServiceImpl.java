package payment.processing.system.Services.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import payment.processing.system.DTO.Request.AccountRequest;
import payment.processing.system.DTO.Response.AccountResponse;
import payment.processing.system.Exception.DuplicateResourceException;
import payment.processing.system.Exception.ResourceNotFoundException;
import payment.processing.system.Model.Account;
import payment.processing.system.Model.Bank;
import payment.processing.system.Model.Currency;
import payment.processing.system.Repository.AccountRepository;
import payment.processing.system.Services.AccountService;
import payment.processing.system.Services.BankService;
import payment.processing.system.Services.CurrencyService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final BankService bankService;
    private final CurrencyService currencyService;

    @Override
    @Transactional
    public AccountResponse createAccount(AccountRequest request) {
        if (accountRepository.existsByAccountNumber(request.getAccountNumber())) {
            throw new DuplicateResourceException("Account already exists with number: " + request.getAccountNumber());
        }
        Bank bank = bankService.getEntityById(request.getBankId());
        Currency currency = currencyService.getEntityByCode(request.getCurrencyCode());

        Account account = Account.builder()
                .bank(bank)
                .currency(currency)
                .accountNumber(request.getAccountNumber())
                .accountHolderName(request.getAccountHolderName())
                .accountType(request.getAccountType())
                .countryCode(request.getCountryCode())
                .balance(request.getBalance())
                .status(request.getStatus())
                .build();
        return toResponse(accountRepository.save(account));
    }

    @Override
    public List<AccountResponse> getAllAccounts() {
        return accountRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public AccountResponse getAccountById(Long accountId) {
        return toResponse(getEntityById(accountId));
    }

    @Override
    public Account getEntityById(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + accountId));
    }

    private AccountResponse toResponse(Account account) {
        return AccountResponse.builder()
                .accountId(account.getAccountId())
                .bankId(account.getBank().getBankId())
                .bankName(account.getBank().getBankName())
                .currencyCode(account.getCurrency().getCurrencyCode())
                .accountNumber(account.getAccountNumber())
                .accountHolderName(account.getAccountHolderName())
                .accountType(account.getAccountType())
                .countryCode(account.getCountryCode())
                .balance(account.getBalance())
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .build();
    }
}

